package com.example.boost_product_data.domain;


import com.example.boost_product_data.common.BaseEntity;
import com.example.boost_product_data.domain.enums.MemberRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member extends BaseEntity {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "member_id")
private Long id;

private String username;

private String password;

private String address;

@Enumerated(EnumType.STRING)
MemberRole role;



}
