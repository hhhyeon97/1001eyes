package com.demo.myshop.controller;


import com.demo.myshop.model.Product;
import com.demo.myshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
//@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // 상품 등록 폼
    @GetMapping("/add")
    public String productForm() {
        return "create";
    }
    // 상품 등록
    @PostMapping("/post")
    public String addProduct(String title, String description, String category, Integer stock, String imageUrl, Integer price) {
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
        model.addAttribute("products", productService.getAvailableProducts());
        return "list";
    }
}