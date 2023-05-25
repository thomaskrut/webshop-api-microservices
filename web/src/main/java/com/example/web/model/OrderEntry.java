package com.example.web.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor


public class OrderEntry {

    private long id;

    private int quantity;

    private long itemId;

    private String itemName;

    private double price;

    public OrderEntry(long itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public double getTotalPrice(){
        return quantity * price;
    }

    public String getPriceAsString(){
        return String.format("%.2f", price);
    }

}
