package com.example.customers.controller;
import com.example.customers.model.Customer;
import com.example.customers.model.CustomerList;
import com.example.customers.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CustomerController {


    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/")
    public CustomerList getAllCustomers() {
        return new CustomerList(customerRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable long id) {
        System.out.println("Customer ID: " + id);
        if (customerRepository.findById(id).isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        return ResponseEntity.ok(customerRepository.findById(id).get());
    }

    @PostMapping("/")
    public String createCustomer(@RequestBody Customer customer) {

        if (customer.getFirstName().isBlank() || customer.getLastName().isBlank()) return "Invalid name";

        if (customer.getSsn().trim().length() != 10) return "Invalid SSN";

        if (customerRepository.findAll().stream().anyMatch(c -> c.getSsn().equals(customer.getSsn()))) return "SSN already in database";

        customerRepository.save(customer);

        return "Customer added to database";
    }

}
