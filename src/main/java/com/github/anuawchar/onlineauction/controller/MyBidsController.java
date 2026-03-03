package com.github.anuawchar.onlineauction.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.github.anuawchar.onlineauction.entity.AuctionItem;
import com.github.anuawchar.onlineauction.entity.Bid;
import com.github.anuawchar.onlineauction.service.UserProfileService;

@Controller
public class MyBidsController {

    private final UserProfileService userProfileService;

    public MyBidsController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/my-bids")
    public String viewMyBids(Authentication authentication, Model model) {
        var user = userProfileService.getCurrentUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        List<Bid> bids = userProfileService.getUserBids(user);
        List<AuctionItem> userAuctions = userProfileService.getUserAuctions(user);
        List<AuctionItem> activeAuctions = userProfileService.getUserActiveAuctions(user);
        model.addAttribute("user", user);
        model.addAttribute("bids", bids);
        model.addAttribute("userAuctions", userAuctions);
        model.addAttribute("activeAuctions", activeAuctions);
        return "my-bids";
    }
}
