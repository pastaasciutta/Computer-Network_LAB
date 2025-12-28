package server;

import common.RegistrationService;
import common.RegistrationResponseDTO;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// UnicastRemoteObject rende l'oggetto capace di ricevere chiamate remote.
public class RegistrationServiceImpl extends UnicastRemoteObject implements RegistrationService {
	private final UserManager userManager;
	
	/**
	 * Classe che implementa l'interfaccia remota RegistrationService.
	 * @param userManager il gestore degli utenti per la logica dell'autenticazione
	 * @throws RemoteException se si verifica un errore nella comunicazione remota
	 * */
	public RegistrationServiceImpl(UserManager userManager) throws RemoteException {
		this.userManager = userManager;
	}

	@Override
	public RegistrationResponseDTO register(String username, String password) throws RemoteException {
		boolean success = userManager.registerUser(username, password);
		
		if (success) {
			return new RegistrationResponseDTO(100, "Registrazione completata con successo");
		} else {
			if (password == null || password.isEmpty())
				return new RegistrationResponseDTO(101, "Password non valida");
			else
				return new RegistrationResponseDTO(102, "Username non disponibile");
		}
	}
}