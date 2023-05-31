package com.example.fitnessisus;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class DailyMenu {
    private static ArrayList<DailyMenu> dailyMenus = new ArrayList<DailyMenu>();
    private boolean isNeedToBeSaved = false;
    private static DailyMenu todayMenu;
    private static Meal newCustomMeal;
    private final ArrayList<Food> breakfast;
    private final ArrayList<Food> lunch;
    private final ArrayList<Food> dinner;
    private double totalProteins;
    private double totalFats;
    private double totalCalories;
    private final String date;

    public DailyMenu(String date) {
        this.breakfast = new ArrayList<Food>();
        this.lunch = new ArrayList<Food>();
        this.dinner = new ArrayList<Food>();

        this.totalProteins = 0;
        this.totalFats = 0;
        this.totalCalories = 0;
        this.date = date;
    }

    public static ArrayList<DailyMenu> getDailyMenus() {
        removeDailyMenuDuplications();
        return dailyMenus;
    }

    public static void setDailyMenus(Context context) {
        DailyMenu.dailyMenus = new ArrayList<DailyMenu>();
        DailyMenu.dailyMenus = getDailyMenusFromFile(context);
    }

    public static DailyMenu getTodayMenu() {
        if(todayMenu == null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy");
            LocalDateTime today = LocalDateTime.now();

            return DailyMenu.getTodayMenuFromAllDailyMenus(dtf.format(today));
        }
        return todayMenu;
    }

    public static void setTodayMenu(DailyMenu todayMeals) {
        DailyMenu.todayMenu = todayMeals;
    }

    public String generateDailyMenuDescriptionForFiles(){
        String message = "       DailyMenu { ";

        message += "breakfast ( ";
        for(int i = 0; i < this.breakfast.size(); i++){
            Food food = this.breakfast.get(i);
            if(food instanceof Meal)
                message += "     " + ((Meal) food).generateMealDescriptionForFiles();
            if(food instanceof Ingredient)
                message += "     " + ((Ingredient) food).generateIngredientDescriptionForFiles();
        }

        if(this.breakfast.size() == 0)
            message += "null";

        message += " ) ";


        message += "lunch ( ";
        for(int i = 0; i < this.lunch.size(); i++){
            Food food = this.lunch.get(i);
            if(food instanceof Meal)
                message += "     " + ((Meal) food).generateMealDescriptionForFiles();
            if(food instanceof Ingredient)
                message += "     " + ((Ingredient) food).generateIngredientDescriptionForFiles();
        }

        if(this.lunch.size() == 0)
            message += "null";

        message += " ) ";


        message += "dinner ( ";
        for(int i = 0; i < this.dinner.size(); i++){
            Food food = this.dinner.get(i);
            if(food instanceof Meal)
                message += "     " + ((Meal) food).generateMealDescriptionForFiles();
            if(food instanceof Ingredient)
                message += "     " + ((Ingredient) food).generateIngredientDescriptionForFiles();
        }

        if(this.dinner.size() == 0)
            message += "null";

        message += " ) ";

        message += "date: " + this.date + " }";

        return message;
    }

    public static String generateEmptyDailyMenuDescriptionForFiles(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy");
        return "       DailyMenu { breakfast ( null ) lunch ( null ) dinner ( null ) date: " + dtf.format(LocalDateTime.now()) + " }";
    }

    public static DailyMenu generateDailyMenuObjectFromFile(String data){
        String[] dataParts, breakfastDataParts, lunchDataParts, dinnerDataParts;
        String breakfastData, lunchData, dinnerData, date = "";

        ArrayList<Food> breakfastFood = new ArrayList<Food>();
        ArrayList<Food> lunchFood = new ArrayList<Food>();
        ArrayList<Food> dinnerFood = new ArrayList<Food>();

        dataParts = data.split("DailyMenu \\{");
        dataParts = dataParts[1].split(" \\}");
        data = dataParts[0];

        dataParts = data.split(" breakfast \\( ");
        dataParts = dataParts[1].split(" \\)");
        breakfastData = dataParts[0];

        dataParts = data.split(" lunch \\( ");
        dataParts = dataParts[1].split(" \\)");
        lunchData = dataParts[0];

        dataParts = data.split(" dinner \\( ");
        dataParts = dataParts[1].split(" \\)");
        dinnerData = dataParts[0];

        date = data.split(" date: ")[1];

        if(!breakfastData.equals("null")){
            breakfastDataParts = breakfastData.split("     ");
            for (String breakfastDataPart : breakfastDataParts) {
                if(!breakfastDataPart.equals("")){
                    if (breakfastDataPart.split(" \\[ ")[0].equals("Meal"))
                        breakfastFood.add(Meal.generateMealObjectFromFileDescription(breakfastDataPart + " ]"));
                    if (breakfastDataPart.split(" \\[ ")[0].equals("Ingredient"))
                        breakfastFood.add(Ingredient.generateIngredientObjectFromFileDescription(breakfastDataPart + " ]"));
                }
            }
        }

        if(!lunchData.equals("null")){
            lunchDataParts = lunchData.split("     ");
            for (String lunchDataPart : lunchDataParts) {
                if(!lunchDataPart.equals("")){
                    if (lunchDataPart.split(" \\[ ")[0].equals("Meal"))
                        lunchFood.add(Meal.generateMealObjectFromFileDescription(lunchDataPart));
                    if (lunchDataPart.split(" \\[ ")[0].equals("Ingredient"))
                        lunchFood.add(Ingredient.generateIngredientObjectFromFileDescription(lunchDataPart));
                }
            }
        }

        if(!dinnerData.equals("null")){
            dinnerDataParts = dinnerData.split("     ");
            for (String dinnerDataPart : dinnerDataParts) {
                if(!dinnerDataPart.equals("")){
                    if (dinnerDataPart.split(" \\[ ")[0].equals("Meal"))
                        dinnerFood.add(Meal.generateMealObjectFromFileDescription(dinnerDataPart));
                    if (dinnerDataPart.split(" \\[ ")[0].equals("Ingredient"))
                        dinnerFood.add(Ingredient.generateIngredientObjectFromFileDescription(dinnerDataPart));
                }
            }
        }

        DailyMenu dailyMenu = new DailyMenu(date);
        for(int i = 0; i < breakfastFood.size(); i++)
            dailyMenu.addBreakfast(breakfastFood.get(i));

        for(int i = 0; i < lunchFood.size(); i++)
            dailyMenu.addLunch(lunchFood.get(i));

        for(int i = 0; i < dinnerFood.size(); i++)
            dailyMenu.addDinner(dinnerFood.get(i));

        return dailyMenu;
    }

    public static void restartDailyMenusFile(Context context){
        FileOutputStream fos;
        OutputStreamWriter osw;
        BufferedWriter bw;

        try {
            fos = context.openFileOutput("dailyMenusFile", Context.MODE_PRIVATE);
            osw = new OutputStreamWriter(fos);
            bw = new BufferedWriter(osw);

            bw.write("Daily menus: " + "\n");

            bw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isThereAtLeastOneThing(){
        boolean found = this.breakfast.size() != 0;

        if(this.lunch.size() != 0)
            found = true;

        if(this.dinner.size() != 0)
            found = true;

        return found;
    }

    private void roundNutritionalValues(){
        this.totalProteins = Math.round(this.totalProteins * 1000.0) / 1000.0;
        this.totalFats = Math.round(this.totalFats * 1000.0) / 1000.0;
        this.totalCalories = Math.round(this.totalCalories * 1000.0) / 1000.0;
        this.isNeedToBeSaved = true;
    }

    private void addFoodNutritionalValues(Food food) {
        this.totalProteins += food.getProteins();
        this.totalFats += food.getFats();
        this.totalCalories += food.getCalories();
        roundNutritionalValues();
    }

    private void subtractFoodNutritionalValues(Food food) {
        this.totalProteins -= food.getProteins();
        this.totalFats -= food.getFats();
        this.totalCalories -= food.getCalories();
        roundNutritionalValues();
    }

    public ArrayList<Food> getBreakfast() {
        return this.breakfast;
    }

    public boolean hasBreakfast() {
        return this.breakfast.size() != 0;
    }

    public void addBreakfast(Food breakfast) {
        this.breakfast.add(breakfast);
        addFoodNutritionalValues(breakfast);
    }

    public void removeBreakfast(Food food) {
        boolean removed = false;
        for(int i = 0; i < this.breakfast.size(); i++){
            Food current = this.breakfast.get(i);

            if(!removed){
                if(food.getCalories() == current.getCalories() && food.getProteins() == current.getProteins() && food.getFats() == current.getFats()) {
                    subtractFoodNutritionalValues(this.breakfast.get(i));
                    this.breakfast.remove(i);
                    removed = true;
                }
            }
        }
    }

    public ArrayList<Ingredient> generateBreakfastIngredientsArray(){
        ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
        Ingredient tmpIngredient;

        for(int i = 0; i < this.breakfast.size(); i++){
            Food food = this.breakfast.get(i);
            if(food instanceof Ingredient)
                ingredients.add(new Ingredient((Ingredient) food));

            if(food instanceof Meal){
                for(int j = 0; j < ((Meal) food).getNeededIngredientsForMeal().size(); j++){
                    tmpIngredient = ((Meal) food).getNeededIngredientsForMeal().get(j);
                    ingredients.add(new Ingredient(tmpIngredient));
                }
            }
        }
        return ingredients;
    }

    public ArrayList<Food> getLunch() {
        return this.lunch;
    }

    public boolean hasLunch(){
        return this.lunch.size() != 0;
    }

    public void addLunch(Food lunch) {
        this.lunch.add(lunch);
        addFoodNutritionalValues(lunch);
    }

    public void removeLunch(Food food) {
        boolean removed = false;
        for(int i = 0; i < this.lunch.size(); i++){
            Food current = this.lunch.get(i);

            if(!removed){
                if(food.getCalories() == current.getCalories() && food.getProteins() == current.getProteins() && food.getFats() == current.getFats()) {
                    subtractFoodNutritionalValues(this.lunch.get(i));
                    this.lunch.remove(i);
                    removed = true;
                }
            }
        }
    }

    public ArrayList<Ingredient> generateLunchIngredientsArray(){
        ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
        Ingredient tmpIngredient;

        for(int i = 0; i < this.lunch.size(); i++){
            Food food = this.lunch.get(i);
            if(food instanceof Ingredient)
                ingredients.add(new Ingredient((Ingredient) food));

            if(food instanceof Meal){
                for(int j = 0; j < ((Meal) food).getNeededIngredientsForMeal().size(); j++){
                    tmpIngredient = ((Meal) food).getNeededIngredientsForMeal().get(j);
                    ingredients.add(new Ingredient(tmpIngredient));
                }
            }
        }
        return ingredients;
    }

    public ArrayList<Food> getDinner() {
        return this.dinner;
    }

    public boolean hasDinner() {
        return this.dinner.size() != 0;
    }

    public void addDinner(Food dinner) {
        this.dinner.add(dinner);
        addFoodNutritionalValues(dinner);
    }

    public void removeDinner(Food food) {
        boolean removed = false;
        for(int i = 0; i < this.dinner.size(); i++){
            Food current = this.dinner.get(i);

            if(!removed){
                if(food.getCalories() == current.getCalories() && food.getProteins() == current.getProteins() && food.getFats() == current.getFats()) {
                    subtractFoodNutritionalValues(this.dinner.get(i));
                    this.dinner.remove(i);
                    removed = true;
                }
            }
        }
    }

    public ArrayList<Ingredient> generateDinnerIngredientsArray(){
        ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
        Ingredient tmpIngredient;

        for(int i = 0; i < this.dinner.size(); i++){
            Food food = this.dinner.get(i);
            if(food instanceof Ingredient)
                ingredients.add(new Ingredient((Ingredient) food));

            if(food instanceof Meal){
                for(int j = 0; j < ((Meal) food).getNeededIngredientsForMeal().size(); j++){
                    tmpIngredient = ((Meal) food).getNeededIngredientsForMeal().get(j);
                    ingredients.add(new Ingredient(tmpIngredient));
                }
            }
        }
        return ingredients;
    }

    public ArrayList<Meal> getBreakfastAsMealsTypeOnly(){
        ArrayList<Ingredient> tmpIngredientList = new ArrayList<Ingredient>();
        ArrayList<Meal> breakfastMeals = new ArrayList<Meal>();

        for(int i = 0; i < todayMenu.breakfast.size(); i++){
            Food food = todayMenu.breakfast.get(i);
            if(food instanceof Ingredient){
                tmpIngredientList.add((Ingredient) food);
                breakfastMeals.add(new Meal(food.getName(), tmpIngredientList));
            }
            if(food instanceof Meal)
                breakfastMeals.add((Meal) food);
            tmpIngredientList.clear();
        }
        return breakfastMeals;
    }

    public ArrayList<Meal> getLunchAsMealsTypeOnly(){
        ArrayList<Ingredient> tmpIngredientList = new ArrayList<Ingredient>();
        ArrayList<Meal> lunchMeals = new ArrayList<Meal>();

        for(int i = 0; i < todayMenu.lunch.size(); i++){
            Food food = todayMenu.lunch.get(i);
            if(food instanceof Ingredient){
                tmpIngredientList.add((Ingredient) food);
                lunchMeals.add(new Meal(food.getName(), tmpIngredientList));
            }
            if(food instanceof Meal)
                lunchMeals.add((Meal) food);
            tmpIngredientList.clear();
        }
        return lunchMeals;
    }

    public ArrayList<Meal> getDinnerAsMealsTypeOnly(){
        ArrayList<Ingredient> tmpIngredientList = new ArrayList<Ingredient>();
        ArrayList<Meal> dinnerMeals = new ArrayList<Meal>();

        for(int i = 0; i < todayMenu.dinner.size(); i++){
            Food food = todayMenu.dinner.get(i);
            if(food instanceof Ingredient){
                tmpIngredientList.add((Ingredient) food);
                dinnerMeals.add(new Meal(food.getName(), tmpIngredientList));
            }
            if(food instanceof Meal)
                dinnerMeals.add((Meal) food);
            tmpIngredientList.clear();
        }
        return dinnerMeals;
    }

    public static void fillMissingDailyMenusDates(Context context){
        LocalDateTime oldestDailyMenu = LocalDateTime.now(), today = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy");

        for(int i = 0; i < dailyMenus.size(); i++){
            String date = dailyMenus.get(i).getDate();
            int day = Integer.parseInt(date.split("_")[0]);
            int month = Integer.parseInt(date.split("_")[1]);
            int year = Integer.parseInt(date.split("_")[2]);

            if(oldestDailyMenu.isAfter(LocalDateTime.of(year, month, day, 23, 59, 59, 59)))
                oldestDailyMenu = LocalDateTime.of(year, month, day, 23, 59, 59, 59);
        }

        int daysUntilToday = (int) oldestDailyMenu.until(today, ChronoUnit.DAYS);
        for(int i = 0; i <= daysUntilToday; i++){
            if(!hasTodayMenuInsideAllDailyMenus(dtf.format(oldestDailyMenu.plusDays(i)))) {
                DailyMenu tmpDailyMenu = new DailyMenu(dtf.format(oldestDailyMenu.plusDays(i)));
                saveDailyMenuIntoFile(tmpDailyMenu, context);
            }
        }
    }

    public boolean canAddFood(Context context, Food food){
        boolean passTests = true;

        if((food.getCalories() + this.totalCalories) > 25000){
            Toast.makeText(context, "Exceeded calories limit, food not added.", Toast.LENGTH_SHORT).show();
            passTests = false;
        }

        if((food.getProteins() + this.totalProteins) > 2500 && passTests) {
            Toast.makeText(context, "Exceeded proteins limit, food not added.", Toast.LENGTH_SHORT).show();
            passTests = false;
        }

        if((food.getFats() + this.totalFats) > 2500 && passTests) {
            Toast.makeText(context, "Exceeded fats limit, food not added.", Toast.LENGTH_SHORT).show();
            passTests = false;
        }

        return passTests;
    }

    public void addMealByMealName(Context context, String selectedMeal, Meal meal){
        if(canAddFood(context, meal)){
            if(selectedMeal.equals("Breakfast"))
                todayMenu.addBreakfast(meal);

            if(selectedMeal.equals("Lunch"))
                todayMenu.addLunch(meal);

            if(selectedMeal.equals("Dinner"))
                todayMenu.addDinner(meal);

            Toast.makeText(context, "Meal successfully added.", Toast.LENGTH_SHORT).show();
        }
    }

    public void addIngredientByMealName(Context context, String selectedMeal, Ingredient ingredient, int grams){
        if(canAddFood(context, ingredient)){
            Ingredient tmpIngredient = Ingredient.getIngredientByName(ingredient.getName(), grams);

            if(selectedMeal.equals("Breakfast"))
                todayMenu.addBreakfast(tmpIngredient);

            if(selectedMeal.equals("Lunch"))
                todayMenu.addLunch(tmpIngredient);

            if(selectedMeal.equals("Dinner"))
                todayMenu.addDinner(tmpIngredient);

            Toast.makeText(context, "Ingredient successfully added.", Toast.LENGTH_SHORT).show();
        }
    }

    public ArrayList<Ingredient> generateAllIngredientsNeededArrayList(){
        ArrayList<Ingredient> tmpIngredients;

        tmpIngredients = generateBreakfastIngredientsArray();
        ArrayList<Ingredient> allIngredients = new ArrayList<Ingredient>(tmpIngredients);

        tmpIngredients = generateLunchIngredientsArray();
        allIngredients.addAll(tmpIngredients);

        tmpIngredients = generateDinnerIngredientsArray();
        allIngredients.addAll(tmpIngredients);

        return allIngredients;
    }

    public void replaceMeal(Meal meal, String mealType){
        if(mealType.equals("Breakfast")){
            removeBreakfast(meal);
            addBreakfast(meal);
        }

        if(mealType.equals("Lunch")){
            removeLunch(meal);
            addLunch(meal);
        }

        if(mealType.equals("Dinner")){
            removeDinner(meal);
            addDinner(meal);
        }
    }

    public void removeMeal(Meal meal, String mealType){
        if(mealType.equals("Breakfast"))
            removeBreakfast(meal);

        if(mealType.equals("Lunch"))
            removeLunch(meal);

        if(mealType.equals("Dinner"))
            removeDinner(meal);
    }

    public void correctNutritiousValues(){
        totalCalories = 0;
        totalProteins = 0;
        totalFats = 0;

        if(isThereAtLeastOneThing()){
            Ingredient tmpIngredient;
            ArrayList<Ingredient> tmpIngredients = generateAllIngredientsNeededArrayList();
            for(int i = 0; i < tmpIngredients.size(); i++){
                tmpIngredient = tmpIngredients.get(i);
                totalCalories += tmpIngredient.getCalories();
                totalProteins += tmpIngredient.getProteins();
                totalFats += tmpIngredient.getFats();
            }
            roundNutritionalValues();
        }
    }

    public static boolean hasTodayMenuInsideAllDailyMenus(String currentDate){
        for(int i = 0; i < dailyMenus.size(); i++){
            if(dailyMenus.get(i).getDate().equals(currentDate))
                return true;
        }
        return false;
    }

    public static DailyMenu getTodayMenuFromAllDailyMenus(String currentDate){
        if(hasTodayMenuInsideAllDailyMenus(currentDate)){
            for(int i = 0; i < dailyMenus.size(); i++){
                if(dailyMenus.get(i).getDate().equals(currentDate))
                    return dailyMenus.get(i);
            }
        }
        return new DailyMenu(currentDate);
    }

    public static void removeDailyMenuDuplications(){
        ArrayList<DailyMenu> tmpDailyMenus = new ArrayList<DailyMenu>();
        boolean found;

        for(int i = 0; i < dailyMenus.size(); i++){
            found = false;
            for(int j = i + 1; j < dailyMenus.size(); j++){
                if(dailyMenus.get(i).date.equals(dailyMenus.get(j).date))
                    found = true;
            }

            if(!found)
                tmpDailyMenus.add(dailyMenus.get(i));
        }

        dailyMenus = tmpDailyMenus;
    }

    public static void removeDailyMenuDuplicationsAndAddAnotherOne(DailyMenu dailyMenu){
        ArrayList<DailyMenu> tmpDailyMenus = new ArrayList<DailyMenu>();
        boolean found;

        for(int i = 0; i < dailyMenus.size(); i++) {
            found = false;
            for(int j = i + 1; j < dailyMenus.size(); j++){
                if(dailyMenus.get(i).date.equals(dailyMenus.get(j).date) || dailyMenus.get(i).date.equals(dailyMenu.date))
                    found = true;
            }

            if(!found)
                tmpDailyMenus.add(dailyMenus.get(i));
        }

        tmpDailyMenus.add(dailyMenu);
        dailyMenus = tmpDailyMenus;
    }

    public static boolean hasCustomMeal(){
        if(newCustomMeal != null){
            if(newCustomMeal.getName().replaceAll(" ", "").equals(""))
                return newCustomMeal.getNeededIngredientsForMeal().size() != 0;
            else
                return true;
        }
        return false;
    }

    public static Meal getCustomMeal() {
        return newCustomMeal;
    }

    public static void saveCustomMeal(Meal newCustomMeal) {
        DailyMenu.newCustomMeal = newCustomMeal;
    }

    public static void saveDailyMenuIntoFile(DailyMenu dailyMenu, Context context){
        FileOutputStream fos;
        OutputStreamWriter osw;
        BufferedWriter bw;

        try {
            fos = context.openFileOutput("dailyMenusFile", Context.MODE_PRIVATE);
            osw = new OutputStreamWriter(fos);
            bw = new BufferedWriter(osw);

            bw.write("Daily menus: " + "\n");

            DailyMenu.removeDailyMenuDuplicationsAndAddAnotherOne(dailyMenu);
            for(int i = 0; i < DailyMenu.getDailyMenus().size(); i++)
                bw.write(DailyMenu.getDailyMenus().get(i).generateDailyMenuDescriptionForFiles() + "\n");

            bw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<DailyMenu> getDailyMenusFromFile(Context context){
        FileAndDatabaseHelper fileAndDatabaseHelper = new FileAndDatabaseHelper(context);
        String[] dataParts = fileAndDatabaseHelper.getFileData("dailyMenusFile").split("\n");
        ArrayList<DailyMenu> dailyMenus = new ArrayList<DailyMenu>();

        for(int i = 1; i < dataParts.length; i++)
            dailyMenus.add(DailyMenu.generateDailyMenuObjectFromFile(dataParts[i]));
        return dailyMenus;
    }

    public static String getTheOldestDailyMenuDate(){
        LocalDateTime oldestDailyMenu = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy");

        for(int i = 0; i < dailyMenus.size(); i++){
            String date = dailyMenus.get(i).getDate();
            int day = Integer.parseInt(date.split("_")[0]);
            int month = Integer.parseInt(date.split("_")[1]);
            int year = Integer.parseInt(date.split("_")[2]);

            if(oldestDailyMenu.isAfter(LocalDateTime.of(year, month, day, 23, 59)))
                oldestDailyMenu = LocalDateTime.of(year, month, day, 23, 59);
        }

        return dtf.format(oldestDailyMenu);
    }

    public boolean isNeedToBeSaved(){
        return this.isNeedToBeSaved;
    }

    public void setIsNeedToBeSaved(boolean isNeedToBeSaved){
        this.isNeedToBeSaved = isNeedToBeSaved;
    }

    public double getTotalProteins() {
        return this.totalProteins;
    }

    public double getTotalFats() {
        return this.totalFats;
    }

    public double getTotalCalories() {
        return this.totalCalories;
    }

    public String getDate() {
        return this.date;
    }

    @Override
    public String toString() {
        return "DailyMenu{" +
                "breakfast=" + breakfast +
                ", lunch=" + lunch +
                ", dinner=" + dinner +
                ", totalProteins=" + totalProteins +
                ", totalFats=" + totalFats +
                ", totalCalories=" + totalCalories +
                ", date='" + date + '\'' +
                '}';
    }
}
