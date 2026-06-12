package com.dong.controller;


import com.dong.common.Result;
import com.dong.dto.AddressDTO;
import com.dong.entity.Address;
import com.dong.service.IAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址表 前端控制器
 * @author author
 * @since 2026-05-03
 */
@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService addressService;

    @GetMapping
    public Result<List<Address>> getAddressList() {
        return addressService.getAddressList();
    }

    @PostMapping
    public Result<String> postAddress(@RequestBody AddressDTO  addressDTO) {
        return addressService.postAddress(addressDTO);
    }

    @DeleteMapping("/{addressId}")
    public Result<String> deleteAddress(@PathVariable Long addressId) {
        return addressService.deleteAddress(addressId);
    }

    @PutMapping("/{id}/default")
    public Result<String> setDefault(@PathVariable Long id) {
        return addressService.setDefault(id);
    }
}
