package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.FundsTransferRequest;
import com.dws.challenge.exception.FundsTransferValidationException;
import com.dws.challenge.exception.FundsTransferException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.notificationService = notificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public void transferFunds(FundsTransferRequest fundsTransferRequest) throws Exception {
    String fromAccountId = fundsTransferRequest.getFromAccountId();
    String toAccountId = fundsTransferRequest.getToAccountId();
    BigDecimal amount = fundsTransferRequest.getAmount();
    Account fromAccount = accountsRepository.getAccount(fromAccountId);
    Account toAccount = accountsRepository.getAccount(toAccountId);
    if(amount.compareTo(BigDecimal.ZERO) == -1)
      throw new FundsTransferValidationException("Amount to be transferred should be greater than 0");
    if(fromAccount == null)
      throw new FundsTransferValidationException("Could not find FromAccount for id " + fromAccountId);
    else if(fromAccount.getBalance().compareTo(amount) == -1)
      throw  new FundsTransferException("FromAccount balance is less than amount to be transferred. So transfer is not done.");
    if(toAccount == null)
      throw new FundsTransferValidationException("Could not find ToAccount for id " + toAccountId);
    accountsRepository.withdraw(fromAccountId, amount);
    accountsRepository.deposit(toAccountId, amount);
    notificationService.notifyAboutTransfer(fromAccount, "Transferred amount ".concat(amount.toString())
            .concat(" to account ").concat(toAccountId));
    notificationService.notifyAboutTransfer(toAccount, "Transferred amount ".concat(amount.toString())
            .concat(" from account ").concat(fromAccountId));
  }

}
