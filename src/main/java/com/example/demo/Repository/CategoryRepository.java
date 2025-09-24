package com.example.demo.Repository;

import java.util.List;
import java.util.Optional;

import com.example.demo.Entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long > {
    //Tìm Kiếm theo nội dung tên
    List<Category> findByCategoryNameContaining(String name);
    //Tìm kiếm và Phân trang
    Page<Category> findByCategoryNameContaining(String name,Pageable pageable);
    Optional<Category> findByCategoryName(String name);
}
