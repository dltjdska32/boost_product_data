package com.example.boost_product_data.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

///  아이디 생성 클래스
///  -> 현재 프로덕트관련 Id 생성 전략이 오토 인크리먼트이므로
///  프로덕트 -> 디테일 -> 옵션매핑 을 만들때마다 모두 인서트를 해줘야하기 때문에
///  쓰기작업의 시간이 오래걸린다.
///  따라서 해당 문제를 해결하기 위해 레디스로 현재 테이블에 저장된 ID + 1 ~ ID + 1 + 5000 의 아이디를 레디스로 생성하여
///  배치를 통해 인서트하기위한 클래스이다.

 @Component
 @RequiredArgsConstructor
 @Slf4j
public class RedisIdInitializer {

    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;


    ///  각 엔티티마다 필요한 키 목록
    private static final Map<String, String> KEY_SEQ = Map.of(
            "product_seq", "product",
            "product_detail_seq" , "product_detail",
            "product_option_seq","product_option_mapping",
            "image_seq", "product_image"
    );

    /// 해당 프로그램 실행초기에 1번 실행되어 현재 테이블의 마지막 키값을 설정.
    /// redisTemplate.opsForValue().setIfAbsent(sqKey, "0"); 여기서 opsForValue()는 무엇?
    /// 레디스가 지원하는 데이터 타입.
    /// .opsForValue() (가장 많이 씀)
    /// 데이터 타입: String (e.g., "key" : "value")
    /// 주요 명령어: set, get, setIfAbsent, increment
    @PostConstruct
    public void latestIdSetting() {
        KEY_SEQ.forEach((sqKey,tableName)->{

            ///  디비에서 가장 최근에 생성된 키 조회.
            Long dbMaxId = jdbcTemplate.queryForObject(
                    String.format("select coalesce(max(%s_id), 0) from %s", tableName, tableName), Long.class
            );

            ///  레디스에서 현재 값 가져오기
            String redisValue = redisTemplate.opsForValue().get(sqKey);
            Long redisId = redisValue != null ? Long.parseLong(redisValue) : 0L;

            ///  DB의 최대값이 Redis 값보다 크면 Redis를 업데이트
            if (dbMaxId > redisId) {
                redisTemplate.opsForValue().set(sqKey, String.valueOf(dbMaxId));
                log.info("Updated Redis key '{}' from {} to {} (DB max ID) for table {}", sqKey, redisId, dbMaxId, tableName);
            } else if (redisValue == null) {
                ///  키가 없으면 DB 최대값으로 초기화
                redisTemplate.opsForValue().set(sqKey, String.valueOf(dbMaxId));
                log.info("Initialized Redis key '{}' with {} (DB max ID) for table {}", sqKey, dbMaxId, tableName);
            } else {
                log.info("Redis key '{}' is up to date: {} for table {}", sqKey, redisId, tableName);
            }
        });
    }
}
