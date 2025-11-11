package com.example.boost_product_data.Repository;

import com.example.boost_product_data.domain.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, Long> {
    
    // 이름으로 검색
    List<ProductDocument> findByNameContaining(String name);
    
    // 이름으로 검색 (페이징)
    Page<ProductDocument> findByNameContaining(String name, Pageable pageable);
    
    // 가격 범위로 검색
    List<ProductDocument> findByPriceBetween(Long minPrice, Long maxPrice);
    
    // 브랜드 ID로 검색
    List<ProductDocument> findByBrandId(Long brandId);
    
    // 카테고리 ID로 검색
    List<ProductDocument> findByCategoryId(Long categoryId);
    
    // 삭제되지 않은 상품만 검색
    List<ProductDocument> findByIsDeletedFalse();
    
    // 배치 저장을 위한 saveAll 메서드
    <S extends ProductDocument> Iterable<S> saveAll(Iterable<S> entities);

}

