package com.platformcommons.identity.api;

import com.platformcommons.common.util.RecordUtils;
import com.platformcommons.identity.api.dto.AddressResponse;
import com.platformcommons.identity.api.dto.CreateAddressRequest;
import com.platformcommons.identity.domain.address.Address;
import com.platformcommons.identity.application.AddressService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 收货地址簿对外接口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "地址簿", description = "收货地址管理")
public class AddressController {

    private final AddressService addressService;

    /**
     * 创建收货地址。
     */
    @PostMapping("/api/members/{memberId}/addresses")
    public AddressResponse createAddress(@PathVariable Long memberId,
                                         @Valid @RequestBody CreateAddressRequest request) {
        log.info("创建收货地址：memberId={}", memberId);
        Address address = RecordUtils.copy(request, Address.class, Map.of("memberId", memberId));
        Address saved = addressService.createAddress(memberId, address);
        return toResponse(saved);
    }

    /**
     * 查询成员全部地址。
     */
    @GetMapping("/api/members/{memberId}/addresses")
    public List<AddressResponse> listAddresses(@PathVariable Long memberId) {
        return addressService.listAddresses(memberId).stream()
                .map(AddressController::toResponse)
                .toList();
    }

    /**
     * 修改收货地址（全量覆盖）。
     */
    @PutMapping("/api/members/{memberId}/addresses/{addressId}")
    public AddressResponse updateAddress(@PathVariable Long memberId,
                                         @PathVariable Long addressId,
                                         @Valid @RequestBody CreateAddressRequest request) {
        log.info("修改收货地址：memberId={}, addressId={}", memberId, addressId);
        Address address = RecordUtils.copy(request, Address.class,
                Map.of("id", addressId, "memberId", memberId));
        Address updated = addressService.updateAddress(memberId, addressId, address);
        return toResponse(updated);
    }

    /**
     * 设置默认地址。
     */
    @PutMapping("/api/members/{memberId}/addresses/{addressId}/default")
    public AddressResponse setDefault(@PathVariable Long memberId,
                                      @PathVariable Long addressId) {
        log.info("设置默认地址：memberId={}, addressId={}", memberId, addressId);
        Address address = addressService.setDefault(memberId, addressId);
        return toResponse(address);
    }

    private static AddressResponse toResponse(Address a) {
        return RecordUtils.copy(a, AddressResponse.class);
    }
}
