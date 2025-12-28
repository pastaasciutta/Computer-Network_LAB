package server.OrderTypes;

/**
 * Classe astratta che rappresenta un ordine generico.
 * Contiene i campi comuni a tutti i tipi di ordine:
 * - orderId: identificativo univoco dell'ordine
 * - type: "bid" o "ask"
 * - size: quantità in millesimi di BTC
 * - price: prezzo in millesimi di USD
 * - timestamp: momento dell'esecuzione (in epoch secondi)
 *
 * La size è sempre espressa in millesimi di BTC e price in millesimi di USD.
 * Ad esempio un ordine di size 1000 e price 58000000 indica la volontà di scambiare 1 BTC per 58 000 USD.
 * Si assuma che nessun ordine possa avere price o size maggiore di (2^31)-1.
 */
public abstract class Order {
    protected int orderId;

    protected int size;
    protected int price;

    protected final String type;
    protected long timestamp;
    
    public Order(String type, int size, int price) {
        orderId = 0;
        this.type = type;
        this.size = size;
        this.price = price;
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    // setters
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    public void setExecutedPrice(int executedPrice) {
        this.price = executedPrice;
    }

    // getters
    public int getOrderId() {
        return this.orderId;
    }
    public String getType() { return this.type; } // ask o bid
    public int getSize() {
        return this.size;
    }
    public int getPrice() { return this.price; }
    public long getTimestamp() {
        return this.timestamp;
    }
    /**
     * Metodo astratto che restituisce il tipo d'ordine (per esempio, "market", "limit" o "stop").
     */
    public abstract String getOrderType();

}