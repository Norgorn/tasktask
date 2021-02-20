package ru.norgorn.tasktask.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class LibStatistics {

    private final LibInfo lib;
    private int count = 1;
}
