package com.example.boost_product_data.domain;


import com.example.boost_product_data.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Table(name = "product_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductDetail extends BaseEntity {
    @Id
    // @GeneratedValue 제거: 수동 ID 할당을 위해 제거
    @Column(name = "product_detail_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Product product;

    private Integer quantity;

    @ColumnDefault("0")
    private boolean isDeleted;

    @OneToMany(mappedBy = "productDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductOptionMapping>  optionMappings = new ArrayList<>();

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        // createdAt이 null이면 새 엔티티로 인식
        return getCreatedAt() == null;
    }

    public static ProductDetail createDefaultProductDetail(Product product, Integer quantity) {

        if(quantity == null) {
            throw new IllegalArgumentException("quantity cannot be null");
        }
        if(product == null) {
            throw new IllegalArgumentException("product cannot be null");
        }
        if(quantity < 1) {
            throw new IllegalArgumentException("quantity cannot be less than 1");
        }

        return ProductDetail.builder()
                .product(product)
                .quantity(quantity)
                .build();
    }

    public void setProductDetailId (Long productDetailId) {
        this.id = productDetailId;
        markAsNew(); // 수동 ID 할당 시 새 엔티티로 인식하도록 설정
    }

    public void restoreStock(int quantity) {
        if (this.quantity == null) {
            this.quantity = 0;
        }

        this.quantity += quantity;
    }
}
