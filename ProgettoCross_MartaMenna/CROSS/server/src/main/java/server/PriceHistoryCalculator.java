package server;

import server.OrderTypes.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class PriceHistoryCalculator {

    // Classe di supporto per calcolare in modo incrementale open/high/low/close di un giorno
    private static class DailyStats {
        long firstTime;    // timestamp (ms) del primo ordine del giorno
        long lastTime;     // timestamp (ms) dell'ultimo ordine del giorno
        double open;       // prezzo di apertura del giorno
        double close;      // prezzo di chiusura del giorno
        double high;       // prezzo massimo del giorno
        double low;        // prezzo minimo del giorno

        // Costruttore iniziale: primo ordine del giorno
        public DailyStats(long timestamp, double price) {
            this.firstTime = timestamp;
            this.lastTime = timestamp;
            this.open = price;
            this.close = price;
            this.high = price;
            this.low = price;
        }
    }

    /**
     * Calcola lo storico giornaliero dei prezzi (open, high, low, close) per il
     * mese e l'anno specificati, a partire dagli ordini forniti.
     * @param year   Anno da elaborare (es. 2025)
     * @param month  Mese da elaborare (1=gennaio,..,12=dicembre)
     * @param orders Lista di ordini (ognuno con timestamp UTC e prezzo)
     * @return       Lista ordinata di DailyPrice, una voce per ogni giorno con ordini,
     *               ordinata per numero di giorno crescente (1..31).
     */
    public static List<DailyPrice> getPriceHistory(int year, int month, List<server.OrderTypes.Order> orders) {
        // Controlla la validità anno/mese: se non corretti, restituisci null
        if (!isCorrect(year, month)) {
            return null;
        }

        // Mappa temporanea: chiave = giorno del mese, valore = statistiche accumulate del giorno
        // (la TreeMap mantiene le chiavi ordinate, così alla fine la lista risulterà già ordinata per giorno)
        Map<Integer, DailyStats> statsByDay = new TreeMap<>();

        // Ciclo su tutti gli ordini dati
        for (server.OrderTypes.Order order : orders) {
            // timestamp in sec dall'epoca UTC
            long timestamp = order.getTimestamp();
            // prezzo associato all'ordine
            int price = order.getPrice();

            // Convertiamo il timestamp in data (anno, mese, giorno) in UTC
            Instant instant = Instant.ofEpochSecond(timestamp);
            OffsetDateTime odt = instant.atOffset(ZoneOffset.UTC);
            // 'odt' ora contiene data/ora in UTC, da cui estrarre anno, mese, giorno

            int oYear = odt.getYear();
            int oMonth = odt.getMonthValue();

            // Se l'ordine è del mese e anno richiesto, lo processiamo
            if (oYear == year && oMonth == month) {
                // giorno del mese (1..31)
                int day = odt.getDayOfMonth();

                // Recupera o inizializza le statistiche per questo giorno
                DailyStats stats = statsByDay.get(day);
                if (stats == null) {
                    // Primo ordine registrato per questo giorno: crea DailyStats iniziale
                    stats = new DailyStats(timestamp, price);
                    statsByDay.put(day, stats);
                } else {
                    // Non è il primo ordine del giorno -> aggiorniamo open/close/high/low
                    if (timestamp < stats.firstTime) {
                        // Nuovo ordine più vecchio di quelli visti: aggiorno open
                        stats.firstTime = timestamp;
                        stats.open = price;
                    } else if (timestamp > stats.lastTime) {
                        // Nuovo ordine più recente di quelli visti: aggiorno close
                        stats.lastTime = timestamp;
                        stats.close = price;
                    }
                    // Aggiorniamo min e max sul prezzo
                    if (price < stats.low) {
                        stats.low = price;
                    }
                    if (price > stats.high) {
                        stats.high = price;
                    }
                }
            }
        }

        // Costruzione della lista finale di DailyPrice, ordinata per giorno
        List<DailyPrice> result = new ArrayList<>();

        for (Map.Entry<Integer, DailyStats> entry : statsByDay.entrySet()) {
            int day = entry.getKey();
            DailyStats stats = entry.getValue();
            // Crea oggetto DailyPrice con i valori calcolati
            DailyPrice dp = new DailyPrice(day, stats.open, stats.close, stats.high, stats.low);
            result.add(dp);
        }

        return result;
    }

    private static boolean isCorrect( int year, int month){
        return year >= 2024 && year <= 2025 && month >= 1 && month <= 12;
    }
}