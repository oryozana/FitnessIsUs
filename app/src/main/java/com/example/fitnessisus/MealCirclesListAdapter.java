package com.example.fitnessisus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class MealCirclesListAdapter extends ArrayAdapter<Meal> {
    public MealCirclesListAdapter(Context context, ArrayList<Meal> meals){
        super(context, R.layout.list_view_circles_meal, meals);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        Meal meal = getItem(position);

        convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_circles_meal, parent, false);

        TextView tvMealName = convertView.findViewById(R.id.tvCircleListMealName);
        TextView tvMealDescription = convertView.findViewById(R.id.tvCircleListMealDescription);

        tvMealName.setText(meal.getName() + ":");
        tvMealDescription.setText(meal.getGrams() + " grams and " + meal.getCalories() + " calories.");

        return convertView;
    }
}
