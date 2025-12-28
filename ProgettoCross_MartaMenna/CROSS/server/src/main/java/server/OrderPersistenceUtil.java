package server;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Classe di utilità per la gestione della persistenza degli ordini in formato JSON,
 * permette di salvare e caricare gli ordini da/verso file con la serializzazione/deserializzazione JSON.
 */
public class OrderPersistenceUtil {
    // Istanza Gson per la serializzazione/deserializzazione JSON
    private static final Gson gson = new Gson();
    
    /**
     * Carica gli ordini da un file JSON in una List.
     * Gli ordini vengono letti usando JsonReader per un parsing efficiente.
     *
     * @param filePath Il percorso del file JSON da cui caricare gli ordini
     * @return List contenente gli ordini ordinati per timestamp
     */
    public static synchronized List<server.OrderTypes.Order> loadOrders(String filePath) {
        // Inizializza la lista
        List<server.OrderTypes.Order> ordersList = new LinkedList<>();
        
        // Ottiene l'URL della risorsa
        URL resourceUrl = OrderPersistenceUtil.class.getClassLoader().getResource(filePath);
        // Verifica se la risorsa esiste
        if (resourceUrl == null) {
            System.err.println("> RISORSA NON TROVATA per il caricamento: " + filePath);
            return ordersList;
        }

        try {
            // Converte l'URL in Path
            URI resourceUri = resourceUrl.toURI();
            Path path = Paths.get(resourceUri);

            // Apre il file JSON in lettura
            try (JsonReader reader = new JsonReader(
                Files.newBufferedReader(path, StandardCharsets.UTF_8))) {

                // Inizia la lettura dell'oggetto JSON principale
                reader.beginObject();
                // Itera attraverso le proprietà dell'oggetto
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    // Se trova l'array "trades", inizia a processare gli ordini
                    if (name.equals("trades")) {
                        reader.beginArray();
                        // Legge ogni ordine nell'array
                        while (reader.hasNext()) {
                            reader.beginObject();
                            // Legge e converte l'ordine
                            server.OrderTypes.Order order = readOrder(reader);
                            // Se l'ordine è valido, lo aggiunge alla coda
                            if (order != null) {
                                ordersList.add(order);
                            }
                            reader.endObject();
                        }
                        reader.endArray();
                    } else {
                        // Salta altri campi non rilevanti
                        reader.skipValue();
                    }
                }
                reader.endObject();

                System.out.println("> Caricati " +  ordersList.size()
                        + " ordini da: " + path);

            } catch (IOException e) {
                // Gestisce gli errori di I/O durante la lettura
                System.err.println("Errore nel caricamento degli ordini: " + 
                    e.getMessage() + " su path: " + path);
                e.printStackTrace(System.err);
            }

        } catch (URISyntaxException | IllegalArgumentException e) {
            // Gestisce gli errori di conversione del percorso
            System.err.println("Errore nell'accesso al file: " + e.getMessage());
            e.printStackTrace(System.err);
        }

