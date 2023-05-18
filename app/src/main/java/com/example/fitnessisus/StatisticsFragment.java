package com.example.fitnessisus;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class StatisticsFragment extends Fragment implements View.OnClickListener {

    TextView tvTargetCaloriesAtPlan, tvTargetCaloriesAtDailyMenu, tvCaloriesLeft, tvCaloriesMultiplication;
    TextView tvTargetProteinsAtPlan, tvTargetProteinsAtDailyMenu, tvProteinsLeft, tvProteinsMultiplication;
    TextView tvTargetFatsAtPlan, tvTargetFatsAtDailyMenu, tvFatsLeft, tvFatsMultiplication;
    PieChart pcCalories, pcProteins, pcFats;

    int gray, green, red;
    TextView tvShowGoal;

    TextView tvPlanDates, tvNoInternetMessage;
    Button btChangeAccordingToPlanType;

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy");
    LocalDateTime today = LocalDateTime.now();
    String currentDate = dtf.format(today);

    Plan plan;
    DailyMenu dailyMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvShowGoal = (TextView) view.findViewById(R.id.tvShowGoal);

        dailyMenu = DailyMenu.getTodayMenu();

        if(User.getCurrentUser() != null && dailyMenu.getDate().equals(currentDate)){
            plan = User.getCurrentUser().getCurrentPlan();
        }

        gray = ContextCompat.getColor(getActivity(), R.color.gray);
        green = ContextCompat.getColor(getActivity(), R.color.green);
        red = ContextCompat.getColor(getActivity(), R.color.red);

        tvTargetCaloriesAtPlan = (TextView) view.findViewById(R.id.tvTargetCaloriesAtPlan);
        tvTargetCaloriesAtDailyMenu = (TextView) view.findViewById(R.id.tvTargetCaloriesAtDailyMenu);
        tvCaloriesLeft = (TextView) view.findViewById(R.id.tvCaloriesLeft);
        tvCaloriesMultiplication = (TextView) view.findViewById(R.id.tvCaloriesMultiplication);
        pcCalories = (PieChart) view.findViewById(R.id.pcCalories);

        tvTargetProteinsAtPlan = (TextView) view.findViewById(R.id.tvTargetProteinsAtPlan);
        tvTargetProteinsAtDailyMenu = (TextView) view.findViewById(R.id.tvTargetProteinsAtDailyMenu);
        tvProteinsLeft = (TextView) view.findViewById(R.id.tvProteinsLeft);
        tvProteinsMultiplication = (TextView) view.findViewById(R.id.tvProteinsMultiplication);
        pcProteins = (PieChart) view.findViewById(R.id.pcProteins);

        tvTargetFatsAtPlan = (TextView) view.findViewById(R.id.tvTargetFatsAtPlan);
        tvTargetFatsAtDailyMenu = (TextView) view.findViewById(R.id.tvTargetFatsAtDailyMenu);
        tvFatsLeft = (TextView) view.findViewById(R.id.tvFatsLeft);
        tvFatsMultiplication = (TextView) view.findViewById(R.id.tvFatsMultiplication);
        pcFats = (PieChart) view.findViewById(R.id.pcFats);

        tvPlanDates = (TextView) view.findViewById(R.id.tvPlanDates);
        tvNoInternetMessage = (TextView) view.findViewById(R.id.tvNoInternetMessage);
        btChangeAccordingToPlanType = (Button) view.findViewById(R.id.btChangeAccordingToPlanType);

        updatePieCharts();
    }

    public void updatePieCharts(){
        if(!User.getCurrentUser().hasPreviousPlans()){
            plan = User.getCurrentUser().getCurrentPlan();
            dailyMenu = DailyMenu.getTodayMenuFromAllDailyMenus(currentDate);

            tvPlanDates.setVisibility(View.INVISIBLE);
            btChangeAccordingToPlanType.setVisibility(View.GONE);
            tvNoInternetMessage.setVisibility(View.GONE);
        }
        else{
            dailyMenu = DailyMenu.getTodayMenu();
            plan = Plan.receivePlanFromDate(dailyMenu.getDate());

            if(dailyMenu.getDate().equals(currentDate))
                tvPlanDates.setText("Menu and Plan are about: Today");
            else
                tvPlanDates.setText("Menu and Plan are about: " + dailyMenu.getDate());

            tvPlanDates.setVisibility(View.VISIBLE);
            btChangeAccordingToPlanType.setVisibility(View.VISIBLE);
            tvNoInternetMessage.setVisibility(View.GONE);
        }

        tvShowGoal.setText("Your goal is: " + plan.getGoal());

        pcCalories.clearChart();
        pcProteins.clearChart();
        pcFats.clearChart();

        // Calories:
        tvTargetCaloriesAtPlan.setText("Target: " + plan.getTargetCalories());
        tvTargetCaloriesAtDailyMenu.setText("You ate: " + dailyMenu.getTotalCalories());

        int caloriesLeftPercentage = Math.toIntExact(Math.round((dailyMenu.getTotalCalories() / plan.getTargetCalories()) * 100));
        int overCaloriesPie = 1;
        while(caloriesLeftPercentage > 100){
            overCaloriesPie++;
            caloriesLeftPercentage -= 100;
        }

        tvCaloriesMultiplication.setText("X" + overCaloriesPie);

        double caloriesHaveLeft = Math.round((plan.getTargetCalories() - dailyMenu.getTotalCalories()) * 1000) / 1000;
        int caloriesColorProgress, caloriesColorHaveLeft = gray;

        if(caloriesHaveLeft >= 0) {
            tvCaloriesLeft.setText("Have left: " + caloriesHaveLeft);
            caloriesColorProgress = green;
        }
        else {
            tvCaloriesLeft.setText("Exceeded by: " + (caloriesHaveLeft * (-1)));
            caloriesColorProgress = red;

            if(overCaloriesPie > 2)
                caloriesColorHaveLeft = red;
            else
                caloriesColorHaveLeft = green;
        }

        pcCalories.addPieSlice(new PieModel("Calories ate", caloriesLeftPercentage, caloriesColorProgress));
        pcCalories.addPieSlice(new PieModel("Calories left", 100 - caloriesLeftPercentage, caloriesColorHaveLeft));

        pcCalories.startAnimation();


        // Proteins:
        tvTargetProteinsAtPlan.setText("Target: " + plan.getTargetProteins());
        tvTargetProteinsAtDailyMenu.setText("You ate: " + dailyMenu.getTotalProteins());

        int proteinsLeftPercentage = Math.toIntExact(Math.round((dailyMenu.getTotalProteins() / plan.getTargetProteins()) * 100));
        int overProteinsPie = 1;
        while(proteinsLeftPercentage > 100){
            overProteinsPie++;
            proteinsLeftPercentage -= 100;
        }

        tvProteinsMultiplication.setText("X" + overProteinsPie);

        double proteinsHaveLeft = Math.round((plan.getTargetProteins() - dailyMenu.getTotalProteins()) * 1000) / 1000;
        int proteinsColorProgress, proteinsColorHaveLeft = gray;

        if(proteinsHaveLeft >= 0) {
            tvProteinsLeft.setText("Have left: " + proteinsHaveLeft);
            proteinsColorProgress = green;
        }
        else {
            tvProteinsLeft.setText("Exceeded by: " + (proteinsHaveLeft * (-1)));
            proteinsColorProgress = red;

            if(overProteinsPie > 2)
                proteinsColorHaveLeft = red;
            else
                proteinsColorHaveLeft = green;
        }

        pcProteins.addPieSlice(new PieModel("Proteins ate", proteinsLeftPercentage, proteinsColorProgress));
        pcProteins.addPieSlice(new PieModel("Proteins left", 100 - proteinsLeftPercentage, proteinsColorHaveLeft));

        pcProteins.startAnimation();


        // Fats:
        tvTargetFatsAtPlan.setText("Target: " + plan.getTargetFats());
        tvTargetFatsAtDailyMenu.setText("You ate: " + dailyMenu.getTotalFats());

        int fatsLeftPercentage = Math.toIntExact(Math.round((dailyMenu.getTotalFats() / plan.getTargetFats()) * 100));
        int overFatsPie = 1;
        while(fatsLeftPercentage > 100){
            overFatsPie++;
            fatsLeftPercentage -= 100;
        }

        tvFatsMultiplication.setText("X" + overFatsPie);

        double fatsHaveLeft = Math.round((plan.getTargetFats() - dailyMenu.getTotalFats()) * 1000) / 1000;
        int fatsColorProgress, fatsColorHaveLeft = gray;

        if(fatsHaveLeft >= 0) {
            tvFatsLeft.setText("Have left: " + fatsHaveLeft);
            fatsColorProgress = green;
        }
        else {
            tvFatsLeft.setText("Exceeded by: " + (fatsHaveLeft * (-1)));
            fatsColorProgress = red;

            if(overFatsPie > 2)
                fatsColorHaveLeft = red;
            else
                fatsColorHaveLeft = green;
        }

        pcFats.addPieSlice(new PieModel("Fats ate", fatsLeftPercentage, fatsColorProgress));
        pcFats.addPieSlice(new PieModel("Fats left", 100 - fatsLeftPercentage, fatsColorHaveLeft));

        pcFats.startAnimation();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

//        if(viewId == btChangePlanDate.getId())
//            showDatePickerForChoosingPlanDate();
    }
}