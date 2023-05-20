package com.example.orders.controller;

import com.example.orders.model.*;
import com.example.orders.repository.CustomerOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class OrderController {

    private final CustomerOrderRepository customerOrderRepository;
    private final RestTemplate restTemplate;

    public OrderController(CustomerOrderRepository customerOrderRepository, RestTemplate restTemplate) {
        this.customerOrderRepository = customerOrderRepository;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/orders")
    public List<CustomerOrder> getAllOrders() {
        return customerOrderRepository.findAll();
    }

    @GetMapping("/orders/{customerId}")
    public List<CustomerOrder> getOrdersByCustomerId(@PathVariable long customerId) {
        return customerOrderRepository.findByCustomerId(customerId);
    }

    @PostMapping("/orders")
    public ResponseEntity<String> createOrder(@RequestBody NewOrderRequest req) {

        Customer customer = restTemplate.getForObject("http://customers:8080/customers/" + req.getCustomerId(), Customer.class);
        assert customer != null;
        if (customer.getFirstName().equals("Customer not found")) {
            return ResponseEntity.badRequest().body("Customer not found");
        }

        CustomerOrder newCustomerOrder = new CustomerOrder(req.getCustomerId());
        customerOrderRepository.save(newCustomerOrder);
        return ResponseEntity.ok("Order " + newCustomerOrder.getId() + " created for customer " + req.getCustomerId() + ".");
    }

    @PostMapping("/orders/{orderId}")
    public ResponseEntity<String> addOrderEntry(@PathVariable long orderId, @RequestBody NewOrderEntryRequest req) {

        if (customerOrderRepository.findById(orderId).isEmpty()) {
            return ResponseEntity.badRequest().body("Order not found");
        }

        CustomerOrder currentOrder = customerOrderRepository.findById(orderId).get();
        currentOrder.addOrderEntry(new OrderEntry(req.getItemId(), req.getQuantity()));
        customerOrderRepository.save(currentOrder);
        return ResponseEntity.ok("Entry added to order " + orderId);

    }

}


