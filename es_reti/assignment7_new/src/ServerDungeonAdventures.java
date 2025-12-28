import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class ServerDungeonAdventures implements Runnable{

    private final static int N = 100;
    private final static String[] names = {"Peach", "Mario_Mario", "Pippo"};
    private String fighter;
    private Socket socket;
    private Random generator;
    private int x, y, z;

    public ServerDungeonAdventures(Socket socket){
        this.socket = socket;
        this.generator = new Random();
        this.fighter = names[ generator.nextInt(3) ];
    }

    @Override
    public void run() {
        System.out.println("\t\tconnected: " + socket);

        try ( Scanner in = new Scanner(socket.getInputStream());
              PrintWriter out = new PrintWriter(socket.getOutputStream(),true)){

            int[] flags = new int[2];
            boolean quitSession = false;
            //invia al client il nome del personaggio
            out.println(fighter);
            do {
                this.x = generator.nextInt(N) +1;
                this.y = generator.nextInt(N) +1;
                this.z = generator.nextInt(N) +1;

                flags[0] = 0;
                while (flags[0] != 1) { // quitFlag == false
                    //in out personaggio x vita y pozione e z vita mostro
                    out.println(x + "_" + y + "_" + z + "_" + flags[0]);
                    //in in comando
                    int command = in.nextInt();
                    flags = execCommand(command);
                }
                //in out personaggio x vita y pozione e z vita mostro
                out.println(x + "_" + y + "_" + z + "_" + flags[0]);

                out.println(flags[1]); //esito partita

                if (flags[1] == 1)
                    quitSession = in.nextInt() == 3 ? true : false; //3 client doesnt want to play again
                else
                    quitSession = true;
            } while (!quitSession);

            System.out.println("\t\tgame over " + socket);

            socket.close();
        } catch (Exception e){
            System.err.println("Error:" + socket);
        }
    }

    //returns {quitFlag, winFlag}
    public int[] execCommand (int num){
        switch (num) {
            case 1 -> {
                //fight
                x = x - (generator.nextInt(x) + 1);
                z = z - (generator.nextInt(z) + 1);
                if (x < 1)
                    return new int[]{1, 0};
                if (z < 1)
                    return new int[]{1, 1};
            }
            case 2 -> {
                //potion
                if( y > 0 ){
                    int drink;
                    if ((N - x) >= y)
                        drink = generator.nextInt(y) + 1;
                    else
                        drink = generator.nextInt(N - x) + 1;
                    y = y - drink;
                    x = x + drink;
                }
            }
            case 3 -> {
                //quit
                //hai perso
                return new int[]{1, 0};
            }
        }
        return new int[]{0, 0};
    }
}
