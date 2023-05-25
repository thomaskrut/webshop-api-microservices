package com.example.items.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ItemList {

    List<Item> itemList;

    public ItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

}
