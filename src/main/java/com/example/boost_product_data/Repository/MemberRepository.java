package com.example.boost_product_data.Repository;

import com.example.boost_product_data.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
