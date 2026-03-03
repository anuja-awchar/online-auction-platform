package com.github.anuawchar.onlineauction.validator;

import com.github.anuawchar.onlineauction.entity.AuctionItem;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EndTimeAfterStartValidator implements ConstraintValidator<ValidEndTimeAfterStart, AuctionItem> {

    @Override
    public boolean isValid(AuctionItem auctionItem, ConstraintValidatorContext context) {
        if (auctionItem.getStartTime() == null || auctionItem.getEndTime() == null) {
            return true; // Let @NotNull handle null checks
        }
        return auctionItem.getEndTime().isAfter(auctionItem.getStartTime());
    }
}
