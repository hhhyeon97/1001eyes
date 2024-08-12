package com.demo.myshop.controller;


import com.demo.myshop.model.Product;
import com.demo.myshop.model.User;
import com.demo.myshop.security.UserDetailsImpl;
import com.demo.myshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 상품 등록 폼
    @GetMapping("/add-form")
    public String productForm() {return "create";
    }
    // 상품 등록
    @PostMapping("/add")
    public String addProduct(@RequestParam String title, @RequestParam String description,
                             @RequestParam String category, @RequestParam Integer price,
                             @RequestParam Integer stock, @RequestParam String imageUrl ) {
        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setCategory(category);
        product.setPrice(price);
        product.setStock(stock);
        product.setImageUrl(imageUrl);
        productService.saveProduct(product);
        return "redirect:/";
    }

    // 상품 리스트 조회
    @GetMapping("/list")
    public String productList(Model model) {
        // 모든 아이템을 조회하여 리스트 페이지로 전달
        model.addAttribute("products", productService.getAllProducts());
        return "list";
    }
    // 상품 상세 조회
    @GetMapping("/{id}")
    public String productDetail(@PathVariable("id") Long id, Model model) {
        Optional<Product> result = productService.findItemById(id);
        if (result.isPresent()) {
            Product product = result.get();
            model.addAttribute("product", product);
            return "detail";
        }else {
            return "redirect:/";
        }
    }

}