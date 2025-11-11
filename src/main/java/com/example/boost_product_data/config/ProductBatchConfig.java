    package com.example.boost_product_data.config;

    import com.example.boost_product_data.Repository.*;
    import com.example.boost_product_data.common.CommonEntities;

    import com.example.boost_product_data.domain.*;
    import com.example.boost_product_data.domain.ProductDocument;
    import com.example.boost_product_data.domain.enums.ProductImageType;
    import com.example.boost_product_data.service.FakeService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.batch.core.Job;
    import org.springframework.batch.core.Step;
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
import java.util.concurrent.ThreadPoolExecutor;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Map;

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
        private final ProductDocumentRepository productDocumentRepository; /// Elasticsearch 저장용



        /// 스레드 16개 생성
        @Bean
        public TaskExecutor batchTaskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(16);  /// 사용할 스레드 갯수 16개
            executor.setMaxPoolSize(16);   /// 풀에 있는 스레드 갯수 16개
            executor.setQueueCapacity(48); /// 큐 용량 48개로 설정하여 16개 실행 중일 때 48개의 청크를 대기할 수 있도록 함 (총 64개 청크까지 메모리에 존재 가능)
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); /// 큐가 가득 차면 메인 스레드가 직접 쓰기작업을하여 읽기작업을 머추고 쓰기작업을 모두 끝내고 다시실행.
                                                                                             /// 혹시모를 메모리 부족 대비..
            executor.setThreadNamePrefix("ProductBatchTask-");
            executor.setWaitForTasksToCompleteOnShutdown(true); /// 종료 시 대기
            executor.setAwaitTerminationSeconds(60); /// 최대 60초 대기
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


            //// 현재 벌크인서트하는 청크사이즈 상품 1000 / 상품디테일 2000 / 옵션매핑 4000 / 이미지 1000 
            /// 합 8000개.
            /// 실제 메모리 사용량 계산:
            /// - 객체 헤더: 각 객체당 약 16 bytes
            /// - Product: 헤더(16) + 필드들(약 100 bytes) + String 필드들(이름/설명 각 50-200 bytes) = 약 300-500 bytes
            /// - ProductImage: 헤더(16) + 필드들(약 50 bytes) + String(imgUrl 약 100 bytes) = 약 170 bytes
            /// - ProductDetail: 헤더(16) + 필드들(약 50 bytes) = 약 70 bytes
            /// - ProductOptionMapping: 헤더(16) + 필드들(약 40 bytes) = 약 60 bytes
            /// 
            /// 청크당 실제 메모리:
            /// - Product 1000개: 1000 × 400 bytes = 400 KB
            /// - ProductImage 1000개: 1000 × 170 bytes = 170 KB
            /// - ProductDetail 2000개: 2000 × 70 bytes = 140 KB
            /// - ProductOptionMapping 4000개: 4000 × 60 bytes = 240 KB
            /// - 합계: 약 950 KB ~ 1 MB (String 길이에 따라 2-3MB까지 가능)
            /// 참고: String 필드(productName, productDescription, imgUrl)는 실제 문자열 길이에 따라
            ///       수십~수백 bytes를 추가로 사용하므로, 실제로는 청크당 약 2-3MB 정도 소요됨
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
                
                /// Elasticsearch 저장을 위한 메타데이터 수집용 Map
                Map<Long, String> productBrandNames = new java.util.HashMap<>();  // Product ID -> Brand Name
                Map<Long, String> productCategoryNames = new java.util.HashMap<>();  // Product ID -> Category Name
                Map<Long, List<String>> productOptionNames = new java.util.HashMap<>();  // Product ID -> Option Names List

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
                    
                    /// Elasticsearch 저장을 위해 실제 브랜드명, 카테고리명 조회 (프록시 초기화)
                    String brandName = refBrand.getBrandName();
                    String categoryName = refCategory.getCategoryName();

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

                    Long currentProductId = curProductId++;
                    product.setProductId(currentProductId); /// 아이디 수동할당 오토인크리먼트 무시..
                    productToSave.add(product); /// 저장할 상품리스트 추가.
                    
                    /// Elasticsearch 저장을 위한 메타데이터 저장
                    productBrandNames.put(currentProductId, brandName);
                    productCategoryNames.put(currentProductId, categoryName);
                    productOptionNames.put(currentProductId, new ArrayList<>());
                    
                        
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
                        
                        /// Elasticsearch 저장을 위해 실제 옵션명 조회 (프록시 초기화)
                        String colorOptionName = refColorOption.getOptionName();
                        String sizeOptionName = refSizeOption.getOptionName();

                        /// 옵션매핑 생성
                        ProductOptionMapping colorOM = ProductOptionMapping.createDefaultProductOptionMapping(refColorOption, productDetail);
                        ProductOptionMapping sizeOM = ProductOptionMapping.createDefaultProductOptionMapping(refSizeOption, productDetail);
                        productOptionMappingToSave.add(colorOM);
                        productOptionMappingToSave.add(sizeOM);
                        colorOM.setMappingId(curOptionMappingId++);
                        sizeOM.setMappingId(curOptionMappingId++);
                        
                        /// Elasticsearch 저장을 위한 옵션명 수집
                        productOptionNames.get(currentProductId).add(colorOptionName);
                        productOptionNames.get(currentProductId).add(sizeOptionName);
                    }
                }

                ///  생성된 모든 객체를 saveAll()로 한번에 DB에 배치 인서트
                ///  saveAll() 호출 시, 영속성 컨텍스트에 1000개(Chunk)의 INSERT 쿼리가 쌓이고,
                ///  flush 될 때 JDBC 배치 기능을 사용하여 DB에 단 1번의 통신으로 대량 전송 (DB I/O 최적화)
                productRepository.saveAll(productToSave);
                productDetailRepository.saveAll(productDetailToSave);
                productOptionMappingRepository.saveAll(productOptionMappingToSave);
                productImageRepository.saveAll(productImageToSave);
                
                /// 영속성컨텍스트 1차캐시에 저장된 데이터를 1000개씩(배치사이즈) 만큼 인서트 시작
                /// 총 8번의 인서트 요청
                entityManager.flush();
                
                /// Elasticsearch에 상품 데이터 저장 (검색용)
                /// Product 엔티티를 ProductDocument로 변환하여 Elasticsearch에 저장
                try {
                    List<ProductDocument> documentsToSave = productToSave.stream()
                            .map(product -> {
                                Long productId = product.getId();
                                String brandName = productBrandNames.get(productId);
                                String categoryName = productCategoryNames.get(productId);
                                List<String> optionNamesList = productOptionNames.get(productId);
                                String optionNames = optionNamesList != null && !optionNamesList.isEmpty() 
                                        ? String.join(", ", optionNamesList.stream().distinct().toList())
                                        : "";
                                return ProductDocument.from(product, brandName, categoryName, optionNames);
                            })
                            .toList();
                    /// Elasticsearch에 배치 저장
                    if (!documentsToSave.isEmpty()) {
                        productDocumentRepository.saveAll(documentsToSave);
                    }
                    documentsToSave.clear();
                } catch (Exception e) {
                    /// Elasticsearch 저장 실패 시 로그만 남기고 계속 진행 (DB 저장은 이미 완료됨)
                    System.err.println("Elasticsearch 저장 실패: " + e.getMessage());
                }

                /// 영속성컨텍스트 1차캐시에 저장된 데이터를 비워준다.
                /// 트랜잭션 매니저가 1차캐시 클리어를 안해주기 때문에 메모리에 누적되어 메모리부족현상이 발생할수
                /// 있어서 명시적으로 클리어를 해줌으로서 메모리 관리해줌ㅁ
                entityManager.clear();
                
                /// List 객체들을 명시적으로 비워서 GC가 메모리를 회수할 수 있도록 함
                /// entityManager.clear()는 영속성 컨텍스트만 비우고, 실제 Java 객체들은 여전히 메모리에 존재함
                /// List를 비워야 8000개 객체들이 GC 대상이 되어 메모리에서 해제됨
                productToSave.clear();
                productImageToSave.clear();
                productDetailToSave.clear();
                productOptionMappingToSave.clear();
                
            };
        }


        @Bean
        @StepScope
        public  ItemReader<Integer> countingItemReader(@Value("#{jobParameters[totalItems]}") Long totalItems) {
            if(totalItems == null){ ///  기본값 세팅
                totalItems = 80000L; /// 총 목표 갯수 8만개.
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
