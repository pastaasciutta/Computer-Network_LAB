import java.util.Random;
public abstract class Persona implements Runnable{
    protected Tutor tutor;
    protected final int mat;
    // num accessi che farà l'utente
    private final int k;
    protected Random random;

    public Persona(Tutor tutor, int mat, int k_bound){
        this.tutor= tutor;
        this.mat= mat;
        this.random= new Random();
        this.k= random.nextInt(k_bound);
    }

    @Override
    public void run() {
        try{
            for(int i=0; i<k; i++){
                accessReq();
                //tempo in cui persona è nel laboratorio
                Thread.sleep(random.nextInt(3000));
                leaving();
                Thread.sleep(random.nextInt(5000));
            }
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    abstract void accessReq();

    abstract void leaving();
}

