package com.occamy.occamyBiosciences.repository;

import com.occamy.occamyBiosciences.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    // JpaRepository already provides save() method, no need for custom saveProductReview
}
