package com.example.boost_product_data.domain.Repository;


import com.space.munova.product.application.dto.cart.ProductInfoForCartDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CartRepositoryCustom {
    Page<ProductInfoForCartDto> findCartItemInfoByMemberId(Long memberId, Pageable pageable);
}

