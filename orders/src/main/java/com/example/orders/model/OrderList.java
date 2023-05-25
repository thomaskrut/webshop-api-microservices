package com.example.orders.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OrderList {

    private List<CustomerOrder> orderList;

    public OrderList(List<CustomerOrder> orderList) {
        this.orderList = orderList;
    }

}
