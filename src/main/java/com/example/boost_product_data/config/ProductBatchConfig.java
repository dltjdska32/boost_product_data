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
    import org.springframework.core.task.TaskExecutor;
    import org.springframework.data.redis.core.RedisTemplate;
    import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
    import org.springframework.transaction.PlatformTransactionManager;
    import jakarta.persistence.EntityManager;

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
        private final EntityManager entityManager;



        /// 스레드 10개 생성
        @Bean
        public TaskExecutor batchTaskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(10);  /// 사용할 스레드 갯수 10개
            executor.setMaxPoolSize(10);   /// 풀에 있는 스레드 갯수 10개
            executor.setThreadNamePrefix("ProductBatchTask-");
            executor.initialize();
            return executor;
        }

        /// 잡
        @Bean
        public Job createProductJob(JobRepository jobRepository
                , Step createProductStep) {
            return new JobBuilder("createProductJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(createProductStep)
                    .build();
        }


        /// 잡 아래에 여러개의 스텝이있을수 있다.
        @Bean
        public Step createProductStep(JobRepository jobRepository
                , PlatformTransactionManager transactionManager) {

            return new StepBuilder("createProductStep", jobRepository)
                    .<Integer, Integer> chunk(1000, transactionManager)   /// chunk -> 한번에 디비에 얼마만큼 크기로 쏴줄지.
                    .reader(countingItemReader(null))  ///  아이템 리더가 호출되면 cnt 를 1증가시킴
                    .writer(productGeneratorWriter())
                    .taskExecutor(batchTaskExecutor()) ///  설정한 스레드풀 적용.
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

                /// 레디스에서 각각의 상품 아이디를 증가시킨다 청크사이즈만큼.
                ///  오토인크리먼트 사용하지 않고 벌크 인서트를 위해 서버단에서 아이디를 설정하기위해
                ///  실제 rdbms 의 pk 값을 미리 확보하기 위한용도.
                Long latestProductId = redisTemplate.opsForValue().increment("product_seq", chunkSize);
                Long latestDetailId = redisTemplate.opsForValue().increment("product_detail_seq", chunkSize * 2);  /// 상품한개당 2개의 디테일
                Long latestOptionMappingId = redisTemplate.opsForValue().increment("product_option_seq", chunkSize * 4); ///디테일한개당 2개의 옵션
                Long latestImageId = redisTemplate.opsForValue().increment("image_seq", chunkSize);

                ///  현재 mysql실제 디비에 저장된 pk값
                Long curImageId = latestImageId - chunkSize + 1;
                Long curProductId = latestProductId - chunkSize + 1;
                Long curDetailId = latestDetailId - (chunkSize * 2) + 1;
                Long curOptionMappingId = latestOptionMappingId - (chunkSize * 4) + 1;


                ///  저장할 아이템 리스트
                List<Product> productToSave = new ArrayList<>();
                List<ProductImage> productImageToSave = new ArrayList<>();
                List<ProductDetail> productDetailToSave = new ArrayList<>();
                List<ProductOptionMapping> productOptionMappingToSave = new ArrayList<>();

                ///  common객체에 저장된 엔터티들의 값들은 트랜잭션이 이미 끝난 상태이기 때문에 Detached상태임
                ///  현재 스텝 범위에 있는 영속성 컨택스트에 올라가 있지않다.
                ///  상품생성시 필요한 객체들을 껍대기객체 (참조객체) 로 만들어 저장한다.
                ///  -> 따로 상품 100개를 만들때마다 셀렉트를 하여 관련객체를 조회할경우 조회 비용이 들기때문에 가짜객체만듦.
                Long refMemberId = commonMember.getId();
                Member refMember = memberRepository.getReferenceById(refMemberId);

                for(Integer seqNum : chunk.getItems()) {
                    int brandId = fakeService.createRandomBrandId();
                    int categoryId = fakeService.createRandomCategoryRange();

                    Long rdBrandId = brandIds.get(brandId);
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

                ///  생성된 모든 객체를 saveAll()로 한번에 DB에 배치 인서트
                ///  saveAll() 호출 시, 영속성 컨텍스트에 100개(Chunk)의 INSERT 쿼리가 쌓이고,
                ///  flush 될 때 JDBC 배치 기능을 사용하여 DB에 단 1번의 통신으로 대량 전송 (DB I/O 최적화)
                productRepository.saveAll(productToSave);
                productDetailRepository.saveAll(productDetailToSave);
                productOptionMappingRepository.saveAll(productOptionMappingToSave);
                productImageRepository.saveAll(productImageToSave);
                
                
                entityManager.flush();
                entityManager.clear();
            };
        }


        @Bean
        @StepScope
        public  ItemReader<Integer> countingItemReader(@Value("#{jobParameters[totalItems]}") Long totalItems) {
            if(totalItems == null){ ///  기본값 세팅
                totalItems = 50000L; /// 총 목표 갯수.
            }

            final long maxCnt = totalItems;

            return new ItemReader<>() {
                private int cnt = 0;

                ///  아이템리더가호출되면 cnt 증가.
                @Override
                public Integer read() {
                    if(cnt < maxCnt){
                        cnt++;
                        return cnt;
                    }

                    return null; /// cnt 가 맥스카운트에 도달하면 null반환 작업종료신호.
                }
            };
        }

    }
