package com.platformcommons.identity.api;

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
        Address address = new Address(
                null,
                memberId,
                request.label(),
                request.receiverName(),
                request.phone(),
                request.province(),
                request.city(),
                request.district(),
                request.detail(),
                request.latitude(),
                request.longitude(),
                request.isDefault(),
                null,
                null
        );
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
        return new AddressResponse(
                a.id(), a.memberId(), a.label(), a.receiverName(), a.phone(),
                a.province(), a.city(), a.district(), a.detail(),
                a.latitude(), a.longitude(), a.isDefault(),
                a.createdAt(), a.updatedAt()
        );
    }
}
