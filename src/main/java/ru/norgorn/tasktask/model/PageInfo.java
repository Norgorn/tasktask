package ru.norgorn.tasktask.model;

import lombok.Value;

import java.util.Collection;

@Value
public class PageInfo {

    private String url;
    private Collection<LibInfo> libs;
}
