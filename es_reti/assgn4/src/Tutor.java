public class Tutor {

    final private Lab lab;

    private int profWaiting;
    private int[] tesistWaiting;

    public Tutor(Lab lab){
        this.lab = lab;
        this.profWaiting = 0;
        this.tesistWaiting = new int[lab.getN()];
        for(int i=0; i<lab.getN(); i++)
            this.tesistWaiting[i] = 0;
    }

    /** @param id professor id */
    public synchronized void profRequestingLab(int id){
        try {
            // incrementa il numero di professori in attesa
            this.profWaiting++;
            while (!lab.isFree()) {
                System.out.println("il prof "+id+" è in attesa di entrare nel laboratorio");
                wait();
            }
            // decrementa il numero di professori in attesa
            this.profWaiting--;
            lab.occupyAll();
            System.out.println("il prof "+id+" sta entrando nel laboratorio");
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** @param id professor id */
    public synchronized void profLeavingLab(int id){
        // rilascia tutti i computer del laboratorio
        lab.releaseAll();
        System.out.println("il prof "+id+" sta lasciando il laboratorio");
        // risveglia tutti gli utenti in attesa
        notifyAll();
    }

    /** il tesista effettua una richiesta di accesso al computer id_pc (del lab)
      * @param t tesista
      * @param pc id del pc a cui il tesista vuole lavorare
      */
    public synchronized void tesistRequestingPc(int t, int pc){
        try {
            this.tesistWaiting[pc]++;
            while (this.profWaiting > 0 || !lab.isAvailable(pc)) {
                System.out.printf("Tesista %d: in attesa del computer %d\n", t, pc);
                wait();
            }
            this.tesistWaiting[pc]--;
            lab.occupyComputer(pc);
            System.out.println("il tesista "+t+" sta usando il computer "+pc);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** avvisa il tutore che il tesista t ha terminato il lavoro al computer pc
      * @param t tesista
      * @param pc id del pc rilasciato dal tesista
      */
    public synchronized void tesistLeavingPc(int t, int pc){
        // rilascia il computer id_pc del laboratorio
        lab.releaseComputer(pc);
        System.out.println("il tesista "+t+" sta lasciando il computer "+pc);
        // risveglia tutti gli utenti in attesa
        notifyAll();
    }

    /** effettua una richiesta di accesso al primo computer disponibile da parte dello studente s
      * @param mat studente
      * @return id del pc acquisito dallo studente s
      */
    public synchronized int studentRequestingPc(int mat){
        int pc = -1;
        try {
            while (this.profWaiting > 0 || lab.getAvailableComputer() == -1) {
                System.out.printf("Studente %d: in attesa di un computer libero\n", mat);
                wait();
            }
            pc = lab.getAvailableComputer();
            lab.occupyComputer(pc);
            System.out.println("lo studente "+mat+" sta usando il computer "+pc);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return pc;
    }

    /** avvisa il tutore che lo studente s ha terminato il lavoro al computer pc
      * @param s studente
      * @param pc id del pc rilasciato dallo studente
      */
    public synchronized void studentLeavingPc(int mat, int pc){
        // rilascia il computer id_pc del laboratorio
        lab.releaseComputer(pc);
        System.out.println("lo studente "+mat+" sta lasciando il computer "+pc);
        // risveglia tutti gli utenti in attesa
        notifyAll();
    }
}
