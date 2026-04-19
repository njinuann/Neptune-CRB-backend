package com.neptune.crb.processing.model;

public class ViewItems {
    private Long id;
    private String description;

    public ViewItems(Long id, String description) {
        this.id = id;
        this.description = description;
    }

    public ViewItems() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
