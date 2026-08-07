package com.platformcommons.identity.application.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.identity.domain.address.Address;
import com.platformcommons.identity.domain.address.AddressRepository;
import com.platformcommons.identity.domain.address.AddressEntity;
import com.platformcommons.identity.application.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * {@link AddressService} 默认实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Address createAddress(Long memberId, Address address) {
        log.info("创建收货地址：memberId={}, label={}", memberId, address.label());
        Instant now = Instant.now();
        AddressEntity entity = new AddressEntity();
        entity.setMemberId(memberId);
        entity.setLabel(address.label());
        entity.setReceiverName(address.receiverName());
        entity.setPhone(address.phone());
        entity.setProvince(address.province());
        entity.setCity(address.city());
        entity.setDistrict(address.district());
        entity.setDetail(address.detail());
        entity.setLatitude(address.latitude());
        entity.setLongitude(address.longitude());
        entity.setIsDefault(address.isDefault() != null && address.isDefault());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // 若标记为默认，则取消原有默认
        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            unsetPreviousDefaults(memberId);
        }

        AddressEntity saved = addressRepository.save(entity);
        log.info("收货地址创建成功：id={}, memberId={}", saved.getId(), memberId);
        return toDomain(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Address updateAddress(Long memberId, Long addressId, Address address) {
        log.info("修改收货地址：memberId={}, addressId={}", memberId, addressId);
        AddressEntity entity = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "地址不存在: " + addressId));
        if (!entity.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "地址不属于该成员");
        }

        // 全量覆盖
        entity.setLabel(address.label());
        entity.setReceiverName(address.receiverName());
        entity.setPhone(address.phone());
        entity.setProvince(address.province());
        entity.setCity(address.city());
        entity.setDistrict(address.district());
        entity.setDetail(address.detail());
        entity.setLatitude(address.latitude());
        entity.setLongitude(address.longitude());
        // 若改为默认，则取消原默认
        boolean newDefault = address.isDefault() != null && address.isDefault();
        if (newDefault) {
            unsetPreviousDefaults(memberId);
        }
        entity.setIsDefault(newDefault);
        entity.setUpdatedAt(Instant.now());

        AddressEntity saved = addressRepository.save(entity);
        log.info("收货地址修改成功：id={}, memberId={}", saved.getId(), memberId);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> listAddresses(Long memberId) {
        return addressRepository.findByMemberIdOrderByIsDefaultDescCreatedAtDesc(memberId).stream()
                .map(AddressServiceImpl::toDomain)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Address setDefault(Long memberId, Long addressId) {
        log.info("设置默认地址：memberId={}, addressId={}", memberId, addressId);
        AddressEntity entity = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "地址不存在: " + addressId));
        if (!entity.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "地址不属于该成员");
        }

        // 取消原默认
        unsetPreviousDefaults(memberId);

        entity.setIsDefault(true);
        entity.setUpdatedAt(Instant.now());
        AddressEntity saved = addressRepository.save(entity);
        log.info("默认地址设置成功：memberId={}, addressId={}", memberId, addressId);
        return toDomain(saved);
    }

    // ===== 内部工具 =====

    private void unsetPreviousDefaults(Long memberId) {
        List<AddressEntity> existing = addressRepository.findByMemberIdOrderByIsDefaultDescCreatedAtDesc(memberId);
        Instant now = Instant.now();
        for (AddressEntity addr : existing) {
            if (Boolean.TRUE.equals(addr.getIsDefault())) {
                addr.setIsDefault(false);
                addr.setUpdatedAt(now);
            }
        }
    }

    private static Address toDomain(AddressEntity entity) {
        return new Address(
                entity.getId(),
                entity.getMemberId(),
                entity.getLabel(),
                entity.getReceiverName(),
                entity.getPhone(),
                entity.getProvince(),
                entity.getCity(),
                entity.getDistrict(),
                entity.getDetail(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getIsDefault(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
