package server;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UserManager {
    private static final String USERS_FILE = "data/users.json";
    private final long INACTIVITY_TIMEOUT;
    private final ConcurrentHashMap<String, User> registeredUsers;
    private final ConcurrentHashMap<String, Long> activeUsers;
    private final ScheduledExecutorService inactivityScheduler;
    private final ScheduledExecutorService savingsScheduler;
    private volatile boolean needSavings;
    /**
     * Gestisce l'autenticazione e la gestione degli utenti del sistema.
     *
     * @param config il caricatore della configurazione che contiene il timeout di inattività
     *
     *
     * <p>Questa classe fornisce funzionalità per:</p>
     * <ul>
     *   <li>Registrazione di nuovi utenti</li>
     *   <li>Autenticazione (login/logout)</li>
     *   <li>Aggiornamento delle credenziali</li>
     *   <li>Gestione automatica del timeout per inattività</li>
     *   <li>Persistenza dei dati utente</li>
     * </ul>
     *
     * <p>La classe utilizza strutture dati thread-safe per gestire gli utenti registrati
     *  e quelli attualmente connessi. Gli utenti vengono automaticamente disconnessi dopo
     *  un periodo di inattività configurabile tramite il parametro 'inactivityTimeout'
     *  nel file di configurazione.</p>
     *
     * <p>I dati degli utenti vengono persistiti su file JSON per mantenere lo stato
     * tra i riavvii del sistema.</p>
     */
    public UserManager(common.ConfigLoader config) {
        this.registeredUsers = new ConcurrentHashMap<>();
        this.activeUsers = new ConcurrentHashMap<>();
        this.INACTIVITY_TIMEOUT = config.getIntProperty("inactivityTimeout") * 1000L;
        this.inactivityScheduler = Executors.newScheduledThreadPool(1);
        this.savingsScheduler = Executors.newScheduledThreadPool(1);
        this.needSavings = false;
        
        loadUsers();
        startInactivityChecker();
        startPersistenceScheduler();
    }

    public boolean isLogged(String username) {
        return activeUsers.containsKey(username);
    }
    
    public synchronized boolean registerUser(String username, String password) {
        if (username == null || password == null || password.isEmpty() || registeredUsers.containsKey(username))
            return false;
        
        User user = new User(username, password);
        registeredUsers.put(username, user);
        needSavings = true;
        System.out.println("Nuova registrazione utente: " + username);
        return true;
    }

    public synchronized boolean loginUser(String username, String password) {
        User user = registeredUsers.get(username);
        if (user == null || !user.getPassword().equals(password) || activeUsers.containsKey(username))
            return false;
        
        activeUsers.put(username, System.currentTimeMillis());
        return true;
    }

    // op atomica non è necessario sincronizzarla
    public boolean logoutUser(String username) {
        return activeUsers.remove(username) != null;
    }

    public synchronized boolean updatePassword(String username, String oldPassword, String newPassword) {
        User user = registeredUsers.get(username);
        if (user == null || newPassword == null || !user.getPassword().equals(oldPassword))
            return false;
        
        user.setPassword(newPassword);
        needSavings = true;
        return true;
    }

    private void startInactivityChecker() {
        // con il comando runnable come una lambda expression
        inactivityScheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            // A map entry (key-value pair). The Map.entrySet method returns a collection-view of the map,
            // whose elements are of this class.
            for (Map.Entry<String, Long> entry : activeUsers.entrySet()) {
                if (currentTime - entry.getValue() > INACTIVITY_TIMEOUT) {
                    activeUsers.remove(entry.getKey());
                    System.out.println("Logout automatico per inattività: " + entry.getKey());
                }
            }
        }, 10, 5, java.util.concurrent.TimeUnit.MINUTES);
    }
    
    // metodo che attiva lo scheduler per la persistenza dei dati utente
    private void startPersistenceScheduler() {
        savingsScheduler.scheduleWithFixedDelay(()-> {
            if (needSavings) {
                System.out.println("Persistenza dei dati utente in corso...");
                saveUsers();
                needSavings = false;
                System.out.println("Persistenza dei dati utente completata");
            }
        }, 30, 120, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void loadUsers() {
        // Carica tutti gli utenti dal file di persistenza
        List<User> users = UserPersistenceUtil.loadUsers(USERS_FILE);
        // Ed in seguito li mette nella hashmap registeredUsers: username -> User
        for (User user : users) {
            registeredUsers.put(user.getUsername(), user);
        }
    }

    // non c'è bisogno di sincronizzare questo metodo perche viene chiamato da un solo thread
    private void saveUsers() {
        // Salva tutti gli utenti registrati nel file di persistenza
        UserPersistenceUtil.saveUsers(USERS_FILE, new ArrayList<>(registeredUsers.values()));
    }
    
    private void shutdown() {
        try {
            // Tentativo di chiusura normale
            inactivityScheduler.shutdown();
            savingsScheduler.shutdown();

            // Aspetta che i task in esecuzione terminino (max 60 secondi)
            if (!inactivityScheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                inactivityScheduler.shutdownNow();
            }
            if (!savingsScheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                savingsScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            // Forza la chiusura in caso di interruzione
            inactivityScheduler.shutdownNow();
            savingsScheduler.shutdownNow();
            // Ripristina il flag di interruzione
            Thread.currentThread().interrupt();
        }
    }
}