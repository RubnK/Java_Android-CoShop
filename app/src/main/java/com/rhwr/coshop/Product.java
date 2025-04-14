package com.rhwr.coshop;

public class Product {
    private String name;
    private int quantity;
    private String category;
    private boolean purchased;

    // Constructeur par défaut nécessaire pour Firestore
    public Product() {
    }

    public Product(String name, int quantity, String category) {
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.purchased = false;  // Par défaut, le produit n'est pas acheté
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }
}