package com.github.anuawchar.onlineauction.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import com.github.anuawchar.onlineauction.dto.AuctionItemForm;
import com.github.anuawchar.onlineauction.entity.AuctionItem;
import com.github.anuawchar.onlineauction.entity.User;
import com.github.anuawchar.onlineauction.service.AuctionItemService;
import com.github.anuawchar.onlineauction.service.FileUploadService;
import com.github.anuawchar.onlineauction.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final AuctionItemService auctionItemService;
    private final FileUploadService fileUploadService;

    public AdminController(UserService userService, AuctionItemService auctionItemService, FileUploadService fileUploadService) {
        this.userService = userService;
        this.auctionItemService = auctionItemService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping
    public String adminDashboard(Model model) {
        List<User> users = userService.getAllUsers();
        List<AuctionItem> items = auctionItemService.getAllItems();
        model.addAttribute("users", users);
        model.addAttribute("items", items);
        return "admin/index";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/items")
    public String listItems(Model model) {
        List<AuctionItem> items = auctionItemService.getAllItems();
        model.addAttribute("items", items);
        return "admin/items";
    }

    // User Management Endpoints
    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUserRoles(@PathVariable Long userId,
                                  @RequestParam Set<String> roles,
                                  RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserRoles(userId, roles);
            redirectAttributes.addFlashAttribute("success", "User roles updated successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // Item Management Endpoints
    @PostMapping("/items/{itemId}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleItemFeatured(@PathVariable Long itemId,
                                     @RequestParam boolean featured,
                                     RedirectAttributes redirectAttributes) {
        try {
            auctionItemService.updateItemFeaturedStatus(itemId, featured);
            redirectAttributes.addFlashAttribute("success",
                "Item " + (featured ? "featured" : "unfeatured") + " successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/items";
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteItem(@PathVariable Long itemId, RedirectAttributes redirectAttributes) {
        try {
            auctionItemService.deleteItem(itemId);
            redirectAttributes.addFlashAttribute("success", "Item deleted successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/items";
    }

    @GetMapping("/items/{itemId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editItemForm(@PathVariable Long itemId, Model model) {
        AuctionItem item = auctionItemService.getAuctionItemById(itemId);
        if (item == null) {
            return "redirect:/admin/items";
        }
        AuctionItemForm form = new AuctionItemForm(item.getTitle(), item.getDescription(), item.getCategory(), item.getStartingPrice(), item.getEndTime());
        model.addAttribute("auctionItemForm", form);
        model.addAttribute("item", item);
        return "admin/edit-item";
    }

    @PostMapping("/items/{itemId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editItem(@PathVariable Long itemId,
                           @Valid @ModelAttribute("auctionItemForm") AuctionItemForm form,
                           BindingResult bindingResult,
                           Model model) {
        AuctionItem item = auctionItemService.getAuctionItemById(itemId);
        if (item == null) {
            return "redirect:/admin/items";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("item", item);
            return "admin/edit-item";
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
            auctionItemService.updateItem(itemId, form.getTitle(), form.getDescription(), form.getCategory(), imageFilename, form.getStartingPrice(), form.getEndTime());
            return "redirect:/admin/items";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("item", item);
            return "admin/edit-item";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred while updating the item");
            model.addAttribute("item", item);
            return "admin/edit-item";
        }
    }
}
