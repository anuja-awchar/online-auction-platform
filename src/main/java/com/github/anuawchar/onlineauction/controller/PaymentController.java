package com.github.anuawchar.onlineauction.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.anuawchar.onlineauction.entity.AuctionItem;
import com.github.anuawchar.onlineauction.entity.User;
import com.github.anuawchar.onlineauction.repository.AuctionItemRepository;
import com.github.anuawchar.onlineauction.service.PaymentService;
import com.stripe.exception.StripeException;

@Controller
public class PaymentController {

    private final PaymentService paymentService;
    private final AuctionItemRepository auctionItemRepository;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    public PaymentController(PaymentService paymentService, AuctionItemRepository auctionItemRepository) {
        this.paymentService = paymentService;
        this.auctionItemRepository = auctionItemRepository;
    }

    @GetMapping("/payment/{itemId}")
    public String showPaymentPage(@PathVariable Long itemId, @AuthenticationPrincipal User user, Model model) {
        AuctionItem item = auctionItemRepository.findById(itemId).orElse(null);
        if (item == null || !item.getWinner().equals(user) || item.isPaid()) {
            return "redirect:/";
        }

        model.addAttribute("item", item);
        model.addAttribute("stripePublishableKey", stripePublishableKey);
        return "payment";
    }

    @PostMapping("/create-payment-intent")
    public String createPaymentIntent(@RequestParam Long itemId, @AuthenticationPrincipal User user, Model model) {
        AuctionItem item = auctionItemRepository.findById(itemId).orElse(null);
        if (item == null || !item.getWinner().equals(user) || item.isPaid()) {
            return "redirect:/";
        }

        try {
            // Get the winning bid amount
            Long amountInCents = item.getBids().stream()
                    .max((b1, b2) -> b1.getAmount().compareTo(b2.getAmount()))
                    .get()
                    .getAmount()
                    .multiply(new java.math.BigDecimal(100))
                    .longValue();

            String clientSecret = paymentService.createPaymentIntent(amountInCents, "usd");
            model.addAttribute("clientSecret", clientSecret);
            model.addAttribute("item", item);
            model.addAttribute("stripePublishableKey", stripePublishableKey);
            return "payment";
        } catch (StripeException e) {
            model.addAttribute("error", "Payment setup failed: " + e.getMessage());
            return "payment";
        }
    }

    @PostMapping("/confirm-payment")
    public String confirmPayment(@RequestParam String paymentIntentId, @RequestParam Long itemId, @AuthenticationPrincipal User user) {
        AuctionItem item = auctionItemRepository.findById(itemId).orElse(null);
        if (item == null || !item.getWinner().equals(user)) {
            return "redirect:/";
        }

        try {
            com.stripe.model.PaymentIntent paymentIntent = paymentService.confirmPayment(paymentIntentId);
            if ("succeeded".equals(paymentIntent.getStatus())) {
                item.setPaid(true);
                auctionItemRepository.save(item);
                return "redirect:/payment/success";
            } else {
                return "redirect:/payment/failure";
            }
        } catch (StripeException e) {
            return "redirect:/payment/failure";
        }
    }

    @GetMapping("/payment/success")
    public String paymentSuccess() {
        return "payment-success";
    }

    @GetMapping("/payment/failure")
    public String paymentFailure() {
        return "payment-failure";
    }
}
