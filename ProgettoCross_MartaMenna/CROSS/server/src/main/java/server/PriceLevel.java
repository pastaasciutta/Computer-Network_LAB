package server;

import server.OrderTypes.Order;
import server.OrderTypes.LimitOrder;
import java.util.PriorityQueue;
import java.util.List;
import java.util.LinkedList;

class PriceLevel {
    private volatile long totalSize;
    private final PriorityQueue<Order> orders;

    public PriceLevel() {
        this.totalSize = 0;  // Inizializzazione esplicita
	    //orders ordinati per ordine di inserimento (timestamp)
        this.orders = new PriorityQueue<>((o1, o2) -> 
            Long.compare(o1.getTimestamp(), o2.getTimestamp()));
    }

    public boolean addOrder(Order order) {
        boolean success = orders.offer(order);
        if (success)
            addTotalSize(order.getSize());
        return success;
    }

    public Order getFirstOrder (){
        Order order = orders.poll();
        if (order != null) {
            subtractTotalSize(order.getSize());
        }
        return order;
    }

    public boolean removeOrder(Order order) {
        if (order != null) {
            subtractTotalSize(order.getSize());
            return orders.remove(order);
        }
        return false;
    }

    public long getTotalSize() {
        return totalSize;
    }
    
    public synchronized void subtractTotalSize(long size) {
        this.totalSize = totalSize - size;
    }
    
    public synchronized void addTotalSize(long size) {
        this.totalSize = totalSize + size;
    }
    
    public boolean isEmpty() {
        return orders.isEmpty();
    }

    public List<Order> getSome(long n){
        List<Order> someOrders = new LinkedList<>();
        LimitOrder currentOrder;
        int size;
        
        do{
            // prende e rimuove il primo elemento dalla lista
            currentOrder = (LimitOrder) orders.poll();
            // se l'ordine è non nullo (se non hai svuotato la lista)
            if (currentOrder != null){
                // prendi la dimensione dell'ordine
                size = currentOrder.getSize();
                if (size == n) { // se è ==n hai finito
                    n = 0;
                } else if (size > n) { // se è maggiore di n ne prendi un pezzo di dimensione n
                    // proseguo a splittare l'ordine in due, temp: la parte non coperta da n ritorna nella coda orders
                    LimitOrder temp = cloneLimitOrder(currentOrder);
                    temp.setSize(size - (int) n);
                    orders.offer(temp);
                    currentOrder.setSize((int) n);
                    n = 0;
                } else { // se é minore di n => dovrò prendere un altro elemento dalla lista e aggiornare n
                    n = n - size;
                }
                someOrders.add(currentOrder);
            }
        } while ( n > 0);

        subtractTotalSize(n);
        return someOrders;
    }
    
    public List<Order> getAll(){
        subtractTotalSize(totalSize);
        List<Order> allOrders = new LinkedList<>(orders);
        orders.clear();
        return allOrders;
    }
    
    // metodo che clona un limit order
    private LimitOrder cloneLimitOrder(Order order) {
        
        int orderId = order.getOrderId();
        String type = order.getType();
        int size = order.getSize();
        long timestamp = order.getTimestamp();
        int price = order.getPrice();
        
        LimitOrder newOrder = new LimitOrder(type, size, price);
        newOrder.setOrderId(orderId);
        newOrder.setTimestamp(timestamp);
        
        return newOrder;
    }
}
