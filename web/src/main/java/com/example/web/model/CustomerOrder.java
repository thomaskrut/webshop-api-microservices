package com.example.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor


public class CustomerOrder {

    private long id;

    private LocalDate date;

    private List<OrderEntry> orderEntries = new ArrayList<>();

    private long customerId;

    public CustomerOrder(LocalDate date) {
        this.date = date;
    }

    public CustomerOrder(long customerId) {
        this.customerId = customerId;
        this.date = LocalDate.now();
    }


    public Double getTotal() {
        return orderEntries.stream().mapToDouble(OrderEntry::getTotalPrice).sum();
    }

    public String getTotalString() {
        return String.format("%.2f", orderEntries.stream().mapToDouble(OrderEntry::getTotalPrice).sum());
    }


    public int getNumberOfItems() {
        return orderEntries.stream().mapToInt(OrderEntry::getQuantity).sum();
    }
}
