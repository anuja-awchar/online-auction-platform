package com.github.anuawchar.onlineauction.service;

import com.github.anuawchar.onlineauction.entity.AuctionItem;
import com.github.anuawchar.onlineauction.entity.Bid;
import com.github.anuawchar.onlineauction.entity.User;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendBidUpdateEmail(User seller, AuctionItem item, Bid newBid) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(seller.getEmail());
        message.setFrom(fromEmail);
        message.setSubject("New Bid on Your Auction: " + item.getTitle());
        message.setText("Hello " + seller.getUsername() + ",\n\n" +
                "A new bid of $" + newBid.getAmount() + " has been placed on your auction item '" + item.getTitle() + "' by " + newBid.getBidder().getUsername() + ".\n\n" +
                "View your auction: http://localhost:8081/item/" + item.getId() + "\n\n" +
                "Best regards,\nOnline Auction Platform");
        mailSender.send(message);
    }

    public void sendAuctionWinEmail(User winner, AuctionItem item, BigDecimal winningAmount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(winner.getEmail());
        message.setFrom(fromEmail);
        message.setSubject("Congratulations! You Won the Auction: " + item.getTitle());
        message.setText("Hello " + winner.getUsername() + ",\n\n" +
                "Congratulations! You have won the auction for '" + item.getTitle() + "' with your bid of $" + winningAmount + ".\n\n" +
                "Proceed to payment: http://localhost:8081/payment/" + item.getId() + "\n\n" +
                "Best regards,\nOnline Auction Platform");
        mailSender.send(message);
    }
}
