package com.platformcommons.identity.service;

import com.platformcommons.identity.domain.Address;

import java.util.List;

/**
 * 收货地址服务接口。
 */
public interface AddressService {

    /**
     * 创建收货地址。
     */
    Address createAddress(Long memberId, Address address);

    /**
     * 查询成员的全部地址（默认优先）。
     */
    List<Address> listAddresses(Long memberId);

    /**
     * 设置默认地址（同时取消原默认）。
     */
    Address setDefault(Long memberId, Long addressId);
}
