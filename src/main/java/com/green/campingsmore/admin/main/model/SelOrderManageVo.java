package com.green.campingsmore.admin.main.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelOrderManageVo {
    private String orderDate;
    private Long iorder;
    private String name;
    private Integer orderPrice;
    private Integer totalPrice;
    private Integer shippingStatus;
    private String refundStatus;
}
