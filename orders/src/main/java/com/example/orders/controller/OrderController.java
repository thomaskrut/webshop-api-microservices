package com.example.orders.controller;

import com.example.orders.model.*;
import com.example.orders.repository.CustomerOrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@RestController
@Validated
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

    @Operation(summary = "Get all orders")
    @GetMapping("/")
    public OrderList getAllOrders() {
        return new OrderList(customerOrderRepository.findAll());
    }

    @Operation(summary = "Get order by customer ID")
    @GetMapping("/{customerId}")
    public List<CustomerOrder> getOrdersByCustomerId(@PathVariable long customerId) {
        return customerOrderRepository.findByCustomerId(customerId);
    }

    @Operation(summary = "Add order")
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    @Retryable(noRetryFor = ObjectNotFoundException.class, maxAttempts = 4, backoff = @Backoff(delay = 500, multiplier = 2))
    public CustomerOrder createOrder(@RequestParam long customerId) {
        try {
            restTemplate.getForEntity(customersServiceUrl + customerId, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ObjectNotFoundException("Customer with ID " + customerId + " does not exist");
        }

        CustomerOrder newCustomerOrder = new CustomerOrder(customerId);
        customerOrderRepository.save(newCustomerOrder);
        return newCustomerOrder;
    }

    @Recover
    public CustomerOrder createOrderRecovery(Exception e) throws Exception {
        throw e;
    }


    @Operation(summary = "Add order entry")
    @PostMapping("/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Retryable(noRetryFor = ObjectNotFoundException.class, maxAttempts = 4, backoff = @Backoff(delay = 500, multiplier = 2))
    public String addOrderEntry(@PathVariable long orderId, @RequestBody NewOrderEntryRequest req) {

        orderExists(orderId).orElseThrow(() -> new ObjectNotFoundException("Order with ID " + orderId + " does not exist"));

        try {
            restTemplate.getForEntity(itemsServiceUrl + req.getItemId(), Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ObjectNotFoundException("Item with ID " + req.getItemId() + " does not exist");
        }

        CustomerOrder currentOrder = customerOrderRepository.findById(orderId).get();
        currentOrder.addOrderEntry(new OrderEntry(req.getItemId(), req.getQuantity()));
        customerOrderRepository.save(currentOrder);
        return "Entry added to order " + orderId;
    }

    @Recover
    public String addOrderEntryRecovery(Exception e) throws Exception {
        throw e;
    }

    @Operation(summary = "Add order entry")
    @PutMapping("/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Retryable(noRetryFor = ObjectNotFoundException.class, maxAttempts = 4, backoff = @Backoff(delay = 500, multiplier = 2))
    public String adjustOrderEntry(@PathVariable long orderId, @RequestBody NewOrderEntryRequest req) {

        orderExists(orderId).orElseThrow(() -> new ObjectNotFoundException("Order with ID " + orderId + " does not exist"));

        try {
            restTemplate.getForEntity(itemsServiceUrl + req.getItemId(), Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ObjectNotFoundException("Item with ID " + req.getItemId() + " does not exist");
        }
        CustomerOrder currentOrder = customerOrderRepository.findById(orderId).get();
        currentOrder.addOrderEntry(new OrderEntry(req.getItemId(), req.getQuantity()));
        customerOrderRepository.save(currentOrder);
        return "Entry added to order " + orderId;
    }

    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    @ExceptionHandler(ResourceAccessException.class)
    public String handleTimeoutException(ResourceAccessException ex) {
        return "Service unavailable";
    }

    private Optional<CustomerOrder> orderExists(long orderId) {
        return customerOrderRepository.findById(orderId);
    }




}