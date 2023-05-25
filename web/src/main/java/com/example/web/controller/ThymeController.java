package com.example.web.controller;

import com.example.web.model.*;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller


public class ThymeController {

    @Value("${customers-service.url}")
    private String customersServiceUrl;

    @Value("${items-service.url}")
    private String itemsServiceUrl;

    @Value("${orders-service.url}")
    private String ordersServiceUrl;

    private final RestTemplate restTemplate;

    public ThymeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping({"/index", "/", ""})
    public String getIndex() {
        return "index.html";
    }

    @RequestMapping({"/customers", "/addorder"})
    public String getAllCustomers(Model model) {

        CustomerList customers = restTemplate.getForObject(customersServiceUrl, CustomerList.class);
        OrderList orders = restTemplate.getForObject(ordersServiceUrl, OrderList.class);

        assert customers != null;
        assert orders != null;
        customers.getCustomerList().forEach(c -> {
            c.setOrders(orders.getOrderList().stream().filter(o -> o.getCustomerId() == c.getId()).count());
        });

        model.addAttribute("allCustomersList", customers.getCustomerList());
        model.addAttribute("firstNameTitle", "firstName");
        model.addAttribute("lastNameTitle", "lastName");
        model.addAttribute("ssnTitle", "ssn");
        model.addAttribute("customerTitle", "ALL CUSTOMERS");
        return "allcustomers.html";
    }

    @RequestMapping("/items")
    public String getAllItems(Model model) {

        ItemList items = restTemplate.getForObject(itemsServiceUrl, ItemList.class);
        assert items != null;

        model.addAttribute("allItemsList", items.getItemList());
        model.addAttribute("nameTitle", "name");
        model.addAttribute("priceTitle", "price");
        model.addAttribute("itemTitle", "ALL ITEMS");
        return "allitems.html";
    }



    @RequestMapping("/orders")
    public String getAllOrders(
            @RequestParam(required = false, defaultValue = "-1") long customerId,
            @RequestParam(required = false, defaultValue = "id") String sortby,
            @RequestParam(required = false, defaultValue = "desc") String order,
            Model model) {

        OrderList orders = restTemplate.getForObject(ordersServiceUrl, OrderList.class);
        assert orders != null;
        ItemList items = restTemplate.getForObject(itemsServiceUrl, ItemList.class);
        assert items != null;

        orders.getOrderList().forEach(o -> {
            o.getOrderEntries().forEach(oe -> {
                Item item = items.getItemList().stream().filter(i -> i.getId() == oe.getItemId()).findFirst().orElse(null);
                assert item != null;
                oe.setPrice(item.getPrice());
            });
        });

        if (customerId > 0) {
            orders.setOrderList(orders.getOrderList().stream().filter(o -> o.getCustomerId() == customerId).collect(Collectors.toList()));
            model.addAttribute("orderTitle", "ORDERS FOR CUSTOMER ID: " + customerId);
        } else {
            model.addAttribute("orderTitle", "ALL ORDERS");
        }

        switch (sortby) {
            case "id" -> orders.getOrderList().sort((o1, o2) -> (int) (o1.getId() - o2.getId()));
            case "date" -> orders.getOrderList().sort(Comparator.comparing(CustomerOrder::getDate));
            case "total" -> orders.getOrderList().sort(Comparator.comparing(CustomerOrder::getTotal));
            case "items" -> orders.getOrderList().sort(Comparator.comparing(CustomerOrder::getNumberOfItems));
            case "customer" -> orders.getOrderList().sort((o1, o2) -> (int) (o1.getCustomerId() - o2.getCustomerId()));
        }

        if (order.equals("desc")) {
            Collections.reverse(orders.getOrderList());
        }

        String newOrder = (order.equals("asc")) ? "desc" : "asc";

        model.addAttribute("customerId", customerId);
        model.addAttribute("sortby", sortby);
        model.addAttribute("newOrder", newOrder);
        model.addAttribute("allOrdersList", orders.getOrderList());
        model.addAttribute("idTitle", "id");
        model.addAttribute("dateTitle", "date");
        return "allorders.html";
    }


    @RequestMapping("/order")
    public String getOrder(@RequestParam long orderId, Model model) {
        OrderList orders = restTemplate.getForObject(ordersServiceUrl, OrderList.class);
        assert orders != null;
        CustomerOrder order = orders.getOrderList().stream().filter(o -> o.getId() == orderId).findFirst().get();

        ItemList items = restTemplate.getForObject(itemsServiceUrl, ItemList.class);
        assert items != null;


            order.getOrderEntries().forEach(oe -> {
                Item item = items.getItemList().stream().filter(i -> i.getId() == oe.getItemId()).findFirst().orElse(null);
                assert item != null;
                oe.setPrice(item.getPrice());
                oe.setItemName(item.getName());

            });

        model.addAttribute("items", items.getItemList());
        model.addAttribute("order", order);
        model.addAttribute("orderTitle", "ORDER ID: " + orderId);
        return "order.html";
    }


    @RequestMapping("/confirmitem")
    public String confirmItem(Model model, Item i, String message) {
        model.addAttribute("item", i);
        model.addAttribute("message", message);
        return "confirmitem.html";
    }

    @GetMapping("/addcustomer")
    public String addCustomerForm() {
        return "addcustomer.html";
    }

    @GetMapping("/registercustomer")
    public String addCustomer(@RequestParam String fname, @RequestParam String lname,
                              @RequestParam String ssn, Model model) {
        Customer c = new Customer();
        c.setFirstName(fname);
        c.setLastName(lname);
        c.setSsn(ssn);
        restTemplate.postForObject(customersServiceUrl, c, String.class);
        return getAllCustomers(model);
    }


    @GetMapping("/additem")
    public String addItemForm(Model model) {
        model.addAttribute("item", new Item());
        return "additem.html";
    }

    @GetMapping("/registeritem")
    public String addItem(@RequestParam String name, @RequestParam Double price, Model model) {

        if (name.isBlank() || price < 0) return confirmItem(model, null, "Error: invalid data");
        Item i = new Item();
        i.setName(name);
        i.setPrice(price);
        restTemplate.postForObject(itemsServiceUrl, i, String.class);
        return confirmItem(model, i, "Item successfully added");
    }
/*
    */

    @GetMapping("/adjustorder")
    public String adjustOrder(@RequestParam long orderId,
                              @RequestParam String itemId,
                              @RequestParam int quantity,
                              Model model) {

        long itemIdLong;
        try {
            itemIdLong = Long.parseLong(itemId);
        } catch (NumberFormatException e) {
            return getOrder(orderId, model);
        }

        OrderList orders = restTemplate.getForObject(ordersServiceUrl, OrderList.class);
        assert orders != null;
        CustomerOrder order = orders.getOrderList().stream().filter(o -> o.getId() == orderId).findFirst().get();

        ItemList items = restTemplate.getForObject(itemsServiceUrl, ItemList.class);
        assert items != null;
        Item item = items.getItemList().stream().filter(i -> i.getId() == itemIdLong).findFirst().get();

        restTemplate.put(ordersServiceUrl + "/" + orderId, new NewOrderEntryRequest(item.getId(), quantity));

        return getOrder(orderId, model);
    }


    @GetMapping("/neworder")
    public String newOrder(@RequestParam long customerId, Model model) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("customerId", Long.toString(customerId));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        restTemplate.postForObject(ordersServiceUrl, request, String.class);

        return getAllOrders(customerId, "id", "desc", model);

    }
}
