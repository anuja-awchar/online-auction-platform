package com.github.anuawchar.onlineauction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.anuawchar.onlineauction.entity.Bid;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByAuctionItemIdOrderByAmountDesc(Long auctionItemId);
    List<Bid> findByBidderId(Long bidderId);
    List<Bid> findByBidderIdOrderByBidTimeDesc(Long bidderId);
}
