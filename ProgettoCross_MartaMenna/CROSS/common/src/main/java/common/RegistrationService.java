package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Quest'interfaccia dichiara i metodi che potranno essere invocati da client remoti */
public interface RegistrationService extends Remote {
	// il metodo che un client potrà invocare
	RegistrationResponseDTO register(String username, String password) throws RemoteException;
}