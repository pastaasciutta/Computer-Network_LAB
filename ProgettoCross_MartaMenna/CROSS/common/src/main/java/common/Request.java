package common;

import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Classe che rappresenta una richiesta generica dal client al server.
 * La struttura della richiesta è in formato JSON:
 * {
 *     "operation": "nomeOperazione",
 *     "values": {
 *         "chiave1": valore1,
 *         "chiave2": valore2
 *     }
 * }
 */
public class Request {
    // Operazione da eseguire (es. "login", "logout", "insertMarketOrder", ecc.)
    private final String operation;
    // Mappa contenente i parametri della richiesta
    private final Map<String, Object> values;
    
    /**
     * Costruttore principale che inizializza una nuova richiesta.
     * @param operation l'operazione da eseguire (non può essere null)
     * @param values i parametri della richiesta (possono essere null)
     * @throws NullPointerException se operation è null
     */
    public Request(String operation, Map<String, Object> values) {
        this.operation = Objects.requireNonNull(operation, "Operation cannot be null");
        this.values = new HashMap<>();
        // Validazione dei valori
        if (values != null) {
            values.forEach((key, value) -> {
                if (key != null) {
                    this.values.put(key, value);
                }
            });
        }
    }

    /**
     * Restituisce il TypeToken per la deserializzazione JSON con Gson.
     *
     * @return TypeToken per la classe Request
     */
    public static TypeToken<Request> getType() {
        return new TypeToken<Request>() {};
    }
    
    /**
     * Restituisce l'operazione richiesta.
     * @return il nome dell'operazione
     */
    public String getOperation() {
        return operation;
    }
    
    /**
     * Restituisce una copia immutabile della mappa dei valori.
     * @return mappa immutabile dei parametri della richiesta
     */
    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }
    
//    /**
//     * Ottiene un valore dalla mappa convertendolo nel tipo specificato.
//     * @param key la chiave del valore da ottenere
//     * @param type il tipo in cui convertire il valore
//     * @return il valore convertito nel tipo specificato, o null se non presente o non convertibile
//     * @throws NullPointerException se key o type sono null
//     */
//    @SuppressWarnings("unchecked")
//    public <T> T getValue(String key, Class<T> type) {
//        Objects.requireNonNull(key, "Key cannot be null");
//        Objects.requireNonNull(type, "Type cannot be null");
//
//        Object value = values.get(key);
//        if (value != null && type.isInstance(value)) {
//            return (T) value;
//        }
//        return null;
//    }
    
    /**
     * Builder pattern per costruire richieste in modo fluente.
     */
    public static class Builder {
        private final String operation;
        private final Map<String, Object> values = new HashMap<>();
        
        /**
         * Inizializza un nuovo builder.
         * @param operation l'operazione da eseguire (non può essere null)
         * @throws NullPointerException se operation è null
         */
        public Builder(String operation) {
            this.operation = Objects.requireNonNull(operation, "Operation cannot be null");
        }
        
        /**
         * Aggiunge un valore alla richiesta.
         * @param key la chiave del valore
         * @param value il valore da aggiungere
         * @return this per concatenamento
         */
        public Builder addValue(String key, Object value) {
            if (key != null) {
                values.put(key, value);
            }
            return this;
        }
        
        /**
         * Costruisce l'oggetto Request.
         * @return una nuova istanza di Request
         */
        public Request build() {
            return new Request(operation, values);
        }
    }
    
    @Override
    public String toString() {
        return String.format("Request{operation='%s', values=%s}", operation, values);
    }
}

// lato client
// // Usando il costruttore
// Map<String, Object> values = new HashMap<>();
// values.put("type", "bid");
// values.put("size", 100);
// Request request = new Request("insertMarketOrder", values);
//
// // Oppure usando il Builder (più elegante)
// Request request = new Request.Builder("insertMarketOrder")
//     .addValue("type", "bid")
//     .addValue("size", 100)
//     .build();

// // Serializzazione in JSON
//         String jsonRequest = gson.toJson(request, Request.getType());
//