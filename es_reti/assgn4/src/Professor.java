public class Professor extends Persona{

    /** @param tutor gestisce gli accessi al laboratorio
      * @param mat id del professore
      * @param k_bound numero richieste massime di accesso al lab per ogni istanza professore
      */
    public Professor(Tutor tutor, int mat, int k_bound) { super(tutor, mat, k_bound); }

    @Override
    void accessReq() {
        this.tutor.profRequestingLab(this.mat);
    }

    @Override
    void leaving() {
        this.tutor.profLeavingLab(this.mat);
    }
}