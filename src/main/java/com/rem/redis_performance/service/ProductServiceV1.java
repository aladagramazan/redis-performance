package com.rem.redis_performance.service;

import com.rem.redis_performance.entity.Product;
import com.rem.redis_performance.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceV1 {

    @Autowired
    private ProductRepository productRepository;

    public Mono<Product> getProduct(int id) {
        return productRepository.findById(id);
    }

    public Mono<Product> updateProduct(int id, Mono<Product> productMono) {
        return this.productRepository.findById(id)
                .flatMap(p -> productMono.doOnNext(e -> e.setId(id)))
                .flatMap(this.productRepository::save);
    }
}
