package com.quantferox.lumeo.config;

import com.quantferox.lumeo.domain.entity.*;
import com.quantferox.lumeo.domain.enums.OrderStatus;
import com.quantferox.lumeo.domain.enums.Role;
import com.quantferox.lumeo.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Populates H2 with realistic seed data at startup.
 * Idempotent: skips if users already exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository  productRepository;
    private final UserRepository     userRepository;
    private final OrderRepository    orderRepository;
    private final PasswordEncoder    passwordEncoder;
    private final EntityManager      entityManager;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("DataSeeder: data already present - skipping.");
            return;
        }
        log.info("DataSeeder: seeding initial data...");
        seedData();
    }

    @Transactional
    public void seedData() {

        // ── Users ──────────────────────────────────────────────────────
        userRepository.save(User.builder()
                .username("admin")
                .email("admin@lumeo.dev")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Ada")
                .lastName("Admin")
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build());

        userRepository.save(User.builder()
                .username("alice")
                .email("alice@lumeo.dev")
                .password(passwordEncoder.encode("user123"))
                .firstName("Alice")
                .lastName("Smith")
                .phoneNumber("+1-555-0100")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build());

        userRepository.save(User.builder()
                .username("bob")
                .email("bob@lumeo.dev")
                .password(passwordEncoder.encode("user123"))
                .firstName("Bob")
                .lastName("Jones")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build());

        // ── Categories ─────────────────────────────────────────────────
        Category electronics = categoryRepository.save(Category.builder()
                .name("Electronics")
                .slug("electronics")
                .description("Gadgets, devices and accessories")
                .imageUrl("https://images.unsplash.com/photo-1498049794561-7780e7231661?w=400")
                .active(true)
                .build());

        Category phones = categoryRepository.save(Category.builder()
                .name("Smartphones")
                .slug("smartphones")
                .description("Latest smartphones and mobile devices")
                .parent(electronics)
                .active(true)
                .build());

        Category laptops = categoryRepository.save(Category.builder()
                .name("Laptops")
                .slug("laptops")
                .description("Laptops and ultrabooks")
                .parent(electronics)
                .active(true)
                .build());

        Category clothing = categoryRepository.save(Category.builder()
                .name("Clothing")
                .slug("clothing")
                .description("Fashion for everyone")
                .imageUrl("https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=400")
                .active(true)
                .build());

        Category books = categoryRepository.save(Category.builder()
                .name("Books")
                .slug("books")
                .description("Technical and fiction books")
                .imageUrl("https://images.unsplash.com/photo-1524578271613-d550eacf6090?w=400")
                .active(true)
                .build());

        // ── Products (saveAllAndFlush returns managed entities with IDs) ──
        List<Product> products = productRepository.saveAllAndFlush(List.of(
                buildProduct("iPhone 16 Pro", "iphone-16-pro", "APPL-IP16P",
                        "Apple iPhone 16 Pro 256GB titanium finish.",
                        "1199.00", "1299.00", 42, true, true,
                        "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=400", phones),
                buildProduct("Samsung Galaxy S25", "samsung-galaxy-s25", "SAMS-GS25",
                        "Samsung Galaxy S25 Ultra 512GB phantom black.",
                        "1099.00", null, 18, true, true,
                        "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=400", phones),
                buildProduct("Google Pixel 9", "google-pixel-9", "GOOG-PX9",
                        "Google Pixel 9 pure Android with 7 years of updates.",
                        "799.00", "899.00", 30, true, false,
                        "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=400", phones),
                buildProduct("MacBook Pro 16", "macbook-pro-16", "APPL-MBP16",
                        "Apple MacBook Pro 16 with M4 Pro, 36GB RAM, 1TB SSD.",
                        "2499.00", "2799.00", 15, true, true,
                        "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400", laptops),
                buildProduct("Dell XPS 15", "dell-xps-15", "DELL-XPS15",
                        "Dell XPS 15 Intel Core Ultra 7, RTX 4060, 32GB RAM.",
                        "1899.00", null, 8, true, false,
                        "https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=400", laptops),
                buildProduct("ThinkPad X1 Carbon", "thinkpad-x1-carbon", "LEN-X1C",
                        "Lenovo ThinkPad X1 Carbon Gen 12 slim business-grade.",
                        "1599.00", "1799.00", 3, true, false,
                        "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400", laptops),
                buildProduct("Classic White Tee", "classic-white-tee", "CLT-WHT-M",
                        "100% organic cotton classic fit t-shirt.",
                        "29.99", "39.99", 200, true, false,
                        "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400", clothing),
                buildProduct("Slim Fit Jeans", "slim-fit-jeans", "CLT-JNS-32",
                        "Premium denim slim fit available in multiple washes.",
                        "79.99", null, 55, true, true,
                        "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400", clothing),
                buildProduct("Running Jacket", "running-jacket", "CLT-RJK-L",
                        "Lightweight windproof running jacket with reflective strips.",
                        "119.00", "149.00", 2, true, false,
                        "https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=400", clothing),
                buildProduct("Clean Code", "clean-code", "BK-CC-001",
                        "Robert C. Martin - A Handbook of Agile Software Craftsmanship.",
                        "39.99", null, 100, true, true,
                        "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400", books),
                buildProduct("Designing Data-Intensive Applications", "designing-data-intensive-apps", "BK-DDIA-001",
                        "Martin Kleppmann - The big ideas behind reliable scalable systems.",
                        "54.99", "64.99", 75, true, true,
                        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400", books),
                buildProduct("Spring Boot in Action", "spring-boot-in-action", "BK-SBA-002",
                        "Craig Walls - Practical guide to Spring Boot development.",
                        "49.99", null, 60, true, false,
                        "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=400", books)
        ));

        // Flush + clear so order items can reference the persisted products
        entityManager.clear();

        // Re-fetch managed entities after clear
        Product p1    = productRepository.findById(products.get(0).getId()).orElseThrow();
        Product p2    = productRepository.findById(products.get(3).getId()).orElseThrow();
        Product p9    = productRepository.findById(products.get(9).getId()).orElseThrow();
        Product p7    = productRepository.findById(products.get(6).getId()).orElseThrow();
        User    alice = userRepository.findByUsername("alice").orElseThrow();
        User    bob   = userRepository.findByUsername("bob").orElseThrow();

        // ── Sample orders ──────────────────────────────────────────────
        Order order1 = Order.builder()
                .orderNumber("ORD-" + System.currentTimeMillis() + "-A1B2C3")
                .status(OrderStatus.DELIVERED)
                .user(alice)
                .shippingStreet("123 Main St")
                .shippingCity("San Francisco")
                .shippingState("CA")
                .shippingZip("94102")
                .shippingCountry("USA")
                .totalAmount(BigDecimal.ZERO)
                .build();
        order1.addItem(OrderItem.builder().product(p1).quantity(1).unitPrice(p1.getPrice()).productName(p1.getName()).build());
        order1.addItem(OrderItem.builder().product(p9).quantity(2).unitPrice(p9.getPrice()).productName(p9.getName()).build());
        order1.recalculateTotal();
        orderRepository.save(order1);

        Order order2 = Order.builder()
                .orderNumber("ORD-" + (System.currentTimeMillis() + 1) + "-D4E5F6")
                .status(OrderStatus.CONFIRMED)
                .user(bob)
                .shippingStreet("456 Oak Ave")
                .shippingCity("New York")
                .shippingState("NY")
                .shippingZip("10001")
                .shippingCountry("USA")
                .totalAmount(BigDecimal.ZERO)
                .build();
        order2.addItem(OrderItem.builder().product(p2).quantity(1).unitPrice(p2.getPrice()).productName(p2.getName()).build());
        order2.recalculateTotal();
        orderRepository.save(order2);

        Order order3 = Order.builder()
                .orderNumber("ORD-" + (System.currentTimeMillis() + 2) + "-G7H8I9")
                .status(OrderStatus.PENDING)
                .user(alice)
                .shippingStreet("789 Pine Rd")
                .shippingCity("Austin")
                .shippingState("TX")
                .shippingZip("78701")
                .shippingCountry("USA")
                .notes("Please leave at door.")
                .totalAmount(BigDecimal.ZERO)
                .build();
        order3.addItem(OrderItem.builder().product(p7).quantity(3).unitPrice(p7.getPrice()).productName(p7.getName()).build());
        order3.recalculateTotal();
        orderRepository.save(order3);

        log.info("DataSeeder: seeded {} categories, {} products, {} users, {} orders.",
                categoryRepository.count(), productRepository.count(),
                userRepository.count(), orderRepository.count());
    }

    private Product buildProduct(String name, String slug, String sku,
                                 String description, String price, String comparePrice,
                                 int stock, boolean active, boolean featured,
                                 String imageUrl, Category category) {
        return Product.builder()
                .name(name).slug(slug).sku(sku).description(description)
                .price(new BigDecimal(price))
                .comparePrice(comparePrice != null ? new BigDecimal(comparePrice) : null)
                .stockQuantity(stock).active(active).featured(featured)
                .imageUrl(imageUrl).category(category)
                .build();
    }
}
