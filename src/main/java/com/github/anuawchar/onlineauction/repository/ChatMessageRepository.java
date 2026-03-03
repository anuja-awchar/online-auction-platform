package com.github.anuawchar.onlineauction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.anuawchar.onlineauction.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByAuctionItemIdOrderByTimestampAsc(Long auctionItemId);
    List<ChatMessage> findBySenderIdOrRecipientIdOrderByTimestampAsc(Long senderId, Long recipientId);
}
