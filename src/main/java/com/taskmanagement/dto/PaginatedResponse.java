package com.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class PaginatedResponse<T>{

    private List<T> items;
    private int page;
    private int totalItems;
    private int totalPages;
    private boolean hasNext;
}
