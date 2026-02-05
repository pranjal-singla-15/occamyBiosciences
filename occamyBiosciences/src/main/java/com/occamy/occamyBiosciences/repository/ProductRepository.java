package com.occamy.occamyBiosciences.repository;

import com.occamy.occamyBiosciences.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
