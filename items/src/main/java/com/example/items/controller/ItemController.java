package com.example.items.controller;
import com.example.items.model.Item;
import com.example.items.model.ItemList;
import com.example.items.repository.ItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
public class ItemController {


    private final ItemRepository itemRepository;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Operation(summary = "Get all items")
    @GetMapping("/")
    public ItemList getAllItems() {
        return new ItemList(itemRepository.findAll());
    }

    @Operation(summary = "Get item by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable long id) {
        if (itemRepository.findById(id).isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        return ResponseEntity.ok(itemRepository.findById(id).get());
    }

    @Operation(summary = "Add item")
    @PostMapping("/")
    public String createItem(@Valid @RequestBody Item item) {

        if (item.getName().isBlank()) return "Invalid product name";

        if (item.getPrice() < 0) return "Price must be 0 or more";

        itemRepository.save(item);
        return "Product added";
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

}
