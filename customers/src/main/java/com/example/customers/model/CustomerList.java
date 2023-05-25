package com.example.customers.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CustomerList {

    List<Customer> customerList;

    public CustomerList(List<Customer> customerList) {
        this.customerList = customerList;
    }

}
