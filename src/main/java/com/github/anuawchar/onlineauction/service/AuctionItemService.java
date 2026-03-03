package com.github.anuawchar.onlineauction.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.anuawchar.onlineauction.entity.AuctionItem;
import com.github.anuawchar.onlineauction.entity.Bid;
import com.github.anuawchar.onlineauction.entity.User;
import com.github.anuawchar.onlineauction.repository.AuctionItemRepository;
import com.github.anuawchar.onlineauction.repository.BidRepository;

@Service
public class AuctionItemService {

    private final AuctionItemRepository auctionItemRepository;
    private final BidRepository bidRepository;
    private final EmailService emailService;
    private final FileUploadService fileUploadService;

    public AuctionItemService(AuctionItemRepository auctionItemRepository, BidRepository bidRepository, EmailService emailService, FileUploadService fileUploadService) {
        this.auctionItemRepository = auctionItemRepository;
        this.bidRepository = bidRepository;
        this.emailService = emailService;
        this.fileUploadService = fileUploadService;
    }

    public AuctionItem createAuctionItem(String title, String description, String category, BigDecimal startingPrice,
                                         LocalDateTime startTime, LocalDateTime endTime, User seller) {
        AuctionItem item = new AuctionItem(title, description, startingPrice, startTime, endTime, seller);
        item.setCategory(category);
        item.setImageFilename("placeholder.png"); // Default image
        return auctionItemRepository.save(item);
    }

    public AuctionItem createAuctionItem(String title, String description, String category, String imageFilename,
                                         BigDecimal startingPrice, LocalDateTime startTime, LocalDateTime endTime, User seller) {
        AuctionItem item = new AuctionItem(title, description, startingPrice, startTime, endTime, seller);
        item.setCategory(category);
        item.setImageFilename(imageFilename != null && !imageFilename.isEmpty() ? imageFilename : "placeholder.png");
        return auctionItemRepository.save(item);
    }

    public AuctionItem getAuctionItemById(Long id) {
        return auctionItemRepository.findById(id).orElse(null);
    }

    public List<AuctionItem> getActiveAuctions() {
        return auctionItemRepository.findByEndTimeAfter(LocalDateTime.now());
    }

    public Page<AuctionItem> getActiveAuctions(Pageable pageable) {
        return auctionItemRepository.findByEndTimeAfter(LocalDateTime.now(), pageable);
    }

    public List<AuctionItem> getFeaturedAuctions() {
        // Return manually featured items first, if any
        List<AuctionItem> featuredItems = auctionItemRepository.findTop5ByFeaturedTrueAndEndTimeAfterOrderByStartingPriceDesc(LocalDateTime.now());
        if (featuredItems.isEmpty()) {
            // Fallback to top 5 by bid count if no manual featured items
            return auctionItemRepository.findTop5ByEndTimeAfterOrderByBidCountDescStartingPriceDesc(LocalDateTime.now());
        }
        return featuredItems;
    }

    public List<AuctionItem> getEndingSoonAuctions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24h = now.plusHours(24);
        return auctionItemRepository.findByEndTimeBetweenOrderByEndTimeAsc(now, next24h);
    }

    public Bid placeBid(AuctionItem item, User bidder, BigDecimal amount) {
        if (bidder.getId().equals(item.getSeller().getId())) {
            throw new RuntimeException("Sellers cannot bid on their own items");
        }
        if (amount.compareTo(item.getStartingPrice()) < 0) {
            throw new RuntimeException("Bid amount must be at least the starting price");
        }
        List<Bid> existingBids = bidRepository.findByAuctionItemIdOrderByAmountDesc(item.getId());
        if (!existingBids.isEmpty() && amount.compareTo(existingBids.get(0).getAmount()) <= 0) {
            throw new RuntimeException("Bid amount must be higher than the current highest bid");
        }
        Bid bid = new Bid(amount, LocalDateTime.now(), bidder, item);
        Bid savedBid = bidRepository.save(bid);
        // Send email notification to seller
        emailService.sendBidUpdateEmail(item.getSeller(), item, savedBid);
        return savedBid;
    }

    public List<Bid> getBidsForItem(Long itemId) {
        return bidRepository.findByAuctionItemIdOrderByAmountDesc(itemId);
    }

    public BigDecimal getCurrentHighestBid(Long itemId) {
        List<Bid> bids = getBidsForItem(itemId);
        return bids.isEmpty() ? null : bids.get(0).getAmount();
    }

    public void setFeatured(Long itemId, boolean featured) {
        AuctionItem item = getAuctionItemById(itemId);
        if (item != null) {
            item.setFeatured(featured);
            auctionItemRepository.save(item);
        }
    }

    public List<AuctionItem> getAllItems() {
        return auctionItemRepository.findAll();
    }

    public void deleteItem(Long itemId) {
        AuctionItem item = getAuctionItemById(itemId);
        if (item == null) {
            throw new RuntimeException("Auction item not found with id: " + itemId);
        }
        // Delete associated image file if it exists and is not the placeholder
        if (item.getImageFilename() != null && !item.getImageFilename().equals("placeholder.png")) {
            fileUploadService.deleteImage(item.getImageFilename());
        }
        auctionItemRepository.deleteById(itemId);
    }

    public AuctionItem updateItem(Long itemId, String title, String description, String category, BigDecimal startingPrice, LocalDateTime endTime) {
        AuctionItem item = getAuctionItemById(itemId);
        if (item == null) {
            throw new RuntimeException("Auction item not found with id: " + itemId);
        }
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        item.setStartingPrice(startingPrice);
        item.setEndTime(endTime);
        return auctionItemRepository.save(item);
    }

    public AuctionItem updateItem(Long itemId, String title, String description, String category, String imageFilename, BigDecimal startingPrice, LocalDateTime endTime) {
        AuctionItem item = getAuctionItemById(itemId);
        if (item == null) {
            throw new RuntimeException("Auction item not found with id: " + itemId);
        }
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        if (imageFilename != null && !imageFilename.isEmpty()) {
            // Delete old image if it exists and is not the placeholder
            if (item.getImageFilename() != null && !item.getImageFilename().equals("placeholder.png")) {
                fileUploadService.deleteImage(item.getImageFilename());
            }
            item.setImageFilename(imageFilename);
        }
        item.setStartingPrice(startingPrice);
        item.setEndTime(endTime);
        return auctionItemRepository.save(item);
    }

    public AuctionItem updateItemFeaturedStatus(Long itemId, boolean featured) {
        AuctionItem item = getAuctionItemById(itemId);
        if (item == null) {
            throw new RuntimeException("Auction item not found with id: " + itemId);
        }
        item.setFeatured(featured);
        return auctionItemRepository.save(item);
    }

    public List<AuctionItem> searchAuctions(String query, BigDecimal minPrice, BigDecimal maxPrice) {
        if (query == null || query.trim().isEmpty()) {
            query = "";
        }
        if (minPrice == null) {
            minPrice = BigDecimal.ZERO;
        }
        if (maxPrice == null) {
            maxPrice = BigDecimal.valueOf(Long.MAX_VALUE);
        }
        return auctionItemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStartingPriceBetweenAndEndTimeAfter(
            query, query, minPrice, maxPrice, LocalDateTime.now());
    }
}
