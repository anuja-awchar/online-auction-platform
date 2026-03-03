package com.github.anuawchar.onlineauction.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.anuawchar.onlineauction.entity.AuctionItem;
import com.github.anuawchar.onlineauction.entity.User;
import com.github.anuawchar.onlineauction.service.AuctionItemService;
import com.github.anuawchar.onlineauction.service.UserService;

@Configuration
public class DataInitializer {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.email}")
    private String adminEmail;

    private final UserService userService;
    private final AuctionItemService auctionItemService;

    public DataInitializer(UserService userService, AuctionItemService auctionItemService) {
        this.userService = userService;
        this.auctionItemService = auctionItemService;
    }

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            // Check if admin user exists, create one if not
            if (!userService.adminExists()) {
                try {
                    userService.createAdminUser(adminUsername, adminPassword, adminEmail);
                    System.out.println("Admin user created: " + adminUsername);
                } catch (Exception e) {
                    System.err.println("Failed to create admin user: " + e.getMessage());
                }
            } else {
                System.out.println("Admin user already exists: " + adminUsername);
            }

            // Create sample auction items if none exist
            List<AuctionItem> allItems = auctionItemService.getAllItems();
            if (allItems.isEmpty()) {
                createSampleAuctionItems();
            }
        };
    }

    private void createSampleAuctionItems() {
        try {
            // Get the admin user to use as seller
            User adminUser = userService.findByUsername(adminUsername);
            if (adminUser == null) {
                System.err.println("Admin user not found, cannot create sample items");
                return;
            }

            // Create featured auction items
            LocalDateTime now = LocalDateTime.now();

            // 1. Vintage Watch - Featured
            AuctionItem vintageWatch = auctionItemService.createAuctionItem(
                "Vintage Rolex Submariner",
                "Authentic 1960s Rolex Submariner in excellent condition. Original dial, hands, and bezel. Comes with original box and papers. A true collector's piece.",
                "Collectibles",
                new BigDecimal("12500.00"),
                now.minusDays(1),
                now.plusDays(7),
                adminUser
            );
            auctionItemService.setFeatured(vintageWatch.getId(), true);

            // 2. Antique Vase - Featured
            AuctionItem antiqueVase = auctionItemService.createAuctionItem(
                "Ming Dynasty Porcelain Vase",
                "Rare 15th century Ming Dynasty blue and white porcelain vase. Perfect condition with original provenance. Museum quality piece.",
                "Art",
                new BigDecimal("45000.00"),
                now.minusDays(2),
                now.plusDays(5),
                adminUser
            );
            auctionItemService.setFeatured(antiqueVase.getId(), true);

            // 3. Rare Book - Featured
            AuctionItem rareBook = auctionItemService.createAuctionItem(
                "First Edition Shakespeare Folio",
                "Original 1623 First Folio of William Shakespeare's works. One of the most important books in English literature. Includes all 36 plays.",
                "Books",
                new BigDecimal("85000.00"),
                now.minusHours(12),
                now.plusDays(10),
                adminUser
            );
            auctionItemService.setFeatured(rareBook.getId(), true);

            // 4. Diamond Ring - Featured
            AuctionItem diamondRing = auctionItemService.createAuctionItem(
                "5 Carat Diamond Engagement Ring",
                "Stunning 5 carat round brilliant cut diamond ring set in platinum. D color, VVS1 clarity. GIA certified with excellent cut grade.",
                "Jewelry",
                new BigDecimal("75000.00"),
                now.minusDays(3),
                now.plusDays(6),
                adminUser
            );
            auctionItemService.setFeatured(diamondRing.getId(), true);

            // 5. Classic Car - Featured
            AuctionItem classicCar = auctionItemService.createAuctionItem(
                "1965 Shelby Cobra 427",
                "Original 1965 Shelby Cobra 427 in pristine condition. Matching numbers, original engine and transmission. Documented history from new.",
                "Vehicles",
                new BigDecimal("1200000.00"),
                now.minusDays(1),
                now.plusDays(14),
                adminUser
            );
            auctionItemService.setFeatured(classicCar.getId(), true);

            // Additional regular auction items to test pagination (total 20 items)
            auctionItemService.createAuctionItem(
                "Antique Oak Writing Desk",
                "Beautiful Victorian era oak writing desk with leather top. Original brass hardware and excellent patina. Perfect for home office.",
                "Furniture",
                new BigDecimal("850.00"),
                now.minusHours(6),
                now.plusDays(4),
                adminUser
            );

            auctionItemService.createAuctionItem(
                "Signed Picasso Print",
                "Limited edition signed lithograph by Pablo Picasso. From the 1960s, excellent condition, framed and ready to hang.",
                "Art",
                new BigDecimal("3200.00"),
                now.minusDays(5),
                now.plusDays(8),
                adminUser
            );

            auctionItemService.createAuctionItem(
                "Vintage Gibson Guitar",
                "1959 Les Paul Standard in sunburst finish. Original pickups and hardware. Plays beautifully with minimal wear.",
                "Musical Instruments",
                new BigDecimal("28000.00"),
                now.minusDays(4),
                now.plusDays(12),
                adminUser
            );

            auctionItemService.createAuctionItem(
                "Ancient Roman Coin",
                "Silver denarius from the reign of Emperor Trajan (98-117 AD). Authentic with certificate of authenticity.",
                "Collectibles",
                new BigDecimal("450.00"),
                now.minusHours(2),
                now.plusDays(3),
                adminUser
            );

            auctionItemService.createAuctionItem(
                "Modern Abstract Painting",
                "Original oil on canvas by contemporary artist. Vibrant colors and bold composition. 36x48 inches.",
                "Art",
                new BigDecimal("1200.00"),
                now.minusDays(1),
                now.plusDays(6),
                adminUser
            );

            auctionItemService.createAuctionItem(
                "Leather Armchair",
                "Mid-century modern leather armchair in cognac color. Comfortable and stylish, minor wear on arms.",
                "Furniture",
                new BigDecimal("650.00"),
                now.minusHours(10),
                now.plusDays(5),
                adminUser
            );

            auctionItemService.createAuctionItem(
                "Gold Necklace",
                "18k gold chain necklace with diamond clasp. Timeless design, 18 inches long, weighs 15 grams.",
                "Jewelry",
                new BigDecimal("2200.00"),
                now.minusDays(2),
                now.plusDays(9),
                adminUser
            );

            auctionItemService.createAuctionItem(
                "Vintage Typewriter",
                "Underwood No. 5 typewriter from 1930s. Fully functional, original case included. Great for collectors.",
                "Collectibles",
                new BigDecimal("350.00"),
                now.minusHours(4),
                now.plusDays(2),
                adminUser
            );

            auctionItemService.createAuctionItem(
                "Acoustic Guitar",
                "Martin D-28 acoustic guitar. Spruce top, mahogany back and sides. Excellent tone and playability.",
                "Musical Instruments",
                new BigDecimal("4500.00"),
                now.minusDays(3),
                now.plusDays(7),
                adminUser
            );

            auctionItemService.createAuctionItem(
                "Porcelain Doll",
                "Antique German porcelain doll from early 1900s. Bisque head, composition body. Dressed in original outfit.",
                "Collectibles",
                new BigDecimal("800.00"),
                now.minusHours(8),
                now.plusDays(4),
                adminUser
            );

            auctionItemService.createAuctionItem(
                "Silver Tea Set",
                "Sterling silver tea set for four. Victorian style with engraved details. Includes teapot, creamer, and sugar bowl.",
                "Antiques",
                new BigDecimal("1500.00"),
                now.minusDays(6),
                now.plusDays(10),
                adminUser
            );

            auctionItemService.createAuctionItem(
                "Baseball Card Collection",
                "1952 Topps Mickey Mantle rookie card in near mint condition. Graded PSA 8. Key piece for any collection.",
                "Sports Memorabilia",
                new BigDecimal("50000.00"),
                now.minusDays(1),
                now.plusDays(11),
                adminUser
            );

            auctionItemService.createAuctionItem(
                "Crystal Chandelier",
                "Baccarat crystal chandelier with 24 lights. Elegant design, perfect for dining room. Original box included.",
                "Home Decor",
                new BigDecimal("6800.00"),
                now.minusDays(4),
                now.plusDays(8),
                adminUser
            );

            auctionItemService.createAuctionItem(
                "Wine Collection",
                "Case of 12 bottles of 1982 Chateau Lafite Rothschild. Stored in temperature-controlled cellar. Excellent provenance.",
                "Wine",
                new BigDecimal("24000.00"),
                now.minusDays(2),
                now.plusDays(5),
                adminUser
            );

            auctionItemService.createAuctionItem(
                "Sculpture",
                "Bronze sculpture by Auguste Rodin reproduction. 'The Thinker' pose, 24 inches tall, mounted on marble base.",
                "Art",
                new BigDecimal("2900.00"),
                now.minusHours(1),
                now.plusDays(6),
                adminUser
            );

            System.out.println("Sample auction items created successfully!");

        } catch (Exception e) {
            System.err.println("Failed to create sample auction items: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
