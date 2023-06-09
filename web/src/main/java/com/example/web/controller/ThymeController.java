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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
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

    private CustomerList getAllCustomers() {
        CustomerList customers = restTemplate.getForObject(customersServiceUrl, CustomerList.class);
        return Objects.requireNonNullElseGet(customers, CustomerList::new);
    }

    private ItemList getAllItems() {
        ItemList items = restTemplate.getForObject(itemsServiceUrl, ItemList.class);
        return Objects.requireNonNullElseGet(items, ItemList::new);
    }

    private OrderList getAllOrders() {
        OrderList orders = restTemplate.getForObject(ordersServiceUrl, OrderList.class);
        return Objects.requireNonNullElseGet(orders, OrderList::new);
    }

    private String getCurrentRole(Principal principal) {
        if (principal != null) {
            return principal.getName();
        } else {
            return "ej inloggad";
        }
    }

    @RequestMapping({"/index", "/", ""})
    public String getIndex(Model model, Principal principal) {
        model.addAttribute("currentRole", getCurrentRole(principal));

        return "index.html";
    }

    @RequestMapping({"/customers", "/addorder"})
    public String getAllCustomers(Model model, Principal principal) {

        CustomerList customers = getAllCustomers();
        OrderList orders = getAllOrders();

        customers.getCustomerList().forEach(c -> {
            c.setOrders(orders.getOrderList().stream().filter(o -> o.getCustomerId() == c.getId()).count());
        });

        model.addAttribute("currentRole", getCurrentRole(principal));
        model.addAttribute("allCustomersList", customers.getCustomerList());
        model.addAttribute("firstNameTitle", "firstName");
        model.addAttribute("lastNameTitle", "lastName");
        model.addAttribute("ssnTitle", "ssn");
        model.addAttribute("customerTitle", "ALL CUSTOMERS");
        return "allcustomers.html";
    }

    @RequestMapping("/items")
    public String getAllItems(Model model, Principal principal) {

        ItemList items = getAllItems();

        model.addAttribute("currentRole", getCurrentRole(principal));
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
            Model model, Principal principal) {

        OrderList orders = getAllOrders();
        ItemList items = getAllItems();

        orders.getOrderList().forEach(o -> {
            o.getOrderEntries().forEach(oe -> {
                Item item = items.getItemList().stream().filter(i -> i.getId() == oe.getItemId()).findFirst().orElse(null);
                assert item != null;
                oe.setPrice(item.getPrice());
            });
        });

        if (customerId > 0) {
            orders.setOrderList(orders.getOrderList().stream().filter(o -> o.getCustomerId() == customerId).collect(Collectors.toList()));
            if (orders.getOrderList().isEmpty()) {
                model.addAttribute("orderTitle", "No orders found");
            } else {
                model.addAttribute("orderTitle", "ORDERS FOR CUSTOMER ID: " + customerId);
            }

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

        model.addAttribute("currentRole", getCurrentRole(principal));
        model.addAttribute("customerId", customerId);
        model.addAttribute("sortby", sortby);
        model.addAttribute("newOrder", newOrder);
        model.addAttribute("allOrdersList", orders.getOrderList());
        model.addAttribute("idTitle", "id");
        model.addAttribute("dateTitle", "date");
        return "allorders.html";
    }


    @RequestMapping("/order")
    public String getOrder(@RequestParam long orderId, String message, Model model, Principal principal) {


        OrderList orders = getAllOrders();

        CustomerOrder order = orders.getOrderList().stream().filter(o -> o.getId() == orderId).findFirst().orElse(null);
        ItemList items = getAllItems();

        if (order == null) {
            model.addAttribute("currentRole", getCurrentRole(principal));
            model.addAttribute("items", items.getItemList());
            model.addAttribute("order", new CustomerOrder());
            model.addAttribute("orderTitle", "Order not found");
            return "order.html";
        }

        if (message == null) message = "ORDER: " + orderId;

        order.getOrderEntries().forEach(oe -> {
            Item item = items.getItemList().stream().filter(i -> i.getId() == oe.getItemId()).findFirst().orElse(new Item());
            oe.setPrice(item.getPrice());
            oe.setItemName(item.getName());

        });

        model.addAttribute("currentRole", getCurrentRole(principal));
        model.addAttribute("items", items.getItemList());
        model.addAttribute("order", order);
        model.addAttribute("orderTitle", message);
        return "order.html";
    }


    @RequestMapping("/confirmitem")
    public String confirmItem(Model model, Principal principal, Item i, String message) {
        model.addAttribute("currentRole", getCurrentRole(principal));
        model.addAttribute("item", i);
        model.addAttribute("message", message);
        return "confirmitem.html";
    }

    @GetMapping("/addcustomer")
    public String addCustomerForm(Model model, Principal principal) {
        model.addAttribute("currentRole", getCurrentRole(principal));
        return "addcustomer.html";
    }

    @GetMapping("/registercustomer")
    public String addCustomer(@RequestParam String fname, @RequestParam String lname,
                              @RequestParam String ssn, Model model, Principal principal) {
        Customer c = new Customer();
        c.setFirstName(fname);
        c.setLastName(lname);
        c.setSsn(ssn);
        try {
            String response = restTemplate.postForObject(customersServiceUrl, c, String.class);
            return confirmCustomer(model, principal, c, response);
        } catch (Exception e) {
            return confirmCustomer(model, principal, null, e.getMessage().substring(e.getMessage().indexOf(":") + 3, e.getMessage().length() - 3));
        }
    }

    @GetMapping("/confirmcustomer")
    public String confirmCustomer(Model model, Principal principal, Customer c, String message) {
        model.addAttribute("currentRole", getCurrentRole(principal));
        model.addAttribute("customer", c);
        model.addAttribute("message", message);
        return "confirmcustomer.html";
    }

    @GetMapping("/additem")
    public String addItemForm(Model model, Principal principal) {
        model.addAttribute("currentRole", getCurrentRole(principal));
        model.addAttribute("item", new Item());
        return "additem.html";
    }

    @GetMapping("/registeritem")
    public String addItem(@RequestParam(defaultValue = "") String name, @RequestParam(defaultValue = "0") Double price, Model model, Principal principal) {

        Item i = new Item();
        i.setName(name);
        i.setPrice(price);

        try {
            String response = restTemplate.postForObject(itemsServiceUrl, i, String.class);
            return confirmItem(model, principal, i, response);
        } catch (Exception e) {
            return confirmItem(model, principal, null, e.getMessage().substring(e.getMessage().indexOf(":") + 3, e.getMessage().length() - 3));
        }

    }

    @GetMapping("/adjustorder")
    public String adjustOrder(@RequestParam long orderId,
                              @RequestParam int itemId,
                              @RequestParam int quantity,
                              Model model, Principal principal) {


        try {
            restTemplate.put(ordersServiceUrl + "/" + orderId, new NewOrderEntryRequest(itemId, quantity));
            return getOrder(orderId, null, model, principal);
        } catch (Exception e) {
            System.out.println(e.getClass());
            return getOrder(orderId, e.getMessage().substring(e.getMessage().indexOf("message") + 10, e.getMessage().indexOf("path") - 3), model, principal);
        }


    }


    @GetMapping("/neworder")
    public String newOrder(@RequestParam long customerId, Model model, Principal principal) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("customerId", Long.toString(customerId));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);


        try {
            CustomerOrder newOrder = restTemplate.postForObject(ordersServiceUrl, request, CustomerOrder.class);
            assert newOrder != null;
            return getOrder(newOrder.getId(), "NEW ORDER, ID: " + newOrder.getId(), model, principal);
        } catch (Exception e) {
            return getOrder(-1, e.getMessage().substring(e.getMessage().indexOf("message") + 10, e.getMessage().indexOf("path") - 3), model, principal);
        }
    }
}
