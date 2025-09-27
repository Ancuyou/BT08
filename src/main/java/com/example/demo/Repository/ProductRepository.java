package com.example.demo.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.example.demo.Entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByProductNameContaining(String name);
    Page<Product> findByProductNameContaining(String name,Pageable pageable);
    List<Product> findByCategoryCategoryId(Long categoryId);
    Page<Product> findByCategoryCategoryId(Long categoryId, Pageable pageable);

    Optional<Product> findByproductName(String productName);
}
