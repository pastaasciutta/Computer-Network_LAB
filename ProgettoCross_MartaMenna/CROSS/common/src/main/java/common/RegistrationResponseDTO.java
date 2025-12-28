// RegistrationResponseDTO.java
package common;

import java.io.Serializable;

public class RegistrationResponseDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private final int statusCode;
	private final String message;
	
	/**
	 * Classe che rappresenta la risposta rmi del servizio di registrazione.
	 * @param statusCode il codice di stato della risposta
	 * @param message il messaggio di risposta
	 */
	public RegistrationResponseDTO(int statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
	}
	
	public int isSuccess() { return statusCode; }
	public String getMessage() { return message; }
}