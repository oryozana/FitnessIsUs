package com.example.fitnessisus;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    MainActivity.MyAsyncClass mac;

    TextView tvTotalProteinsMain, tvTotalFatsMain, tvTotalCaloriesMain;
    LinearLayout mainActivityLinearLayout;

    MealOverviewFragment mealOverviewFragment;

    MealCirclesListAdapter breakfastMealListAdapter, lunchMealListAdapter, dinnerMealListAdapter;
    ListView lvBreakfastMeals, lvLunchMeals, lvDinnerMeals;

    DailyMenu todayMenu = DailyMenu.getTodayMenu();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalCaloriesMain = (TextView) view.findViewById(R.id.tvTotalCaloriesMain);
        tvTotalProteinsMain = (TextView) view.findViewById(R.id.tvTotalProteinsMain);
        tvTotalFatsMain = (TextView) view.findViewById(R.id.tvTotalFatsMain);

        mainActivityLinearLayout = (LinearLayout) view.findViewById(R.id.mainActivityLinearLayout);

        lvBreakfastMeals = (ListView) view.findViewById(R.id.lvBreakfastMeals);
        lvLunchMeals = (ListView) view.findViewById(R.id.lvLunchMeals);
        lvDinnerMeals = (ListView) view.findViewById(R.id.lvDinnerMeals);

        updateMealsIfNeeded();
    }


    @Override
    public void onResume() {
        super.onResume();
        updateMealsIfNeeded();
    }

    public void updateMealsIfNeeded(){
        int totalHeight;
        int mealsAmount;

        todayMenu = DailyMenu.getTodayMenu();
        todayMenu.correctNutritiousValues();

        mac = new MainActivity.MyAsyncClass(getActivity());
        mac.execute();

        if(todayMenu.hasBreakfast()) {
            lvBreakfastMeals.setVisibility(View.VISIBLE);
            breakfastMealListAdapter = new MealCirclesListAdapter(getActivity(), todayMenu.getBreakfastAsMealsTypeOnly());
            lvBreakfastMeals.setAdapter(breakfastMealListAdapter);

            lvBreakfastMeals.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Meal selectedItem = (Meal) adapterView.getItemAtPosition(i);

                    mealOverviewFragment = new MealOverviewFragment(selectedItem, "Breakfast");
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainActivityFrameLayout, mealOverviewFragment).commit();
                }
            });

            totalHeight = 0;
            mealsAmount = todayMenu.getBreakfast().size();

            ViewGroup.LayoutParams params = lvBreakfastMeals.getLayoutParams();
            if(mealsAmount < 3) {
                for (int i = 0; i < mealsAmount; i++) {
                    View listItem = breakfastMealListAdapter.getView(i, null, lvBreakfastMeals);
                    listItem.measure(0, 0);
                    totalHeight += listItem.getMeasuredHeight();
                }
                params.height = (totalHeight + (lvBreakfastMeals.getDividerHeight() * (mealsAmount)));
            }
            else {
                for (int i = 0; i < 3; i++) {
                    View listItem = breakfastMealListAdapter.getView(i, null, lvBreakfastMeals);
                    listItem.measure(0, 0);
                    totalHeight += listItem.getMeasuredHeight();
                }
                params.height = (totalHeight + (lvBreakfastMeals.getDividerHeight() * 3));
            }
            lvBreakfastMeals.setLayoutParams(params);
        }
        else
            lvBreakfastMeals.setVisibility(View.GONE);

        if(todayMenu.hasLunch()){
            lvLunchMeals.setVisibility(View.VISIBLE);
            lunchMealListAdapter = new MealCirclesListAdapter(getActivity(), todayMenu.getLunchAsMealsTypeOnly());
            lvLunchMeals.setAdapter(lunchMealListAdapter);

            lvLunchMeals.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Meal selectedItem = (Meal) adapterView.getItemAtPosition(i);

                    mealOverviewFragment = new MealOverviewFragment(selectedItem, "Lunch");
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainActivityFrameLayout, mealOverviewFragment).commit();
                }
            });

            totalHeight = 0;
            mealsAmount = todayMenu.getLunch().size();

            ViewGroup.LayoutParams params = lvLunchMeals.getLayoutParams();
            if(mealsAmount < 3) {
                for (int i = 0; i < mealsAmount; i++) {
                    View listItem = lunchMealListAdapter.getView(i, null, lvLunchMeals);
                    listItem.measure(0, 0);
                    totalHeight += listItem.getMeasuredHeight();
                }
                params.height = (totalHeight + (lvLunchMeals.getDividerHeight() * (mealsAmount)));
            }
            else {
                for (int i = 0; i < 3; i++) {
                    View listItem = lunchMealListAdapter.getView(i, null, lvLunchMeals);
                    listItem.measure(0, 0);
                    totalHeight += listItem.getMeasuredHeight();
                }
                params.height = (totalHeight + (lvLunchMeals.getDividerHeight() * 3));
            }
            lvLunchMeals.setLayoutParams(params);
        }
        else
            lvLunchMeals.setVisibility(View.GONE);

        if(todayMenu.hasDinner()){
            lvDinnerMeals.setVisibility(View.VISIBLE);
            dinnerMealListAdapter = new MealCirclesListAdapter(getActivity(), todayMenu.getDinnerAsMealsTypeOnly());
            lvDinnerMeals.setAdapter(dinnerMealListAdapter);

            lvDinnerMeals.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Meal selectedItem = (Meal) adapterView.getItemAtPosition(i);

                    mealOverviewFragment = new MealOverviewFragment(selectedItem, "Dinner");
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainActivityFrameLayout, mealOverviewFragment).commit();
                }
            });

            totalHeight = 0;
            mealsAmount = todayMenu.getDinner().size();

            ViewGroup.LayoutParams params = lvDinnerMeals.getLayoutParams();
            if(mealsAmount < 3) {
                for (int i = 0; i < mealsAmount; i++) {
                    View listItem = dinnerMealListAdapter.getView(i, null, lvDinnerMeals);
                    listItem.measure(0, 0);
                    totalHeight += listItem.getMeasuredHeight();
                }
                params.height = (totalHeight + (lvDinnerMeals.getDividerHeight() * (mealsAmount)));
            }
            else {
                for (int i = 0; i < 3; i++) {
                    View listItem = dinnerMealListAdapter.getView(i, null, lvDinnerMeals);
                    listItem.measure(0, 0);
                    totalHeight += listItem.getMeasuredHeight();
                }
                params.height = (totalHeight + (lvDinnerMeals.getDividerHeight() * 3));
            }
            lvDinnerMeals.setLayoutParams(params);
        }
        else
            lvDinnerMeals.setVisibility(View.GONE);

        tvTotalProteinsMain.setText("Total Proteins: " + todayMenu.getTotalProteins() + " .");
        tvTotalFatsMain.setText("Total Fats: " + todayMenu.getTotalFats() + " .");
        tvTotalCaloriesMain.setText("Total calories: " + todayMenu.getTotalCalories() + " .");
    }
}