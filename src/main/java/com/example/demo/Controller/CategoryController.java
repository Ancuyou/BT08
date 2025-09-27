package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("admin/ajax")
public class CategoryController {
    @GetMapping("/category")
    public String showCategoryPage() {
        return "admin/views/category";
    }
    @GetMapping("/product")
    public String showProductPage() {return "admin/views/product";}
}
