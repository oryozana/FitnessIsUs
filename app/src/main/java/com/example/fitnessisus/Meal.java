package com.example.fitnessisus;

import java.util.ArrayList;
import java.util.Locale;

public class Meal extends Food {
    private final ArrayList<Ingredient> ingredients = Ingredient.getIngredientsList();
    private final ArrayList<Ingredient> neededIngredientsForMeal = new ArrayList<Ingredient>();

    public Meal(String name){
        super(name);
    }

    public Meal(String name, int grams) {  // Single ingredient meal.
        super(name);
        initiateNeededIngredientsForMeal(name, grams);
        updateMealInfo();
    }

    public Meal(String name, ArrayList<Ingredient> ingredientsNeeded){
        super(name);
        for(int i = 0; i < ingredientsNeeded.size(); i++) {
            Ingredient ingredient = ingredientsNeeded.get(i);
            ingredient.setName(ingredient.getName().toLowerCase(Locale.ROOT));
            this.neededIngredientsForMeal.add(ingredient);
        }
        updateMealInfo();
    }

    public String generateMealDescriptionForFiles(){
        String message = "Meal [ " + this.name + ": ";

        for(int i = 0; i < this.neededIngredientsForMeal.size(); i++)
            message += "   " + this.neededIngredientsForMeal.get(i).generateIngredientDescriptionForFiles();

        message += " ]";

        return message;
    }

    public static Meal generateMealObjectFromFileDescription(String data){
        ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
        String[] dataParts;
        String name;

        dataParts = data.split("Meal \\[ ", 2);
        dataParts = dataParts[1].split(" \\] \\]");
        data = dataParts[0];

        name = data.split(": ")[0];
        data = data.split(": ")[1];

        dataParts = data.split("   ");
        dataParts[dataParts.length - 1] += " ]";
        for(int i = 1; i < dataParts.length; i++)   // i = 1 because it's has "" in the first index.
            ingredients.add(Ingredient.generateIngredientObjectFromFileDescription(dataParts[i] + " ]"));

        return new Meal(name, ingredients);
    }

    public void initiateNeededIngredientsForMeal(String name, int grams){
        String[] mealParts = name.split(" and | with | include ");
        for(String mealPart : mealParts){
            mealPart = mealPart.toLowerCase(Locale.ROOT);
            if(ingredients.contains(Ingredient.getIngredientByName(mealPart)))
                addIfNeeded(Ingredient.getIngredientByName(mealPart, grams));
        }
    }

    private void addIfNeeded(Ingredient ingredient) {
        if(this.neededIngredientsForMeal.contains(ingredient)){
            this.neededIngredientsForMeal.get(this.neededIngredientsForMeal.indexOf(ingredient)).addGrams(ingredient.getGrams());
        }
        else
            this.neededIngredientsForMeal.add(ingredient);
    }

    public void updateMealInfo(){
        this.grams = 0;
        this.calories = 0;
        this.proteins = 0;
        this.fats = 0;

        for(int i = 0; i < this.neededIngredientsForMeal.size(); i++){
            if(this.neededIngredientsForMeal.get(i) != null) {
                Ingredient ingredient = this.neededIngredientsForMeal.get(i);
                this.grams += ingredient.grams;
                this.proteins += ingredient.proteins;
                this.fats += ingredient.fats;
                this.calories += ingredient.calories;
            }
        }
        roundValues();
    }

    public ArrayList<Ingredient> getNeededIngredientsForMeal() {
        return this.neededIngredientsForMeal;
    }

    public ArrayList<Ingredient> getNeededIngredientsForMeal2(){
        ArrayList<Ingredient> tmpIngredients = new ArrayList<Ingredient>();
        for(int i = 0; i < this.neededIngredientsForMeal.size(); i++)
            tmpIngredients.add(new Ingredient(this.neededIngredientsForMeal.get(i)));
        return tmpIngredients;
    }

    public void addNeededIngredientForMeal(Ingredient ingredient){
        this.neededIngredientsForMeal.add(ingredient);
        updateMealInfo();
    }

    public void removeNeededIngredientForMeal(Ingredient ingredient){
        boolean found = false;
        for(int i = 0; i < this.neededIngredientsForMeal.size(); i++){
            if(neededIngredientsForMeal.get(i).getName().equals(ingredient.getName()) && neededIngredientsForMeal.get(i).getGrams() == ingredient.getGrams() && !found){
                this.neededIngredientsForMeal.remove(i);
                found = true;
            }
        }
        updateMealInfo();
    }

    @Override
    public String toString() {
        return this.name + ": " + this.grams + " grams, " + this.calories + " calories.";
    }
}
