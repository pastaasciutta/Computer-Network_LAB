package server;

import server.OrderTypes.Order;
import server.OrderTypes.StopOrder;
import server.OrderTypes.LimitOrder;
import server.OrderTypes.MarketOrder;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.TreeMap;

public class OrderBookManager {
	//path relativo per il json file
	private static final String ORDERS_FILE = "data/storicoOrdini.json";
	//riferimento alla classe udp notification
	private final UDPNotificationSender udpNotificationSender;

	// Per gli ordini Limit ASK, VENDITA (ordinamento crescente per prezzo)
	private final ConcurrentSkipListMap<Integer, PriceLevel> askLimitOrders;
	// Per gli ordini BID, ACQUISTO (ordinamento decrescente per prezzo)
	private final ConcurrentSkipListMap<Integer, PriceLevel> bidLimitOrders;
	// Scheduler che esegue periodicamente il matching dei limit orders (time-price priority algorithm)
	private final ScheduledExecutorService limitOrdersScheduler;

	// utilizzo di strutture analoghe per gli stop orders
	private final ConcurrentSkipListMap<Integer, PriceLevel> askStopOrders;
	private final ConcurrentSkipListMap<Integer, PriceLevel> bidStopOrders;
	private final ScheduledExecutorService stopOrdersScheduler;

	// uso una hash map per tenere traccia degli ordini attivi (orderId -> Order)
	// perche non è necessario siano ordinati
	private final ConcurrentHashMap <Integer, Order> activeOrders;
	
	// uso list per gli ordini completati e/o presenti nello storicoOrdini.json
	private List<Order> completedOrders;

	// scheduler e variabile utili per la persistenza dei dati
	private final ScheduledExecutorService savingsScheduler;
	private volatile boolean needSavings;
	
	// Contatore sincronizzato per generare ID univoci
	private volatile static int orderIdCounter = 0;
	
	OrderBookManager( UDPNotificationSender udpNotificationSender ) {
		this.udpNotificationSender = udpNotificationSender;

        this.askLimitOrders = new ConcurrentSkipListMap<>();
		this.bidLimitOrders = new ConcurrentSkipListMap<>(Collections.reverseOrder());

		this.askStopOrders = new ConcurrentSkipListMap<>();
		this.bidStopOrders = new ConcurrentSkipListMap<>(Collections.reverseOrder());

		this.activeOrders = new ConcurrentHashMap<>();
		
		this.completedOrders = new LinkedList<>();
		loadOrders();

		this.limitOrdersScheduler = Executors.newSingleThreadScheduledExecutor();
		startLimitOrderScheduler();

		this.stopOrdersScheduler = Executors.newSingleThreadScheduledExecutor();
		startStopOrderScheduler();

		this.needSavings = false;
		this.savingsScheduler = Executors.newSingleThreadScheduledExecutor();
		startPersistenceScheduler();
	}

	/** Inserisce o esegue un nuovo ordine nell'orderBook */
	public int insertOrder(server.OrderTypes.Order order) {
		int orderId = 0;
		if (order.getOrderType().equals("market"))
			// non viene salvato in active orders perche viene mandato direttamente in esecuzione
			orderId = executeMarketOrder((MarketOrder) order);
		else
			orderId = insertLimitStopOrder(order);
		
		return orderId;
	}

	/** Inserisce uno stop o un limit order nelle strutture dedicate */
	private int insertLimitStopOrder(server.OrderTypes.Order order) {
		order.setOrderId(generateOrderId());
		// determina il tipo di ordine
		boolean isBid = order.getType().equals("bid");
		boolean isLimit = order.getOrderType().equals("limit");

		ConcurrentSkipListMap<Integer, PriceLevel> directBook;

		if (isLimit)
			// procedi all accesso alla giusta struttura dati
			directBook = isBid ? bidLimitOrders : askLimitOrders;
		else
			directBook = isBid ? bidStopOrders : askStopOrders;

		int price = order.getPrice();
		directBook.putIfAbsent(price, new PriceLevel());

		// prova ad inserirlo nella struttura dati dedicata
		if (directBook.get(price).addOrder(order)) {
			// l'inserimento ha avuto successo
			// a questo punto inseriscilo nella struttura dati generale
			activeOrders.put(order.getOrderId(), order);
			return order.getOrderId();
		} else {
			// L'inserimento è fallito
			return -1;
		}
	}

