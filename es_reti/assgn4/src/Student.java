public class Student extends Persona{

    private int pc;
    /** @param tutor gestisce gli accessi al laboratorio
      * @param mat matricola
      * @param k_bound numero richieste massime di accesso al lab per ogni istanza studente
      */
    public Student(Tutor tutor, int mat, int k_bound){
        super(tutor, mat, k_bound);
    }

    @Override
    void accessReq() {
        this.pc= tutor.studentRequestingPc(mat);
    }

    @Override
    void leaving() {
        tutor.studentLeavingPc(mat, pc);
    }
}