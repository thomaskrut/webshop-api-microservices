package com.example.orders.controller;

import com.example.orders.model.*;
import com.example.orders.repository.CustomerOrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

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
    public OrderList getAllOrders() {
        return new OrderList(customerOrderRepository.findAll());
    }

    @GetMapping("/{customerId}")
    public List<CustomerOrder> getOrdersByCustomerId(@PathVariable long customerId) {
        return customerOrderRepository.findByCustomerId(customerId);
    }

    @PostMapping("/")
    public CustomerOrder createOrder(@RequestParam long customerId) {

        try {
            restTemplate.getForEntity(customersServiceUrl + customerId, Object.class);
        } catch (HttpClientErrorException e) {

            return null;
        }

        CustomerOrder newCustomerOrder = new CustomerOrder(customerId);
        customerOrderRepository.save(newCustomerOrder);
        return newCustomerOrder;
    }

    @PostMapping("/{orderId}")
    public String addOrderEntry(@PathVariable long orderId, @RequestBody NewOrderEntryRequest req) {

        if (customerOrderRepository.findById(orderId).isEmpty()) {
            return "Order not found";
        }

        try {
            restTemplate.getForEntity(itemsServiceUrl + req.getItemId(), Object.class);
        } catch (HttpClientErrorException e) {
            return "Item not found";
        }

        CustomerOrder currentOrder = customerOrderRepository.findById(orderId).get();
        currentOrder.addOrderEntry(new OrderEntry(req.getItemId(), req.getQuantity()));
        customerOrderRepository.save(currentOrder);
        return "Entry added to order " + orderId;
    }


    @PutMapping("/{orderId}")
    public String adjustOrderEntry(@PathVariable long orderId, @RequestBody NewOrderEntryRequest req) {
        if (customerOrderRepository.findById(orderId).isEmpty()) {
            return "Order not found";
        }

        try {
            restTemplate.getForEntity(itemsServiceUrl + req.getItemId(), Object.class);
        } catch (HttpClientErrorException e) {
            return "Item not found";
        }

        CustomerOrder currentOrder = customerOrderRepository.findById(orderId).get();
        currentOrder.addOrderEntry(new OrderEntry(req.getItemId(), req.getQuantity()));
        customerOrderRepository.save(currentOrder);
        return "Entry added to order " + orderId;

    }

}