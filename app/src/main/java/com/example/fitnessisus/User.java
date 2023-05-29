package com.example.fitnessisus;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class User {
    private static User currentUser;
    private final String username;
    private String password;
    private final String email;
    private final double startingWeight;
    private double weight;
    private Plan currentPlan;
    private ArrayList<Plan> previousPlans;
    private String userDailyMenus;
    private int profilePictureId;

    public User(DataSnapshot dataSnapshot){
        this.username = dataSnapshot.getKey();
        this.password = String.valueOf(dataSnapshot.child("password").getValue());

        this.currentPlan = new Plan(dataSnapshot.child("currentPlan"), Plan.CURRENT_PLAN);

        this.previousPlans = new ArrayList<Plan>();
        for(DataSnapshot planInfo : dataSnapshot.child("previousPlans").getChildren())
            this.previousPlans.add(new Plan(planInfo, Plan.PREVIOUS_PLANS));

        this.email = String.valueOf(dataSnapshot.child("email").getValue());
        this.startingWeight = Double.parseDouble(String.valueOf(dataSnapshot.child("startingWeight").getValue()));
        this.weight = Double.parseDouble(String.valueOf(dataSnapshot.child("weight").getValue()));

        this.profilePictureId = Integer.parseInt(String.valueOf(dataSnapshot.child("profilePictureId").getValue()));
        this.userDailyMenus = String.valueOf(dataSnapshot.child("userDailyMenus").getValue());
    }

    public User(String username, String password, String email, double startingWeight, Plan currentPlan, int profilePictureId, String dailyMenus){  // New User contracture so weight = startingWeight
        this.username = username;
        this.password = password;
        this.email = email;
        this.startingWeight = startingWeight;
        this.weight = startingWeight;
        this.currentPlan = currentPlan;
        this.profilePictureId = profilePictureId;
        this.userDailyMenus = dailyMenus;
    }

    public User(String username, String password, String email, String startingWeight, String weight, String planFromDate, String planUntilDate, String targetCalories, String targetProteins, String targetFats, String goal, String profilePictureId, String dailyMenus){
        this.username = username;
        this.password = password;
        this.email = email;
        this.startingWeight = Double.parseDouble(startingWeight);
        this.weight = Double.parseDouble(weight);
        this.currentPlan = new Plan(planFromDate, planUntilDate, targetCalories, targetProteins, targetFats, goal);
        this.profilePictureId = Integer.parseInt(profilePictureId);
        this.userDailyMenus = dailyMenus;
    }

    public void generatePreviousPlans(DataSnapshot dataSnapshot) {
        this.previousPlans = new ArrayList<Plan>();
        for(DataSnapshot planInfo : dataSnapshot.getChildren())
            this.previousPlans.add(new Plan(planInfo, Plan.PREVIOUS_PLANS));
    }

    public ArrayList<Plan> receivePreviousPlans(){
        return this.previousPlans;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public double getStartingWeight() {
        return startingWeight;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Plan getCurrentPlan() {
        this.currentPlan.setUntilDateAsToday();
        return currentPlan;
    }

    public void setCurrentPlan(Plan currentPlan) {
        this.currentPlan = currentPlan;
    }

    public void setPreviousPlans(ArrayList<Plan> previousPlans) {
        this.previousPlans = previousPlans;
    }

    public boolean hasPreviousPlans(){
        return previousPlans != null;
    }

    public int getProfilePictureId() {
        return profilePictureId;
    }

    public void setProfilePictureId(int profilePictureId) {
        this.profilePictureId = profilePictureId;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        User.currentUser = currentUser;
    }

    public String getUserDailyMenus() {
        return userDailyMenus;
    }

    public void downloadUserDailyMenusFromTemporaryFile(Context context){
        DailyMenu.fillMissingDailyMenusDates(context);
        this.userDailyMenus = DailyMenu.getTodayMenu().generateDailyMenuDescriptionForFiles();

        for(DailyMenu dailyMenu : DailyMenu.getDailyMenusFromFile(context)){
            if(!dailyMenu.getDate().equals(DailyMenu.getTodayMenu().getDate()))
                this.userDailyMenus += dailyMenu.generateDailyMenuDescriptionForFiles();
        }
    }

    public void uploadUserDailyMenusIntoTemporaryFile(Context context){
        if(this.userDailyMenus.equals(""))
            return;

        String[] dataParts = this.userDailyMenus.split("DailyMenu ");  // Just upload it into the file
        DailyMenu.restartDailyMenusFile(context);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy");
        LocalDateTime today = LocalDateTime.now();
        String currentDate = dtf.format(today);

        for(int i = 0; i < dataParts.length; i++) {
            if(!dataParts[i].replaceAll(" ", "").equals("")) {
                dataParts[i] = "       DailyMenu " + dataParts[i];
                if(dataParts[i].split(" date: ")[1].split(" \\}")[0].equals(currentDate))
                    DailyMenu.setTodayMenu(DailyMenu.generateDailyMenuObjectFromFile(dataParts[i]));
                DailyMenu.saveDailyMenuIntoFile(DailyMenu.generateDailyMenuObjectFromFile(dataParts[i]), context);
            }
        }
    }

    @Override
    public String toString() {
        return this.username + " , " + this.password + " , " + this.email + " , " + this.startingWeight + " , " + this.weight + " , " + this.currentPlan.toString();
    }
}
