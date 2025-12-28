public class Tesist extends Persona{

    private final int pc;
    /** @param tutor gestisce gli accessi al laboratorio
      * @param mat matricola
      * @param k_bound numero richieste massime di accesso al lab per ogni istanza tesista
      */
    public Tesist(Tutor tutor, int mat, int k_bound){
        super(tutor, mat, k_bound);
        this.pc = super.random.nextInt(20)-1;
    }

    @Override
    void accessReq() {
        tutor.tesistRequestingPc(mat, pc);
    }

    @Override
    void leaving() {
        tutor.tesistLeavingPc(mat, pc);
    }
}