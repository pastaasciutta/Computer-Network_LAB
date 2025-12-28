package server.OrderTypes;

/**
 * Rappresenta uno Stop Order.
 * L'ordine diventa un Market Order una volta che il prezzo di mercato raggiunge o supera la soglia (stopPrice).
 */
public class StopOrder extends Order {

    public StopOrder(String type, int size, int stopPrice) {
        super(type, size, stopPrice);
    }
    
    @Override
    public String getOrderType() {
        return "stop";
    }

}
