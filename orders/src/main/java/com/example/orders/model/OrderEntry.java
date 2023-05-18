package com.example.orders.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class OrderEntry {

    @Id
    @GeneratedValue
    private long id;

    private int quantity;

    private long itemId;

    public OrderEntry(long itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

}
