package com.dws.challenge.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class FundsTransferRequest {

    @NotBlank(message = "From Account Id can not be null or blank")
    private String fromAccountId;

    @NotBlank(message = "From Account Id can not be null or blank")
    private String toAccountId;

    @NotNull(message = "Amount to be transferred can not be null")
    private BigDecimal amount;
}
