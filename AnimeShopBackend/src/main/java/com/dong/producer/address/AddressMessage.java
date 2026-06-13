package com.dong.producer.address;

import com.dong.dto.AddressDTO;
import lombok.Data;

@Data
public class AddressMessage {

    private Long userId; // 用户ID
    private AddressDTO addressDTO;
    private String operation; //执行的方法，用来区分执行什么操作
}
