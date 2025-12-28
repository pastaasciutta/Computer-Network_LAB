public class Professor extends Persona{

    public Professor(Tutor tutor, int mat, int k_bound) {
        super(tutor, mat, k_bound);
    }

    @Override
    void accessReq() {
        this.tutor.profRequestingLab(this.mat);
    }

    @Override
    void leaving() {
        this.tutor.profLeavingLab(this.mat);
    }
}
