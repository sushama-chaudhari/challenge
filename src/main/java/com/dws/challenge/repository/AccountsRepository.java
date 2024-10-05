package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;

import java.math.BigDecimal;

public interface AccountsRepository {

  void createAccount(Account account) throws DuplicateAccountIdException;

  Account getAccount(String accountId);

  void clearAccounts();

  public void deposit(String accountId, BigDecimal amount);

  public void withdraw(String accountId, BigDecimal amount);
}
