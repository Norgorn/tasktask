package ru.norgorn.tasktask.model;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;

@Value
@AllArgsConstructor
public class LibInfo {

    private String url;

    // This can be used to find same libraries with different names
    private Optional<String> hashCode;

    public LibInfo(String url) {
        this.url = url;
        hashCode = Optional.empty();
    }
}
