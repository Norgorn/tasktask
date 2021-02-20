package ru.norgorn.tasktask.model;

import lombok.Value;

@Value
public class HttpBodyResponse {

    private String requestedUrl;
    private String body;
}
