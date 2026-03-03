package com.github.anuawchar.onlineauction.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.github.anuawchar.onlineauction.entity.AuctionItem;
import com.github.anuawchar.onlineauction.entity.ChatMessage;
import com.github.anuawchar.onlineauction.entity.User;
import com.github.anuawchar.onlineauction.repository.ChatMessageRepository;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final AuctionItemService auctionItemService;

    public ChatService(ChatMessageRepository chatMessageRepository, UserService userService, AuctionItemService auctionItemService) {
        this.chatMessageRepository = chatMessageRepository;
        this.userService = userService;
        this.auctionItemService = auctionItemService;
    }

    public ChatMessage saveMessage(String message, Long auctionItemId, Long senderId, Long recipientId) {
        User sender = userService.getUserById(senderId);
        User recipient = userService.getUserById(recipientId);
        AuctionItem auctionItem = auctionItemService.getAuctionItemById(auctionItemId);

        if (sender == null || recipient == null || auctionItem == null) {
            throw new IllegalArgumentException("Invalid sender, recipient, or auction item");
        }

        ChatMessage chatMessage = new ChatMessage(message, LocalDateTime.now(), sender, recipient, auctionItem);
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getMessagesForAuction(Long auctionItemId) {
        return chatMessageRepository.findByAuctionItemIdOrderByTimestampAsc(auctionItemId);
    }
}
