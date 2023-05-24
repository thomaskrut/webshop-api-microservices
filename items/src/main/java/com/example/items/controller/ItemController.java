package com.example.items.controller;
import com.example.items.model.Item;
import com.example.items.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class ItemController {


    private final ItemRepository itemRepository;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping("/")
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable long id) {
        if (itemRepository.findById(id).isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        return ResponseEntity.ok(itemRepository.findById(id).get());
    }

    @PostMapping("/")
    public ResponseEntity<String> createItem(@RequestBody Item item) {

        if (item.getName().isBlank()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid product name");

        if (item.getPrice() < 0) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Price must be 0 or more");

        itemRepository.save(item);
        return ResponseEntity.ok("Product added");
    }

}
