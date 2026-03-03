
package com.github.anuawchar.onlineauction.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.github.anuawchar.onlineauction.entity.AuctionItem;

public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {
    List<AuctionItem> findBySellerId(Long sellerId);
    List<AuctionItem> findByEndTimeAfter(java.time.LocalDateTime currentTime);
    Page<AuctionItem> findByEndTimeAfter(java.time.LocalDateTime currentTime, Pageable pageable);
    List<AuctionItem> findTop5ByEndTimeAfterOrderByStartingPriceDesc(java.time.LocalDateTime currentTime);

    List<AuctionItem> findByEndTimeBetweenOrderByEndTimeAsc(java.time.LocalDateTime start, java.time.LocalDateTime end);

    List<AuctionItem> findTop5ByFeaturedTrueAndEndTimeAfterOrderByStartingPriceDesc(java.time.LocalDateTime now);

    @Query(value = "SELECT * FROM auction_items a WHERE a.end_time > :now ORDER BY (SELECT COUNT(*) FROM bids b WHERE b.auction_item_id = a.id) DESC, a.starting_price DESC LIMIT 5", nativeQuery = true)
    List<AuctionItem> findTop5ByEndTimeAfterOrderByBidCountDescStartingPriceDesc(java.time.LocalDateTime now);

    List<AuctionItem> findBySellerIdOrderByStartTimeDesc(Long sellerId);

    List<AuctionItem> findBySellerIdAndEndTimeAfterOrderByStartTimeDesc(Long sellerId, java.time.LocalDateTime now);

    List<AuctionItem> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStartingPriceBetweenAndEndTimeAfter(
        String title, String description, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, java.time.LocalDateTime now);

    List<AuctionItem> findByEndTimeBeforeAndWinnerIsNull(java.time.LocalDateTime now);
}
