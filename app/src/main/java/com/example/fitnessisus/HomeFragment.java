package com.example.fitnessisus;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class HomeFragment extends Fragment implements View.OnClickListener {

    MainActivity.UploadInfoTask uploadInfoTask;

    TextView tvTotalProteinsMain, tvTotalFatsMain, tvTotalCaloriesMain;
    LinearLayout mainActivityLinearLayout;

    MealOverviewFragment mealOverviewFragment;

    MealCirclesListAdapter breakfastMealListAdapter, lunchMealListAdapter, dinnerMealListAdapter;
    ListView lvBreakfastMeals, lvLunchMeals, lvDinnerMeals;

    RelativeLayout changeTodayMenuLayout;
    Button btSelectDailyMenuDate;
    TextView tvDailyMenusDates;

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

        changeTodayMenuLayout = (RelativeLayout) view.findViewById(R.id.changeTodayMenuLayout);
        tvDailyMenusDates = (TextView) view.findViewById(R.id.tvDailyMenusDates);

        btSelectDailyMenuDate = (Button) view.findViewById(R.id.btSelectDailyMenuDate);
        btSelectDailyMenuDate.setOnClickListener(this);

        updateMealsIfNeeded();
    }

    public void showDatePickerForChoosingTodayMenu(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog with the current date as the default
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy");
                        LocalDateTime tmp =  LocalDateTime.of(year, (month + 1), dayOfMonth, 0, 0);
                        String targetDate = tmp.format(dtf);

                        DailyMenu.setTodayMenu(DailyMenu.getTodayMenuFromAllDailyMenus(targetDate));
                        updateMealsIfNeeded();
                    }
                }, year, month, dayOfMonth);

        // Set the maximum date to today
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        // Set the minimum date to a custom date
        String[] oldestDailyMenu = DailyMenu.getTheOldestDailyMenuDate().split("_");
        int oldestDay = Integer.parseInt(oldestDailyMenu[0]);
        int oldestMonth = Integer.parseInt(oldestDailyMenu[1]);
        int oldestYear = Integer.parseInt(oldestDailyMenu[2]);

        calendar.set(oldestYear, (oldestMonth - 1), oldestDay);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        // Show the dialog
        datePickerDialog.show();
    }

    public void removeMealFromDailyMenuAlertDialog(Meal meal, String mealType){
        AlertDialog ad;
        AlertDialog.Builder adb;
        adb = new AlertDialog.Builder(getActivity());
        adb.setTitle("What do you want to do?");
        adb.setMessage("You choose the meal or ingredient: " + meal.getName());
        adb.setIcon(R.drawable.ic_delete_icon);
        adb.setCancelable(true);

        adb.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getActivity(), meal.getName() + " deleted successfully.", Toast.LENGTH_SHORT).show();
                todayMenu.removeMeal(meal, mealType);
                updateMealsIfNeeded();
            }
        });

        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        ad = adb.create();
        ad.show();
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
        DailyMenu.saveDailyMenuIntoFile(todayMenu, getActivity());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy");
        LocalDateTime today = LocalDateTime.now();
        String currentDate = dtf.format(today);

        if(todayMenu.getDate().equals(currentDate))
            tvDailyMenusDates.setText("Today menu is about: Today");
        else
            tvDailyMenusDates.setText("Today menu is about: " + todayMenu.getDate());

        uploadInfoTask = new MainActivity.UploadInfoTask(getActivity());
        uploadInfoTask.execute();

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

            lvBreakfastMeals.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Meal selectedItem = (Meal) parent.getItemAtPosition(position);

                    removeMealFromDailyMenuAlertDialog(selectedItem, "Breakfast");
                    return true;
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

            lvLunchMeals.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Meal selectedItem = (Meal) parent.getItemAtPosition(position);

                    removeMealFromDailyMenuAlertDialog(selectedItem, "Lunch");
                    return true;
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

            lvDinnerMeals.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Meal selectedItem = (Meal) parent.getItemAtPosition(position);

                    removeMealFromDailyMenuAlertDialog(selectedItem, "Dinner");
                    return true;
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

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if(viewId == btSelectDailyMenuDate.getId())
            showDatePickerForChoosingTodayMenu();
    }
}