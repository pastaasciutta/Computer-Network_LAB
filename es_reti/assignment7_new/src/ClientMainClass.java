import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.io.PrintWriter;

public class ClientMainClass {

    static final int port = 8888;

    public static void main(String[] args) throws IOException {

        System.out.println("\t\tUsage: 1 -> fight," +
                           "\n\t\t\t2 -> drink potion," +
                           "\n\t\t\t3 -> quit");

        //instauro connessione
        try (Socket socket = new Socket(InetAddress.getLocalHost(), port);
             //socket inputStream
             Scanner in = new Scanner(socket.getInputStream());
             //socket outputStream))
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // input from command line (terminal)
                Scanner scanner = new Scanner(System.in);
                String name = in.nextLine();
                //stampa nome del personggio
                System.out.println("\tWelcome to Dungeon Adventures, you're: " + name);

                boolean playAgain;
                do {
                    boolean quit = false;
                    int commandline = 0;
                    while (!quit) {
                        //reading from socket inputStream
                        String[] xyzq = in.nextLine().split("_");

                        System.out.println("Life: " + xyzq[0] +
                                "\nPotion: " + xyzq[1] +
                                "\nMonster's Life: " + xyzq[2]);

                        if (!xyzq[3].equals("1")){
                            System.out.println("\tselect command: 1, 2, 3?");
                            // reading from command line (terminal)
                            commandline = scanner.nextInt();
                            out.println(commandline);
                        }

                        if ( commandline == 3 || xyzq[3].equals("1"))
                            quit = true;
                    }

                    int endgame = in.nextInt();

                    if (endgame == 1) {
                        System.out.println("\tYOU WON " + "\n" + name + " wanna play again? y(1) or n(3)");
                        commandline = scanner.nextInt();
                        out.println(commandline);
                        playAgain = (commandline == 1);
                    } else {
                        System.out.println("\tGAME OVER " + name);
                        playAgain = false;
                    }
                } while (playAgain);
                scanner.close();
        }
    }
}
