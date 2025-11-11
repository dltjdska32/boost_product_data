# Docker로 Elasticsearch 실행하기

## 1. Docker Compose로 실행 (가장 쉬운 방법)

### Elasticsearch만 실행
```bash
# Elasticsearch 시작
docker-compose up -d elasticsearch

# 실행 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f elasticsearch

# 중지
docker-compose stop elasticsearch

# 완전히 제거 (데이터도 삭제)
docker-compose down -v
```

### Elasticsearch + Redis 함께 실행
`docker-compose.yml`에서 Redis 주석을 해제한 후:
```bash
docker-compose up -d
```

## 2. 연결 확인

```bash
# Elasticsearch 연결 확인
curl http://localhost:9200

# 또는 브라우저에서
# http://localhost:9200
```

## 3. application.yml 설정

현재 설정이 이미 올바르게 되어 있습니다:
```yaml
spring:
  data:
    elasticsearch:
      uris: http://localhost:9200  # Docker로 실행한 Elasticsearch
```

## 4. Docker 네트워크를 사용하는 경우

만약 애플리케이션도 Docker로 실행한다면, `application.yml`을 다음과 같이 수정:

```yaml
spring:
  data:
    elasticsearch:
      uris: http://elasticsearch:9200  # Docker 네트워크 내부 주소
```

## 5. 유용한 명령어

```bash
# Elasticsearch 컨테이너 접속
docker exec -it elasticsearch bash

# Elasticsearch 인덱스 확인
curl http://localhost:9200/_cat/indices

# Elasticsearch 데이터 삭제 (주의!)
curl -X DELETE http://localhost:9200/products

# Elasticsearch 재시작
docker-compose restart elasticsearch

# 볼륨 확인 (데이터 저장 위치)
docker volume ls
docker volume inspect boost_product_data_elasticsearch_data
```

## 6. 문제 해결

### 포트가 이미 사용 중인 경우
```bash
# 포트 사용 확인
lsof -i :9200

# 다른 포트로 변경하려면 docker-compose.yml 수정
ports:
  - "9201:9200"  # 호스트:컨테이너
```

### 메모리 부족 오류
`docker-compose.yml`에서 메모리 설정 조정:
```yaml
environment:
  - "ES_JAVA_OPTS=-Xms256m -Xmx256m"  # 더 작은 메모리
```

### 데이터 영구 저장
`docker-compose.yml`에 이미 볼륨 설정이 되어 있어서 데이터가 유지됩니다:
```yaml
volumes:
  - elasticsearch_data:/usr/share/elasticsearch/data
```

## 7. 프로덕션 환경

프로덕션에서는 보안 설정을 활성화하세요:
```yaml
environment:
  - discovery.type=single-node
  - xpack.security.enabled=true
  - ELASTIC_PASSWORD=your_password
```

