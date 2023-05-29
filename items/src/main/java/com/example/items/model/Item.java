package com.example.items.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
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
    @Size(min = 2, max = 20, message = "Item name must be between 2 and 20 characters")
    private String name;

    @Min(value = 0, message = "Price must be positive")
    @Max(value = 999999, message = "Price must be less than 999999")
    private double price;

    public Item(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getPriceAsString(){
        return String.format("%.2f", price);
    }

}
