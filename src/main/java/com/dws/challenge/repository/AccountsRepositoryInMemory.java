package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        Lock readLock = rwlock.readLock();
        readLock.lock();
        try {
            return accounts.get(accountId);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

    @Override
    public void deposit(String accountId, BigDecimal amount) {
        Lock writeLock = rwlock.writeLock();
        writeLock.lock();
        try {
            Account account = accounts.get(accountId);
            BigDecimal newBalance = account.getBalance().add(amount);
            account.setBalance(newBalance);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void withdraw(String accountId, BigDecimal amount) {
        Lock writeLock = rwlock.writeLock();
        writeLock.lock();
        try {
            Account account = accounts.get(accountId);
            BigDecimal newBalance = account.getBalance().subtract(amount);
            account.setBalance(newBalance);
        } finally {
            writeLock.unlock();
        }
    }

}
