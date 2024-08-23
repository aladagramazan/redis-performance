package com.rem.redis_performance.service.util;

import com.rem.redis_performance.entity.Product;
import com.rem.redis_performance.repository.ProductRepository;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProductLocalCacheTemplate extends CacheTemplate<Integer, Product> {

    @Autowired
    private ProductRepository productRepository;
    private RLocalCachedMap<Integer, Product> map;

    public ProductLocalCacheTemplate(RedissonClient client) {
        LocalCachedMapOptions<Integer, Product> mapOptions = LocalCachedMapOptions.<Integer, Product>defaults()
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.CLEAR);

        this.map = client.getLocalCachedMap("product", new TypedJsonJacksonCodec(Integer.class, Product.class), mapOptions);
    }

    @Override
    protected Mono<Product> getFromSource(Integer id) {
        return this.productRepository.findById(id);
    }

    @Override
    protected Mono<Product> getFromCache(Integer id) {
        return Mono.justOrEmpty(this.map.get(id));
    }

    @Override
    protected Mono<Product> updateSource(Integer id, Product product) {
        return this.productRepository.findById(id)
                .doOnNext(p -> product.setId(id))
                .flatMap(p -> this.productRepository.save(product));
    }

    @Override
    protected Mono<Product> updateCache(Integer id, Product product) {
        return Mono.create(productMonoSink -> {
            this.map.fastPutAsync(id, product)
                    .thenAccept(v -> productMonoSink.success(product))
                    .exceptionally(ex -> {
                        productMonoSink.error(ex);
                        return null;
                    });
        });
    }

    @Override
    protected Mono<Void> deleteFromSource(Integer id) {
        return this.productRepository.deleteById(id);
    }

    @Override
    protected Mono<Void> deleteFromCache(Integer id) {
        return Mono.create(voidMonoSink -> {
            this.map.fastRemoveAsync(id)
                    .thenAccept(v -> voidMonoSink.success())
                    .exceptionally(ex -> {
                        voidMonoSink.error(ex);
                        return null;
                    });
        });
    }
}
