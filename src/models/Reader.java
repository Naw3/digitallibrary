package models;

public class Reader {
    private String subscriberNumber;
    private String firstName;
    private String lastName;
    private String email;
    private int maxLoanDays;

    public Reader() {
    }

    public Reader(String subscriberNumber, String firstName, String lastName, String email, int maxLoanDays) {
        this.subscriberNumber = subscriberNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.maxLoanDays = maxLoanDays;
    }

    public String getSubscriberNumber() {
        return subscriberNumber;
    }

    public void setSubscriberNumber(String subscriberNumber) {
        this.subscriberNumber = subscriberNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getMaxLoanDays() {
        return maxLoanDays;
    }

    public void setMaxLoanDays(int maxLoanDays) {
        this.maxLoanDays = maxLoanDays;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + subscriberNumber + ")";
    }
}
