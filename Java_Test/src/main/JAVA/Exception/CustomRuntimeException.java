package Exception;
/*
* You are building the backend logic for a Digital Wallet app (like Apple Wallet or Google Wallet). Users can load money into their wallet and transfer money to merchants.Your job is to write a Wallet class that handles these transactions safely. If a transaction fails due to business rules, you must throw a custom unchecked exception.
* */
public class CustomRuntimeException {
    public static void main(String[] args) {
        Wallet w = new Wallet(50);

        try {
            w.transfer(-10);
        } catch(WalletTransactionException e) {
            System.out.println("Transaction Failed: " + e.getMessage());
        }

        try {
            w.transfer(10);
        } catch(WalletTransactionException e) {
            System.out.println("Transaction Failed: " + e.getMessage());
        }

        try {
            w.transfer(100);
        } catch(WalletTransactionException e) {
            System.out.println("Transaction Failed: " + e.getMessage());
            System.out.println("Your wallet only has: $" + e.getCurrentBalance());
        }
    }
}

class WalletTransactionException extends RuntimeException {

    double currentBalance;
    public double getCurrentBalance() {
        return currentBalance;
    }
    public WalletTransactionException() {
        super();
    }
    public WalletTransactionException(String msg) {
        super(msg);
    }
    public WalletTransactionException(String msg, Throwable t) {
        super(msg, t);
    }
    public WalletTransactionException(String msg, double balance) {
        super(msg);
        this.currentBalance = balance;
    }
}

class Wallet {
    private double balance;

    public Wallet(double balance) {
        this.balance = balance;
    }

    public void transfer(double amount) {
        if (amount <= 0) {
            //  COMPILES PERFECTLY. No try-catch or throws needed. Because, it is RuntimeException.
            throw new WalletTransactionException("Invalid transfer amount. Entered amount is $"+amount);
        }
        if (amount > balance) {
            //  COMPILES PERFECTLY. No try-catch or throws needed. Because, it is RuntimeException.
            throw new WalletTransactionException("Insufficient funds for transfer.", balance);
        }
        balance = balance - amount;
        System.out.println("Balance deducted. Current balance is $"+ balance);
    }
}

