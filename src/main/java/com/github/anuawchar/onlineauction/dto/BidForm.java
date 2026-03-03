package com.github.anuawchar.onlineauction.dto;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class BidForm {
    @Positive(message = "Bid amount must be positive")
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
