package com.rhwr.coshop;

public class ListItem {
    private String name;

    // Constructeur sans argument requis pour Firebase
    public ListItem() {}

    // Constructeur
    public ListItem(String name) {
        this.name = name;
    }

    // Getter et Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
