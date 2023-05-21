package com.example.orders.controller;

import com.example.orders.model.CustomerOrder;
import com.example.orders.model.NewOrderEntryRequest;
import com.example.orders.model.NewOrderRequest;
import com.example.orders.model.OrderEntry;
import com.example.orders.repository.CustomerOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    private final CustomerOrderRepository customerOrderRepository;

    public OrderController(CustomerOrderRepository customerOrderRepository) {
        this.customerOrderRepository = customerOrderRepository;
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
    public ResponseEntity<String> createOrder(@RequestBody NewOrderRequest req) {
        System.out.println(req.getCustomerId());
        CustomerOrder newCustomerOrder = new CustomerOrder(req.getCustomerId());
        customerOrderRepository.save(newCustomerOrder);
        return ResponseEntity.ok("Order " + newCustomerOrder.getId() + " created for customer " + req.getCustomerId() + ".");
    }

    @PostMapping("/{orderId}")
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


