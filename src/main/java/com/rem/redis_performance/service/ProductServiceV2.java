package com.rem.redis_performance.service;

import com.rem.redis_performance.entity.Product;
import com.rem.redis_performance.service.util.CacheTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceV2 {

    @Autowired
    private CacheTemplate<Integer, Product> cacheTemplate;

    @Autowired
    private ProductVisitService productVisitService;

    public Mono<Product> getProduct(int id) {
        return cacheTemplate.get(id)
                .doFirst(() -> this.productVisitService.addVisit(id));
    }

    public Mono<Product> updateProduct(int id, Mono<Product> productMono) {
        return productMono
                .flatMap(product -> cacheTemplate.update(id, product));
    }

    public Mono<Void> deleteProduct(int id) {
        return this.cacheTemplate.delete(id);
    }
}
