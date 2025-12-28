import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// Tutor gestisce gli accessi (Lock) al laboratorio.
public class Tutor {
    /* le variabili Condidtion permettono di far bloccare o notificare threads
       che richiedono una lock: sono utili per gestire le priorità, potendo
       scelgliere quale thread notificare prima e chi lasciare in attesa */
    private Lab lab;
    private ReentrantLock labLock;
    private Condition profWaiting;
    private Condition[] tesistWaiting;
    private Condition studentWaiting;

    public Tutor(Lab lab){
        this.lab = lab;
        this.labLock = new ReentrantLock();
        this.profWaiting = labLock.newCondition();
        this.tesistWaiting = new Condition[lab.getN()];
        for(int i=0; i< lab.getN(); i++)
            this.tesistWaiting[i]= labLock.newCondition();
        this.studentWaiting = labLock.newCondition();
    }

    public void notifyGerarchy(){
        if(labLock.hasWaiters(profWaiting))
            profWaiting.signal();
        else{
            for(int i=0; i<lab.getN(); i++)
                tesistWaiting[i].signal();
            studentWaiting.signalAll();
        }
    }

    public void profRequestingLab(int id){
        labLock.lock();
        try{
            System.out.println("il prof "+id+" è in attesa di entrare nel laboratorio");
            while (!lab.isFree())
                profWaiting.await();
            lab.occupyAll();
            System.out.println("il prof "+id+" sta entrando nel laboratorio");
        } catch (InterruptedException e){
            e.printStackTrace();
        } finally{
            labLock.unlock();
        }
    }

    public void profLeavingLab(int id){
        labLock.lock();
        System.out.println("il prof "+id+" sta lasciando il laboratorio");
        lab.releaseAll();
        this.notifyGerarchy();
        labLock.unlock();
    }

    public void tesistRequestingPc(int mat, int pc){
        labLock.lock();
        try{
            System.out.println("il tesista "+mat+" è in attesa di usare il computer "+pc);
            while(labLock.hasWaiters(profWaiting) && !lab.isAvailable(pc))
                tesistWaiting[pc].await();
            lab.occupyComputer(pc);
            System.out.println("il tesista "+mat+" sta usando il computer "+pc);
        } catch (InterruptedException e){
            e.printStackTrace();
        } finally{
            labLock.unlock();
        }
    }

    public void tesistLeavingPc(int mat, int pc){
        labLock.lock();
        System.out.println("il tesista "+mat+" sta lasciando il computer "+pc);
        lab.releaseComputer(pc);
        this.notifyGerarchy();
        labLock.unlock();
    }

    public int studentRequestingPc(int mat){
        int pc = 0;
        labLock.lock();
        try{
            System.out.println("lo studente "+mat+" è in attesa per usare un computer");
            while(labLock.hasWaiters(profWaiting) && !lab.isFree())
                studentWaiting.await();
            pc = lab.getAvailableComputer();
            lab.occupyComputer(pc);
            System.out.println("lo studente "+mat+" sta usando il computer "+pc);
        } catch(InterruptedException e){
            e.printStackTrace();
        } finally {
            labLock.unlock();
            return pc;
        }
    }

    public void studentLeavingPc(int mat, int pc){
        labLock.lock();
        System.out.println("lo studente "+mat+" sta lasciando il computer "+pc);
        lab.releaseComputer(pc);
        this.notifyGerarchy();
        labLock.unlock();
    }
}