        return ordersList;
    }
    
    /**
     * Salva una List di ordini in formato JSON.
     *
     * @param filePath Il percorso del file JSON in cui salvare gli ordini
     * @param orderList La LinkedList di ordini da salvare
     */
    public static synchronized void saveOrders(String filePath, List<server.OrderTypes.Order> orderList) {
        // Verifica la validità della coda di input
        if (orderList == null) {
            System.err.println("> Tentativo di salvare una coda di ordini null. Nessun salvataggio eseguito.");
            return;
        }

        // Ottiene l'URL della risorsa 
        URL resourceUrl = OrderPersistenceUtil.class.getClassLoader().getResource(filePath);
        if (resourceUrl == null) {
            System.err.println("> RISORSA NON TROVATA per il salvataggio: " + filePath);
            return;
        }

        try {
            // Converte l'URL in un percorso file valido
            URI resourceUri = resourceUrl.toURI();
            Path path = Paths.get(resourceUri);
            System.out.println("> Salvataggio di " + orderList.size() + " ordini in: " + path);
            
            // In termini di numeri approssimativi, con 1000 ordini:
            // - JsonWriter diretto: ~100ms > - Accesso diretto ai getter
            //                                - Nessuna reflection
            //                                - Scrittura immediata nel buffer
            //                                - Consumo di memoria costante e prevedibile
            // - gson.toJson(): ~150-200ms >  - Usa reflection per esaminare la classe
            //                                - Deve scoprire i campi a runtime
            //                                - Crea oggetti temporanei durante la serializzazione
            //                                - Overhead di memoria per la reflection
            // - toString(): ~300-400ms >     - Crea molte stringhe temporanee
            //                                - Ogni concatenazione crea un nuovo oggetto String
            //                                - Maggior pressione sul garbage collector
            //                                - Alto consumo di memoria heap
            //                                - **Prestazioni**: Il più lento dei tre
            
            // Apre il writer JSON e scrive il contenuto
            try (JsonWriter writer = new JsonWriter(
                Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
                
                writer.setIndent("");  // rimuove l'indentazione
                // Inizia la struttura JSON principale
                writer.beginObject();
                writer.jsonValue("\n");  // accapo prima di trades
                writer.name("trades");
                writer.beginArray();
                writer.jsonValue("\n");

                // Scrive gli ordini in sequenza, mantenendo l'ordinamento temporale
                for (server.OrderTypes.Order order : orderList) {
                    writer.beginObject();
                    writer.name("orderId").value(order.getOrderId());
                    writer.name("type").value(order.getType());
                    writer.name("orderType").value(order.getOrderType());
                    writer.name("size").value(order.getSize());
                    writer.name("price").value(order.getPrice());
                    writer.name("timestamp").value(order.getTimestamp());
                    writer.endObject();
                    
                    writer.jsonValue("\n");
                }
                
                // Chiude la struttura JSON
                writer.endArray();
                writer.endObject();

                System.out.println("> Ordini salvati con successo in: " + path);

            } catch (IOException e) {
                // Gestisce gli errori di I/O durante la scrittura
                System.err.println("Errore nel salvataggio degli ordini: " + 
                    e.getMessage() + " su path: " + path);
                e.printStackTrace(System.err);
            }

        } catch (URISyntaxException | IllegalArgumentException e) {
            // Gestisce gli errori di conversione del percorso
            System.err.println("Errore nell'accesso al file: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Legge un singolo ordine dal JsonReader.
     * 
     * @param reader Il JsonReader da cui leggere l'ordine
     * @return L'ordine letto o null se c'è un errore
     * @throws IOException se si verifica un errore di lettura
     */
    private static server.OrderTypes.Order readOrder(JsonReader reader) throws IOException {
        // Variabili per memorizzare i campi dell'ordine
        String type = null;
        String orderType = null;
        int orderId = 0;
        int size = 0;
        int price = 0;
        long timestamp = 0;
        
        // Legge tutti i campi dell'ordine
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "orderId":
                    orderId = reader.nextInt();
                    break;
                case "type":
                    type = reader.nextString();
                    break;
                case "orderType":
                    orderType = reader.nextString();
                    break;
                case "size":
                    size = reader.nextInt();
                    break;
                case "price":
                    price = reader.nextInt();
                    break;
                case "timestamp":
                    timestamp = reader.nextLong();
                    break;
                default:
                    reader.skipValue();
            }
        }
        
        // Crea l'ordine appropriato in base al tipo
        server.OrderTypes.Order order;
        switch (orderType) {
            case "market":
                order = new server.OrderTypes.MarketOrder(type, size, price);
                break;
            case "limit":
                order = new server.OrderTypes.LimitOrder(type, size, price);
                break;
            case "stop":
                order = new server.OrderTypes.StopOrder(type, size, price);
                break;
            default:
                return null;
        }
        order.setOrderId(orderId);
        return order;
    }
}