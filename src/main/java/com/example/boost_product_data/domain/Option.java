package com.example.boost_product_data.domain;


import com.example.boost_product_data.common.BaseEntity;
import com.example.boost_product_data.domain.enums.OptionCategory;
import jakarta.persistence.*;
import lombok.*;


@Builder
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "product_option")
public class Option extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private OptionCategory optionType;

    private String optionName;

    public static Option createDefaultOption(OptionCategory optionType, String optionName) {

        return Option.builder()
                .optionType(optionType)
                .optionName(optionName)
                .build();
    }


}
