package client;

import common.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class ClientMain {
	public static void main(String[] args) {

		// Carica le configurazioni dal file properties (server.properties)
		ConfigLoader config;
		try {
			config = new ConfigLoader("client.properties");
		} catch (IOException e) {
			System.out.println("Errore nel caricamento del file di configurazione: " + e.getMessage());
			return;
		}

		// Legge i parametri di configurazione dal file properties
		int serverPort = config.getIntProperty("serverTcpPort");

		String host = config.getProperty("serverHost");
		int rmiPort = config.getIntProperty("rmiPort");

		int clientPort = config.getIntProperty("udpPort");
		int udpReadBuffSize = config.getIntProperty("udpReadBuffSize");


		try ( SocketChannel channel = SocketChannel.open(
				new InetSocketAddress(host, serverPort)) ) {

			// Crea e connette il client al servizio RMI (per la registrazione)
			RegistrationClient client = new RegistrationClient(host, rmiPort);
			client.connect();

			System.out.println("> Benvenuto su CROSS!");

			// istanzia un elemento della classe connection handler che permette di gestire la connessione col server
			ConnectionHandler connectionHandler = new ConnectionHandler(channel, client, config);
			Thread thread1 = new Thread(connectionHandler);
			thread1.start();

			// istanzia un elemento della classe udpnotifier che rimane in ascolto per le notifiche relative agli ordini
			UDPNotificationReceiver udpNotifReceiver = new UDPNotificationReceiver(clientPort, udpReadBuffSize);
			Thread thread2 = new Thread(udpNotifReceiver);
			thread2.start();

			thread1.join();

			udpNotifReceiver.stop();
			thread2.join();

		} catch (Exception e) {
			System.err.println("Errore: " + e.getMessage());
		}
	}
}
