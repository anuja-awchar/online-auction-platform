package com.github.anuawchar.onlineauction.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.anuawchar.onlineauction.dto.AuctionItemForm;
import com.github.anuawchar.onlineauction.dto.BidForm;
import com.github.anuawchar.onlineauction.entity.AuctionItem;
import com.github.anuawchar.onlineauction.entity.Bid;
import com.github.anuawchar.onlineauction.entity.ChatMessage;
import com.github.anuawchar.onlineauction.entity.User;
import com.github.anuawchar.onlineauction.service.AuctionItemService;
import com.github.anuawchar.onlineauction.service.ChatService;
import com.github.anuawchar.onlineauction.service.FileUploadService;
import com.github.anuawchar.onlineauction.service.UserService;

import jakarta.validation.Valid;

@Controller
public class AuctionItemController {

    private final AuctionItemService auctionItemService;
    private final UserService userService;
    private final FileUploadService fileUploadService;
    private final ChatService chatService;

    public AuctionItemController(AuctionItemService auctionItemService, UserService userService, FileUploadService fileUploadService, ChatService chatService) {
        this.auctionItemService = auctionItemService;
        this.userService = userService;
        this.fileUploadService = fileUploadService;
        this.chatService = chatService;
    }

    @GetMapping("/")
    public String listActiveAuctions(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "12") int size,
                                     @RequestParam(defaultValue = "endTime") String sortBy,
                                     @RequestParam(defaultValue = "asc") String sortDir,
                                     Model model) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AuctionItem> activeItemsPage = auctionItemService.getActiveAuctions(pageable);
        List<AuctionItem> featuredItems = auctionItemService.getFeaturedAuctions();
        List<AuctionItem> endingSoonItems = auctionItemService.getEndingSoonAuctions();

        model.addAttribute("itemsPage", activeItemsPage);
        model.addAttribute("items", activeItemsPage.getContent()); // For backward compatibility in template
        model.addAttribute("featuredItems", featuredItems);
        model.addAttribute("endingSoonItems", endingSoonItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", activeItemsPage.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "index";
    }

    @GetMapping("/item/{id}")
    public String viewItem(@PathVariable Long id, Model model, Authentication authentication) {
        AuctionItem item = auctionItemService.getAuctionItemById(id);
        if (item == null) {
            return "redirect:/";
        }
        List<Bid> bids = auctionItemService.getBidsForItem(id);
        BigDecimal currentPrice = auctionItemService.getCurrentHighestBid(id);
        model.addAttribute("item", item);
        model.addAttribute("bids", bids);
        model.addAttribute("currentPrice", currentPrice);
        if (authentication != null) {
            User currentUser = userService.findByUsername(authentication.getName());
            model.addAttribute("isSeller", currentUser.getId().equals(item.getSeller().getId()));
            model.addAttribute("isAdmin", authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("chatMessages", chatService.getMessagesForAuction(id));
        } else {
            model.addAttribute("isSeller", false);
            model.addAttribute("isAdmin", false);
            model.addAttribute("chatMessages", List.of());
        }
        return "item-detail";
    }

    @GetMapping("/item/{id}/chat")
    public String getChatHistory(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        AuctionItem item = auctionItemService.getAuctionItemById(id);
        if (item == null) {
            return "redirect:/";
        }
        User currentUser = userService.findByUsername(authentication.getName());
        if (!currentUser.getId().equals(item.getSeller().getId()) && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            // For now, allow bidders to see chat, but in production might restrict
        }
        List<ChatMessage> messages = chatService.getMessagesForAuction(id);
        model.addAttribute("chatMessages", messages);
        model.addAttribute("auctionId", id);
        model.addAttribute("sellerId", item.getSeller().getId());
        model.addAttribute("currentUserId", currentUser.getId());
        return "fragments/chat-history :: chatMessages"; // Fragment for AJAX
    }

    @MessageMapping("/chat/{auctionId}")
    @SendTo("/topic/chat/{auctionId}")
    public ChatMessage sendMessage(@DestinationVariable Long auctionId, ChatMessage incomingMessage, Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("User not authenticated");
        }
        User sender = userService.findByUsername(authentication.getName());
        AuctionItem auctionItem = auctionItemService.getAuctionItemById(auctionId);
        if (auctionItem == null || sender == null) {
            throw new IllegalArgumentException("Invalid auction or sender");
        }
        // Recipient is the seller for bidders, or vice versa
        User recipient = sender.getId().equals(auctionItem.getSeller().getId()) ? 
            // If sender is seller, message to a specific bidder? For simplicity, assume broadcast to all, but set recipient as seller or first bidder
            auctionItem.getBids().isEmpty() ? sender : auctionItem.getBids().get(0).getBidder() : auctionItem.getSeller();
        
        ChatMessage message = chatService.saveMessage(incomingMessage.getMessage(), auctionId, sender.getId(), recipient.getId());
        return message;
    }

    @GetMapping("/create-item")
    public String createItemForm(Model model) {
        model.addAttribute("auctionItemForm", new AuctionItemForm());
        return "create-item";
    }

    @PostMapping("/create-item")
    public String createItem(@Valid @ModelAttribute("auctionItemForm") AuctionItemForm form,
                             BindingResult bindingResult,
                             Authentication authentication,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "create-item";
        }
        User seller = userService.findByUsername(authentication.getName());
        try {
            String imageFilename = null;
            if (form.getImage() != null && !form.getImage().isEmpty()) {
                imageFilename = fileUploadService.uploadImage(form.getImage());
            }
            auctionItemService.createAuctionItem(form.getTitle(), form.getDescription(), form.getCategory(), imageFilename, form.getStartingPrice(), LocalDateTime.now(), form.getEndTime(), seller);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "create-item";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred while creating the item");
            return "create-item";
        }
     }
    //   http://localhost:8080/admin

    @PostMapping("/item/{id}/bid")
    public String placeBid(@PathVariable Long id,
                           @Valid @ModelAttribute("bidForm") BidForm bidForm,
                           BindingResult bindingResult,
                           Authentication authentication,
                           Model model) {
        AuctionItem item = auctionItemService.getAuctionItemById(id);
        if (bindingResult.hasErrors()) {
            List<Bid> bids = auctionItemService.getBidsForItem(id);
            BigDecimal currentPrice = auctionItemService.getCurrentHighestBid(id);
            model.addAttribute("item", item);
            model.addAttribute("bids", bids);
            model.addAttribute("currentPrice", currentPrice);
            model.addAttribute("bidForm", bidForm);
            return "item-detail";
        }
        User bidder = userService.findByUsername(authentication.getName());
        try {
            auctionItemService.placeBid(item, bidder, bidForm.getAmount());
            return "redirect:/item/" + id;
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            List<Bid> bids = auctionItemService.getBidsForItem(id);
            BigDecimal currentPrice = auctionItemService.getCurrentHighestBid(id);
            model.addAttribute("item", item);
            model.addAttribute("bids", bids);
            model.addAttribute("currentPrice", currentPrice);
            model.addAttribute("bidForm", bidForm);
            return "item-detail";
        }
    }

    @PostMapping("/item/{id}/featured")
    public String toggleFeatured(@PathVariable Long id,
                                 @RequestParam boolean featured,
                                 Authentication authentication) {
        // Assuming admin role check, for now just call the service
        auctionItemService.setFeatured(id, featured);
        return "redirect:/item/" + id;
    }

    @GetMapping("/search")
    public String searchAuctions(@RequestParam(required = false) String query,
                                 @RequestParam(required = false) BigDecimal minPrice,
                                 @RequestParam(required = false) BigDecimal maxPrice,
                                 Model model) {
        List<AuctionItem> searchResults = auctionItemService.searchAuctions(query, minPrice, maxPrice);
        model.addAttribute("items", searchResults);
        model.addAttribute("query", query);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "index";
    }

    @GetMapping("/item/{id}/edit")
    public String editItemForm(@PathVariable Long id, Model model, Authentication authentication) {
        AuctionItem item = auctionItemService.getAuctionItemById(id);
        if (item == null) {
            return "redirect:/";
        }
        User currentUser = userService.findByUsername(authentication.getName());
        if (!currentUser.getId().equals(item.getSeller().getId()) && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/item/" + id;
        }
        AuctionItemForm form = new AuctionItemForm(item.getTitle(), item.getDescription(), item.getCategory(), item.getStartingPrice(), item.getEndTime());
        model.addAttribute("auctionItemForm", form);
        model.addAttribute("item", item);
        return "edit-item";
    }

    @PostMapping("/item/{id}/edit")
    public String editItem(@PathVariable Long id,
                           @Valid @ModelAttribute("auctionItemForm") AuctionItemForm form,
                           BindingResult bindingResult,
                           Authentication authentication,
                           Model model) {
        AuctionItem item = auctionItemService.getAuctionItemById(id);
        if (item == null) {
            return "redirect:/";
        }
        User currentUser = userService.findByUsername(authentication.getName());
        if (!currentUser.getId().equals(item.getSeller().getId()) && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/item/" + id;
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("item", item);
            return "edit-item";
        }
        try {
            String imageFilename = item.getImageFilename(); // Keep existing image by default
            if (form.getImage() != null && !form.getImage().isEmpty()) {
                // Delete old image if it exists
                if (imageFilename != null && !imageFilename.equals("placeholder.png")) {
                    fileUploadService.deleteImage(imageFilename);
                }
                // Upload new image
                imageFilename = fileUploadService.uploadImage(form.getImage());
            }
            auctionItemService.updateItem(id, form.getTitle(), form.getDescription(), form.getCategory(), imageFilename, form.getStartingPrice(), form.getEndTime());
            return "redirect:/item/" + id;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("item", item);
            return "edit-item";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred while updating the item");
            model.addAttribute("item", item);
            return "edit-item";
        }
    }

    @PostMapping("/item/{id}/delete")
    public String deleteItem(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        AuctionItem item = auctionItemService.getAuctionItemById(id);
        if (item == null) {
            return "redirect:/";
        }
        User currentUser = userService.findByUsername(authentication.getName());
        if (!currentUser.getId().equals(item.getSeller().getId()) && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/item/" + id;
        }
        try {
            auctionItemService.deleteItem(id);
            redirectAttributes.addFlashAttribute("success", "Item deleted successfully");
            return "redirect:/";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/item/" + id;
        }
    }

    @GetMapping("/uploads/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get("uploads/images").resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // or determine from file extension
                        .body(resource);
            } else {
                // Return placeholder if image not found
                java.nio.file.Path placeholderPath = java.nio.file.Paths.get("src/main/resources/static/images/placeholder.png");
                Resource placeholderResource = new UrlResource(placeholderPath.toUri());
                if (placeholderResource.exists() && placeholderResource.isReadable()) {
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_PNG)
                            .body(placeholderResource);
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
