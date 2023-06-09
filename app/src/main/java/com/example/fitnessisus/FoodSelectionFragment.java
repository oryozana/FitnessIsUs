package com.example.fitnessisus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FoodSelectionFragment extends Fragment implements View.OnClickListener {

    MainActivity.UploadInfoTask uploadInfoTask;

    CustomMealsFragment customMealsFragment = new CustomMealsFragment();
    MealOverviewFragment mealOverviewFragment;

    LinearLayout linearLayout, loadingWorldSavedCustomMealsLinearLayout;
    Button btSwitchBetweenLocalAndGlobalFood;
    EditText etFilterFood;
    ListView listView;

    DailyMenu todayMenu = DailyMenu.getTodayMenu();
    ArrayList<Meal> internetMealsList = new ArrayList<Meal>();
    MealListAdapter mealsAdapter;

    ArrayList<Ingredient> ingredients;
    IngredientListAdapter ingredientsAdapter;

    boolean isOnLocalMode = true;

    DatabaseReference databaseReference;

    String cameFrom;

    public FoodSelectionFragment(){
        cameFrom = "";
    }

    public FoodSelectionFragment(String fromWhere){
        cameFrom = fromWhere;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_food_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = (ListView) view.findViewById(R.id.listViewFood);

        loadingWorldSavedCustomMealsLinearLayout = (LinearLayout) view.findViewById(R.id.loadingWorldSavedCustomMealsLinearLayout);
        loadingWorldSavedCustomMealsLinearLayout.setVisibility(View.GONE);
        linearLayout = (LinearLayout) view.findViewById(R.id.foodSelectionLinearLayout);
        linearLayout.setVisibility(View.VISIBLE);

        btSwitchBetweenLocalAndGlobalFood = (Button) view.findViewById(R.id.btSwitchBetweenLocalAndGlobalFood);
        btSwitchBetweenLocalAndGlobalFood.setOnClickListener(this);

        etFilterFood = (EditText) view.findViewById(R.id.etFilterFood);
        etFilterFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(isOnLocalMode)
                    ingredientsAdapter.getFilter().filter(s);
                else
                    mealsAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if(cameFrom.equals("CustomMealsFragment"))
            btSwitchBetweenLocalAndGlobalFood.setVisibility(View.INVISIBLE);

        isOnLocalMode = true;

        setIngredientListViewAdapters();
    }

    public void setIngredientListViewAdapters() {
        ingredients = Ingredient.getIngredientsList();

        ingredientsAdapter = new IngredientListAdapter(getActivity(), ingredients);
        listView.setAdapter(ingredientsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Ingredient selectedItem = (Ingredient) parent.getItemAtPosition(position);

                ingredientOverviewAlertDialog(selectedItem);
            }
        });
    }

    public void setInternetListViewAdapter(){
        initiateInternetListViewFields();

        mealsAdapter = new MealListAdapter(getActivity(), internetMealsList);
        listView.setAdapter(mealsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Meal selectedItem = (Meal) adapterView.getItemAtPosition(i);
                selectedItem.setName(selectedItem.getName().split(" - ")[1]);

                mealOverviewFragment = new MealOverviewFragment("FoodSelectionFragment", selectedItem);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainActivityFrameLayout, mealOverviewFragment).commit();
            }
        });
    }

    public void initiateInternetListViewFields(){
        loadingWorldSavedCustomMealsLinearLayout.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.GONE);

        internetMealsList.clear();
        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");
        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                ArrayList<Ingredient> ingredientsNeededInfo = new ArrayList<Ingredient>();
                Ingredient ingredient;
                String mealName, ingredientName;
                double ingredientGrams;

                if(isAdded() && isVisible() && getUserVisibleHint()) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            DataSnapshot dataSnapshot = task.getResult();
                            for (DataSnapshot customMeal : dataSnapshot.getChildren()) {
                                ingredientsNeededInfo.clear();
                                mealName = customMeal.getKey();
                                for (int i = 0; i < customMeal.getChildrenCount(); i++) {
                                    ingredientName = (customMeal.child(i + "").child("name").getValue().toString());
                                    ingredientGrams = Double.parseDouble((customMeal.child(i + "").child("grams").getValue().toString()));

                                    ingredient = new Ingredient(Ingredient.getIngredientByName(ingredientName), ingredientGrams);
                                    ingredientsNeededInfo.add(ingredient);
                                }
                                internetMealsList.add(new Meal(mealName, ingredientsNeededInfo));
                                mealsAdapter.notifyDataSetChanged();
                            }
                        }
                        else {
                            notEvenOneCustomMealAdded();
                        }
                    }
                    else {
                        recipesDatabaseNotFound();
                        Toast.makeText(getActivity(), "Failed to load meals.", Toast.LENGTH_SHORT).show();
                    }
                }

                loadingWorldSavedCustomMealsLinearLayout.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    public void recipesDatabaseNotFound(){
        AlertDialog ad;
        AlertDialog.Builder adb;
        adb = new AlertDialog.Builder(getActivity());
        adb.setTitle("Custom meals not found!");
        adb.setMessage("We have trouble connecting our database right now, please come back later");
        adb.setIcon(R.drawable.ic_error_icon);
        adb.setCancelable(false);

        adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switchBetweenLocalAndGlobalFood();
            }
        });

        ad = adb.create();
        ad.show();
    }

    public void notEvenOneCustomMealAdded(){
        AlertDialog ad;
        AlertDialog.Builder adb;
        adb = new AlertDialog.Builder(getActivity());
        adb.setTitle("Custom meals not found!");
        adb.setMessage("It's seems like no one saved any custom meal so far, you can be the first!.");
        adb.setIcon(R.drawable.ic_food_icon);
        adb.setCancelable(false);

        adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switchBetweenLocalAndGlobalFood();
            }
        });

        ad = adb.create();
        ad.show();
    }

    public void ingredientOverviewAlertDialog(Ingredient ingredient){
        AlertDialog ad;
        AlertDialog.Builder adb;
        adb = new AlertDialog.Builder(getActivity());

        Ingredient tmpIngredient = new Ingredient(ingredient);

        View customAlertDialog = LayoutInflater.from(getActivity()).inflate(R.layout.alert_dialog_ingredient_overview, null);
        TextView tvAlertDialogIngredientName = (TextView) customAlertDialog.findViewById(R.id.tvAlertDialogIngredientName);
        tvAlertDialogIngredientName.setText("Name: " + ingredient.getName());
        TextView tvAlertDialogIngredientCalories = (TextView) customAlertDialog.findViewById(R.id.tvAlertDialogIngredientCalories);
        tvAlertDialogIngredientCalories.setText("Calories: " + ingredient.getCalories());
        TextView tvAlertDialogIngredientProteins = (TextView) customAlertDialog.findViewById(R.id.tvAlertDialogIngredientProteins);
        tvAlertDialogIngredientProteins.setText("Proteins: " + ingredient.getProteins());
        TextView tvAlertDialogIngredientFats = (TextView) customAlertDialog.findViewById(R.id.tvAlertDialogIngredientFats);
        tvAlertDialogIngredientFats.setText("Fats: " + ingredient.getFats());

        ImageView ivAlertDialogIngredientImage = (ImageView) customAlertDialog.findViewById(R.id.ivAlertDialogIngredientImage);
        ivAlertDialogIngredientImage.setImageResource(ingredient.getImgId());

        Spinner sAlertDialogSelectMeal = (Spinner) customAlertDialog.findViewById(R.id.sAlertDialogSelectMeal);
        String[] selectMealOptions = new String[]{"Breakfast", "Lunch", "Dinner"};
        ArrayAdapter<String> alertDialogSelectedMealAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, selectMealOptions);
        sAlertDialogSelectMeal.setAdapter(alertDialogSelectedMealAdapter);

        LinearLayout alertDialogMealSelectionLinearLayout = (LinearLayout) customAlertDialog.findViewById(R.id.alertDialogMealSelectionLinearLayout);

        if(cameFrom.equals("CustomMealsFragment")) {
            alertDialogMealSelectionLinearLayout.setVisibility(View.INVISIBLE);
            sAlertDialogSelectMeal.setVisibility(View.INVISIBLE);
        }

        EditText etAlertDialogIngredientGrams = customAlertDialog.findViewById(R.id.etAlertDialogIngredientGrams);
        etAlertDialogIngredientGrams.setText("100");
        etAlertDialogIngredientGrams.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!etAlertDialogIngredientGrams.getText().toString().equals("")){
                    tmpIngredient.setGrams(Integer.parseInt(etAlertDialogIngredientGrams.getText().toString()));
                    tvAlertDialogIngredientCalories.setText("Calories: " + tmpIngredient.getCalories());
                    tvAlertDialogIngredientProteins.setText("Proteins: " + tmpIngredient.getProteins());
                    tvAlertDialogIngredientFats.setText("Fats: " + tmpIngredient.getFats());
                }
                else{
                    tvAlertDialogIngredientCalories.setText("Calories: 0");
                    tvAlertDialogIngredientProteins.setText("Proteins: 0");
                    tvAlertDialogIngredientFats.setText("Fats: 0");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        adb.setView(customAlertDialog);
        ad = adb.create();

        Button btAlertDialogConfirmIngredient = (Button) customAlertDialog.findViewById(R.id.btAlertDialogConfirmIngredient);
        btAlertDialogConfirmIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean passTests = true;
                if(etAlertDialogIngredientGrams.getText().toString().replaceAll(" ", "").equals("")){
                    Toast.makeText(getActivity(), "Must enter grams value first.", Toast.LENGTH_SHORT).show();
                    passTests = false;
                }

                if(etAlertDialogIngredientGrams.getText().toString().replaceAll("0", "").equals("") && passTests) {
                    Toast.makeText(getActivity(), "Must be more than 0 grams.", Toast.LENGTH_SHORT).show();
                    passTests = false;
                }

                if(sAlertDialogSelectMeal.getSelectedItem() == null && passTests) {
                    Toast.makeText(getActivity(), "Choose breakfast, lunch or dinner.", Toast.LENGTH_SHORT).show();
                    passTests = false;
                }

                if(passTests){
                    String selectedMeal = sAlertDialogSelectMeal.getSelectedItem().toString();
                    int grams = Integer.parseInt(etAlertDialogIngredientGrams.getText().toString());

                    if(cameFrom.equals("CustomMealsFragment")) {
                        DailyMenu.getCustomMeal().addNeededIngredientForMeal(getActivity(), new Ingredient(Ingredient.getIngredientByName(ingredient.getName()), grams), "");
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainActivityFrameLayout, customMealsFragment).commit();
                    }
                    else
                        todayMenu.addIngredientByMealName(getActivity(), selectedMeal, ingredient, grams);

                    uploadInfoTask = new MainActivity.UploadInfoTask(getActivity());
                    uploadInfoTask.execute();

                    DailyMenu.saveDailyMenuIntoFile(todayMenu, getActivity());

                    ad.cancel();
                }
            }
        });

        Button btAlertDialogCancelIngredient = (Button) customAlertDialog.findViewById(R.id.btAlertDialogCancelIngredient);
        btAlertDialogCancelIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.cancel();
            }
        });

        ad.show();
    }

    public void switchBetweenLocalAndGlobalFood(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);  // Set initial value.
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isInternetConnectionAvailable = (networkInfo != null && networkInfo.isConnected());

        if(isInternetConnectionAvailable || !isOnLocalMode){
            if(!etFilterFood.getText().toString().equals(""))
                etFilterFood.setText("");

            isOnLocalMode = !isOnLocalMode;  // Must be after the filter reset... if not produce error.

            if(isOnLocalMode){
                btSwitchBetweenLocalAndGlobalFood.setText("Choose from internet");
                setIngredientListViewAdapters();
            }
            else{
                btSwitchBetweenLocalAndGlobalFood.setText("Choose from local");
                setInternetListViewAdapter();
            }
        }
        else
            noInternetAccessAlertDialog();
    }

    public void noInternetAccessAlertDialog(){
        AlertDialog ad;
        AlertDialog.Builder adb;
        adb = new AlertDialog.Builder(getActivity());
        adb.setTitle("Oh no...");
        adb.setMessage("Internet connection unavailable!" + "\n" + "Connect to the internet and try again.");
        adb.setIcon(R.drawable.ic_network_not_found);

        adb.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        ad = adb.create();
        ad.show();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if(viewId == btSwitchBetweenLocalAndGlobalFood.getId())
            switchBetweenLocalAndGlobalFood();
    }
}