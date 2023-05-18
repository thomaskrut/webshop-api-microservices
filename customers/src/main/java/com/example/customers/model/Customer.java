package com.example.customers.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity


public class Customer {

    @Id
    @GeneratedValue
    private long id;

    private String firstName;
    private String lastName;
    private String ssn;

    public Customer(String firstName, String lastName, String ssn) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
    }

}
