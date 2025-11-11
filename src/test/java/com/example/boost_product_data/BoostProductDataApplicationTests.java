package com.example.boost_product_data;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "spring.data.elasticsearch.repositories.enabled=false",  // 테스트 시 Elasticsearch 비활성화
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
    "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
    "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
class BoostProductDataApplicationTests {

    @Test
    void contextLoads() {
        // 기본 컨텍스트 로드 테스트
    }

}
