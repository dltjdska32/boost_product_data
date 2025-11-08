package com.example.boost_product_data.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OptionCategory {
    SIZE ("사이즈"),
    COLOR ("색상");

    private String description;
}
