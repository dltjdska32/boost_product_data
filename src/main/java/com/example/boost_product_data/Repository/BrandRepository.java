package com.example.boost_product_data.Repository;


import com.example.boost_product_data.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BrandRepository extends JpaRepository<Brand, Long> {
}
