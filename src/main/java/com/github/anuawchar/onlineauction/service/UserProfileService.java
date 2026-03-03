package com.github.anuawchar.onlineauction.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.github.anuawchar.onlineauction.entity.AuctionItem;
import com.github.anuawchar.onlineauction.entity.Bid;
import com.github.anuawchar.onlineauction.entity.User;
import com.github.anuawchar.onlineauction.repository.AuctionItemRepository;
import com.github.anuawchar.onlineauction.repository.BidRepository;
import com.github.anuawchar.onlineauction.service.UserService;

@Service
public class UserProfileService {

    private final UserService userService;
    private final AuctionItemRepository auctionItemRepository;
    private final BidRepository bidRepository;

    public UserProfileService(UserService userService, AuctionItemRepository auctionItemRepository, BidRepository bidRepository) {
        this.userService = userService;
        this.auctionItemRepository = auctionItemRepository;
        this.bidRepository = bidRepository;
    }

    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userService.findByUsername(authentication.getName());
    }

    public List<AuctionItem> getUserAuctions(User user) {
        return auctionItemRepository.findBySellerIdOrderByStartTimeDesc(user.getId());
    }

    public List<Bid> getUserBids(User user) {
        return bidRepository.findByBidderIdOrderByBidTimeDesc(user.getId());
    }

    public List<AuctionItem> getUserActiveAuctions(User user) {
        return auctionItemRepository.findBySellerIdAndEndTimeAfterOrderByStartTimeDesc(user.getId(), java.time.LocalDateTime.now());
    }
}
