package com.example.items.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class Item {

    @Id
    @GeneratedValue
    private long id;

    @NotBlank(message = "Item name is mandatory")
    private String name;

    @Positive(message = "Price must be positive")
    private double price;

    public Item(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getPriceAsString(){
        return String.format("%.2f", price);
    }

}
