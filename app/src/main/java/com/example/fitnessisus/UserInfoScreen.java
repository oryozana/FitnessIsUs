package com.example.fitnessisus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class UserInfoScreen extends AppCompatActivity implements View.OnClickListener {
    private NetworkConnectionReceiver networkConnectionReceiver;

    private MediaPlayer mediaPlayer;
    private VideoView videoView;

    EditText etGetNewTargetCalories, etGetNewTargetProteins, etGetNewTargetFats;
    Button btSetPrimaryUser, btRemovePrimaryUser, btLogoutUser, btDeleteUser;
    Button btSendToProfilePictureSelection, btChangePassword;
    Button btGetHelpCreatingANewPlan, btUpdatePlan;
    EditText etGetOldPassword, etGetNewPassword;
    TextView tvUsernameDisplay;
    ImageView ivProfilePicture;
    LinearLayout linearLayout;

    Plan currentGeneratedPlan, maintainWeightPlan, loseWeightPlan, gainWeightPlan;
    int currentPlanIndex = 1, maxPlansAmount = 3;

    TextView tvPictureNumberOutOf, tvNoInternetConnectionToChangePictureMessage;
    Button btChoseProfilePicture, btCancelProfilePictureSelection;
    LinearLayout profilePictureSelectionLinearLayout;
    ImageButton ibtPreviousPicture, ibtNextPicture;
    ImageView ivProfilePictureSelector;

    int currentPictureIndex = 0, maxPictureAmount = 10;

    boolean internetConnection = true, isChoosingProfilePicture = false;
    FileAndDatabaseHelper fileAndDatabaseHelper;
    Song activeSong = Song.getSongs().get(0);
    User user = User.getCurrentUser();

    FirebaseDatabase usersDb;
    DatabaseReference databaseReference;

    Intent exitAppService;
    Intent me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info_screen);

        me = getIntent();
        if(me.hasExtra("activeSong"))
            activeSong = (Song) me.getSerializableExtra("activeSong");

        profilePictureSelectionLinearLayout = (LinearLayout) findViewById(R.id.profilePictureSelectionLinearLayout);
        linearLayout = (LinearLayout) findViewById(R.id.userInfoScreenLinearLayout);
        videoView = (VideoView) findViewById(R.id.userInfoScreenVideoView);

        ivProfilePicture = (ImageView) findViewById(R.id.ivProfilePicture);
        ivProfilePicture.setImageResource(user.getProfilePictureId());

        btSendToProfilePictureSelection = (Button) findViewById(R.id.btSendToProfilePictureSelection);
        btSendToProfilePictureSelection.setOnClickListener(this);
        btGetHelpCreatingANewPlan = (Button) findViewById(R.id.btGetHelpCreatingANewPlan);
        btGetHelpCreatingANewPlan.setOnClickListener(this);
        btChangePassword = (Button) findViewById(R.id.btChangePassword);
        btChangePassword.setOnClickListener(this);
        btRemovePrimaryUser = (Button) findViewById(R.id.btRemovePrimaryUser);
        btRemovePrimaryUser.setOnClickListener(this);
        btSetPrimaryUser = (Button) findViewById(R.id.btSetPrimaryUser);
        btSetPrimaryUser.setOnClickListener(this);
        btLogoutUser = (Button) findViewById(R.id.btLogoutUser);
        btLogoutUser.setOnClickListener(this);
        btDeleteUser = (Button) findViewById(R.id.btDeleteUser);
        btDeleteUser.setOnClickListener(this);
        btUpdatePlan = (Button) findViewById(R.id.btUpdatePlan);
        btUpdatePlan.setOnClickListener(this);

        etGetOldPassword = (EditText) findViewById(R.id.etGetOldPassword);
        etGetNewPassword = (EditText) findViewById(R.id.etGetNewPassword);

        etGetNewTargetCalories = (EditText) findViewById(R.id.etGetNewTargetCalories);
        etGetNewTargetProteins = (EditText) findViewById(R.id.etGetNewTargetProteins);
        etGetNewTargetFats = (EditText) findViewById(R.id.etGetNewTargetFats);

        tvUsernameDisplay = (TextView) findViewById(R.id.tvUsernameDisplay);
        tvUsernameDisplay.setText(User.getCurrentUser().getUsername());

        // ProfilePictureSelection:
        ivProfilePictureSelector = (ImageView) findViewById(R.id.ivProfilePictureSelector);
        tvPictureNumberOutOf = (TextView) findViewById(R.id.tvPictureNumberOutOf);

        btCancelProfilePictureSelection = (Button) findViewById(R.id.btCancelProfilePictureSelection);
        btCancelProfilePictureSelection.setOnClickListener(this);
        btChoseProfilePicture = (Button) findViewById(R.id.btChoseProfilePicture);
        btChoseProfilePicture.setOnClickListener(this);

        ibtPreviousPicture = (ImageButton) findViewById(R.id.ibtPreviousPicture);
        ibtPreviousPicture.setOnClickListener(this);
        ibtNextPicture = (ImageButton) findViewById(R.id.ibtNextPicture);
        ibtNextPicture.setOnClickListener(this);

        tvNoInternetConnectionToChangePictureMessage = (TextView) findViewById(R.id.tvNoInternetConnectionToChangePictureMessage);
        tvNoInternetConnectionToChangePictureMessage.setText("You don't have Internet connection." + "\n" + "Reconnect in order to change picture.");

        fileAndDatabaseHelper = new FileAndDatabaseHelper(this, me);
        activeSong = fileAndDatabaseHelper.implementSettingsData();

        if(fileAndDatabaseHelper.checkIfPrimaryUserExist()) {
            if(fileAndDatabaseHelper.getPrimaryUsername().equals(user.getUsername())){
                btRemovePrimaryUser.setVisibility(View.VISIBLE);
                btSetPrimaryUser.setVisibility(View.GONE);
            }
            else {
                btRemovePrimaryUser.setVisibility(View.GONE);
                btSetPrimaryUser.setVisibility(View.VISIBLE);
            }
        }
        else {
            btRemovePrimaryUser.setVisibility(View.GONE);
            btSetPrimaryUser.setVisibility(View.VISIBLE);
        }

        nextPicture();
        setCustomNetworkConnectionReceiver();
        initiateVideoPlayer();
        initiateMediaPlayer();
    }

    public void switchBetweenProfilePictureSelectionAndUserInfoScreen(){
        if(isChoosingProfilePicture){
            profilePictureSelectionLinearLayout.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
            isChoosingProfilePicture = false;
        }
        else{
            linearLayout.setVisibility(View.GONE);
            profilePictureSelectionLinearLayout.setVisibility(View.VISIBLE);
            currentPictureIndex = 0;
            ibtPreviousPicture.setVisibility(View.INVISIBLE);
            ibtNextPicture.setVisibility(View.VISIBLE);
            nextPicture();
            isChoosingProfilePicture = true;
        }
    }

    public void setImageBasedOnIndex(){
        ivProfilePictureSelector.setImageResource(getResources().getIdentifier("user_picture_" + (currentPictureIndex), "drawable", getPackageName()));
    }

    public void previousPicture(){
        if(0 < currentPictureIndex && currentPictureIndex <= maxPictureAmount){
            currentPictureIndex--;
            tvPictureNumberOutOf.setText("  Picture number " + currentPictureIndex + " out of " + maxPictureAmount + "  ");
            setImageBasedOnIndex();

            ibtNextPicture.setVisibility(View.VISIBLE);
            if(currentPictureIndex == 1)
                ibtPreviousPicture.setVisibility(View.INVISIBLE);
        }
    }

    public void nextPicture(){
        if(0 <= currentPictureIndex && currentPictureIndex < maxPictureAmount){
            currentPictureIndex++;
            tvPictureNumberOutOf.setText("   Picture number " + currentPictureIndex + " out of " + maxPictureAmount + "   ");
            setImageBasedOnIndex();

            if(1 < currentPictureIndex)
                ibtPreviousPicture.setVisibility(View.VISIBLE);
            if(currentPictureIndex == maxPictureAmount)
                ibtNextPicture.setVisibility(View.INVISIBLE);
        }
    }

    public void profilePictureSelected(){
        User.getCurrentUser().setProfilePictureId(getResources().getIdentifier("user_picture_" + (currentPictureIndex), "drawable", getPackageName()));
        updateUserProfilePictureIdInFirebaseAndPrimaryUser(User.getCurrentUser());
    }

    public void updateUserProfilePictureIdInFirebaseAndPrimaryUser(User user){
        usersDb = FirebaseDatabase.getInstance();
        databaseReference = usersDb.getReference("users");
        databaseReference.child(user.getUsername()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(fileAndDatabaseHelper.getPrimaryUsername().equals(user.getUsername()))
                    fileAndDatabaseHelper.updatePrimaryUserProfilePictureId(user.getProfilePictureId());

                Toast.makeText(UserInfoScreen.this, "User profile picture successfully changed.", Toast.LENGTH_SHORT).show();

                me.setClass(UserInfoScreen.this, UserInfoScreen.class);
                startActivity(me);
            }
        });
    }

    public void changePassword(){
        boolean passTests = passChangePasswordTests();

        if(passTests){
            if(internetConnection){
                String userPassword = User.getCurrentUser().getPassword();

                if(userPassword.equals(etGetOldPassword.getText().toString())){
                    User.getCurrentUser().setPassword(etGetNewPassword.getText().toString());
                    updateUserPasswordInFirebaseAndInPrimaryUser(User.getCurrentUser());
                }
                else
                    Toast.makeText(this, "Wrong password.", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(this, "No internet connection, can't change password.", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean passChangePasswordTests(){
        boolean passTests = true;

        if(etGetOldPassword.getText().toString().replaceAll(" ", "").equals("") && passTests){
            Toast.makeText(this, "Enter your old password!", Toast.LENGTH_SHORT).show();
            passTests = false;
        }

        if(etGetNewPassword.getText().toString().replaceAll(" ", "").equals("") && passTests){
            Toast.makeText(this, "Enter new password!", Toast.LENGTH_SHORT).show();
            passTests = false;
        }
        else{
            if(etGetNewPassword.getText().toString().length() < 4 && passTests){
                Toast.makeText(this, "Password should be at least 4 characters wide!", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
        }

        return passTests;
    }

    public void updateUserPasswordInFirebaseAndInPrimaryUser(User user){
        usersDb = FirebaseDatabase.getInstance();
        databaseReference = usersDb.getReference("users");
        databaseReference.child(user.getUsername()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(UserInfoScreen.this, "Password successfully changed.", Toast.LENGTH_SHORT).show();
                fileAndDatabaseHelper.updatePrimaryUserPassword(etGetNewPassword.getText().toString());
                etGetOldPassword.setText("");
                etGetNewPassword.setText("");
            }
        });
    }

    public void updatePlan(){
        boolean passTests = passUpdatePlanTests();

        if(passTests){
            if(internetConnection){
                String targetCalories = etGetNewTargetCalories.getText().toString();
                String targetProteins = etGetNewTargetProteins.getText().toString();
                String targetFats = etGetNewTargetFats.getText().toString();
                Plan tmpPlan = new Plan(targetCalories, targetProteins, targetFats);
                User.getCurrentUser().setCurrentPlan(tmpPlan);
                updateUserPlanInFirebaseAndInPrimaryUser(User.getCurrentUser());
            }
            else
                Toast.makeText(this, "No internet connection, can't change password.", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean passUpdatePlanTests(){
        boolean passTests = true;
        String targetCalories = etGetNewTargetCalories.getText().toString();
        String targetProteins = etGetNewTargetProteins.getText().toString();
        String targetFats = etGetNewTargetFats.getText().toString();

        if(targetCalories.replaceAll(" ", "").equals("")){
            Toast.makeText(UserInfoScreen.this, "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
            passTests = false;
        }
        else{
            if(!(0 < Double.parseDouble(targetCalories) && Double.parseDouble(targetCalories) < 5000)){
                Toast.makeText(UserInfoScreen.this, "Target calories should be between 0 to 5000.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
        }

        if(passTests){
            if(targetProteins.replaceAll(" ", "").equals("")){
                Toast.makeText(UserInfoScreen.this, "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
            else{
                if(!(0 < Double.parseDouble(targetProteins) && Double.parseDouble(targetProteins) < 1000)){
                    Toast.makeText(UserInfoScreen.this, "Target proteins should be between 0 to 1000.", Toast.LENGTH_SHORT).show();
                    passTests = false;
                }
            }
        }

        if(passTests){
            if(targetFats.replaceAll(" ", "").equals("")){
                Toast.makeText(UserInfoScreen.this, "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
            else{
                if(!(0 < Double.parseDouble(targetFats) && Double.parseDouble(targetFats) < 1000)){
                    Toast.makeText(UserInfoScreen.this, "Target fats should be between 0 to 1000.", Toast.LENGTH_SHORT).show();
                    passTests = false;
                }
            }
        }

        return passTests;
    }

    public void updateUserPlanInFirebaseAndInPrimaryUser(User user){
        usersDb = FirebaseDatabase.getInstance();
        databaseReference = usersDb.getReference("users");
        databaseReference.child(user.getUsername()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(UserInfoScreen.this, "Plan successfully updated.", Toast.LENGTH_SHORT).show();
                fileAndDatabaseHelper.updatePrimaryUserPlan(user.getCurrentPlan());
                etGetNewTargetCalories.setText("");
                etGetNewTargetProteins.setText("");
                etGetNewTargetFats.setText("");
            }
        });
    }

    public void getUserFromFirebaseDatabase(String username, String entered_password){
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(username).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        DataSnapshot dataSnapshot = task.getResult();
                        String username = dataSnapshot.getKey();
                        String password = String.valueOf(dataSnapshot.child("password").getValue());

                        if(entered_password.equals(password)){
                            User.setCurrentUser(new User(dataSnapshot));
                        }
                    }
                    else
                        Toast.makeText(UserInfoScreen.this, "Not the correct password.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void generatePlanAlertDialog(){
        AlertDialog ad;
        AlertDialog.Builder adb;
        adb = new AlertDialog.Builder(UserInfoScreen.this);

        View customAlertDialog = LayoutInflater.from(UserInfoScreen.this).inflate(R.layout.alert_dialog_generate_plan, null);

        LinearLayout generatePlansLinearLayout = (LinearLayout) customAlertDialog.findViewById(R.id.generatePlansLinearLayout);
        EditText etGetAge = (EditText) customAlertDialog.findViewById(R.id.etGetAge);
        EditText etGetHeight = (EditText) customAlertDialog.findViewById(R.id.etGetHeight);
        EditText etGetWeight = (EditText) customAlertDialog.findViewById(R.id.etGetWeight);
        Spinner sHowActiveAreYou = (Spinner) customAlertDialog.findViewById(R.id.sHowActiveAreYou);
        RadioGroup rgChooseGender = (RadioGroup) customAlertDialog.findViewById(R.id.rgChooseGender);
        Button btGeneratePlans = (Button) customAlertDialog.findViewById(R.id.btGeneratePlans);

        ArrayAdapter<String> alertDialogAdapter = new ArrayAdapter<String>(UserInfoScreen.this, android.R.layout.simple_spinner_dropdown_item, Plan.getActiveLevelOptions());
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
                Toast.makeText(UserInfoScreen.this, "The plan to " + currentGeneratedPlan.getGoal() + " chosen successfully !", Toast.LENGTH_SHORT).show();
                etGetNewTargetCalories.setText(currentGeneratedPlan.getTargetCalories() + "");
                etGetNewTargetProteins.setText(currentGeneratedPlan.getTargetProteins() + "");
                etGetNewTargetFats.setText(currentGeneratedPlan.getTargetFats() + "");
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
            Toast.makeText(UserInfoScreen.this, "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
            passTests = false;
        }
        else{
            if(!(0 < Double.parseDouble(weight) && Double.parseDouble(weight) < 500)){
                Toast.makeText(UserInfoScreen.this, "Starting weight should be between 0 to 500 kg.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
        }

        if(passTests){
            if(height.replaceAll(" ", "").equals("")){
                Toast.makeText(UserInfoScreen.this, "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
            else{
                if((!(50 < Double.parseDouble(height) && Double.parseDouble(height) < 250))){
                    Toast.makeText(UserInfoScreen.this, "Height should be between 50 to 250 cm.", Toast.LENGTH_SHORT).show();
                    passTests = false;
                }
            }
        }

        if(passTests){
            if(age.replaceAll(" ", "").equals("")){
                Toast.makeText(UserInfoScreen.this, "One or more of the fields is empty.", Toast.LENGTH_SHORT).show();
                passTests = false;
            }
            else{
                if((!(0 < Integer.parseInt(age) && Integer.parseInt(age) < 120))){
                    Toast.makeText(UserInfoScreen.this, "Age should be between 0 to 120 years.", Toast.LENGTH_SHORT).show();
                    passTests = false;
                }
            }
        }

        if(activeLevel == null && passTests){
            Toast.makeText(UserInfoScreen.this, "Choose an active level first.", Toast.LENGTH_SHORT).show();
            passTests = false;
        }

        return passTests;
    }

//    public void showCodeGenerator(){
//        AlertDialog ad;
//        AlertDialog.Builder adb;
//        adb = new AlertDialog.Builder(this);
//        adb.setTitle("Your user code is: ");
//        adb.setIcon(R.drawable.ic_code_icon);
//
//        LinearLayout shareUserByCodeLinearLayout = (LinearLayout) findViewById(R.id.shareUserByCodeLinearLayout);
//        shareUserByCodeLinearLayout.setVisibility(View.VISIBLE);
//        adb.setView(shareUserByCodeLinearLayout);
//
//        adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//
//            }
//        });
//
//        ad = adb.create();
//        ad.show();
//    }

    public void deleteUserAlertDialog(){
        AlertDialog ad;
        AlertDialog.Builder adb;
        adb = new AlertDialog.Builder(this);
        adb.setTitle("Are you sure you want to delete your user?:");
        adb.setIcon(R.drawable.ic_account_icon);
        adb.setMessage("Username: " + user.getUsername());

        final ImageView ivUserImage = new ImageView(UserInfoScreen.this);
        ivUserImage.setImageResource(user.getProfilePictureId());
        ivUserImage.setAdjustViewBounds(true);
        ivUserImage.setMaxHeight(1000);
        ivUserImage.setMaxWidth(1000);
        adb.setView(ivUserImage);

        adb.setPositiveButton("Yes, delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteUser();
            }
        });

        adb.setNegativeButton("No, cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        ad = adb.create();
        ad.show();
    }

    public void setPrimaryUser(){
        Toast.makeText(this, "Primary user selected.", Toast.LENGTH_SHORT).show();
        btRemovePrimaryUser.setVisibility(View.VISIBLE);
        btSetPrimaryUser.setVisibility(View.GONE);
        fileAndDatabaseHelper.setPrimaryUser(user);
    }

    public void removePrimaryUser(){
        Toast.makeText(this, "Primary user deselected.", Toast.LENGTH_SHORT).show();
        fileAndDatabaseHelper.removePrimaryUser();

        btRemovePrimaryUser.setVisibility(View.GONE);
        btSetPrimaryUser.setVisibility(View.VISIBLE);
    }

    public void deleteUser(){
        if(internetConnection){
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUsername());
            databaseReference.removeValue();

            if(fileAndDatabaseHelper.checkIfPrimaryUserExist()){
                if(fileAndDatabaseHelper.getPrimaryUsername().equals(user.getUsername()))
                    removePrimaryUser();
            }

            Toast.makeText(this, "User deleted successfully.", Toast.LENGTH_SHORT).show();
            logoutUser();
        }
        else
            Toast.makeText(this, "No internet connection, can't delete user.", Toast.LENGTH_SHORT).show();
    }

    public void logoutUser(){
        User.setCurrentUser(null);
        me.setClass(UserInfoScreen.this, LoginAndRegister.class);
        DailyMenu.restartDailyMenusFile(UserInfoScreen.this);
        DailyMenu.setTodayMenu(null);

        if(fileAndDatabaseHelper.checkIfPrimaryUserExist()){
            if(fileAndDatabaseHelper.getPrimaryUsername().equals(user.getUsername()))
                fileAndDatabaseHelper.removePrimaryUser();
        }

        me.putExtra("cameFromLogout", true);
        startActivity(me);
    }

    public void setCustomNetworkConnectionReceiver(){
        unregisterRegisteredReceiver();
        networkConnectionReceiver = null;
        networkConnectionReceiver = new NetworkConnectionReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    internetConnection = isOnline(context);

                    if(internetConnection) {
                        btChoseProfilePicture.setVisibility(View.VISIBLE);
                        tvNoInternetConnectionToChangePictureMessage.setVisibility(View.INVISIBLE);
                    }
                    else {
                        btChoseProfilePicture.setVisibility(View.INVISIBLE);
                        tvNoInternetConnectionToChangePictureMessage.setVisibility(View.VISIBLE);
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        registerUnregisteredReceiver();
    }

    public void unregisterRegisteredReceiver(){
        try{
            unregisterReceiver(networkConnectionReceiver);
        }
        catch (IllegalArgumentException e){
            e.getStackTrace();
        }
    }

    public void registerUnregisteredReceiver(){
        IntentFilter networkConnectionFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            registerReceiver(networkConnectionReceiver, networkConnectionFilter);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            registerReceiver(networkConnectionReceiver, networkConnectionFilter);
        }
    }

    public void initiateVideoPlayer(){
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.dinner_selection_background_video);
        videoView.setVideoURI(uri);

        if(me.getBooleanExtra("useVideos", true))
            videoView.start();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
    }

    public void initiateMediaPlayer(){
        mediaPlayer = MediaPlayer.create(UserInfoScreen.this, activeSong.getId());
        mediaPlayer.setLooping(true);
        if(me.getBooleanExtra("playMusic", true)){
            mediaPlayer.start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
//        if(itemID == R.id.sendToMusicMaster){
//            me.setClass(UserInfoScreen.this, musicMaster.class);
//            me.putExtra("cameToMusicMasterFrom", getLocalClassName());
//            startActivity(me);
//        }
//
//        if(itemID == R.id.sendToSettings){
//            me.setClass(UserInfoScreen.this, settingsSetter.class);
//            me.putExtra("cameToSettingsFrom", getLocalClassName());
//            startActivity(me);
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        videoView.resume();
        if(!me.getBooleanExtra("useVideos", true)){
            findViewById(R.id.userInfoScreenLinearLayout).setBackground(getDrawable(R.drawable.dinner_selection_background));
            videoView.stopPlayback();
        }
        else
            videoView.start();
    }

    @Override
    protected void onRestart() {
        videoView.start();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerUnregisteredReceiver();
        mediaPlayer.start();
        if(!me.getBooleanExtra("playMusic", true)){
            mediaPlayer.stop();
        }
    }

    @Override
    protected void onPause() {
        unregisterRegisteredReceiver();
        videoView.suspend();
        mediaPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        videoView.stopPlayback();
        mediaPlayer.stop();
        mediaPlayer.release();
        startService(exitAppService);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if(viewId == btSendToProfilePictureSelection.getId())
            switchBetweenProfilePictureSelectionAndUserInfoScreen();

        if(viewId == btChangePassword.getId())
            changePassword();

        if(viewId == btLogoutUser.getId())
            logoutUser();

        if(viewId == btDeleteUser.getId())
            deleteUserAlertDialog();

        if(viewId == ibtNextPicture.getId())
            nextPicture();

        if(viewId == ibtPreviousPicture.getId())
            previousPicture();

        if(viewId == btChoseProfilePicture.getId())
            profilePictureSelected();

        if(viewId == btCancelProfilePictureSelection.getId())
            switchBetweenProfilePictureSelectionAndUserInfoScreen();

        if(viewId == btGetHelpCreatingANewPlan.getId())
            generatePlanAlertDialog();

        if(viewId == btUpdatePlan.getId())
            updatePlan();

        if(viewId == btRemovePrimaryUser.getId())
            removePrimaryUser();

        if(viewId == btSetPrimaryUser.getId())
            setPrimaryUser();
    }

    @Override
    public void onBackPressed(){
        if(isChoosingProfilePicture)
            switchBetweenProfilePictureSelectionAndUserInfoScreen();
        else{
            me.setClass(UserInfoScreen.this, MainActivity.class);
            startActivity(me);
        }
    }
}