import java.util.List;

public class Account {
    private String name;
    private List<Transazioni> payments;

    public Account(final String name, final List<Transazioni> payments){
        this.name = name;
        this.payments = payments;
    }

    public void setName(final String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public List<Transazioni> getPayments(){
        return payments;
    }

    public void setPayments(final List<Transazioni> payments) {
        this.payments = payments;
    }

    public void addPayment(Transazioni e){
        this.payments.add(e);
    }

}
