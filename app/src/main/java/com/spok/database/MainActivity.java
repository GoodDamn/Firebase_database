package com.spok.database;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView textView_signIn;
    private Button btn_login;
    private EditText editText_nickname, editText_password;
    private boolean isSignIn = false;
    private ProgressBar progressBar;

    private void Visibility(int visible)
    {
        btn_login.setVisibility(visible);
        textView_signIn.setVisibility(visible);
    }

    private void MessageToast(String text)
    {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
    }

    private void Enable(boolean isEnabled)
    {
        btn_login.setEnabled(isEnabled);
        textView_signIn.setEnabled(isEnabled);
        editText_nickname.setEnabled(isEnabled);
        editText_password.setEnabled(isEnabled);
        if (!isEnabled) progressBar.setVisibility(View.VISIBLE);
        else progressBar.setVisibility(View.INVISIBLE);
    }

    private void StartNextActivity()
    {
        Intent intent = new Intent(MainActivity.this, ActivityDatabase.class);
        intent.putExtra("nick", editText_nickname.getText().toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        ActivityCompat.finishAffinity(MainActivity.this);
    }

    private void hideKeyboard()
    {
        View view = getCurrentFocus();
        if (view != null)
        {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void StartAnimations(Animation animation, int idAnim)
    {
        btn_login.startAnimation(animation);
        textView_signIn.startAnimation(AnimationUtils.loadAnimation(this, idAnim));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressbar);
        textView_signIn = findViewById(R.id.textView_signIn);
        btn_login = findViewById(R.id.btn_login);
        editText_nickname = findViewById(R.id.editText_nickname);
        editText_password = findViewById(R.id.editText_password);

        final Animation animation_fadeIN = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadein),
                animation_fadeOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadeout);

        final SpannableString spannableString_signIn = new SpannableString("Нет аккаунта? Зарегистрироваться."),
            spannableString_login = new SpannableString("Есть аккаунт? Войти.");

        ConstraintLayout constraintLayout = findViewById(R.id.main_constrLayout);
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });

        spannableString_login.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                isSignIn = false;
                StartAnimations(animation_fadeOut, R.anim.fadeout);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.rgb(61, 153, 237));
                ds.setUnderlineText(false);
            }

        }, spannableString_login.length() - 6, spannableString_login.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString_signIn.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                isSignIn = true;
                StartAnimations(animation_fadeOut, R.anim.fadeout);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.rgb(61, 153, 237));
                ds.setUnderlineText(false);
            }
        }, spannableString_signIn.length() - 19, spannableString_signIn.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView_signIn.setText(spannableString_signIn);
        textView_signIn.setMovementMethod(LinkMovementMethod.getInstance());

        animation_fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationEnd(Animation animation) {
                Visibility(View.INVISIBLE);
                if (isSignIn)
                {
                    btn_login.setText("Зарегистрироваться");
                    textView_signIn.setText(spannableString_login);
                }
                else
                {
                    btn_login.setText("Войти");
                    textView_signIn.setText(spannableString_signIn);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        StartAnimations(animation_fadeIN, R.anim.fadein);
                    }
                },500);
            }
            @Override public void onAnimationRepeat(Animation animation) { }
        });

        animation_fadeIN.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationEnd(Animation animation)
            { Visibility(View.VISIBLE); }
            @Override public void onAnimationRepeat(Animation animation) { }
        });


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText_password.getText().toString().isEmpty()
                && !editText_nickname.getText().toString().isEmpty())
                {
                    if (((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null
                            && ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo().isConnected()) {
                        Enable(false);
                        final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                                .getReference(editText_nickname.getText().toString());

                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && !isSignIn)
                                {
                                    if (snapshot.child("password").getValue().toString().equals(editText_password.getText().toString()))
                                    {
                                        MessageToast("Здравствуй, " + editText_nickname.getText().toString());
                                        StartNextActivity();
                                    }
                                    else
                                    {
                                        MessageToast("Неверный пароль.");
                                        Enable(true);
                                    }
                                }
                                else if (isSignIn)
                                {
                                    if (snapshot.exists())
                                    {
                                        Enable(true);
                                        MessageToast("Такой никнейм уже занят. Попробуйте другой.");
                                    }
                                    else
                                    {
                                        MessageToast("Добро пожаловать, " + editText_nickname.getText().toString());
                                        databaseReference.child("counter").setValue(0);
                                        databaseReference.child("password").setValue(editText_password.getText().toString());
                                        StartNextActivity();
                                    }
                                }
                                else
                                {
                                    MessageToast("Может пройти регистрацию ? :)");
                                    Enable(true);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                MessageToast(error.getMessage());
                                Enable(true);
                            }
                        });
                    }
                    else
                    {
                        MessageToast("Нет подключения к интернету.");
                        Enable(true);
                    }
                }
                else MessageToast("Не все поля заполнены.");
            }
        });
    }
}
