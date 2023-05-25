package com.example.web.model;

import lombok.AllArgsConstructor;
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
