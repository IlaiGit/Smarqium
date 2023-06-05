package com.example.beta;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * @author		Ilai Shimoni ilaigithub@gmail.com
 * @version	    3.0
 * @since		12/10/22
 * this class is responsible for user creation and its upload to the database
 * includes data check and comparison to existing data on the database
 */

public class Register extends AppCompatActivity {

    Button btn;
    EditText FirstName, LastName, Email, Password;
    ProgressBar prog;
    private FirebaseAuth firebaseAuth;
    BroadcastReceiver broadcastReceiver;


    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        String RegistryMail = intent.getStringExtra("RegistryMail");
        String RegistryPassword = intent.getStringExtra("RegistryPassword");

        Email.setText(RegistryMail);
        Password.setText(RegistryPassword);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btn = (Button) findViewById(R.id.btn);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E3545")));

        firebaseAuth = FirebaseAuth.getInstance();

        prog = (ProgressBar) findViewById(R.id.prog);
        FirstName = (EditText) findViewById(R.id.FirstName);
        LastName = (EditText) findViewById(R.id.LastName);
        Email = (EditText) findViewById(R.id.Email);
        Password = (EditText) findViewById(R.id.Password);

    }


    protected void unregisterNetwork(){
        try {
            unregisterReceiver(broadcastReceiver);

        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetwork();
    }

    protected void registerNetworkBrodcastReciever(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    /**
     * happens at a register user button press
     * @return	a created user saved to database
     */

    public void CreateAccount(View view) {
        broadcastReceiver = new NetworkConnectionReciever();
        registerNetworkBrodcastReciever();



        String firstName = FirstName.getText().toString().trim();
        if(firstName.isEmpty()){
            FirstName.setError("Please enter your first name!");
            FirstName.requestFocus();
            return;
        }
        String lastName = LastName.getText().toString().trim();
        if(lastName.isEmpty()){
            LastName.setError("Please enter your last name!");
            LastName.requestFocus();
            return;
        }
        String email = Email.getText().toString().trim();
        if(email.isEmpty()){
            Email.setError("Please enter your email!");
            Email.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Email.setError("Please provide a valid email!");
            Email.requestFocus();
            return;
        }
        String password = Password.getText().toString().trim();
        if(password.isEmpty()){
            Password.setError("Please enter your password!");
            Password.requestFocus();
            return;
        }
        if(password.length() < 6){
            Password.setError("Password must contain at least 6 characters");
            Password.requestFocus();
            return;
        }

        prog.setVisibility(View.VISIBLE);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            UserInfo user = new UserInfo(firstName, lastName,email, password, FirebaseAuth.getInstance().getCurrentUser().getUid());

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference UserRef = database.getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            UserRef.setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                prog.setVisibility(View.INVISIBLE);

                                                //send email verification + app notification
                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                user.sendEmailVerification();
                                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                                                    NotificationChannel channel = new NotificationChannel("EmailVerify", "Email verification", NotificationManager.IMPORTANCE_DEFAULT);
                                                    NotificationManager manager = getSystemService(NotificationManager.class);
                                                    manager.createNotificationChannel(channel);
                                                }

                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(Register.this, "EmailVerify");
                                                builder.setContentTitle("Smarqium");
                                                builder.setContentText("Please check your email to verify it ! ");
                                                builder.setSmallIcon(R.drawable.ic_noti_icon);
                                                builder.setAutoCancel(true);

                                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                                mNotificationManager.notify(1, builder.build());


                                                // redirect
                                                Intent intent = new Intent(Register.this, Login.class);
                                                intent.putExtra("Email", email);
                                                intent.putExtra("Password", password);
                                                startActivity(intent);
                                            }
                                            else{
                                                Toast.makeText(Register.this, "Failed to register, try again", Toast.LENGTH_LONG).show();
                                                prog.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                        }else{
                            Toast.makeText(Register.this, "Failed to register, try again", Toast.LENGTH_LONG).show();
                            prog.setVisibility(View.INVISIBLE);
                        }
                    }
                });

    }

    public void redirect(View view) {
        startActivity(new Intent(Register.this, Login.class));
    }
}