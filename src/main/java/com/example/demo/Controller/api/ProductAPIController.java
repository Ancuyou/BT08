package com.example.demo.Controller.api;

import com.example.demo.Entities.Product;
import com.example.demo.Models.Response;
import com.example.demo.Service.ICategoryService;
import com.example.demo.Service.IProductService;
import com.example.demo.Service.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/product")
public class ProductAPIController {
    @Autowired
    private IProductService productService;
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    IStorageService storageService;
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        List<Product> all = productService.findAll();
        return new ResponseEntity<Response>(new Response(true, "Thành công", all), HttpStatus.OK);
    }
    @PostMapping("/getProduct")
    public ResponseEntity<?> getProduct(@Validated @RequestParam("id") Long id) {
        Optional<Product> opt = productService.findById(id);
        if (opt.isPresent()) {
            return new ResponseEntity<Response>(new Response(true, "Thành công", opt.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<Response>(new Response(false, "Không tìm thấy Product", null), HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/addProduct")
    public ResponseEntity<?> addProduct(
            @Validated @RequestParam("productName") String productName,
            @RequestParam(value = "quantity", required = false, defaultValue = "0") int quantity,
            @RequestParam(value = "unitPrice", required = false, defaultValue = "0") double unitPrice,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "discount", required = false, defaultValue = "0") double discount,
            @RequestParam(value = "status", required = false, defaultValue = "1") short status,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "images", required = false) MultipartFile images
    ) {
        Optional<Product> optName = productService.findByProductName(productName);
        if (optName.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(false, "Product đã tồn tại", optName.get()));
        }

        Product p = new Product();
        p.setProductName(productName);
        p.setQuantity(quantity);
        p.setUnitPrice(unitPrice);
        p.setDescription(description);
        p.setDiscount(discount);
        p.setStatus(status);
        p.setCreateDate(new java.util.Date());

        if (categoryId != null) {
            categoryService.findById(categoryId).ifPresent(c -> p.setCategory(c));
        }

        if (images != null && !images.isEmpty()) {
            UUID uuid = UUID.randomUUID();
            String filename = storageService.getSorageFilename(images, uuid.toString());
            p.setImages(filename);
            storageService.store(images, filename);
        }

        productService.save(p);
        return new ResponseEntity<Response>(new Response(true,"Thêm thành công", p), HttpStatus.OK);
    }

    @PutMapping("/updateProduct")
    public ResponseEntity<?> updateProduct(
            @Validated @RequestParam("productId") Long productId,
            @Validated @RequestParam("productName") String productName,
            @RequestParam(value = "quantity", required = false, defaultValue = "0") int quantity,
            @RequestParam(value = "unitPrice", required = false, defaultValue = "0") double unitPrice,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "discount", required = false, defaultValue = "0") double discount,
            @RequestParam(value = "status", required = false, defaultValue = "1") short status,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "images", required = false) MultipartFile images
    ) {
        Optional<Product> opt = productService.findById(productId);
        if (opt.isEmpty()) {
            return new ResponseEntity<Response>(new Response(false, "Không tìm thấy Product", null), HttpStatus.BAD_REQUEST);
        }
        Product p = opt.get();
        p.setProductName(productName);
        p.setQuantity(quantity);
        p.setUnitPrice(unitPrice);
        p.setDescription(description);
        p.setDiscount(discount);
        p.setStatus(status);

        if (categoryId != null) {
            categoryService.findById(categoryId).ifPresent(c -> p.setCategory(c));
        } else {
            p.setCategory(null);
        }

        if (images != null && !images.isEmpty()) {
            UUID uuid = UUID.randomUUID();
            String filename = storageService.getSorageFilename(images, uuid.toString());
            p.setImages(filename);
            storageService.store(images, filename);
        } else {
            // nếu client ko gửi file mới thì giữ images cũ (service.save đã xử lý)
        }

        productService.save(p);
        return new ResponseEntity<Response>(new Response(true, "Cập nhật thành công", p), HttpStatus.OK);
    }

    @DeleteMapping("/deleteProduct")
    public ResponseEntity<?> deleteProduct(@Validated @RequestParam("productId") Long productId) {
        Optional<Product> opt = productService.findById(productId);
        if (opt.isEmpty()) {
            return new ResponseEntity<Response>(new Response(false, "Không tìm thấy Product", null), HttpStatus.BAD_REQUEST);
        }
        productService.delete(opt.get());
        return new ResponseEntity<Response>(new Response(true, "Xóa thành công", opt.get()), HttpStatus.OK);
    }
}
