package com.example.boost_product_data.Repository;

import com.example.boost_product_data.domain.Option;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionRepository extends JpaRepository<Option, Long> {
}
