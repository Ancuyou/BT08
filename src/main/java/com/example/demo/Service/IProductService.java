package com.example.demo.Service;

import com.example.demo.Entities.Product;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface IProductService {
    <S extends Product> S save(S entity);

    Optional<Product> findByProductName(String name);

    Optional<Product> findById(Long id);

    List<Product> findAll();

    Page<Product> findAll(Pageable pageable);

    List<Product> findAll(Sort sort);

    List<Product> findAllById(Iterable<Long> ids);

    void delete(Product entity);

    void deleteById(Long id);

    long count();

    <S extends Product> Optional<S> findOne(Example<S> example);

    List<Product> findByProductNameContaining(String name);

    Page<Product> findByProductNameContaining(String name, Pageable pageable);

    List<Product> findByCategoryId(Long categoryId);
}
