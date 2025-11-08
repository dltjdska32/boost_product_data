package com.example.boost_product_data.domain.Repository;

import com.space.munova.product.domain.Option;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionRepository extends JpaRepository<Option, Long> {
}
