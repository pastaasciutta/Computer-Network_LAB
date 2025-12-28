import java.util.Scanner;

public class MainClass {
    public static void main(String[] args){
        /*if(args.length != 3) {
            System.err.println("Usage: MainLaboratorio numStudenti numTesisti numProfessori \n"
                    + "\tnumstudenti \t numero di studenti che accedono al laboratorio \n "
                    + "\tnumTesisti \t numero di tesisti che accedono al laboratorio\n"
                    + "\tnumProfessori \tnumero di professori che accedono al laboratorio\n"
                    + "\n\nExample: MainLaboratorio 10 5 2.");
            System.exit(1);
        }
        int ns = 0;  // numero di studenti
        int nt = 0;  // numero di tesisti
        int np = 0;  // numero di professori

        ns = Integer.parseInt(args[0]);
        nt = Integer.parseInt(args[1]);
        np = Integer.parseInt(args[2]);
        */

        int ns, nt, np;

        Scanner scanner = new Scanner(System.in);
        ns = scanner.nextInt();
        nt = scanner.nextInt();
        np = scanner.nextInt();

        //creo lab
        Lab lab = new Lab(20);
        //creo tutor
        Tutor tutor = new Tutor(lab);

        //avvio ns studenti
        for(int i=0; i<ns; i++){
            Student s = new Student(tutor, i, 2);
            Thread thread = new Thread(s);
            thread.start();
        }

        //avvio nt tesisti
        for(int i=0; i<nt; i++){
            Tesist t = new Tesist(tutor, i, 3);
            Thread thread = new Thread(t);
            thread.start();
        }

        //avvio np prof
        for(int i=0; i<np; i++){
            Professor p = new Professor(tutor, i, 3);
            Thread thread = new Thread(p);
            thread.start();
        }
    }
}
