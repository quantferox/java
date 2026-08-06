package com.quantferox.lumeo.web;

import com.quantferox.lumeo.dto.response.CategoryResponse;
import com.quantferox.lumeo.dto.response.PageResponse;
import com.quantferox.lumeo.dto.response.ProductResponse;
import com.quantferox.lumeo.service.CategoryService;
import com.quantferox.lumeo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ShopController {

    private final ProductService  productService;
    private final CategoryService categoryService;

    // ── Public storefront ─────────────────────────────────────────────────

    @GetMapping({"/", "/shop"})
    public String home(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false)    String q,
            @RequestParam(required = false)    Long category,
            Model model) {

        var pageable = PageRequest.of(page, size, Sort.by("name"));

        PageResponse<ProductResponse> products;
        if (q != null && !q.isBlank()) {
            products = productService.search(q, pageable);
            model.addAttribute("query", q);
        } else if (category != null) {
            products = productService.findByCategory(category, pageable);
            model.addAttribute("selectedCategoryId", category);
        } else {
            products = productService.findAll(pageable);
        }

        List<CategoryResponse> categories = categoryService.findAll();
        List<ProductResponse>  featured   = productService.findFeatured();

        model.addAttribute("products",   products);
        model.addAttribute("categories", categories);
        model.addAttribute("featured",   featured);
        model.addAttribute("currentPage", page);

        return "shop/index";
    }

    @GetMapping("/shop/product/{slug}")
    public String productDetail(@PathVariable String slug, Model model) {
        ProductResponse product = productService.findBySlug(slug);

        // Related products from same category
        var related = productService
                .findByCategory(product.getCategoryId(), PageRequest.of(0, 4))
                .getContent()
                .stream()
                .filter(p -> !p.getId().equals(product.getId()))
                .limit(3)
                .toList();

        model.addAttribute("product",  product);
        model.addAttribute("related",  related);
        return "shop/product";
    }

    @GetMapping("/shop/category/{slug}")
    public String categoryPage(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        var cat      = categoryService.findBySlug(slug);
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        var products = productService.findByCategory(cat.getId(), pageable);

        model.addAttribute("category",    cat);
        model.addAttribute("products",    products);
        model.addAttribute("categories",  categoryService.findAll());
        model.addAttribute("currentPage", page);
        return "shop/index";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) model.addAttribute("loginError", "Invalid username or password.");
        return "auth/login";
    }
}
