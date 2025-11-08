package com.example.boost_product_data.domain.Repository;

import com.space.munova.product.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BrandRepository extends JpaRepository<Brand, Long> {
}
