package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe per la gestione del file utenti in formato JSON,
 * fornisce il salvataggio e il caricamento degli utenti da/verso file.<br>
 * Per il caricamento (loadUsers): utilizza Gson standard per aumentare l'efficienza<br>
 * Per il salvataggio (saveUsers): utilizza GsonBuilder con pretty printing per leggibilità<br><br>
 * 
 * Esempio di utilizzo:<br>
 * List<User> users = UserPersistenceUtil.loadUsers("users.json");<br>
 * UserPersistenceUtil.saveUsers("users.json", usersList);
 *
 */
public class UserPersistenceUtil {
	// GSON viene usato solo per loadUsers, prettyGson per saveUsers
	private static final Gson GSON = new Gson();
	private static final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
	private static final Type USER_LIST_TYPE = new TypeToken<List<User>>() { }.getType();
	
	/**
	 * Salva l'intera collezione di utenti nel file specificato:
	 * sovrascrive il file con la lista aggiornata degli utenti.
	 *
	 * @param filePath Il percorso relativo del file JSON
	 * @param users    La collezione di utenti da salvare
	 */
	public static synchronized void saveUsers(String filePath, List<User> users) {
		if (users == null) {
			// Questa potrebbe essere considerata una condizione anomala, quindi un log info/warn va bene
			System.err.println("> Tentativo di salvare una lista di utenti null. Nessun salvataggio eseguito.");
			return;
		}
		
		URL resourceUrl = UserPersistenceUtil.class.getClassLoader().getResource(filePath);
		if (resourceUrl == null) {
			System.err.println("> RISORSA NON TROVATA per il salvataggio: " + filePath);
			return;
		}
		
		try {
			// Convertendo URL in URI, poi URI in Path. Questo è più robusto.
			URI resourceUri = resourceUrl.toURI();
			Path path = Paths.get(resourceUri);
			
			System.out.println("> Salvataggio di " + users.size() + " utenti in: " + path.toString());
			
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
			     JsonWriter jsonWriter = new JsonWriter(writer)) {
				
				jsonWriter.setIndent("  "); // Configura l'indentazione per il pretty printing
				prettyGson.toJson(users, USER_LIST_TYPE, jsonWriter);
				jsonWriter.flush();
				System.out.println("> Utenti salvati con successo in: " + path.toString());
				
			} catch (IOException e) {
				System.err.println("Errore nel salvataggio degli utenti (scrittura file GSON): " + e.getMessage()
						+ " su path: " + path.toString());
				e.printStackTrace(System.err);
				// Considerare se rilanciare un'eccezione personalizzata o RuntimeException
			}
			
		} catch (URISyntaxException e) {
			System.err.println("Errore nella conversione da URL a URI: " + e.getMessage());
			e.printStackTrace(System.err);
		} catch (IllegalArgumentException e) {
			System.err.println("Errore nella creazione del Path dall'URI: " + e.getMessage()
					+ " per URI: " + (resourceUrl != null ? resourceUrl.toString() : "null"));
			e.printStackTrace(System.err);
		}
	}
	
	/**
     * Carica la lista degli utenti dal file JSON.
     *
     * @param filePath Il percorso relativo del file JSON
     * @return La lista degli utenti caricati; se il file non esiste, restituisce una lista vuota.
     */
	public static List<User> loadUsers(String filePath) {
		
		URL resourceUrl = UserPersistenceUtil.class.getClassLoader().getResource(filePath);
		if (resourceUrl == null) {
			System.err.println("> Risorsa user.json NON trovata, per il caricamento: " + filePath +
					". Verrà restituita una lista vuota.");
			return new ArrayList<>();
		}
		
		try {
			URI resourceUri = resourceUrl.toURI();
			Path path = Paths.get(resourceUri);
			
			if (!Files.exists(path)) {
				System.out.println("> Il file utenti " + path.toString() +
						" non esiste. Verrà restituita una lista vuota.");
				return new ArrayList<>();
			}
			
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				
				List<User> users = GSON.fromJson(reader, USER_LIST_TYPE);
				if (users != null) {
					System.out.println("> " + users.size() + " utenti caricati con successo da: " + path.toString());
					return users;
				} else {
					System.out.println("> Nessun utente caricato (file vuoto o formato non valido) da: " +
							path.toString() + ". Verrà restituita una lista vuota.");
					return new ArrayList<>();
				}
				
			} catch (IOException | JsonSyntaxException e) {
				System.err.println("Errore nella lettura o parsing JSON degli utenti: " + e.getMessage() +
						" da path: " + path.toString());
				e.printStackTrace(System.err);
				return new ArrayList<>(); // Restituisce lista vuota in caso di errore
			}
		} catch (URISyntaxException e) {
			System.err.println("Errore nella conversione URL in URI (load): " + e.getMessage());
			e.printStackTrace(System.err);
			return new ArrayList<>();
		} catch (IllegalArgumentException e) {
			System.err.println("Errore nella creazione del Path dall'URI (load): " + e.getMessage() +
					" per URI: " + (resourceUrl != null ? resourceUrl.toString() : "null"));
			e.printStackTrace(System.err);
			return new ArrayList<>();
		}
	}
}