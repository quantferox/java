package com.quantferox.lumeo.web;

import com.quantferox.lumeo.domain.enums.OrderStatus;
import com.quantferox.lumeo.dto.request.CategoryRequest;
import com.quantferox.lumeo.dto.request.OrderStatusRequest;
import com.quantferox.lumeo.dto.request.ProductRequest;
import com.quantferox.lumeo.dto.response.CategoryResponse;
import com.quantferox.lumeo.service.CategoryService;
import com.quantferox.lumeo.service.OrderService;
import com.quantferox.lumeo.service.ProductService;
import com.quantferox.lumeo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService  productService;
    private final CategoryService categoryService;
    private final OrderService    orderService;
    private final UserService     userService;

    // ── Dashboard ─────────────────────────────────────────────────────────

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalProducts",    productService.findAll(PageRequest.of(0, 1)).getTotalElements());
        model.addAttribute("totalOrders",      orderService.findAll(PageRequest.of(0, 1)).getTotalElements());
        model.addAttribute("totalUsers",       userService.findAll(PageRequest.of(0, 1)).getTotalElements());
        model.addAttribute("revenue",          orderService.getTotalRevenue());
        model.addAttribute("lowStock",         productService.findLowStock(5));
        model.addAttribute("recentOrders",     orderService.findAll(PageRequest.of(0, 5, Sort.by("createdAt").descending())).getContent());
        return "admin/dashboard";
    }

    // ── Products ──────────────────────────────────────────────────────────

    @GetMapping("/products")
    public String products(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String q,
            Model model) {

        var pageable = PageRequest.of(page, size, Sort.by("name"));
        var products = (q != null && !q.isBlank())
                ? productService.search(q, pageable)
                : productService.findAll(pageable);

        model.addAttribute("products", products);
        model.addAttribute("query",    q);
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product",    new ProductRequest());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isNew",      true);
        return "admin/product-form";
    }

    @PostMapping("/products/new")
    public String createProduct(@Valid @ModelAttribute("product") ProductRequest request,
                                BindingResult result,
                                Model model,
                                RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("isNew", true);
            return "admin/product-form";
        }
        productService.create(request);
        ra.addFlashAttribute("successMessage", "Product created successfully.");
        return "redirect:/admin/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        var response = productService.findById(id);
        // Map response fields into a request object for the form
        ProductRequest req = new ProductRequest();
        req.setName(response.getName());
        req.setSlug(response.getSlug());
        req.setSku(response.getSku());
        req.setDescription(response.getDescription());
        req.setPrice(response.getPrice());
        req.setComparePrice(response.getComparePrice());
        req.setStockQuantity(response.getStockQuantity());
        req.setImageUrl(response.getImageUrl());
        req.setActive(response.isActive());
        req.setFeatured(response.isFeatured());
        req.setCategoryId(response.getCategoryId());

        model.addAttribute("product",    req);
        model.addAttribute("productId",  id);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isNew",      false);
        return "admin/product-form";
    }

    @PostMapping("/products/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("product") ProductRequest request,
                                BindingResult result,
                                Model model,
                                RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("productId",  id);
            model.addAttribute("isNew",      false);
            return "admin/product-form";
        }
        productService.update(id, request);
        ra.addFlashAttribute("successMessage", "Product updated successfully.");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("successMessage", "Product deleted.");
        return "redirect:/admin/products";
    }

    // ── Categories ────────────────────────────────────────────────────────

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories";
    }

    @GetMapping("/categories/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category",  new CategoryRequest());
        model.addAttribute("allCats",   categoryService.findAll());
        model.addAttribute("isNew",     true);
        return "admin/category-form";
    }

    @PostMapping("/categories/new")
    public String createCategory(@Valid @ModelAttribute("category") CategoryRequest request,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("allCats", categoryService.findAll());
            model.addAttribute("isNew", true);
            return "admin/category-form";
        }
        categoryService.create(request);
        ra.addFlashAttribute("successMessage", "Category created.");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        CategoryResponse cat = categoryService.findById(id);
        CategoryRequest  req = new CategoryRequest();
        req.setName(cat.getName());
        req.setSlug(cat.getSlug());
        req.setDescription(cat.getDescription());
        req.setImageUrl(cat.getImageUrl());
        req.setActive(cat.isActive());
        req.setParentId(cat.getParentId());

        model.addAttribute("category",   req);
        model.addAttribute("categoryId", id);
        model.addAttribute("allCats",    categoryService.findAll());
        model.addAttribute("isNew",      false);
        return "admin/category-form";
    }

    @PostMapping("/categories/{id}/edit")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute("category") CategoryRequest request,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("allCats", categoryService.findAll());
            model.addAttribute("categoryId", id);
            model.addAttribute("isNew", false);
            return "admin/category-form";
        }
        categoryService.update(id, request);
        ra.addFlashAttribute("successMessage", "Category updated.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.delete(id);
        ra.addFlashAttribute("successMessage", "Category deleted.");
        return "redirect:/admin/categories";
    }

    // ── Orders ────────────────────────────────────────────────────────────

    @GetMapping("/orders")
    public String orders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    OrderStatus status,
            Model model) {

        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var orders   = (status != null)
                ? orderService.findByStatus(status, pageable)
                : orderService.findAll(pageable);

        model.addAttribute("orders",   orders);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selected", status);
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        model.addAttribute("order",    orderService.findById(id));
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("statusReq", new OrderStatusRequest());
        return "admin/order-detail";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @Valid @ModelAttribute("statusReq") OrderStatusRequest request,
                                    RedirectAttributes ra) {
        orderService.updateStatus(id, request.getStatus());
        ra.addFlashAttribute("successMessage", "Order status updated.");
        return "redirect:/admin/orders/" + id;
    }

    // ── Users ─────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public String users(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String q,
            Model model) {

        var pageable = PageRequest.of(page, size, Sort.by("username"));
        var users    = (q != null && !q.isBlank())
                ? userService.search(q, pageable)
                : userService.findAll(pageable);

        model.addAttribute("users", users);
        model.addAttribute("query", q);
        return "admin/users";
    }

    @PostMapping("/users/{id}/promote")
    public String promote(@PathVariable Long id, RedirectAttributes ra) {
        userService.promoteToAdmin(id);
        ra.addFlashAttribute("successMessage", "User promoted to ADMIN.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/disable")
    public String disableUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.disable(id);
        ra.addFlashAttribute("successMessage", "User disabled.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/enable")
    public String enableUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.enable(id);
        ra.addFlashAttribute("successMessage", "User enabled.");
        return "redirect:/admin/users";
    }
}
