package com.dong.producer.address;

import lombok.Data;

@Data
public class RemoveAddressMessage {

    private Long addressId;
    private Long userId;
}
