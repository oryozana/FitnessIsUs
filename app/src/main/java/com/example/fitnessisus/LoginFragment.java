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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginFragment extends Fragment implements View.OnClickListener {

    EditText etGetUsernameLoginInfo, etGetPasswordLoginInfo;
    LinearLayout linearLayout, loginLoadingLinearLayout;
    CheckBox cbRememberLoggedUserInLocalDatabase;
    TextView tvForgotPassword;
    Button btLogin;

    FileAndDatabaseHelper fileAndDatabaseHelper;

    User forgotUser;

    FirebaseDatabase usersDb;
    DatabaseReference databaseReference;

    Intent me;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        me = getActivity().getIntent();
        loginLoadingLinearLayout = (LinearLayout) view.findViewById(R.id.loginLoadingLinearLayout);
        linearLayout = (LinearLayout) view.findViewById(R.id.loginLinearLayout);

        etGetUsernameLoginInfo = (EditText) view.findViewById(R.id.etGetUsernameLoginInfo);
        etGetPasswordLoginInfo = (EditText) view.findViewById(R.id.etGetPasswordLoginInfo);

        tvForgotPassword = (TextView) view.findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(this);

        btLogin = (Button) view.findViewById(R.id.btLogin);
        btLogin.setOnClickListener(this);

        cbRememberLoggedUserInLocalDatabase = (CheckBox) view.findViewById(R.id.cbRememberLoggedUserInLocalDatabase);

        fileAndDatabaseHelper = new FileAndDatabaseHelper(getActivity(), me);
    }

    public void getUserFromFirebaseDatabase(String username, String enteredPassword){
        linearLayout.setVisibility(View.GONE);
        loginLoadingLinearLayout.setVisibility(View.VISIBLE);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(username).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        DataSnapshot dataSnapshot = task.getResult();
                        String password = String.valueOf(dataSnapshot.child("password").getValue());

                        if(enteredPassword.equals(password)){
                            User.setCurrentUser(new User(dataSnapshot));
                            User.getCurrentUser().uploadUserDailyMenusIntoTemporaryFile(getActivity());

                            if(cbRememberLoggedUserInLocalDatabase.isChecked())
                                fileAndDatabaseHelper.setPrimaryUser(User.getCurrentUser());

                            me.setClass(getActivity(), MainActivity.class);
                            me.putExtra("cameFromLogin", 0);
                            startActivity(me);
                        }
                        else
                            Toast.makeText(getActivity(), "Username or password incorrect.", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(getActivity(), "Username or password incorrect.", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getActivity(), "Username or password incorrect.", Toast.LENGTH_SHORT).show();

                loginLoadingLinearLayout.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    public void forgotPasswordAlertDialog() {
        AlertDialog ad;
        AlertDialog.Builder adb;
        adb = new AlertDialog.Builder(getActivity());

        View customAlertDialog = LayoutInflater.from(getActivity()).inflate(R.layout.alert_dialog_forgot_password, null);

        LinearLayout forgotPasswordLinearLayout = (LinearLayout) customAlertDialog.findViewById(R.id.forgotPasswordLinearLayout);
        EditText etGetUsernameForgot = (EditText) customAlertDialog.findViewById(R.id.etGetUsernameForgot);
        EditText etGetEmailForgot = (EditText) customAlertDialog.findViewById(R.id.etGetEmailForgot);
        Button btCancelAndExitForgot = (Button) customAlertDialog.findViewById(R.id.btCancelAndExitForgot);
        Button btContinueForgot = (Button) customAlertDialog.findViewById(R.id.btContinueForgot);

        LinearLayout newPasswordLinearLayout = (LinearLayout) customAlertDialog.findViewById(R.id.newPasswordLinearLayout);
        EditText etNewPassword1 = (EditText) customAlertDialog.findViewById(R.id.etNewPassword1);
        EditText etNewPassword2 = (EditText) customAlertDialog.findViewById(R.id.etNewPassword2);
        Button btBackForgot = (Button) customAlertDialog.findViewById(R.id.btBackForgot);
        Button btChangeForgot = (Button) customAlertDialog.findViewById(R.id.btChangeForgot);

        LinearLayout loadingLinearLayout = (LinearLayout) customAlertDialog.findViewById(R.id.loadingLinearLayout);

        adb.setView(customAlertDialog);
        ad = adb.create();

        btCancelAndExitForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.cancel();
            }
        });

        btContinueForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredUsername = etGetUsernameForgot.getText().toString();
                String enteredEmail = etGetEmailForgot.getText().toString();

                if(!enteredUsername.replaceAll(" ", "").equals("") && !enteredEmail.replaceAll(" ", "").equals("")) {
                    forgotPasswordLinearLayout.setVisibility(View.GONE);
                    loadingLinearLayout.setVisibility(View.VISIBLE);

                    databaseReference = FirebaseDatabase.getInstance().getReference("users");
                    databaseReference.child(enteredUsername).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful()){
                                if(task.getResult().exists()){
                                    DataSnapshot dataSnapshot = task.getResult();
                                    String username = dataSnapshot.getKey();
                                    String email = String.valueOf(dataSnapshot.child("email").getValue());

                                    String enteredEmail = etGetEmailForgot.getText().toString();

                                    if(email.equals(enteredEmail)){
                                        forgotUser = new User(dataSnapshot);
                                        forgotUser.uploadUserDailyMenusIntoTemporaryFile(getActivity());

                                        loadingLinearLayout.setVisibility(View.GONE);
                                        newPasswordLinearLayout.setVisibility(View.VISIBLE);
                                        Toast.makeText(getActivity(), "User found successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(getActivity(), "Username or email incorrect.", Toast.LENGTH_SHORT).show();
                                        loadingLinearLayout.setVisibility(View.GONE);
                                        forgotPasswordLinearLayout.setVisibility(View.VISIBLE);
                                    }
                                }
                                else {
                                    Toast.makeText(getActivity(), "Username or email incorrect.", Toast.LENGTH_SHORT).show();
                                    loadingLinearLayout.setVisibility(View.GONE);
                                    forgotPasswordLinearLayout.setVisibility(View.VISIBLE);
                                }
                            }
                            else {
                                Toast.makeText(getActivity(), "Username or email incorrect.", Toast.LENGTH_SHORT).show();
                                loadingLinearLayout.setVisibility(View.GONE);
                                forgotPasswordLinearLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(getActivity(), "One or more of the fields were empty.", Toast.LENGTH_SHORT).show();
                    loadingLinearLayout.setVisibility(View.GONE);
                    forgotPasswordLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        btBackForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotUser = null;

                newPasswordLinearLayout.setVisibility(View.GONE);
                forgotPasswordLinearLayout.setVisibility(View.VISIBLE);
            }
        });

        btChangeForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword1 = etNewPassword1.getText().toString();
                String newPassword2 = etNewPassword2.getText().toString();

                if(!newPassword1.replaceAll(" ", "").equals("") && !newPassword2.replaceAll(" ", "").equals("")){
                    if(newPassword1.equals(newPassword2)) {
                        newPasswordLinearLayout.setVisibility(View.GONE);
                        loadingLinearLayout.setVisibility(View.VISIBLE);
                        changePassword(newPassword1);
                    }
                    else
                        Toast.makeText(getActivity(), "Passwords doesn't match.", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getActivity(), "One or more fields were empty.", Toast.LENGTH_SHORT).show();
            }
        });

        ad.show();
    }

    public void changePassword(String newPassword){
        boolean passTests = passChangePasswordTests(newPassword);

        if(passTests){
            forgotUser.setPassword(newPassword);
            updateUserPasswordInFirebaseAndInFile();
        }
        else{
            loginLoadingLinearLayout.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
        }
    }

    public boolean passChangePasswordTests(String newPassword){
        boolean passTests = true;

        if(newPassword.length() < 4){
            Toast.makeText(getActivity(), "Password should be at least 4 characters wide!", Toast.LENGTH_SHORT).show();
            passTests = false;
        }

        if(newPassword.length() > 16){
            Toast.makeText(getActivity(), "Password should be at most 16 characters wide!", Toast.LENGTH_SHORT).show();
            passTests = false;
        }

        return passTests;
    }

    public void updateUserPasswordInFirebaseAndInFile(){
        usersDb = FirebaseDatabase.getInstance();
        databaseReference = usersDb.getReference("users");
        Toast.makeText(getActivity(), forgotUser + "", Toast.LENGTH_SHORT).show();
        databaseReference.child(forgotUser.getUsername()).child("password").setValue(forgotUser.getPassword()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    User.setCurrentUser(forgotUser);

                    if(fileAndDatabaseHelper.hasPrimaryUser()){
                        if(fileAndDatabaseHelper.getPrimaryUser().getUsername().equals(forgotUser.getUsername()))
                            fileAndDatabaseHelper.updatePrimaryUserPassword(forgotUser.getPassword());
                    }

                    me.setClass(getActivity(), MainActivity.class);
                    me.putExtra("cameFromLogin", true);
                    startActivity(me);
                }
                else
                    Toast.makeText(getActivity(), "Failed to change password.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if(viewId == btLogin.getId())
            getUserFromFirebaseDatabase(etGetUsernameLoginInfo.getText().toString(), etGetPasswordLoginInfo.getText().toString());

        if(viewId == tvForgotPassword.getId())
            forgotPasswordAlertDialog();
    }
}