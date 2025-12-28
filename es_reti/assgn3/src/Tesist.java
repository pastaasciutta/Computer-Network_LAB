public class Tesist extends Persona{

    private int pc;
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
