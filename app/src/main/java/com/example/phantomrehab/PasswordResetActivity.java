package com.example.phantomrehab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PasswordResetActivity extends AppCompatActivity {

    private ImageView PlayIcon, MuteIcon;
    private FirebaseAuth fAuth;
    private EditText Email;
    private Button Submit;


    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)//?
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        Email = (EditText) findViewById(R.id.enter_email);
        Submit = (Button) findViewById(R.id.btn_submit);

        fAuth = FirebaseAuth.getInstance();


        //color management
        //TextView navbar = findViewById(R.id.navbar);
        RelativeLayout navbar = findViewById(R.id.navbar);


        if (getColor() != getResources().getColor(R.color.blue_theme)){
            navbar.setBackgroundColor(getColor());
            Submit.setBackgroundColor(getColor());
            //SignUp.setBackgroundTintList(ColorStateList.valueOf(getColor()));
        }

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = Email.getText().toString().trim();

                if (!emailAddress.isEmpty()) {
                    checkEmailAndSendPasswordReset(emailAddress);
                } else {
                    Toast.makeText(PasswordResetActivity.this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //manage music
        MuteIcon = findViewById(R.id.mute);
        PlayIcon = findViewById(R.id.volume);

        if (!getMusicPref()) {
            //update UI
//            Toast.makeText(getApplicationContext(), "music_pref = false", Toast.LENGTH_SHORT).show();

            MuteIcon.setVisibility(View.GONE);
            PlayIcon.setVisibility(View.VISIBLE);
        }
        else {
            //if music_pref is true, autoplay music when returning from a video activity
            startService(new Intent(getApplicationContext(), MusicService.class));
        }

        MuteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mute on click of btn; display mute icon (click to play); current status is play
                stopService(new Intent(getApplicationContext(), MusicService.class));
                storeMusicPref(false);

                //update UI
                PlayIcon.setVisibility(View.VISIBLE);
                MuteIcon.setVisibility(View.GONE);
            }
        });

        PlayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(getApplicationContext(), MusicService.class));
                storeMusicPref(true);

                //update UI
                MuteIcon.setVisibility(View.VISIBLE);
                PlayIcon.setVisibility(View.GONE);
            }
        });
    }

    private void checkEmailAndSendPasswordReset(String emailAddress) {
        // Check if the email is registered
        fAuth.fetchSignInMethodsForEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            List<String> signInMethods = result.getSignInMethods();

                            if (signInMethods != null && !signInMethods.isEmpty()) {
                                // The email is registered, send password reset email
                                fAuth.sendPasswordResetEmail(emailAddress)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("PasswordReset", "Email sent.");
                                                    Toast.makeText(PasswordResetActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                // The email is not registered
                                Log.d("PasswordReset", "This email is not registered.");
                                Toast.makeText(PasswordResetActivity.this, "This email is not registered.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("PasswordReset", "Error getting sign in methods for user", task.getException());
                            Toast.makeText(PasswordResetActivity.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //music management
    private void storeMusicPref(boolean pref) {
        SharedPreferences sharedPreferences = getSharedPreferences("Music", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("music",pref);
        editor.apply();
//        Toast.makeText(getApplicationContext(), "music_pref stored", Toast.LENGTH_SHORT).show();
    }

    private boolean getMusicPref(){
        SharedPreferences sharedPreferences = getSharedPreferences("Music", MODE_PRIVATE);
        return sharedPreferences.getBoolean("music", true);
    }

    //color management
    private int getColor(){
        SharedPreferences sharedPreferences = getSharedPreferences("Color", MODE_PRIVATE);
        int selectedColor = sharedPreferences.getInt("color", getResources().getColor(R.color.blue_theme));
        return selectedColor;
    }
}

