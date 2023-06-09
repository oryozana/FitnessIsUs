package com.example.fitnessisus;

public class FirebaseIngredient {
    private String name;
    private double grams;

    public FirebaseIngredient(Ingredient ingredient){
        this.name = ingredient.getName();
        this.grams = ingredient.getGrams();
    }

    public double getGrams() {
        return grams;
    }

    public void setGrams(double grams) {
        this.grams = grams;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
