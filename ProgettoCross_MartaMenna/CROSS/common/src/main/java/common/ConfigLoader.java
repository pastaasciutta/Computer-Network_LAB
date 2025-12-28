package common;

import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;

public class ConfigLoader {
private final Properties properties;

    /**
     * Costruisce un nuovo caricatore di configurazioni da file properties.
     *
     * @param filePath il percorso del file properties da caricare (deve essere nel classpath)
     * @throws IOException se il file non viene trovato o non può essere letto
     *
     * <p>Il file properties deve essere presente nella cartella resources del progetto.
     * Durante la compilazione, il contenuto della cartella resources viene copiato
     * nella root del classpath, da dove verrà poi caricato.</p>
     */
    public ConfigLoader(String filePath) throws IOException {
        properties = new Properties();
        //getResourceAsStream()` cerca il file nel classpath, che include sia il codice compilato che le risorse.
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(filePath)) {
            // Verifichiamo che il file sia stato trovato
            if (input == null) {
                throw new IOException("File di configurazione non trovato: " + filePath);
            }
            properties.load(input);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public int getIntProperty(String key) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (NumberFormatException e) {
            System.err.println("Errore durante la lettura del file properties, argomento mancante: " + key);
            return -1;
        }
    }
}