package com.example.orders.controller;

import com.example.orders.model.*;
import com.example.orders.repository.CustomerOrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class OrderController {

    private final CustomerOrderRepository customerOrderRepository;
    private final RestTemplate restTemplate;

    @Value("${customers-service.url}")
    private String customersServiceUrl;

    @Value("${items-service.url}")
    private String itemsServiceUrl;

    public OrderController(CustomerOrderRepository customerOrderRepository, RestTemplate restTemplate) {
        this.customerOrderRepository = customerOrderRepository;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public List<CustomerOrder> getAllOrders() {
        return customerOrderRepository.findAll();
    }

    @GetMapping("/{customerId}")
    public List<CustomerOrder> getOrdersByCustomerId(@PathVariable long customerId) {
        return customerOrderRepository.findByCustomerId(customerId);
    }

    @PostMapping("/")
    public ResponseEntity<String> createOrder(@RequestParam long customerId) {

        System.out.println("Customer ID in order request: " + customerId);
        Customer customer = restTemplate.getForObject(customersServiceUrl + customerId, Customer.class);
        assert customer != null;
        if (customer.getFirstName().equals("Customer not found")) {
            return ResponseEntity.badRequest().body("Customer not found");
        }

        CustomerOrder newCustomerOrder = new CustomerOrder(customerId);
        customerOrderRepository.save(newCustomerOrder);
        return ResponseEntity.ok("Order " + newCustomerOrder.getId() + " created for customer " + customerId + ".");
    }

    @PostMapping("/{orderId}")
    public ResponseEntity<String> addOrderEntry(@PathVariable long orderId, @RequestBody NewOrderEntryRequest req) {

        if (customerOrderRepository.findById(orderId).isEmpty()) {
            return ResponseEntity.badRequest().body("Order not found");
        }

        Item item = restTemplate.getForObject(itemsServiceUrl + req.getItemId(), Item.class);
        assert item != null;
        if (item.getName().equals("Item not found")) {
            return ResponseEntity.badRequest().body("Item not found");
        }

        CustomerOrder currentOrder = customerOrderRepository.findById(orderId).get();
        currentOrder.addOrderEntry(new OrderEntry(req.getItemId(), req.getQuantity()));
        customerOrderRepository.save(currentOrder);
        return ResponseEntity.ok("Entry added to order " + orderId);
    }
}