	/** Tenta di eliminare l'ordine.
	 *  @return true in caso di successo, false altrimenti */
	public synchronized boolean cancelOrder(int orderId) {
		// risali all'ordine dalL'id
		Order order = activeOrders.get(orderId);
		// flag di controllo per la corretta eliminazione
		boolean isRemoved = false;

		// se l'oggetto (limit o stop) esiste prosegui a eliminarlo
		if (order != null) {
			// verifica il tipo di ordine: bid o ask
			boolean isBid = order.getType().equals("bid");
			// verifica se l'ordine è di tipo stop o limit
			boolean isLimit = order.getOrderType().equals("limit");
			ConcurrentSkipListMap<Integer, PriceLevel> directBook;
			if (isLimit)
				// procedi all accesso alla giusta struttura dati
				directBook = isBid ? bidLimitOrders : askLimitOrders;
			else
				directBook = isBid ? bidStopOrders : askStopOrders;
			int key = order.getPrice();

			// prima lo rimuovo dalla struttura dati dedicata
			if (isRemoved = directBook.get(key).removeOrder(order))
				// poi se la rimozione ha successo, rimuovilo daLla struttura dati generale degli ordini attivi
				activeOrders.remove(orderId);

			// se quello rimosso era l'ultimo ordine
			if (directBook.get(key).isEmpty())
				// -> allora rimuovi completamente la entry dalla mappa
				directBook.remove(key);
		}
		return isRemoved;
	}

	/**
	 * Metodo che permette di eseguire, se possibile, un Market order
	 * @param order l'ordine da eseguire
	 * @return orderId se l'ordine è stato eseguito, -1 altrimenti
	*/
	private synchronized int executeMarketOrder (server.OrderTypes.MarketOrder order) {
		// verifica il tipo di ordine
		boolean isBid = order.getType().equals("bid");
		// salva la sua dimensione
		int sizeRemaining = order.getSize();
		// prendi la mappa degli ordini opposti
		ConcurrentSkipListMap<Integer, PriceLevel> oppositeBook = isBid ? askLimitOrders : bidLimitOrders;
		
		// se la somma di tutte le size di oppositeBook puo soddisfare l'ordine
		// allora prosegui a eseguirlo
		if (sumTotalSizes(oppositeBook) >= sizeRemaining) {
			// inizializza le variabili utili per l'effettiva esecuzione degli ordini
			// in list ci vanno tutti gli i limit orders necessari per completare la transazione col relativo market
			List <server.OrderTypes.Order> someOrders = new LinkedList<>();
			// in map ci vanno tutte le coppie price-size per tenere traccia dei vari prezzi a cui viene eseguito il market
			TreeMap <Integer, Long> priceSizeCouple = isBid ?
					new TreeMap<>() : new TreeMap<>(Collections.reverseOrder());
			
			// per ottenere la coppia chiave-valore
			Map.Entry<Integer, PriceLevel> firstEntry;
			// per ottenere il valore
			PriceLevel firstPriceLevel;
			//per ottenere la dimensione totale
			long totalSize;
			
			while (sizeRemaining > 0) {
				// assegna nuovi valori ad ogni ciclo alle variabili inizializzate fuori dal while
				firstEntry = getBestEntry(isBid ? "ask" : "bid");
				firstPriceLevel = firstEntry.getValue();
				totalSize = firstPriceLevel.getTotalSize();
				// aggiungi alla mappa il price dell'ordine/i della transazione
				// e la relativa size che é il minimo tra totalSize e sizeRemaining
				priceSizeCouple.put(firstEntry.getKey(), Math.min(totalSize, sizeRemaining));
				
				if (totalSize > sizeRemaining) { //caso in cui totalSize > size -> ho finito
					// aggiungi a someOrders alcuni ordini di firstPriceLevel
					someOrders.addAll(firstPriceLevel.getSome(sizeRemaining));
					sizeRemaining = 0;
				} else {
					// aggiungi a someOrders tutti gli ordini di firstPriceLevel
					someOrders.addAll(firstPriceLevel.getAll());
					// quindi una volta svuotata la coda di firstPriceLevel
					// rimuovi dalla mappa la coppia <chiave, valore>
					oppositeBook.remove(firstEntry.getKey());
					
					if (totalSize == sizeRemaining) { //caso in cui totalSize == size -> ho finito
						sizeRemaining = 0;
					} else { //caso in cui totalSize < size -> continue
						sizeRemaining = (int)(sizeRemaining - totalSize);
					}
				}
			}
			// setta il prezzo di esecuzione del market al primo prezzo letto da lista
			order.setExecutedPrice(priceSizeCouple.firstKey());

			if ( order.getOrderId() == 0 ) //se stiamo avendo a che fare con un market order
				// allora setta l'orderId
				order.setOrderId(generateOrderId());

			someOrders.add(order);

			registerExecution(someOrders);

			return order.getOrderId();

		} else { // altrimenti se non puoi eseguirlo ritorna -1
			return -1;
		}
	}

