package com.labs.lab1.entities.account;

import com.labs.lab1.entities.bank.CentralBank;
import com.labs.lab1.entities.transaction.Transaction;
import com.labs.lab1.services.interfaces.Replenishable;
import com.labs.lab1.services.interfaces.Updatable;
import com.labs.lab1.services.interfaces.Withdrowable;
import com.labs.lab1.valueObjects.AccountState;
import exceptions.NotEnoughMoneyException;
import exceptions.NotVerifiedException;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
public abstract class Account implements Replenishable, Withdrowable, Updatable {
    protected UUID id;
    protected UUID userId;
    protected UUID bankId;
    protected double balance;
    protected AccountState state;
    protected List<Transaction> transactionsHistory = new ArrayList<>();
    public Account(UUID userId, UUID bankId, double balance, AccountState state) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.bankId = bankId;
        this.balance = balance;
        this.state = state;
    }

    @Override
    public void replenish(double amount) {
        balance += amount;
    }

    @Override
    public void withdraw(double amount) throws NotEnoughMoneyException, NotVerifiedException {
        //if (state == AccountState.NotVerified && amount > notVerifiedLimit) throw new NotVerifiedException("Transaction cannot be done, account not verified");
        if (balance < amount) throw new NotEnoughMoneyException("Transaction cannot be done");
        balance -= amount;
    }
    public void checkAccountState() {
       var bank = CentralBank.getInstance().getBanks().stream().filter(x -> x.getId() == bankId).findAny().get();
       var user = bank.getCustomers().stream().filter(x -> x.getId() == userId).findAny().get();
       if (user.checkVerification()) state = AccountState.Verified;
    }
}
