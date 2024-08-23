package com.rem.redis_performance.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("product_2")
public class Product {

    @Id
    private Integer id;
    private String description;
    private double price;
}
