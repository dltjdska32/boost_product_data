package com.example.boost_product_data.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;

@Document(indexName = "products")  // Elasticsearch 인덱스 이름
@Setting(settingPath = "elasticsearch/nori-settings.json")  // Nori 분석기 설정 파일 경로
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {
    
    @Id
    private Long id;
    
    @Field(type = FieldType.Text, analyzer = "nori", searchAnalyzer = "nori")  // 한국어 형태소 분석기 사용
    private String name;
    
    @Field(type = FieldType.Text, analyzer = "nori", searchAnalyzer = "nori")  // 한국어 형태소 분석기 사용
    private String info;
    
    @Field(type = FieldType.Long)
    private Long price;
    
    @Field(type = FieldType.Long)
    private Long brandId;
    
    @Field(type = FieldType.Text, analyzer = "nori", searchAnalyzer = "nori")  // 브랜드명 검색용
    private String brandName;
    
    @Field(type = FieldType.Long)
    private Long categoryId;
    
    @Field(type = FieldType.Text, analyzer = "nori", searchAnalyzer = "nori")  // 카테고리명 검색용
    private String categoryName;
    
    @Field(type = FieldType.Text, analyzer = "nori", searchAnalyzer = "nori")  // 옵션명들 검색용 (예: "빨강, 파랑, XL, L")
    private String optionNames;
    
    @Field(type = FieldType.Integer)
    private Integer likeCount;
    
    @Field(type = FieldType.Integer)
    private Integer salesCount;
    
    @Field(type = FieldType.Integer)
    private Integer viewCount;
    
    @Field(type = FieldType.Boolean)
    private Boolean isDeleted;
    
    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;
    
    // Product 엔티티로부터 Document 생성 (배치 처리용 - 옵션명 포함)
    public static ProductDocument from(Product product, String brandName, String categoryName, String optionNames) {
        return ProductDocument.builder()
                .id(product.getId())
                .name(product.getName())
                .info(product.getInfo())
                .price(product.getPrice())
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(brandName)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(categoryName)
                .optionNames(optionNames)
                .likeCount(product.getLikeCount())
                .salesCount(product.getSalesCount())
                .viewCount(product.getViewCount())
                .isDeleted(product.isDeleted())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    // Product 엔티티로부터 Document 생성 (기존 호환성 유지)
    public static ProductDocument from(Product product) {
        String brandName = product.getBrand() != null ? product.getBrand().getBrandName() : null;
        String categoryName = product.getCategory() != null ? product.getCategory().getCategoryName() : null;
        
        // 옵션명 수집 (ProductDetail -> ProductOptionMapping -> Option)
        String optionNames = product.getProductDetails().stream()
                .flatMap(detail -> detail.getOptionMappings().stream())
                .map(mapping -> mapping.getOption().getOptionName())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        
        return from(product, brandName, categoryName, optionNames);
    }
}

