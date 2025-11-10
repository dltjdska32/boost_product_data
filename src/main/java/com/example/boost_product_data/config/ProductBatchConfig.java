    package com.example.boost_product_data.config;

    import com.example.boost_product_data.Repository.*;
    import com.example.boost_product_data.common.CommonEntities;

    import com.example.boost_product_data.domain.*;
    import com.example.boost_product_data.domain.enums.ProductImageType;
    import com.example.boost_product_data.service.FakeService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.batch.core.Job;
    import org.springframework.batch.core.Step;
    import org.springframework.batch.core.StepExecution;
    import org.springframework.batch.core.StepExecutionListener;
    import org.springframework.batch.core.configuration.annotation.StepScope;
    import org.springframework.batch.core.job.builder.JobBuilder;
    import org.springframework.batch.core.launch.support.RunIdIncrementer;
    import org.springframework.batch.core.repository.JobRepository;
    import org.springframework.batch.core.step.builder.StepBuilder;
    import org.springframework.batch.item.Chunk;
    import org.springframework.batch.item.ItemReader;
    import org.springframework.batch.item.ItemWriter;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.data.redis.core.RedisTemplate;
    import org.springframework.transaction.PlatformTransactionManager;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Map;
    import java.util.stream.Collectors;

    @Configuration
    @RequiredArgsConstructor
    public class ProductBatchConfig {

        private final ProductRepository productRepository;
        private final ProductOptionMappingRepository productOptionMappingRepository;
        private final FakeService fakeService;
        private final ProductDetailRepository productDetailRepository;
        private final RedisTemplate<String, String> redisTemplate;
        private final CommonEntities commonEntities;
        private final ProductImageRepository productImageRepository;
        private final BrandRepository brandRepository;
        private final ProductOptionRepository productOptionRepository;
        private final OptionRepository optionRepository;
        private final CategoryRepository categoryRepository;
        private final MemberRepository memberRepository;

        @Bean
        public Job createProductJob(JobRepository jobRepository
                , Step createProductStep) {
            return new JobBuilder("createProductJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(createProductStep)
                    .build();
        }

        @Bean
        public Step createProductStep(JobRepository jobRepository
                , PlatformTransactionManager transactionManager) {

            return new StepBuilder("createProductStep", jobRepository)
                    .<Integer, Integer> chunk(100, transactionManager)
                    .reader(countingItemReader(null))
                    .writer(productGeneratorWriter())
                    .build();
        }


        @Bean
        @StepScope
        public ItemWriter<Integer> productGeneratorWriter() {

            final Map<Long, ProductImage> productImage = commonEntities.getProductImage();
            final List<Long> brandIds = commonEntities.rtBrandIds();
            final List<Long> categoryIds = commonEntities.rtCategoryIds();
            final List<Long> optionIds = commonEntities.rtProductOptionIds();
            final Member commonMember = commonEntities.getMember();

            return (Chunk<? extends Integer> chunk) -> {
                int chunkSize = chunk.getItems().size();

                /// 레디스에서 아이디 묶음 가져오기
                Long latestProductId = redisTemplate.opsForValue().increment("product_seq", chunkSize);
                Long latestDetailId = redisTemplate.opsForValue().increment("product_detail_seq", chunkSize * 2);
                Long latestOptionMappingId = redisTemplate.opsForValue().increment("product_option_seq", chunkSize * 4);
                Long latestImageId = redisTemplate.opsForValue().increment("image_seq", chunkSize);
                
                Long curImageId = latestImageId - chunkSize + 1;
                Long curProductId = latestProductId - chunkSize + 1;
                Long curDetailId = latestDetailId - (chunkSize * 2) + 1;
                Long curOptionMappingId = latestOptionMappingId - (chunkSize * 4) + 1;

                List<Product> productToSave = new ArrayList<>();
                List<ProductImage> productImageToSave = new ArrayList<>();
                List<ProductDetail> productDetailToSave = new ArrayList<>();
                List<ProductOptionMapping> productOptionMappingToSave = new ArrayList<>();
                Long refMemberId = commonMember.getId();
                Member refMember = memberRepository.getReferenceById(refMemberId);

                for(Integer seqNum : chunk.getItems()) {
                    int brandId = fakeService.createRandomBrandId();
                    int categoryId = fakeService.createRandomCategoryRange();

                    Long rdBrandId = optionIds.get(brandId);
                    Long rdCategoryId = categoryIds.get(categoryId);

                    /// 해당 트랜잭션에서는 영속성 컨택스트에 브랜드, 카테고리 ,이미지, 옵션등등의 객체가 올라가지않아서
                    /// 아이디만 가져와 참조객체 (껍대기 객체)를 만들어서 넣어준다.
                    Brand refBrand = brandRepository.getReferenceById(rdBrandId);
                    Category refCategory = categoryRepository.getReferenceById(rdCategoryId);

                    /// 랜덤 이름 내용 가격 생성.
                    String productDescription = fakeService.createProductDescription();
                    String productName = fakeService.createProductName();
                    Long randomProductPrice = fakeService.createRandomProductPrice();

                    Product product = Product.createDefaultProduct(productName
                            , productDescription
                            , randomProductPrice
                            , refBrand
                            , refCategory
                            , refMember);

                    product.setProductId(curProductId++); /// 아이디 수동할당 오토인크리먼트 무시..
                    productToSave.add(product); /// 저장할 상품리스트 추가.
                    
                        
                    ///  새로운 이미지 복사 (imgUrl 만 랜덤으로 뽑아서 새로운 이미지생성)
                    ProductImage img = productImage.get(fakeService.createRandomProductImageId());
                    String imgUrl = img.getImgUrl();
                    ProductImage newProductImage = ProductImage.createDefaultProductImage(ProductImageType.MAIN, imgUrl, product);
                    newProductImage.setImageId(curImageId++);
                    productImageToSave.add(newProductImage);

                    for(int i = 0; i < 2; i++){
                        /// 디테일 생성
                        ProductDetail productDetail = ProductDetail.createDefaultProductDetail(product, 1000);
                        productDetail.setProductDetailId(curDetailId++);
                        productDetailToSave.add(productDetail);

                        Long colorOpId = optionIds.get(fakeService.createColorOptionId());
                        Long sizeOpId = optionIds.get(fakeService.createSizeOptionId());
                        Option refColorOption = optionRepository.getReferenceById(colorOpId);
                        Option refSizeOption = optionRepository.getReferenceById(sizeOpId);

                        /// 옵션매핑 생성
                        ProductOptionMapping colorOM = ProductOptionMapping.createDefaultProductOptionMapping(refColorOption, productDetail);
                        ProductOptionMapping sizeOM = ProductOptionMapping.createDefaultProductOptionMapping(refSizeOption, productDetail);
                        productOptionMappingToSave.add(colorOM);
                        productOptionMappingToSave.add(sizeOM);
                        colorOM.setMappingId(curOptionMappingId++);
                        sizeOM.setMappingId(curOptionMappingId++);
                    }
                }

                ///  생성된 상품 , 디테일 , 옵션매핑, 메인이미지 한번에 배치 인서트
                // @GeneratedValue를 제거했으므로 saveAll() 사용 가능 (배치 인서트)
                productRepository.saveAll(productToSave);
                productDetailRepository.saveAll(productDetailToSave);
                productOptionMappingRepository.saveAll(productOptionMappingToSave);
                productImageRepository.saveAll(productImageToSave);
            };
        }


        @Bean
        @StepScope
        public  ItemReader<Integer> countingItemReader(@Value("#{jobParameters[totalItems]}") Long totalItems) {
            if(totalItems == null){ ///  기본값 세팅
                totalItems = 5000L;
            }

            final long maxCnt = totalItems;

            return new ItemReader<>() {
                private int cnt = 0;

                @Override
                public Integer read() {
                    if(cnt < maxCnt){
                        cnt++;
                        return cnt;
                    }

                    return null; ///종료
                }
            };
        }

    }
