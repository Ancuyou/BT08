package com.example.demo.Service.Impl;

import com.example.demo.Entities.Product;
import com.example.demo.Repository.ProductRepository;
import com.example.demo.Service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements IProductService {
    private final ProductRepository productRepository;
    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    @Override
    public <S extends Product> S save(S entity) {
        // Them createDate khi them moi
        if (entity.getProductId() == null) {
            if (entity.getCreateDate() == null){
                entity.setCreateDate(new java.util.Date());
            }
            return productRepository.save(entity);
        }
        else{
            Optional<Product> opt = productRepository.findById(entity.getProductId());
            if (opt.isPresent()) {
                // Giu image cu neu khong co image moi
                if(StringUtils.isEmpty(entity.getImages())){
                    entity.setImages(opt.get().getImages());
                }
                if (entity.getCreateDate() == null) {
                    entity.setCreateDate(opt.get().getCreateDate());
                }
            }
            return productRepository.save(entity);
        }
    }
    @Override
    public Optional<Product> findByProductName(String name) {return productRepository.findByproductName(name);}
    @Override
    public Optional<Product> findById(Long id) { return productRepository.findById(id); }
    @Override
    public List<Product> findAll() { return productRepository.findAll(); }
    @Override
    public Page<Product> findAll(Pageable pageable) { return productRepository.findAll(pageable); }
    @Override
    public List<Product> findAll(Sort sort) { return productRepository.findAll(sort); }
    @Override
    public List<Product> findAllById(Iterable<Long> ids) { return productRepository.findAllById(ids); }
    @Override
    public void delete(Product entity) { productRepository.delete(entity); }
    @Override
    public void deleteById(Long id) { productRepository.deleteById(id); }
    @Override
    public long count() { return productRepository.count(); }
    @Override
    public <S extends Product> Optional<S> findOne(Example<S> example) { return productRepository.findOne(example); }
    @Override
    public List<Product> findByProductNameContaining(String name) { return productRepository.findByProductNameContaining(name); }
    @Override
    public Page<Product> findByProductNameContaining(String name, Pageable pageable) { return productRepository.findByProductNameContaining(name, pageable); }
    @Override
    public List<Product> findByCategoryId(Long categoryId) { return productRepository.findByCategoryCategoryId(categoryId); }
}
