package server.OrderTypes;

/**
 * Rappresenta un Limit Order.
 * L'ordine viene eseguito solo se il prezzo di mercato raggiunge il prezzo limite specificato.
 */
public class LimitOrder extends server.OrderTypes.Order {

    public LimitOrder(String type, int size, int price) {
        super(type, size, price);
    }

    // metodi utile per clonare e splittare un ordine
    public void setSize(int size) { this.size = size; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String getOrderType() {
        return "limit";
    }

}