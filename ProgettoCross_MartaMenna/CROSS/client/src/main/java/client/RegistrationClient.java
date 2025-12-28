// RegistrationClient.java
package client;

import common.RegistrationService;
import common.RegistrationResponseDTO;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/** FUNZIONAMENTO RMI:
 * Quando il client chiama un metodo sullo stub (es. `service.register(...)`):
 * 1. Lo stub si occupa di "marshallare" (serializzare) i parametri del metodo (username e password).
 * 2. Invia la richiesta attraverso la rete al server RMI.
 * 3. Sul lato server, uno "skeleton" (che fa parte dell'infrastruttura RMI sul server) riceve la richiesta.
 * 4. Lo skeleton "unmarshalla" (deserializza) i parametri.
 * 5. Invoca il metodo effettivo sull'oggetto reale. `RegistrationServiceImpl`
 * 6. Il risultato (o l'eccezione) del metodo viene marshallato dallo skeleton.
 * 7. Il risultato viene inviato indietro allo stub sul client.
 * 8. Lo stub unmarshalla il risultato e lo restituisce al codice client.
 */
public class RegistrationClient {
	private final String host;
	private final int rmiPort;
	private RegistrationService registrationService;
	
	/**
	 * Costruisce un nuovo RegistrationClient.
	 *
	 * @param host l'hostname o l'indirizzo IP del server RMI
	 * @param rmiPort la porta RMI su cui il server è in ascolto
	 */
	public RegistrationClient(String host, int rmiPort) {
		this.host = host;
		this.rmiPort = rmiPort;
	}

	/** @throws java.rmi.RemoteException
	 *  @throws java.rmi.NotBoundException
	 *  @throws java.rmi.AccessException
	 */
	public void connect() throws Exception {
		// Returns a reference to the remote object Registry on the specified host and port
		Registry registry = LocateRegistry.getRegistry(host, rmiPort);
		// Returns the remote reference bound to the specified name in this registry.
		registrationService = (RegistrationService) registry.lookup("RegistrationService");
	}
	
	public RegistrationResponseDTO register(String username, String password) throws RemoteException {
		if (registrationService == null) {
			throw new IllegalStateException("Client non connesso");
		}
		return registrationService.register(username, password);
	}
}