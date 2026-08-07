package com.platformcommons.identity.application.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.identity.domain.address.Address;
import com.platformcommons.identity.domain.address.AddressEntity;
import com.platformcommons.identity.domain.address.AddressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AddressServiceImpl} 单元测试（纯 Mockito，不依赖 DB / Spring 上下文）。
 *
 * <p>覆盖：修改地址成功（全量覆盖）、地址不存在、非本人地址、改为默认时取消原默认。</p>
 */
@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    /** 地址归属成员 ID。 */
    private static final Long MEMBER_ID = 1L;
    /** 地址 ID。 */
    private static final Long ADDRESS_ID = 100L;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    @Test
    void updateAddress_success_overwritesAllFieldsAndSaves() {
        AddressEntity existing = entity(MEMBER_ID, false);
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.of(existing));
        when(addressRepository.save(any(AddressEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Address updated = addressService.updateAddress(MEMBER_ID, ADDRESS_ID, newAddress("新家", "李四", true));

        assertEquals("新家", updated.label(), "标签应被覆盖");
        assertEquals("李四", updated.receiverName(), "收件人应被覆盖");
        assertTrue(updated.isDefault(), "应改为默认");
        verify(addressRepository).save(existing);
    }

    @Test
    void updateAddress_notFound_throwsDataNotFound() {
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> addressService.updateAddress(MEMBER_ID, ADDRESS_ID, newAddress("家", "张三", false)));

        assertEquals(ResultCode.DATA_NOT_FOUND.getCode(), ex.getCode(), "地址不存在应报数据未找到");
        verify(addressRepository, never()).save(any(AddressEntity.class));
    }

    @Test
    void updateAddress_otherMembersAddress_throwsForbidden() {
        AddressEntity otherOwned = entity(99L, false);
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.of(otherOwned));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> addressService.updateAddress(MEMBER_ID, ADDRESS_ID, newAddress("家", "张三", false)));

        assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode(), "非本人地址应报禁止访问");
        verify(addressRepository, never()).save(any(AddressEntity.class));
    }

    @Test
    void updateAddress_markDefault_unsetsPreviousDefault() {
        AddressEntity existing = entity(MEMBER_ID, false);
        AddressEntity previousDefault = entity(MEMBER_ID, true);
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.of(existing));
        when(addressRepository.findByMemberIdOrderByIsDefaultDescCreatedAtDesc(MEMBER_ID))
                .thenReturn(List.of(existing, previousDefault));
        when(addressRepository.save(any(AddressEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Address updated = addressService.updateAddress(MEMBER_ID, ADDRESS_ID, newAddress("家", "张三", true));

        assertTrue(updated.isDefault(), "当前地址应改为默认");
        assertFalse(previousDefault.getIsDefault(), "原默认地址应被取消");
    }

    // ===== 测试工具 =====

    private static AddressEntity entity(Long memberId, boolean isDefault) {
        AddressEntity e = new AddressEntity();
        e.setId(ADDRESS_ID);
        e.setMemberId(memberId);
        e.setLabel("家");
        e.setReceiverName("张三");
        e.setPhone("13800138000");
        e.setProvince("浙江省");
        e.setCity("杭州市");
        e.setDistrict("西湖区");
        e.setDetail("文一西路 100 号");
        e.setLatitude(30.28);
        e.setLongitude(120.12);
        e.setIsDefault(isDefault);
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        return e;
    }

    private static Address newAddress(String label, String receiverName, boolean isDefault) {
        return new Address(
                ADDRESS_ID, MEMBER_ID, label, receiverName, "13900139000",
                "浙江省", "杭州市", "余杭区", "文一西路 969 号",
                30.27, 120.11, isDefault, Instant.now(), Instant.now()
        );
    }
}
