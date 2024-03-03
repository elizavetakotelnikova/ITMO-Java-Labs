package com.labs.lab1.entities.account;

import com.labs.lab1.services.Updatable;
import com.labs.lab1.valueObjects.AccountState;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class SavingsAccount extends Account implements Updatable {
    /**
     *
     * @param userId - user id
     * @param bankId - bank id
     * @param balance - current account balance
     * @param notVerifiedLimit - withdraw/replenish limit for not verified accounts
     * @param state - account state (verified/not verified)
     * @param endDate - date of account closing
     * @param percentage - percentage
     */
    public SavingsAccount(UUID userId, UUID bankId, double balance, double notVerifiedLimit, AccountState state,
                          LocalDate endDate, double percentage) {
        super(userId, bankId, balance, notVerifiedLimit, state);
        this.endDate = endDate;
        this.percentage = percentage;
    }
    public SavingsAccount(UUID id, UUID userId, UUID bankId, double balance, double notVerifiedLimit, AccountState state,
                          LocalDate endDate, double percentage) {
        super(userId, bankId, balance, notVerifiedLimit, state);
        this.id = id;
        this.endDate = endDate;
        this.percentage = percentage;
    }
    private LocalDate endDate;
    private double percentage;

    /**
     * monthly update => new sum is added based on a percentage
     */
    @Override
    public void makeRegularUpdate() {
        if (endDate.isAfter(LocalDate.now())) balance += balance * (percentage / 100);
    }
}
