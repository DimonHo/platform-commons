package com.platformcommons.identity.domain.address;

import com.platformcommons.identity.domain.address.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 收货地址 Repository。
 */
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {

    /**
     * 根据成员 ID 查询地址列表，默认地址优先、创建时间倒序。
     */
    List<AddressEntity> findByMemberIdOrderByIsDefaultDescCreatedAtDesc(Long memberId);
}
