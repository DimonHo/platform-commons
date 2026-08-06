package com.platformcommons.identity.domain.member;

import com.platformcommons.identity.domain.member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 成员 Repository。
 */
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    /**
     * 根据手机号查询成员。
     *
     * @param phone 手机号
     * @return 成员实体
     */
    Optional<MemberEntity> findByPhone(String phone);
}
