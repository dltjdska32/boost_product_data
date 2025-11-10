package com.example.boost_product_data.common;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Auditable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity implements Serializable , Persistable<Long> {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Override
    public Long getId() {
        return 0L;
    }

    // 3. ⬇️ (핵심) 'isNew' 상태를 관리할 transient 필드 추가
    // @Transient: DB 컬럼에는 매핑하지 않음
    @Transient
    private boolean isNew = true; // ◀ 기본값은 'true' (새 객체)

    // 4. ⬇️ 'Persistable' 인터페이스의 'isNew' 메소드 구현
    @Override
    public boolean isNew() {
        return this.isNew;
    }


    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    //  수동 ID 할당 시 isNew를 true로 설정하기 위한 메서드
    protected void markAsNew() {
        this.isNew = true;
    }
}
