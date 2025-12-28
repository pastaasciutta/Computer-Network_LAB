package server;

import common.ConfigLoader;
import common.RegistrationService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.*;

public class ServerMain {
	public static void main(String[] args) {
		
		// Carica le configurazioni dal file properties (server.properties)
		ConfigLoader config;
		try {
			config = new ConfigLoader("server.properties");
		} catch (IOException e) {
			System.out.println("Errore nel caricamento del file di configurazione: " + e.getMessage());
			return;
		}

		// Legge i parametri di configurazione dal file properties
		int port = config.getIntProperty("port");
		int threadPoolSize = config.getIntProperty("threadPoolSize");
		int udpPort = config.getIntProperty("udpPort");
		String multicastAddress = config.getProperty("multicastAddress");
		// Leggi la porta RMI dal file di configurazione
		int rmiPort = config.getIntProperty("rmiPort");
		
		// Crea il thread pool per gestire le operazioni di I/O
		ExecutorService threadPool =
				new ThreadPoolExecutor( 0,
										threadPoolSize,
										30,
										TimeUnit.SECONDS,
										new LinkedBlockingQueue<>(32));

		try ( Selector selector = Selector.open(); // Apre il selector e ServerSocketChannel
		      ServerSocketChannel serverChannel = ServerSocketChannel.open() ) {
			
			// ServerSocketChannel in modalità non bloccante e bindato all'InetSocketAddress
			serverChannel.configureBlocking(false);
			serverChannel.socket().bind(new InetSocketAddress(port));
			// Registra il ServerSocketChannel sul selector per accettare connessioni in entrata
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);

			System.out.println("Server avviato sulla porta " + port);

			// Istanzia la classe che gestisce l'invio di notifiche udp asincrone
			UDPNotificationSender udpNotificationSender = new UDPNotificationSender();

			// Istanzia la classe di gestione utenti (registrazione, login, logout)
			UserManager userManager = new UserManager(config);
			
			// Istanzia la classe di gestione ordini (order book, matching)
			OrderBookManager orderBookManager = new OrderBookManager(udpNotificationSender);

			// Istanzia il servizio di notifiche per il superamento delle soglie di prezzo
			NotificationService notificationService = new NotificationService(udpPort, multicastAddress, orderBookManager);

			// Istanzia il SelectorManager che gestisce il ciclo di 'selezione' del selector
			SelectorManager selectorManager = new SelectorManager(selector, threadPool, userManager, orderBookManager,
					notificationService, udpNotificationSender, config);
			
			// Avvia il registry RMI
			Registry registry = LocateRegistry.createRegistry(rmiPort);
			
			// Crea e pubblica il servizio RMI
			RegistrationService registrationService = new RegistrationServiceImpl(userManager);
			// Una volta che il registry è attivo e che l'oggetto remoto è stato creato,
			// l'oggetto remoto deve essere "registrato" (o "bound") nel registry con un nome univoco.
			// I client useranno questo nome per cercare l'oggetto
			registry.rebind("RegistrationService", registrationService);
			System.out.println("Servizio RMI avviato sulla porta " + rmiPort);
			
			// Avvia il selector manager in un thread separato
			Thread selectorThread = new Thread(selectorManager, "SelectorManager-Thread");
			selectorThread.start();

			// Il main attende la terminazione dei thread
			selectorThread.join();

			notificationService.shutdown();

		} catch (IOException | InterruptedException e) {
			System.out.println("Errore durante la gestione del server: " + e.getMessage());
		} finally {
			// Chiusura del threadPool
			threadPool.shutdown();
			try {
				if (!threadPool.awaitTermination(10, TimeUnit.SECONDS))
					threadPool.shutdownNow();
			} catch (InterruptedException e) {
				threadPool.shutdownNow();
			}
			System.out.println("Server terminato.");
		}
	}
}