	/** Metodo che implementa un algoritmo di time-price priority per il matching dei Limit orders */
	private synchronized void matchLimitOrders () {
		boolean flag = true;
		// Continua finché ci sono ordini che possono essere matchati
		while (flag && !askLimitOrders.isEmpty() && !bidLimitOrders.isEmpty()) {
			// Ottieni il miglior prezzo di vendita (più basso) e di acquisto (più alto)
			Map.Entry<Integer, PriceLevel> bestAsk = getBestEntry("ask");
			Map.Entry<Integer, PriceLevel> bestBid = getBestEntry("bid");

			// Se il prezzo di acquisto è >= al prezzo di vendita => possiamo matchare gli ordini
			if (bestBid.getKey() >= bestAsk.getKey()) {
				PriceLevel askLevel = bestAsk.getValue();
				PriceLevel bidLevel = bestBid.getValue();

				long totalAskSize = askLevel.getTotalSize();
				long totalBidSize = bidLevel.getTotalSize();

				// Determina la quantità da eseguire (il minimo tra le due size)
				long executeSize = Math.min(totalAskSize, totalBidSize);

				// SCELTA IMPLEMENTATIVA: Eseguiamo al prezzo di vendita (più favorevole per l'acquirente)
				int executedPrice = bestAsk.getKey();

				List<Order> ask;
				List<Order> bid;

				if (totalAskSize == totalBidSize) {
					// case 1 askSize == bidSize
					ask = askLevel.getAll();
					bid = bidLevel.getAll();
					askLimitOrders.remove(bestAsk.getKey());
					bidLimitOrders.remove(bestBid.getKey());
				} else if (executeSize == totalAskSize) {
					// case 2 askSize < bidSize
					ask = askLevel.getAll();
					askLimitOrders.remove(bestAsk.getKey());
					bid = bidLevel.getSome(executeSize);
				} else {
					// case 3 askSize > bidSize
					bid = bidLevel.getAll();
					bidLimitOrders.remove(bestBid.getKey());
					ask = askLevel.getSome(executeSize);
				}
				// setta il nuovo executed price per gli ordini di acquisto
				bid.forEach(order -> order.setExecutedPrice(executedPrice));

				// unisci le due liste
				ask.addAll(bid);

				registerExecution(ask);

			} else {
				// Se il miglior bid è minore del miglior ask, non ci sono più match possibili
				flag = false;
			}
		}

	}

	/**
	 * Processa gli ordini di tipo stop analizzando se le condizioni di attivazione sono state raggiunte.
	 * Quando un ordine stop viene attivato, viene convertito in un market order.
	 *
	 * @param type Il tipo di ordine da processare ("bid" per acquisto o "ask" per vendita)
	 */
	private void processStopOrders(String type) {
		// Seleziona la struttura dati appropriata in base al tipo di ordine
		ConcurrentSkipListMap<Integer, PriceLevel> stopOrders =
				type.equals("bid") ? bidStopOrders : askStopOrders;

		// Inizializza il flag di controllo - continua se ci sono ordini stop da processare
		boolean canProcess = !stopOrders.isEmpty();

		while (canProcess) {
			// Ottiene il miglior prezzo corrente dal book degli ordini LIMIT
			Map.Entry<Integer, PriceLevel> bestEntry = getBestEntry(type);
			// Ottiene il prezzo del primo ordine stop in attesa
			int stopPrice = stopOrders.firstKey();

			// Verifica se le condizioni di attivazione sono soddisfatte:
			// Per bid: il prezzo di mercato deve essere >= al prezzo stop
			// Per ask: il prezzo di mercato deve essere <= al prezzo stop
			canProcess = bestEntry != null &&
					!stopOrders.isEmpty() &&
					(type.equals("bid") ?
							bestEntry.getKey() >= stopPrice :
							bestEntry.getKey() <= stopPrice);

			if (canProcess) {
				// Recupera il priceLevel in cui è collocato lo stop order
				PriceLevel stopLevel = stopOrders.get(stopPrice);
				if (stopLevel != null && !stopLevel.isEmpty()) {
					// Converte ed esegue lo stop order come market order
					executeMarketOrder((MarketOrder) stopLevel.getFirstOrder());
					// Rimuove il priceLevel se non ci sono più ordini
					if (stopLevel.isEmpty())
						stopOrders.remove(stopPrice);
				} else {
					// Rimuove livelli di prezzo vuoti
					stopOrders.remove(stopPrice);
				}
			}
		}
	}

