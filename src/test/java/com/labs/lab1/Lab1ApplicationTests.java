package com.labs.lab1;
import com.labs.lab1.entities.account.Account;
import com.labs.lab1.entities.bank.Bank;
import com.labs.lab1.entities.bank.CentralBank;
import com.labs.lab1.entities.customer.Customer;
import com.labs.lab1.entities.transaction.ReplenishTransaction;
import com.labs.lab1.models.RangeConditionsInfo;
import com.labs.lab1.entities.transaction.WithdrawTransaction;
import com.labs.lab1.services.implementations.AccountsManager;
import com.labs.lab1.services.implementations.CustomerCreator;
import com.labs.lab1.services.implementations.TimeMachineService;
import com.labs.lab1.valueObjects.TransactionState;
import exceptions.IncorrectArgumentsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class Lab1ApplicationTests {
    CentralBank centralBank;
    Bank testBank;
    Customer testCustomer;
    @BeforeEach
    void setUp() {
        centralBank = CentralBank.getInstance();
        var savingListConditions = new ArrayList<RangeConditionsInfo>();
        savingListConditions.add(new RangeConditionsInfo(0, 50000, 6));
        savingListConditions.add(new RangeConditionsInfo(50000, 100000000, 11));
        try {
            testBank = centralBank.createBank("ITMO bank", savingListConditions, 10,
                    110, 7, 50000);
        } catch (IncorrectArgumentsException e) {
            throw new RuntimeException(e);
        }
        try {
            testCustomer =  new CustomerCreator(testBank).createCustomer(null, null, "Maria", "Chistyakova");
        } catch (IncorrectArgumentsException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void withdrawIncorrectAmount() {
        Account account = null;
        try {
            account = new AccountsManager(testBank).createSavingsAccount(testCustomer, 5000, 6);
        } catch (IncorrectArgumentsException e) {
            throw new RuntimeException(e);
        }
        var secondTransaction = new WithdrawTransaction(account, 7000);
        testBank.makeTransaction(new WithdrawTransaction(account, 50));
        assert(account.getBalance() == 4950);
        testBank.makeTransaction(secondTransaction);
        assert(account.getBalance() == 4950);
        assert(secondTransaction.getState() == TransactionState.Rollback);
    }

    @Test
    void withdrawFromCreditAccount() {
        Account account = null;
        try {
            account = new AccountsManager(testBank).createCreditAccount(testCustomer, 50000); //commandInvoker.Consume(new CreateCreditCommand(testCustommer, 50000);
        } catch (IncorrectArgumentsException e) {
            throw new RuntimeException(e);
        }
        testBank.makeTransaction(new WithdrawTransaction(account, 50));
        testBank.makeTransaction(new WithdrawTransaction(account, 500));
        assert(account.getBalance() == (0 - 50 - 500 - testBank.getBaseCreditCommission()));
    }

    @Test
    void undoReplenish() {
        Account account = null;
        try {
            account = new AccountsManager(testBank).createCreditAccount(testCustomer, 50000); //commandInvoker.Consume(new CreateCreditCommand(testCustommer, 50000);
        } catch (IncorrectArgumentsException e) {
            throw new RuntimeException(e);
        }
        var replenish = new ReplenishTransaction(account, 50);
        testBank.makeTransaction(replenish);
        testBank.makeTransaction(new WithdrawTransaction(account, 500));
        testBank.rollbackTransaction(replenish);
        assert(account.getBalance() == (0 - 500 - testBank.getBaseCreditCommission()));
    }

    @Test
    void timeServiceTest() {
        Account account = null;
        try {
            account = new AccountsManager(testBank).createSavingsAccount(testCustomer, 5000, 2);
        } catch (IncorrectArgumentsException e) {
            throw new RuntimeException(e);
        }
        var timeService = new TimeMachineService();
        timeService.speedUpTime(account, 2);
        assert(account.getBalance() == 5618);
    }

    @Test
    void creditLimitTest() {
        Account account = null;
        try {
            account = new AccountsManager(testBank).createCreditAccount(testCustomer, 50000);
        } catch (IncorrectArgumentsException e) {
            throw new RuntimeException(e);
        }
        var transaction = new WithdrawTransaction(account, 1000000);
        testBank.makeTransaction(transaction);
        //assertThrows(NotVerifiedException.class, () -> testBank.makeTransaction(transaction));
        assert(transaction.getState() == TransactionState.Rollback);
    }

    @Test
    void notVerifiedExceptionTesting() {
        Account account = null;
        try {
            account = new AccountsManager(testBank).createCreditAccount(testCustomer, 500000);
        } catch (IncorrectArgumentsException e) {
            throw new RuntimeException(e);
        }
        var transaction = new WithdrawTransaction(account, 60000);
        testBank.makeTransaction(transaction);
        assertEquals(TransactionState.Rollback, transaction.getState());
        //assertThrows(NotVerifiedException.class, () -> testBank.makeTransaction(new WithdrawTransaction(finalAccount, 60000)));
    }

    @Test
    void notificationsTest() {
        // creating customer mock
        Customer customer = mock(Customer.class);
        testBank.getCustomers().add(customer);
        Mockito.when(customer.getId()).thenReturn(UUID.randomUUID());
        // creating account
        try {
            new AccountsManager(testBank).createCreditAccount(customer, 50000);
        } catch (IncorrectArgumentsException e) {
            throw new RuntimeException(e);
        }
        // subscribe mock to notifications
        testBank.RegisterObserver(customer);
        // change commission => notify subscribers
        testBank.changeBaseCommission(200);
        // verify
        verify(customer, times(1)).getNotification("New commission set by bank");
    }

}
