    package com.example.boost_product_data.common;

    import com.example.boost_product_data.Repository.*;
    import com.example.boost_product_data.domain.*;
    import jakarta.annotation.PostConstruct;
    import lombok.Getter;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Component;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Map;
    import java.util.stream.Collectors;

    @Component // 2. 스프링 빈으로 등록
    @RequiredArgsConstructor
    @Getter // 3. 외부에서 멤버를 가져갈 수 있도록 Getter 추가
    public class CommonEntities {

        private final MemberRepository memberRepository;
        private final CategoryRepository categoryRepository;
        private final BrandRepository brandRepository;
        private final ProductRepository productRepository;
        private final ProductImageRepository productImageRepository;
        private final ProductOptionRepository productOptionRepository;

        private Member member;
        private Map<Long, Category> categoryMap;
        private Map<Long, Option> optionMap;
        private Map<Long, Brand> brandMap;
        private Map<Long, ProductImage> productImage;


        @PostConstruct // 빈 초기화 시 1회 실행
        public void initEntities() { // (메소드 이름 변경 권장)

            this.member = memberRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("1L 멤버가 존재하지 않습니다.")); // (null 대신 예외처리 권장)


            // Map으로 변환하여 저장
            this.categoryMap = categoryRepository.findByIdBetween(5L, 56L).stream()
                    .collect(Collectors.toMap(Category::getId, c -> c));

            this.brandMap = brandRepository.findAll().stream()
                    .collect(Collectors.toMap(Brand::getId, b -> b));

            this.optionMap = productOptionRepository.findAll().stream()
                    .collect(Collectors.toMap(Option::getId, o -> o));

            this.productImage = productImageRepository.findByIdBetween(1L, 1000L).stream()
                    .collect(Collectors.toMap(ProductImage::getId, p -> p));

        }

        public List<Long> rtProductImageIds() {
            return new ArrayList<>(this.productImage.keySet());
        }

        public List<Long> rtBrandIds() {
            return new ArrayList<>(this.brandMap.keySet());
        }

        public List<Long> rtProductOptionIds() {
            return new ArrayList<>(this.optionMap.keySet());
        }

        public List<Long> rtCategoryIds() {
            return new ArrayList<>(this.categoryMap.keySet());
        }

        public Long rtMemberId() {
            return this.member.getId();
        }
    }
