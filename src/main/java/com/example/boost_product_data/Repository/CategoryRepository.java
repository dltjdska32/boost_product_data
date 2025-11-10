package com.example.boost_product_data.Repository;


import com.example.boost_product_data.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByIdBetween(Long s, Long e);
}
