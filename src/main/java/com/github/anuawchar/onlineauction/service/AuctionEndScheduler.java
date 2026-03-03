package com.github.anuawchar.onlineauction.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.anuawchar.onlineauction.entity.AuctionItem;
import com.github.anuawchar.onlineauction.entity.Bid;
import com.github.anuawchar.onlineauction.repository.AuctionItemRepository;
import com.github.anuawchar.onlineauction.repository.BidRepository;

@Service
public class AuctionEndScheduler {

    private final AuctionItemRepository auctionItemRepository;
    private final BidRepository bidRepository;
    private final EmailService emailService;

    public AuctionEndScheduler(AuctionItemRepository auctionItemRepository, BidRepository bidRepository, EmailService emailService) {
        this.auctionItemRepository = auctionItemRepository;
        this.bidRepository = bidRepository;
        this.emailService = emailService;
    }

    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkEndedAuctions() {
        List<AuctionItem> endedAuctions = auctionItemRepository.findByEndTimeBeforeAndWinnerIsNull(LocalDateTime.now());
        for (AuctionItem item : endedAuctions) {
            List<Bid> bids = bidRepository.findByAuctionItemIdOrderByAmountDesc(item.getId());
            if (!bids.isEmpty()) {
                Bid winningBid = bids.get(0);
                item.setWinner(winningBid.getBidder());
                item.setPaid(false); // Payment not yet made
                auctionItemRepository.save(item);
                emailService.sendAuctionWinEmail(winningBid.getBidder(), item, winningBid.getAmount());
            }
        }
    }
}
