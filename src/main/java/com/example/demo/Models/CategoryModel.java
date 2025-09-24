package com.example.demo.Models;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryModel {
    private Integer categoryId;
    private String categoryName;
    private boolean edit;
}
