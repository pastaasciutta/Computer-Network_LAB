package server.OrderTypes;

/**
 * Rappresenta un Market Order.
 * Viene eseguito immediatamente al miglior prezzo disponibile.
 * Il campo executedPrice indica il prezzo effettivo di esecuzione.
 */
public class MarketOrder extends server.OrderTypes.Order {

    public MarketOrder(String type, int size, int executedPrice) {
        super(type, size, executedPrice);
    }
    
    @Override
    public String getOrderType() {
        return "market";
    }

}