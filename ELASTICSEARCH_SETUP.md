# Elasticsearch 설정 가이드

## 1. Elasticsearch 설치 및 실행

### Docker로 실행 (권장)
```bash
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

### 로컬 설치 (macOS)
```bash
brew install elasticsearch
brew services start elasticsearch
```

### 로컬 설치 (Linux)
```bash
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.11.0-linux-x86_64.tar.gz
tar -xzf elasticsearch-8.11.0-linux-x86_64.tar.gz
cd elasticsearch-8.11.0
./bin/elasticsearch
```

## 2. Elasticsearch 연결 확인

```bash
curl http://localhost:9200
```

응답 예시:
```json
{
  "name" : "node-1",
  "cluster_name" : "elasticsearch",
  "version" : {
    "number" : "8.11.0"
  }
}
```

## 3. 프로젝트 설정

### build.gradle
의존성이 이미 추가되어 있습니다:
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'
```

### application.yml
설정이 이미 추가되어 있습니다:
```yaml
spring:
  data:
    elasticsearch:
      uris: http://localhost:9200
      connection-timeout: 5s
      socket-timeout: 60s
```

## 4. 사용 방법

### Product를 Elasticsearch에 저장
```java
@Autowired
private ProductDocumentRepository productDocumentRepository;

@Autowired
private ProductRepository productRepository;

// Product 엔티티를 Elasticsearch에 저장
public void saveProductToElasticsearch(Long productId) {
    Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    
    ProductDocument document = ProductDocument.from(product);
    productDocumentRepository.save(document);
}

// 여러 상품을 일괄 저장
public void saveAllProductsToElasticsearch() {
    List<Product> products = productRepository.findAll();
    List<ProductDocument> documents = products.stream()
            .map(ProductDocument::from)
            .toList();
    productDocumentRepository.saveAll(documents);
}
```

### 검색 예시
```java
// 이름으로 검색
List<ProductDocument> results = productDocumentRepository.findByNameContaining("상품명");

// 가격 범위로 검색
List<ProductDocument> results = productDocumentRepository.findByPriceBetween(10000L, 50000L);

// 페이징 검색
Page<ProductDocument> page = productDocumentRepository.findByNameContaining(
    "상품명", 
    PageRequest.of(0, 10)
);
```

## 5. 인덱스 생성 확인

```bash
# 인덱스 목록 확인
curl http://localhost:9200/_cat/indices

# products 인덱스 확인
curl http://localhost:9200/products

# products 인덱스의 문서 수 확인
curl http://localhost:9200/products/_count
```

## 6. 문제 해결

### Elasticsearch 연결 실패
- Elasticsearch가 실행 중인지 확인: `curl http://localhost:9200`
- 포트가 9200인지 확인
- 방화벽 설정 확인

### 인덱스가 생성되지 않음
- 첫 번째 문서를 저장하면 자동으로 인덱스가 생성됩니다
- 또는 수동으로 인덱스를 생성할 수 있습니다

