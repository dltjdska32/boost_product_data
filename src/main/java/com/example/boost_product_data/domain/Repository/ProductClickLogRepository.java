package com.example.boost_product_data.domain.Repository;

import com.space.munova.product.domain.ProductClickLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductClickLogRepository extends JpaRepository<ProductClickLog, Long> {
}
