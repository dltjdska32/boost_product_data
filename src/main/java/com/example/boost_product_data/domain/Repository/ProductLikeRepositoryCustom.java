package com.example.boost_product_data.domain.Repository;

import com.space.munova.product.application.dto.FindProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductLikeRepositoryCustom {
    Page<FindProductResponseDto> findLikeProductByMemberId(Pageable pageable, Long memberId);
}
