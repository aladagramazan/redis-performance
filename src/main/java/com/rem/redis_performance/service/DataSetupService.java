package com.rem.redis_performance.service;

import com.rem.redis_performance.entity.Product;
import com.rem.redis_performance.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.ThreadLocalRandom;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class DataSetupService implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @Value("classpath:schema.sql")
    private Resource resource;

    @Override
    public void run(String... args) throws Exception {
        String query = StreamUtils.copyToString(resource.getInputStream(), UTF_8);
        System.out.println(query);

        Mono<Void> insert = Flux.range(1, 1000)
                .map(i -> Product.builder()
                        .id(null)
                        .description("Product " + i)
                        .price(ThreadLocalRandom.current().nextInt(100))
                        .build())
                .collectList()
                .flatMapMany(productRepository::saveAll)
                .then();

        this.r2dbcEntityTemplate.getDatabaseClient()
                .sql(query)
                .then()
                .then(insert)
                .doFinally(signalType -> System.out.println("Data setup completed" + signalType))
                .subscribe();
    }
}
