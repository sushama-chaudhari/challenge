package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.FundsTransferRequest;
import com.dws.challenge.exception.FundsTransferValidationException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

  @PostMapping(value = "/funds-transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> transferFunds(@Valid @RequestBody FundsTransferRequest fundsTransferRequest) {
    log.info("Request received to transfer funds {}", fundsTransferRequest);
    try {
      accountsService.transferFunds(fundsTransferRequest);
    } catch (FundsTransferValidationException exception){
      log.error("Funds Transfer is failed :: Exception-{} : Request-{}", exception.getMessage(), fundsTransferRequest);
      return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("Funds Transfer is failed :: Exception-{} : Request-{}", e.getMessage(), fundsTransferRequest);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>("Funds Transfer is successful", HttpStatus.OK);
  }



}
