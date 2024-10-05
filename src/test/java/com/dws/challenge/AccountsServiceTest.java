package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;;

import java.math.BigDecimal;
import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.FundsTransferRequest;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.FundsTransferException;
import com.dws.challenge.exception.FundsTransferValidationException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Mock
  private NotificationService notificationService;

  @BeforeEach
  void setup() {
    accountsService.getAccountsRepository().clearAccounts();
    Account account1 = new Account("A100", new BigDecimal("500.90"));
    this.accountsService.createAccount(account1);
    Account account2 = new Account("A101", new BigDecimal("600.60"));
    this.accountsService.createAccount(account2);
  }

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }


  @Test
  void fundsTransferTest_NegativeAmount() throws Exception {
    FundsTransferRequest request = new FundsTransferRequest("A100", "A101", new BigDecimal(-100));
    try {
      this.accountsService.transferFunds(request);
    } catch (FundsTransferValidationException exception) {
      assertThat(exception.getMessage()).isEqualTo("Amount to be transferred should be greater than 0");
    }
  }

  @Test
  void fundsTransferTest_Success() throws Exception {
    FundsTransferRequest request = new FundsTransferRequest("A100", "A101", new BigDecimal(100));
    this.accountsService.transferFunds(request);
    Account account = this.accountsService.getAccount("A101");
    assertThat(account.getBalance()).isEqualTo(new BigDecimal(700.60).setScale(2, BigDecimal.ROUND_FLOOR));
    //Mockito.doNothing().when(notificationService).notifyAboutTransfer(Mockito.any(), Mockito.anyString());
    //Mockito.verify(notificationService, Mockito.times(1)).notifyAboutTransfer(Mockito.any(), Mockito.anyString());
  }

  @Test
  void fundsTransferTest_InvalidFromAccount() throws Exception {
    FundsTransferRequest request = new FundsTransferRequest("A104", "A101", new BigDecimal(100));
    try {
      this.accountsService.transferFunds(request);
    } catch (FundsTransferValidationException exception) {
      assertThat(exception.getMessage()).isEqualTo("Could not find FromAccount for id A104");
    }
  }

  @Test
  void fundsTransferTest_InvalidToAccount() throws Exception {
    FundsTransferRequest request = new FundsTransferRequest("A100", "A104", new BigDecimal(100));
    try {
      this.accountsService.transferFunds(request);
    } catch (FundsTransferValidationException exception) {
      assertThat(exception.getMessage()).isEqualTo("Could not find ToAccount for id A104");
    }
  }

  @Test
  void fundsTransferTest_InsufficientFunds() throws Exception {
    FundsTransferRequest request = new FundsTransferRequest("A100", "A104", new BigDecimal(800));
    try {
      this.accountsService.transferFunds(request);
    } catch (FundsTransferException exception) {
      assertThat(exception.getMessage()).isEqualTo("FromAccount balance is less than amount to be transferred. So transfer is not done.");
    }
  }

}
