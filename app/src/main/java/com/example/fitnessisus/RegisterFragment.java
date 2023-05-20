package com.example.fitnessisus;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RegisterFragment extends Fragment implements View.OnClickListener {

    EditText etGetUsername, etGetPassword, etGetEmail, etGetStartingWeight;
    EditText etGetTargetCalories, etGetTargetProteins, etGetTargetFats;
    LinearLayout linearLayout, registerLoadingLinearLayout;
    CheckBox cbRememberRegisteredUserInLocalDatabase;
    Button btRegister, btGetHelpCreatingAPlan;

    FileAndDatabaseHelper fileAndDatabaseHelper;

    Plan currentGeneratedPlan, maintainWeightPlan, loseWeightPlan, gainWeightPlan;
    int currentPlanIndex = 1, maxPlansAmount = 3;
    String chosenGoal = "Custom";

    ArrayList<String> usernamesList = new ArrayList<String>();
    int userPicturesAmount = 10;

    FirebaseDatabase usersDb;
    DatabaseReference databaseReference;

    String pattern = "^[a-zA-Z0-9 ]+$"; // Only allows letters and numbers

    Intent me;

    public RegisterFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        me = getActivity().getIntent();

        registerLoadingLinearLayout = (LinearLayout) view.findViewById(R.id.registerLoadingLinearLayout);
        linearLayout = (LinearLayout) view.findViewById(R.id.registerLinearLayout);

        etGetUsername = (EditText) view.findViewById(R.id.etGetUsername);
        etGetPassword = (EditText) view.findViewById(R.id.etGetPassword);
        etGetEmail = (EditText) view.findViewById(R.id.etGetEmail);
        etGetStartingWeight = (EditText) view.findViewById(R.id.etGetStartingWeight);

        etGetTargetCalories = (EditText) view.findViewById(R.id.etGetTargetCalories);
        etGetTargetProteins = (EditText) view.findViewById(R.id.etGetTargetProteins);
        etGetTargetFats = (EditText) view.findViewById(R.id.etGetTargetFats);

        btGetHelpCreatingAPlan = (Button) view.findViewById(R.id.btGetHelpCreatingAPlan);
        btGetHelpCreatingAPlan.setOnClickListener(this);
        btRegister = (Button) view.findViewById(R.id.btRegister);
        btRegister.setOnClickListener(this);

        cbRememberRegisteredUserInLocalDatabase = (CheckBox) view.findViewById(R.id.cbRememberRegisteredUserInLocalDatabase);

        fileAndDatabaseHelper = new FileAndDatabaseHelper(getActivity(), me);

        getAllExistingUsernames();
    }

    public void createUserAndUserPlan(){
        if(passUserInfoTests()){  // Not to be included in the User(dataSnapshot) builder.
            String targetCalories = etGetTargetCalories.getText().toString();
            String targetProteins = etGetTargetProteins.getText().toString();
            String targetFats = etGetTargetFats.getText().toString();
            Plan userPlan = new Plan(targetCalories, targetProteins, targetFats);

            if(Plan.isTheSamePlan(userPlan, currentGeneratedPlan))
                userPlan.setGoal(chosenGoal);

            String username = etGetUsername.getText().toString();
            String password = etGetPassword.getText().toString();
            String email = etGetEmail.getText().toString();
            double startingWeight = Double.parseDouble(etGetStartingWeight.getText().toString());

            int profilePictureId = getResources().getIdentifier("user_picture_" + (((int)(Math.random() * userPicturesAmount)) + 1), "drawable", getActivity().getPackageName());
            User user = new User(username, password, email, startingWeight, userPlan, profilePictureId, DailyMenu.generateEmptyDailyMenuDescriptionForFiles());

            linearLayout.setVisibility(View.GONE);
            registerLoadingLinearLayout.setVisibility(View.VISIBLE);

            saveUserInFirebaseAndAsPrimaryUserIfNeeded(user);
        }
    }

    public void saveUserInFirebaseAndAsPrimaryUserIfNeeded(User user){
        usersDb = FirebaseDatabase.getInstance();
        databaseReference = usersDb.getReference("users");
        databaseReference.child(user.getUsername()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(), "User successfully created.", Toast.LENGTH_SHORT).show();

                    fileAndDatabaseHelper.removePrimaryUser();
                    if(cbRememberRegisteredUserInLocalDatabase.isChecked())
                        fileAndDatabaseHelper.setPrimaryUser(user);

                    User.setCurrentUser(user);
                    DailyMenu.restartDailyMenusFile(getActivity());
                    DailyMenu.setTodayMenu(null);
                    DailyMenu.setDailyMenus(getActivity());
                    DailyMenu.saveDailyMenuIntoFile(DailyMenu.generateDailyMenuObjectFromFile(DailyMenu.generateEmptyDailyMenuDescriptionForFiles()), getActivity());

                    me.setClass(getActivity(), MainActivity.class);
                    startActivity(me);
                }
                else
                    Toast.makeText(getActivity(), "Failed to create user, please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean passUserInfoTests(){
        boolean passTests = true;

        if(etGetUsername.getText().toString().replaceAll(" ", "").equals("")){
            Toast.makeText(getActivity(), "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
            passTests = false;
        }
        else{
            if(isUserAlreadyExistsInFirebase(etGetUsername.getText().toString())){
                Toast.makeText(getActivity(), "Username already exists.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }

            if(!etGetUsername.getText().toString().matches(pattern) && passTests){
                Toast.makeText(getActivity(), "Username can contain letters and numbers only.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
        }

        if(etGetPassword.getText().toString().replaceAll(" ", "").equals("") && passTests) {
            Toast.makeText(getActivity(), "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
            passTests = false;
        }
        else{
            if(etGetPassword.getText().toString().length() < 4 && passTests){
                Toast.makeText(getActivity(), "Password should be at least 4 characters wide.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }

            if(etGetPassword.getText().toString().length() > 16 && passTests){
                Toast.makeText(getActivity(), "Password should be at most 16 characters wide.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
        }

        if(etGetEmail.getText().toString().replaceAll(" ", "").equals("") && passTests) {
            Toast.makeText(getActivity(), "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
            passTests = false;
        }
        else{
            if(!etGetEmail.getText().toString().contains("@") && passTests){
                Toast.makeText(getActivity(), "Email address should have @ in them.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }

            if(!etGetEmail.getText().toString().contains(".") && passTests){
                Toast.makeText(getActivity(), "Email address should have . in them.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
        }

        if(passTests){
            if(etGetStartingWeight.getText().toString().replaceAll(" ", "").equals("")) {
                Toast.makeText(getActivity(), "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
            else{
                if(!(0 < Double.parseDouble(etGetStartingWeight.getText().toString()) && Double.parseDouble(etGetStartingWeight.getText().toString()) < 500)){
                    Toast.makeText(getActivity(), "Starting weight should be between 0 to 500 kg.", Toast.LENGTH_SHORT).show();
                    passTests = false;
                }
            }
        }

        if(passTests){
            if(etGetTargetCalories.getText().toString().replaceAll(" ", "").equals("")){
                Toast.makeText(getActivity(), "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
            else{
                if(!(0 < Double.parseDouble(etGetTargetCalories.getText().toString()) && Double.parseDouble(etGetTargetCalories.getText().toString()) < 5000)){
                    Toast.makeText(getActivity(), "Target calories should be between 0 to 5000.", Toast.LENGTH_SHORT).show();
                    passTests = false;
                }
            }
        }

        if(passTests){
            if(etGetTargetProteins.getText().toString().replaceAll(" ", "").equals("")){
                Toast.makeText(getActivity(), "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
            else{
                if(!(0 < Double.parseDouble(etGetTargetProteins.getText().toString()) && Double.parseDouble(etGetTargetProteins.getText().toString()) < 1000)){
                    Toast.makeText(getActivity(), "Target proteins should be between 0 to 1000.", Toast.LENGTH_SHORT).show();
                    passTests = false;
                }
            }
        }

        if(passTests){
            if(etGetTargetFats.getText().toString().replaceAll(" ", "").equals("")){
                Toast.makeText(getActivity(), "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
            else{
                if(!(0 < Double.parseDouble(etGetTargetFats.getText().toString()) && Double.parseDouble(etGetTargetFats.getText().toString()) < 1000)){
                    Toast.makeText(getActivity(), "Target fats should be between 0 to 1000.", Toast.LENGTH_SHORT).show();
                    passTests = false;
                }
            }
        }

        return passTests;
    }

    public boolean isUserAlreadyExistsInFirebase(String username){
        for(int i = 0; i < usernamesList.size(); i++){
            if(usernamesList.get(i).equals(username))
                return true;
        }
        return false;
    }

    public void getAllExistingUsernames(){
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        DataSnapshot dataSnapshot = task.getResult();
                        for(DataSnapshot user : dataSnapshot.getChildren())
                            usernamesList.add(user.getKey());
                    }
                }
                else
                    Toast.makeText(getActivity(), "Failed to load users.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void generatePlanAlertDialog(){
        AlertDialog ad;
        AlertDialog.Builder adb;
        adb = new AlertDialog.Builder(getActivity());

        View customAlertDialog = LayoutInflater.from(getActivity()).inflate(R.layout.alert_dialog_generate_plan, null);

        LinearLayout generatePlansLinearLayout = (LinearLayout) customAlertDialog.findViewById(R.id.generatePlansLinearLayout);
        EditText etGetAge = (EditText) customAlertDialog.findViewById(R.id.etGetAge);
        EditText etGetHeight = (EditText) customAlertDialog.findViewById(R.id.etGetHeight);
        EditText etGetWeight = (EditText) customAlertDialog.findViewById(R.id.etGetWeight);
        Spinner sHowActiveAreYou = (Spinner) customAlertDialog.findViewById(R.id.sHowActiveAreYou);
        RadioGroup rgChooseGender = (RadioGroup) customAlertDialog.findViewById(R.id.rgChooseGender);
        Button btGeneratePlans = (Button) customAlertDialog.findViewById(R.id.btGeneratePlans);

        ArrayAdapter<String> alertDialogAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, Plan.activeLevelOptions);
        sHowActiveAreYou.setAdapter(alertDialogAdapter);

        LinearLayout showPlansLinearLayout = (LinearLayout) customAlertDialog.findViewById(R.id.showPlansLinearLayout);
        TextView tvPlanName = (TextView) customAlertDialog.findViewById(R.id.tvPlanName);
        TextView tvPlanCalories = (TextView) customAlertDialog.findViewById(R.id.tvPlanCalories);
        TextView tvPlanProteins = (TextView) customAlertDialog.findViewById(R.id.tvPlanProteins);
        TextView tvPlanFats = (TextView) customAlertDialog.findViewById(R.id.tvPlanFats);
        ImageButton ibtPreviousPlan = (ImageButton) customAlertDialog.findViewById(R.id.ibtPreviousPlan);
        TextView tvPlanNumberOutOf = (TextView) customAlertDialog.findViewById(R.id.tvPlanNumberOutOf);
        ImageButton ibtNextPlan = (ImageButton) customAlertDialog.findViewById(R.id.ibtNextPlan);
        Button btChoosePlan = (Button) customAlertDialog.findViewById(R.id.btChoosePlan);
        Button btCancelPlan = (Button) customAlertDialog.findViewById(R.id.btCancelPlan);

        adb.setView(customAlertDialog);
        ad = adb.create();

        btGeneratePlans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String testWeight = etGetWeight.getText().toString();
                String testHeight = etGetHeight.getText().toString();
                String testAge = etGetAge.getText().toString();
                String activeLevel = sHowActiveAreYou.getSelectedItem().toString();

                if(passAlertDialogInfoTests(testWeight, testHeight, testAge, activeLevel)) {
                    double weight = Double.parseDouble(testWeight);
                    double height = Double.parseDouble(testHeight);
                    int age = Integer.parseInt(testAge);

                    String gender;
                    if(rgChooseGender.getCheckedRadioButtonId() == R.id.rbChooseMale)
                        gender = "Male";
                    else
                        gender = "Female";

                    loseWeightPlan = new Plan("Lose weight", gender, weight, height, age, activeLevel);
                    maintainWeightPlan = new Plan("Maintain weight", gender, weight, height, age, activeLevel);
                    gainWeightPlan = new Plan("Gain weight", gender, weight, height, age, activeLevel);

                    generatePlansLinearLayout.setVisibility(View.GONE);
                    showPlansLinearLayout.setVisibility(View.VISIBLE);

                    tvPlanName.setText("Name: Lose weight");
                    tvPlanCalories.setText("Calories: " + loseWeightPlan.getTargetCalories());
                    tvPlanProteins.setText("Proteins: " + loseWeightPlan.getTargetProteins());
                    tvPlanFats.setText("Fats: " + loseWeightPlan.getTargetFats());

                    tvPlanNumberOutOf.setText("Plan number 1 out of " + maxPlansAmount);
                    ibtPreviousPlan.setVisibility(View.INVISIBLE);
                    ibtNextPlan.setVisibility(View.VISIBLE);

                    currentPlanIndex = 1;
                }
            }
        });

        ibtPreviousPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(0 < currentPlanIndex && currentPlanIndex <= maxPlansAmount){
                    currentPlanIndex--;

                    if(currentPlanIndex == 1)
                        currentGeneratedPlan = loseWeightPlan;
                    if(currentPlanIndex == 2)
                        currentGeneratedPlan = maintainWeightPlan;
                    if(currentPlanIndex == 3)
                        currentGeneratedPlan = gainWeightPlan;

                    tvPlanName.setText("Name: " + currentGeneratedPlan.getGoal());
                    tvPlanCalories.setText("Calories: " + currentGeneratedPlan.getTargetCalories());
                    tvPlanProteins.setText("Proteins: " + currentGeneratedPlan.getTargetProteins());
                    tvPlanFats.setText("Fats: " + currentGeneratedPlan.getTargetFats());

                    tvPlanNumberOutOf.setText("Plan number " + currentPlanIndex + " out of " + maxPlansAmount);

                    ibtNextPlan.setVisibility(View.VISIBLE);
                    if(currentPlanIndex == 1)
                        ibtPreviousPlan.setVisibility(View.INVISIBLE);
                }
            }
        });

        ibtNextPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(0 <= currentPlanIndex && currentPlanIndex < maxPlansAmount) {
                    currentPlanIndex++;

                    if(currentPlanIndex == 1)
                        currentGeneratedPlan = loseWeightPlan;
                    if(currentPlanIndex == 2)
                        currentGeneratedPlan = maintainWeightPlan;
                    if(currentPlanIndex == 3)
                        currentGeneratedPlan = gainWeightPlan;

                    tvPlanName.setText("Name: " + currentGeneratedPlan.getGoal());
                    tvPlanCalories.setText("Calories: " + currentGeneratedPlan.getTargetCalories());
                    tvPlanProteins.setText("Proteins: " + currentGeneratedPlan.getTargetProteins());
                    tvPlanFats.setText("Fats: " + currentGeneratedPlan.getTargetFats());

                    tvPlanNumberOutOf.setText("Plan number " + currentPlanIndex + " out of " + maxPlansAmount);

                    if(1 < currentPlanIndex)
                        ibtPreviousPlan.setVisibility(View.VISIBLE);
                    if(currentPlanIndex == maxPlansAmount)
                        ibtNextPlan.setVisibility(View.INVISIBLE);
                }
            }
        });

        btChoosePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "The plan to " + currentGeneratedPlan.getGoal() + " chosen successfully !", Toast.LENGTH_SHORT).show();
                etGetTargetCalories.setText(currentGeneratedPlan.getTargetCalories() + "");
                etGetTargetProteins.setText(currentGeneratedPlan.getTargetProteins() + "");
                etGetTargetFats.setText(currentGeneratedPlan.getTargetFats() + "");
                chosenGoal = currentGeneratedPlan.getGoal();
                ad.cancel();
            }
        });

        btCancelPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.cancel();
            }
        });

        ad.show();
    }

    public boolean passAlertDialogInfoTests(String weight, String height, String age, String activeLevel) {
        boolean passTests = true;

        if(weight.replaceAll(" ", "").equals("")){
            Toast.makeText(getActivity(), "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
            passTests = false;
        }
        else{
            if(!(0 < Double.parseDouble(weight) && Double.parseDouble(weight) < 500)){
                Toast.makeText(getActivity(), "Starting weight should be between 0 to 500 kg.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
        }

        if(passTests){
            if(height.replaceAll(" ", "").equals("")){
                Toast.makeText(getActivity(), "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
            else{
                if((!(50 < Double.parseDouble(height) && Double.parseDouble(height) < 250))){
                    Toast.makeText(getActivity(), "Height should be between 50 to 250 cm.", Toast.LENGTH_SHORT).show();
                    passTests = false;
                }
            }
        }

        if(passTests){
            if(age.replaceAll(" ", "").equals("")){
                Toast.makeText(getActivity(), "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
            else{
                if((!(0 < Integer.parseInt(age) && Integer.parseInt(age) < 120))){
                    Toast.makeText(getActivity(), "Age should be between 0 to 120 years.", Toast.LENGTH_SHORT).show();
                    passTests = false;
                }
            }
        }

        if(activeLevel == null && passTests){
            Toast.makeText(getActivity(), "Choose an active level first.", Toast.LENGTH_SHORT).show();
            passTests = false;
        }

        return passTests;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if(viewId == btGetHelpCreatingAPlan.getId())
            generatePlanAlertDialog();

        if(viewId == btRegister.getId())
            createUserAndUserPlan();
    }
}