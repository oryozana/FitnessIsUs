package com.example.fitnessisus;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Plan implements Serializable {
    private static final String[] activeLevelOptions = new String[]{"Sedentary", "Lightly active", "Moderately active", "Very active", "Extra active"};
    public static final int CURRENT_PLAN = 0, PREVIOUS_PLANS = 1;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy");

    private String fromDate;
    private String untilDate;
    private double targetCalories;
    private double targetProteins;
    private double targetFats;
    private String goal;

    public Plan(DataSnapshot dataSnapshot, int mode) {  // Used for current Plan and previous plans
        this.targetCalories = Double.parseDouble(String.valueOf(dataSnapshot.child("targetCalories").getValue()));
        this.targetProteins = Double.parseDouble(String.valueOf(dataSnapshot.child("targetProteins").getValue()));
        this.targetFats = Double.parseDouble(String.valueOf(dataSnapshot.child("targetFats").getValue()));
        this.goal = String.valueOf(dataSnapshot.child("goal").getValue());

        this.fromDate = String.valueOf(dataSnapshot.child("fromDate").getValue());

        if(mode == CURRENT_PLAN)
            this.untilDate = dtf.format(LocalDateTime.now());

        if(mode == PREVIOUS_PLANS)
            this.untilDate = String.valueOf(dataSnapshot.child("untilDate").getValue());
    }

    public Plan(String targetCalories, String targetProteins, String targetFats) {  // Used for creating a new plan.
        this.targetCalories = Double.parseDouble(targetCalories);
        this.targetProteins = Double.parseDouble(targetProteins);
        this.targetFats = Double.parseDouble(targetFats);
        this.goal = "Custom";

        this.fromDate = dtf.format(LocalDateTime.now());
        this.untilDate = dtf.format(LocalDateTime.now());
    }

    public Plan(String fromDate, String untilDate, String targetCalories, String targetProteins, String targetFats, String goal) {  // Used for ready User Plan
        this.fromDate = fromDate;
        this.untilDate = untilDate;

        this.targetCalories = Double.parseDouble(targetCalories);
        this.targetProteins = Double.parseDouble(targetProteins);
        this.targetFats = Double.parseDouble(targetFats);
        this.goal = goal;
    }

//    public Plan(String targetCalories, String targetProteins, String targetFats, String goal, String fromDate, String untilDate) {  // Used for creating Plan from description
//        this.targetCalories = Double.parseDouble(targetCalories);
//        this.targetProteins = Double.parseDouble(targetProteins);
//        this.targetFats = Double.parseDouble(targetFats);
//        this.goal = goal;
//
//        this.fromDate = LocalDateTime.parse(fromDate);
//        this.untilDate = LocalDateTime.parse(untilDate);
//    }

    public Plan(String goal, String sex, double weight, double height, int age, String activeLevel){  // Just for help so don't need to use fromDate and untilDate
        double[] activeLevelValues = new double[]{1.2, 1.375, 1.55, 1.725, 1.9};
        double calories, proteins, fats;

        if(sex.equals("Male")) {
            calories = (10 * weight) + (6.25 * height) - (5 * age) + 5;
            proteins = weight + (height * 0.4) - (age * 0.4) - 19;
            fats = (weight * 0.5) + (height * 0.03) - (age * 0.02) - 5.4;
        }
        else {
            calories = (10 * weight) + (6.25 * height) - (5 * age) - 161;
            proteins = (weight * 0.9) + (height * 0.3) - (age * 0.3) - 25;
            fats = (weight * 0.4) + (height * 0.025) - (age * 0.015) - 4.3;
        }

        if(goal.equals("Maintain weight")){
            this.targetCalories = calories;
            this.targetProteins = proteins;
            this.targetFats = fats;
        }

        if(goal.equals("Lose weight")){
            this.targetCalories = calories * 0.8;
            this.targetProteins = proteins * 0.8;
            this.targetFats = fats * 0.8;
        }

        if(goal.equals("Gain weight")){
            this.targetCalories = calories * 1.15;
            this.targetProteins = proteins * 1.15;
            this.targetFats = fats * 1.15;
        }

        for(int level = 0; level < activeLevelOptions.length; level++){
            if(activeLevelOptions[level].equals(activeLevel)){
                this.targetCalories *= activeLevelValues[level];
                this.targetProteins *= activeLevelValues[level];
                this.targetFats *= activeLevelValues[level];
            }
        }

        this.goal = goal;
        roundValues();
    }

//    public String generatePlanDescription(){
//        String message = "       Plan { ";
//
//        message += "fromDate ( " + this.fromDate + " ) ";
//        message += "untilDate ( " + this.untilDate + " ) ";
//
//        message += "targetCalories: " + this.targetCalories + " , ";
//        message += "targetProteins: " + this.targetProteins + " , ";
//        message += "targetFats: " + this.targetFats + " , ";
//        message += "goal: " + this.goal + " }";
//
//        return message;
//    }
//
//    public static Plan generatePlanObjectFromDescription(String data){
//        String[] dataParts;
//        String fromDate, untilDate;
//        String targetCalories, targetProteins, targetFats, goal;
//
//        dataParts = data.split("Plan \\{");
//        dataParts = dataParts[1].split(" \\}");
//        data = dataParts[0];
//
//        dataParts = data.split(" fromDate \\( ");
//        dataParts = dataParts[1].split(" \\)");
//        fromDate = dataParts[0];
//
//        dataParts = data.split(" untilDate \\( ");
//        dataParts = dataParts[1].split(" \\)");
//        untilDate = dataParts[0];
//
//        dataParts = data.split(" targetCalories: ");
//        dataParts = dataParts[1].split(" ,");
//        targetCalories = dataParts[0];
//
//        dataParts = data.split(" targetProteins: ");
//        dataParts = dataParts[1].split(" ,");
//        targetProteins = dataParts[0];
//
//        dataParts = data.split(" targetFats: ");
//        dataParts = dataParts[1].split(" ,");
//        targetFats = dataParts[0];
//
//        goal = data.split(" goal: ")[1];
//
//        return new Plan(targetCalories, targetProteins, targetFats, goal, fromDate, untilDate);
//    }
//
//    public static void fillMissingPlansDates(){
//        LocalDateTime oldestPlan = LocalDateTime.now(), today = LocalDateTime.now();
//
//        for(int i = 0; i < plans.size(); i++){
//            LocalDateTime fromDate = plans.get(i).getFromDate();
//            LocalDateTime untilDate = plans.get(i).getUntilDate();
//
//            if(oldestPlan.isAfter(fromDate))
//                oldestPlan = ;
//        }
//    }

    public static boolean isTheSamePlan(Plan plan1, Plan plan2){
        if(plan1 == null || plan2 == null)
            return false;

        boolean isTheSame = true;

        if(plan1.getTargetCalories() != plan2.getTargetCalories())
            isTheSame = false;

        if(plan1.getTargetProteins() != plan2.getTargetProteins())
            isTheSame = false;

        if(plan1.getTargetFats() != plan2.getTargetFats())
            isTheSame = false;

        return isTheSame;
    }

    public void roundValues(){
        this.targetCalories = Math.round(this.targetCalories * 1000.0) / 1000.0;
        this.targetProteins = Math.round(this.targetProteins * 1000.0) / 1000.0;
        this.targetFats = Math.round(this.targetFats * 1000.0) / 1000.0;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getUntilDate() {
        return untilDate;
    }

    public void setUntilDate(String untilDate) {
        this.untilDate = untilDate;
    }

    public void setUntilDateAsToday() {
        this.untilDate = dtf.format(LocalDateTime.now());
    }

    public double getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(double targetCalories) {
        this.targetCalories = targetCalories;
    }

    public double getTargetProteins() {
        return targetProteins;
    }

    public void setTargetProteins(double targetProteins) {
        this.targetProteins = targetProteins;
    }

    public double getTargetFats() {
        return targetFats;
    }

    public void setTargetFats(double targetFats) {
        this.targetFats = targetFats;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal){
        this.goal = goal;
    }

    public static String[] getActiveLevelOptions() {
        return activeLevelOptions;
    }

    @Override
    public String toString() {
        return this.targetCalories + " , " + this.targetProteins + " , " + this.targetFats;
    }
}
