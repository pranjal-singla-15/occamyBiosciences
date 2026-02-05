package com.occamy.occamyBiosciences.repository;

import com.occamy.occamyBiosciences.entity.Product;
import com.occamy.occamyBiosciences.entity.ProductSalesRecord;
import com.occamy.occamyBiosciences.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSalesRecordRepository extends JpaRepository<ProductSalesRecord, Long> {

    List<ProductSalesRecord> findByOfficer(User officer);

    List<ProductSalesRecord> findByProduct(Product product);

    List<ProductSalesRecord> findByOfficerAndProduct(User officer, Product product);

    @Query("SELECT psr FROM ProductSalesRecord psr WHERE psr.officer.id = :officerId")
    List<ProductSalesRecord> findByOfficerId(@Param("officerId") Long officerId);

    @Query("SELECT psr FROM ProductSalesRecord psr WHERE psr.product.id = :productId")
    List<ProductSalesRecord> findByProductId(@Param("productId") Long productId);

    @Query("SELECT psr FROM ProductSalesRecord psr WHERE psr.officer.id = :officerId AND psr.product.id = :productId")
    List<ProductSalesRecord> findByOfficerIdAndProductId(@Param("officerId") Long officerId, @Param("productId") Long productId);
}

