package com.dong.service.impl;

import com.dong.common.RabbitMQProducer;
import com.dong.common.Result;
import com.dong.dto.AddressDTO;
import com.dong.entity.Address;
import com.dong.mapper.AddressMapper;
import com.dong.producer.address.AddressMessage;
import com.dong.producer.address.RemoveAddressMessage;
import com.dong.service.IAddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.utils.RabbitMQConstants;
import com.dong.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 收货地址表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {

    private final RabbitMQProducer rabbitMQProducer;

    /**
     * 获取收货地址列表
     */
    @Override
    public Result<List<Address>> getAddressList() {
        // 1. 获取登录用户id
        Long userId = SecurityUtils.getCurrentUser().getId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        // 2. 查询
        List<Address> addressList = lambdaQuery()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .list();
        // 3. 返回，空集合也正常返回
        return Result.success(addressList);
    }

    /**
     * 添加收货地址
     */
    @Override
    public Result<String> postAddress(AddressDTO addressDTO) {
        // 1. 获取登录用户id和地址对象
        Long userId = SecurityUtils.getCurrentUser().getId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        // 2. 构建消息体
        AddressMessage addressMessage = new AddressMessage();
        addressMessage.setUserId(userId);
        addressMessage.setAddressDTO(addressDTO);
        addressMessage.setOperation("add");
        // 3. 判断是否重复
        // 3.1 拼接详细对象
        String detail = addressDTO.getProvince() + addressDTO.getCity() +
                addressDTO.getDistrict() + addressDTO.getDetail();
        boolean exists = lambdaQuery().eq(Address::getUserId, userId)
                .eq(Address::getAddress, detail)
                .exists();
        // 3.2 判断是否正确
        if (exists) {
            return Result.error("该地址已存在");
        }
        // 4. 发送消息，时间戳做id做幂等性
        Address address = new Address();
        String businessId = "address:add" + userId + ":"
                + address.getId() + ":" + System.currentTimeMillis();
        rabbitMQProducer.send(
                RabbitMQConstants.ADDRESS_EXCHANGE,
                RabbitMQConstants.ADDRESS_ROUTING,
                addressMessage,
                businessId);
        return Result.success("添加地址成功");
    }

    /**
     * 删除收货地址
     */
    @Override
    public Result<String> deleteAddress(Long addressId) {
        // 1. 获取登录用户id
        Long userId = SecurityUtils.getCurrentUser().getId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        // 2. 判断该地址是否存在
        boolean exists = lambdaQuery()
                .eq(Address::getUserId, userId)
                .eq(Address::getId, addressId)
                .exists();
        if (!exists) {
            return Result.error("地址不存在");
        }
        // 3. 构建消息体
        RemoveAddressMessage removeAddressMessage = new RemoveAddressMessage();
        removeAddressMessage.setUserId(userId);
        removeAddressMessage.setAddressId(addressId);
        // 4. 发送消息
        String businessId = "address:remove" + userId + ":"
                + addressId + ":" + System.currentTimeMillis();
        rabbitMQProducer.send(
                RabbitMQConstants.ADDRESS_EXCHANGE,
                RabbitMQConstants.REMOVE_ADDRESS_ROUTING,
                removeAddressMessage,
                businessId
        );
        return Result.success("删除地址成功");
    }

    @Override
    public Result<String> setDefault(Long id) {
        // 1. 获取登录用户id
        Long userId = SecurityUtils.getCurrentUser().getId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        // 2. 查询该地址是否属于该用户
        Address address = lambdaQuery()
                .eq(Address::getUserId, userId)
                .eq(Address::getId, id)
                .one();
        if (address == null) {
            return Result.error("地址Id不对");
        }
        // 3. 设置默认地址
        boolean update = lambdaUpdate()
                .eq(Address::getUserId, userId)
                .eq(Address::getId, id)
                .set(Address::getIsDefault, 1)
                .update();
        if (!update) {
            return Result.error("设置默认地址失败");
        }
        return Result.success("设置默认地址成功");
    }
}
