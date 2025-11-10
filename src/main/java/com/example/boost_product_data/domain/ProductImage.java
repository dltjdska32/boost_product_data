package com.example.boost_product_data.domain;




import com.example.boost_product_data.common.BaseEntity;
import com.example.boost_product_data.domain.enums.ProductImageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "product_image")
@Builder
public class ProductImage extends BaseEntity {

    @Id
    // @GeneratedValue 제거: 수동 ID 할당을 위해 제거
    @Column(name = "product_image_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProductImageType imageType;

    private String imgUrl;

    @ColumnDefault("0")
    private boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Product product;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        // createdAt이 null이면 새 엔티티로 인식
        return getCreatedAt() == null;
    }

    public static ProductImage createDefaultProductImage(ProductImageType imageType,
                                                         String imgUrl,
                                                         Product product) {
        return ProductImage.builder()
                .imageType(imageType)
                .imgUrl(imgUrl)
                .product(product)
                .build();
    }

    public void deleteImage() {
        this.isDeleted = true;
    }

    public void updateProductImage(String imagUrl) {
        this.imgUrl = imgUrl;
    }


    public void setImageId(Long id) {
        this.id = id;
        markAsNew(); // 수동 ID 할당 시 새 엔티티로 인식하도록 설정
    }
}
