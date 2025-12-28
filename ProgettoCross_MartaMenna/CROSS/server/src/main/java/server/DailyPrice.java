package server;

public class DailyPrice {
    private int day; // giorno del mese
    private double open;
    private double close;
    private double high;
    private double low;

    public DailyPrice(int day, double open, double close, double high, double low) {
        this.day = day;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
    }
}

