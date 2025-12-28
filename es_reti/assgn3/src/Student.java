public class Student extends Persona{

    private int pc;
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
