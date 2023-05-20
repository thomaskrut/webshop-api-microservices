package com.example.customers.controller;
import com.example.customers.model.Customer;
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

    @GetMapping("/customers")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @GetMapping("/customers/{id}")
    public Customer getCustomerById(@PathVariable long id) {
        System.out.println("Customer ID: " + id);
        if (customerRepository.findById(id).isEmpty()) return new Customer(-1, "Customer not found", "Customer not found", "Customer not found");
        return customerRepository.findById(id).get();
    }

    @PostMapping("/customers")
    public ResponseEntity<String> createCustomer(@RequestBody Customer customer) {

        if (customer.getFirstName().isBlank() || customer.getLastName().isBlank()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid name");

        if (customer.getSsn().trim().length() != 10) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid SSN");

        if (customerRepository.findAll().stream().anyMatch(c -> c.getSsn().equals(customer.getSsn()))) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("SSN already in database");

        customerRepository.save(customer);

        return ResponseEntity.ok("Customer added to database");
    }

}
