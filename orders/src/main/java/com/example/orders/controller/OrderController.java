package com.example.orders.controller;

import com.example.orders.model.*;
import com.example.orders.repository.CustomerOrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.management.ServiceNotFoundException;
import java.io.IOException;
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
    public OrderList getAllOrders() {
        return new OrderList(customerOrderRepository.findAll());
    }

    @GetMapping("/{customerId}")
    public List<CustomerOrder> getOrdersByCustomerId(@PathVariable long customerId) {
        return customerOrderRepository.findByCustomerId(customerId);
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    @Retryable(exclude = HttpClientErrorException.NotFound.class, maxAttempts=5, backoff = @Backoff(delay = 2000, multiplier = 2))
    public CustomerOrder createOrder(@RequestParam long customerId) {
        try {
           restTemplate.getForEntity(customersServiceUrl + customerId, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ObjectNotFoundException("Customer with ID " + customerId + " does not exist");
        }
        catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode());
        }

        CustomerOrder newCustomerOrder = new CustomerOrder(customerId);
        customerOrderRepository.save(newCustomerOrder);
        return newCustomerOrder;
    }

    @Recover
    public void connectionException(Exception e) throws Exception {
        // Insert backup method
        System.out.println("Retry failure");
        throw e;
    }

    @PostMapping("/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    public String addOrderEntry(@PathVariable long orderId, @RequestBody NewOrderEntryRequest req) {

        if (customerOrderRepository.findById(orderId).isEmpty()) {
            return "Order not found";
        }
        try {
            restTemplate.getForEntity(itemsServiceUrl + req.getItemId(), Object.class);
        }  catch (HttpClientErrorException.NotFound e) {
            throw new ObjectNotFoundException("Item with ID " + req.getItemId() + " does not exist");
        }
        catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode());
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
        }
        catch (HttpClientErrorException.NotFound e) {
            throw new ObjectNotFoundException("Item with ID " + req.getItemId() + " does not exist");
            }
        catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode());
        }
        CustomerOrder currentOrder = customerOrderRepository.findById(orderId).get();
        currentOrder.addOrderEntry(new OrderEntry(req.getItemId(), req.getQuantity()));
        customerOrderRepository.save(currentOrder);
        return "Entry added to order " + orderId;
    }

}