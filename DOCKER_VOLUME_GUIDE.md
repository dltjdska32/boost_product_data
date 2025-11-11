# Docker 볼륨 사용 가이드

## 1. Docker 볼륨이란?

Docker 볼륨은 컨테이너의 데이터를 영구적으로 저장하는 방법입니다. 컨테이너를 삭제해도 데이터가 유지됩니다.

## 2. 볼륨 타입

### 방법 1: Docker 관리 볼륨 (권장) ✅
```yaml
volumes:
  - elasticsearch_data:/usr/share/elasticsearch/data
```
- Docker가 자동으로 관리
- 가장 안전하고 권장되는 방법
- 볼륨 위치: `/var/lib/docker/volumes/`

### 방법 2: 로컬 디렉토리 마운트
```yaml
volumes:
  - ./elasticsearch_data:/usr/share/elasticsearch/data
```
- 프로젝트 폴더에 직접 저장
- 접근이 쉬움
- 권한 문제 발생 가능

## 3. 현재 설정 확인

현재 `docker-compose.yml`은 **Docker 관리 볼륨**을 사용합니다:
```yaml
volumes:
  elasticsearch_data:
    driver: local
```

## 4. 볼륨 관리 명령어

### 볼륨 목록 확인
```bash
docker volume ls
```

### 볼륨 상세 정보 확인
```bash
docker volume inspect boost_product_data_elasticsearch_data
```

### 볼륨 위치 확인
```bash
# macOS/Linux
docker volume inspect boost_product_data_elasticsearch_data | grep Mountpoint

# 실제 데이터 위치 (macOS Docker Desktop)
~/Library/Containers/com.docker.docker/Data/vms/0/data/docker/volumes/
```

### 볼륨 데이터 백업
```bash
# 볼륨을 tar 파일로 백업
docker run --rm \
  -v boost_product_data_elasticsearch_data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/elasticsearch_backup.tar.gz -C /data .
```

### 볼륨 데이터 복원
```bash
# 백업에서 복원
docker run --rm \
  -v boost_product_data_elasticsearch_data:/data \
  -v $(pwd):/backup \
  alpine tar xzf /backup/elasticsearch_backup.tar.gz -C /data
```

### 볼륨 삭제
```bash
# 컨테이너와 함께 볼륨도 삭제
docker-compose down -v

# 볼륨만 삭제 (컨테이너는 유지)
docker volume rm boost_product_data_elasticsearch_data
```

## 5. 로컬 디렉토리로 변경하려면

`docker-compose.yml`을 다음과 같이 수정:

```yaml
volumes:
  # Docker 관리 볼륨 대신 로컬 디렉토리 사용
  - ./elasticsearch_data:/usr/share/elasticsearch/data
```

그리고 `volumes:` 섹션에서 `elasticsearch_data:` 줄을 제거하거나 주석 처리.

## 6. 데이터 확인

### Elasticsearch 인덱스 확인
```bash
curl http://localhost:9200/_cat/indices
```

### 특정 인덱스의 문서 수 확인
```bash
curl http://localhost:9200/products/_count
```

## 7. 주의사항

⚠️ **중요**: 
- `docker-compose down -v` 실행 시 볼륨이 삭제되어 **모든 데이터가 사라집니다**
- 백업이 필요하면 위의 백업 명령어를 사용하세요
- 프로덕션 환경에서는 정기적인 백업이 필수입니다

## 8. 볼륨 크기 확인

```bash
# 볼륨 사용량 확인
docker system df -v
```

