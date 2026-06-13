package com.dong.service;

import com.dong.common.Result;
import com.dong.dto.AddressDTO;
import com.dong.entity.Address;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 收货地址表 服务类
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
public interface IAddressService extends IService<Address> {

    Result<List<Address>> getAddressList();

    Result<String> postAddress(AddressDTO addressDTO);

    Result<String> deleteAddress(Long addressId);

    Result<String> setDefault(Long id);
}
