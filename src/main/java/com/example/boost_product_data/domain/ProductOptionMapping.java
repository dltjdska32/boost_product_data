package com.example.boost_product_data.domain;



import com.example.boost_product_data.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Builder
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "product_option_mapping")
public class ProductOptionMapping extends BaseEntity {

    @Id
    // @GeneratedValue 제거: 수동 ID 할당을 위해 제거
    @Column(name = "product_option_mapping_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "option_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Option option;

    @ManyToOne
    @JoinColumn(name = "product_detail_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ProductDetail productDetail;

    @ColumnDefault("0")
    private boolean isDeleted;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        // createdAt이 null이면 새 엔티티로 인식
        return getCreatedAt() == null;
    }

    public static ProductOptionMapping createDefaultProductOptionMapping(Option option, ProductDetail productDetail) {

        return ProductOptionMapping.builder()
                .option(option)
                .productDetail(productDetail)
                .build();
    }

    public void setMappingId(Long id) {
        this.id = id;
        markAsNew(); // 수동 ID 할당 시 새 엔티티로 인식하도록 설정
    }
}
