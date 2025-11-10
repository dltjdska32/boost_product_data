package com.example.boost_product_data.Repository;


import com.example.boost_product_data.domain.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>{



    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p " +
            "SET p.viewCount = p.viewCount + 1 " +
            "WHERE p.id = :productId")
    void updateProductViewCount(Long productId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p " +
            "SET p.isDeleted = true " +
            "WHERE p.id IN :productIds ")
    void deleteAllByProductIds(List<Long> productIds);

    Optional<Product> findByIdAndIsDeletedFalse(Long productId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p " +
            "SET p.likeCount = p.likeCount - 1 " +
            "WHERE p.id = :productId " +
            "AND p.likeCount > 0")
    int minusLikeCountInProductIds(Long productId);



    boolean existsByIdAndMemberIdAndIsDeletedFalse(Long productId , Long sellerId);

    @Query("SELECT p FROM Product p " +
            "WHERE p.isDeleted = false " +
            "AND p.id = :productId " +
            "AND p.member.id = :sellerId ")
    Optional<Product> findByIdAndMemberIdAndIsDeletedFalse(Long productId, Long sellerId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p " +
            "SET p.likeCount = p.likeCount + 1 " +
            "WHERE p.id = :productId")
    void plusLikeCountByProductId(Long productId);
}
