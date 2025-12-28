/* Scrivere un programma JAVA che implementi un server che apre una serversocket su una porta
   e sta in attesa di richieste di connessione.

   Quando arriva una richiesta di connessione, il server accetta la connessione,
   trasferisce al client un file e poi chiude la connessione.

   Ulteriori dettagli:
   - Il server gestisce una richiesta per volta;
   - Il server invia sempre lo stesso file, usate un file di testo. */

import java.io.*;
import java.net.*;

class FileServer
{
    public static void main (String[] args) throws Exception
    {
        String fileName= "file.txt";
        // check if a port number is given as the first command line argument
        // if not argument is given, use port number 6789
        int myPort = 6789;
        if (args.length > 0)
        {
            try {
                myPort = Integer.parseInt(args[0]);
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                System.out.println("Need port number as argument");
                System.exit(-1);
            }
            catch (NumberFormatException e)
            {
                System.out.println("Please give port number as integer.");
                System.exit(-1);
            }
        }
        // set up connection socket
        try (ServerSocket listenSocket = new ServerSocket (myPort)){

            // listen (i.e. wait) for connection request
            System.out.println ("Web server waiting for request on port " + myPort);

            while (true) {
                // set up the read and write end of the communication socket
                try (Socket connectionSocket = listenSocket.accept();
                     //BufferedReader inFromClient = new BufferedReader (new InputStreamReader(connectionSocket.getInputStream()));
                     DataOutputStream outToClient = new DataOutputStream (connectionSocket.getOutputStream())
                ) {
                    // retrieve first line of request and set up for parsing
					/*		fileName = inFromClient.readLine();
							String message = "File richiesto " + fileName;
							outToClient.writeBytes(message);*/
                    // access the requested file
                    File file = new File(fileName);

                    // convert file to a byte array
                    int numOfBytes = (int) file.length();
                    try (FileInputStream inFile = new FileInputStream (fileName)){

                        byte[] fileInBytes = new byte[numOfBytes];
                        inFile.read(fileInBytes);
                        outToClient.write(fileInBytes, 0, numOfBytes);
                        outToClient.flush();
                    }
                    catch(FileNotFoundException e) {
                        String message1 = "File not Found";
                        outToClient.writeBytes(message1);
                    }
                    catch(IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}