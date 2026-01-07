package com.taingy.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ListResponse<T> {
    List<T> content;
    Integer totalElements;
}
