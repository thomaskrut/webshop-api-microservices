package com.example.orders.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity


public class CustomerOrder {

    @Id
    @GeneratedValue
    private long id;

    private LocalDate date;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    @Cascade(CascadeType.ALL)
    private List<OrderEntry> orderEntries = new ArrayList<>();

    private long customerId;

    public CustomerOrder(LocalDate date) {
        this.date = date;
    }

    public void addOrderEntry(OrderEntry orderEntry) {

        orderEntries.stream()
                .filter(e -> e.getItemId() == orderEntry.getItemId())
                .findFirst().ifPresentOrElse(e -> e.setQuantity(e.getQuantity() + orderEntry.getQuantity()), () -> orderEntries.add(orderEntry));

        orderEntries.stream().filter(e -> e.getQuantity() <= 0).toList().forEach(orderEntries::remove);


    }

    public Double getTotal() {
        //return orderEntries.stream().mapToDouble(e -> e.getItem().getPrice() * e.getQuantity()).sum();
        return null;
    }

    public String getTotalString() {
        //return String.format("%.2f", orderEntries.stream().mapToDouble(e -> e.getItem().getPrice() * e.getQuantity()).sum());
        return null;
    }

    public int getNumberOfItems() {
        return orderEntries.stream().mapToInt(OrderEntry::getQuantity).sum();
    }



}