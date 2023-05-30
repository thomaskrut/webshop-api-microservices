package com.example.customers.controller;

import com.example.customers.model.Customer;
import com.example.customers.model.CustomerList;
import com.example.customers.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Validated
public class CustomerController {


    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Operation(summary = "Get all customers")
    @GetMapping("/")
    public CustomerList getAllCustomers() {
        return new CustomerList(customerRepository.findAll());
    }

    @Operation(summary = "Get customer by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable long id) {
        System.out.println("Customer ID: " + id);
        if (customerRepository.findById(id).isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        return ResponseEntity.ok(customerRepository.findById(id).get());
    }

    @Operation(summary = "Add customer")
    @PostMapping("/")
    public String createCustomer(@Valid @RequestBody Customer customer) {

        if (customerRepository.findAll().stream().anyMatch(c -> c.getSsn().equals(customer.getSsn())))
            throw new IllegalArgumentException("Customer with this SSN already exists");
        customerRepository.save(customer);
        return "Customer added to database";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationExceptions(MethodArgumentNotValidException ex) {

        StringBuilder result = new StringBuilder();

        ex.getBindingResult().getAllErrors().forEach(e -> {
            result.append(e.getDefaultMessage());
            result.append(", ");
        });

        return result.toString();

    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentExceptions(IllegalArgumentException ex) {

        return ex.getMessage();

    }

}