	/** Metodo che ritorna la somma di tutte le sizes di ogni Pricelevel contenuto nella cuncurrentskiplist corrente */
	private long sumTotalSizes(ConcurrentSkipListMap<Integer, PriceLevel> book) {
		AtomicLong sum = new AtomicLong(0);
		book.forEach((key, value) -> {
			sum.addAndGet(value.getTotalSize());
		});
		return sum.get();
	}

	/** Metodo che aggiurna le strutture dati dell'orderbookManager
	 *  in seguto all esecuzione di una lista di ordini
	 *  e invoca il metodo di notifica di UDPnotificationSender*/
	// implicitamente sincronizzato perche viene chiamato solo in executeMarketOrder e match limit orders
	private void registerExecution(List<server.OrderTypes.Order> executedOrders) {

		for (Order order : executedOrders) {
			this.activeOrders.remove(order.getOrderId());
		}

		this.completedOrders.addAll(executedOrders);
		this.needSavings = true;

		udpNotificationSender.removeAndNotify(executedOrders);
	}

	/// Metodo che attiva lo scheduler dedicato al matching dei limit orders
	private void startLimitOrderScheduler(){
		limitOrdersScheduler.scheduleWithFixedDelay( this::matchLimitOrders, 60, 180, TimeUnit.SECONDS);
	}

	/// Metodo che attiva lo scheduler dedicato al processing degli stop orders
	private void startStopOrderScheduler() {
		stopOrdersScheduler.scheduleWithFixedDelay(() -> {
			try {
				processStopOrders("bid");
				processStopOrders("ask");
			} catch (Exception e) {
				System.err.println("Errore nello scheduler degli stop orders: " + e.getMessage());
			}
		}, 120, 240, TimeUnit.SECONDS);
	}

	/** Metodo che attiva lo scheduler per la persistenza dei dati order */
	private void startPersistenceScheduler() {
		//uso scheduleWithFixedDelay per evitare di rallentare l'esecuzione se c'è un accumulo di task da eseguire
		savingsScheduler.scheduleWithFixedDelay(()-> {
			if (needSavings) {
				System.out.println("Persistenza degli ordini in corso...");
				saveUsers();
				needSavings = false;
				System.out.println("Persistenza degli ordini completata");
			}
		}, 60, 300, java.util.concurrent.TimeUnit.SECONDS);
	}
	
	/**
	 * Carica lo storico degli ordini completati dal file di persistenza.
	 * Utilizza la classe di utilità OrderPersistenceUtil per recuperare
	 * gli ordini salvati nel file specificato da ORDERS_FILE
	 */
	private void loadOrders(){
		this.completedOrders = OrderPersistenceUtil.loadOrders(ORDERS_FILE);
	}
	
	/**
	 * Salva lo storico degli ordini completati nel file di persistenza.
	 */
	private void saveUsers() { OrderPersistenceUtil.saveOrders(ORDERS_FILE, completedOrders);}
	
	/**
	 * Recupera lo storico dei Prezzi di un determinato mese e anno.
	 * 
	 * @param year  l'anno di riferimento
	 * @param month il mese di riferimento
	 * @return una lista di ordini filtrata per il mese e l'anno specificati
	 */
	public List<DailyPrice> getPriceHistory(int year, int month) {
		return PriceHistoryCalculator.getPriceHistory(year, month, completedOrders);
	}

	/**
	 * Ottiene la migliore entry dei LIMIT ORDERS (prezzo e PriceLevel) per il tipo di ordine specificato
	 * @param type il tipo di ordine ("ask" o "bid")
	 * @return Map.Entry contenente il miglior prezzo e il relativo PriceLevel, o null se non ci sono ordini
	 */
	public synchronized Map.Entry<Integer, PriceLevel> getBestEntry(String type) {
		ConcurrentSkipListMap<Integer, PriceLevel> orderBook =
				type.equalsIgnoreCase("ask") ? askLimitOrders : bidLimitOrders;

		return orderBook.isEmpty() ? null : orderBook.firstEntry();
	}

	/**
	 * Genera un ID univoco per un nuovo ordine,
	 * il metodo è sincronizzato per evitare race conditions nell'incremento del contatore.
	 * 
	 * @return un nuovo ID progressivo univoco per l'ordine
	 */
	private synchronized static int generateOrderId() { return ++orderIdCounter; }
}