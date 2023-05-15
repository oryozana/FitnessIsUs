package com.example.fitnessisus;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

public class StatisticsFragment extends Fragment {

    TextView tvTargetCaloriesAtPlan, tvTargetCaloriesAtDailyMenu, tvCaloriesLeft;
    PieChart pcCalories;

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

        Plan currentPlan = User.getCurrentUser().getCurrentPlan();
        DailyMenu todayMenu = DailyMenu.getTodayMenu();

        tvTargetCaloriesAtPlan = (TextView) view.findViewById(R.id.tvTargetCaloriesAtPlan);
        tvTargetCaloriesAtDailyMenu = (TextView) view.findViewById(R.id.tvTargetCaloriesAtDailyMenu);
        tvCaloriesLeft = (TextView) view.findViewById(R.id.tvCaloriesLeft);
        pcCalories = (PieChart) view.findViewById(R.id.pcCalories);

        tvTargetCaloriesAtPlan.setText("Target: " + currentPlan.getTargetCalories());
        tvTargetCaloriesAtDailyMenu.setText("You ate: " + todayMenu.getTotalCalories());

        double caloriesHaveLeft = Math.round((currentPlan.getTargetCalories() - todayMenu.getTotalCalories()) * 1000) / 1000;
        if(caloriesHaveLeft >= 0)
            tvCaloriesLeft.setText("Have left: " + caloriesHaveLeft);
        else
            tvCaloriesLeft.setText("Exceeded by: " + (caloriesHaveLeft * (-1)));

        int caloriesLeftPercentage = Math.toIntExact(Math.floorDiv((int) todayMenu.getTotalCalories() , (int) currentPlan.getTargetCalories())) * 100;

        Toast.makeText(getActivity(), caloriesLeftPercentage + "%", Toast.LENGTH_SHORT).show();
        pcCalories.addPieSlice(new PieModel("Calories left", caloriesLeftPercentage, Color.parseColor("#FFA726")));
//        pcCalories.addPieSlice(new PieModel("Calories ate", Integer.parseInt(tvTargetCaloriesAtDailyMenu.getText().toString().split(": ")[1]), Color.parseColor("#66BB6A")));

        pcCalories.startAnimation();

    }


}