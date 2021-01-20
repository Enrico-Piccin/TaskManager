package com.example.taskmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import model.User;

public class LoginActivity extends AppCompatActivity implements Serializable {
    EditText editTextEmail, editTextPassword;
    Button btnLogin;
    TextView txtViewRegister;
    TextInputLayout errEmail, errPassword;
    boolean emailValida, pswValida;
    private final int PSW_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.login);
        txtViewRegister = findViewById(R.id.register);
        errEmail = findViewById(R.id.emailError);
        errPassword = findViewById(R.id.passError);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convalidaLogin(); // Verifica della correttezza delle credenziali
            }
        });

        txtViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reindirizzamento alla RegisterActivity
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    public void convalidaLogin() {
        // Verifica della validità dell'indirizzo e-mail
        if (editTextEmail.getText().toString().isEmpty()) {
            errEmail.setError(getResources().getString(R.string.email_error));
            emailValida = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(editTextEmail.getText().toString()).matches()) {
            errEmail.setError(getResources().getString(R.string.error_invalid_email));
            emailValida = false;
        } else  {
            emailValida = true;
            errEmail.setErrorEnabled(false);
        }

        // Verifica della validità della password
        if (editTextPassword.getText().toString().isEmpty()) {
            errPassword.setError(getResources().getString(R.string.password_error));
            pswValida = false;
        } else if (editTextPassword.getText().length() < PSW_LENGTH) {
            errPassword.setError(getResources().getString(R.string.error_invalid_password));
            pswValida = false;
        } else  {
            pswValida = true;
            errPassword.setErrorEnabled(false);
        }

        if (emailValida && pswValida) {  // Verifica della correttezza delle credenziali
            DatabaseHelper db = new DatabaseHelper(this);
            String psw_encrypted = null;
            Log.d("Errore", "La password non criptata letta in LOGIN è: " + editTextPassword.getText().toString());
            try {
                psw_encrypted = new Cryptograph(this).encrypt(this, editTextPassword.getText().toString().getBytes());
                Log.d("Errore", "La password criptata in fase di LOGIN è: " + psw_encrypted);
            } catch (Exception e) {
                e.printStackTrace();

            }

            Log.d("Errore", "La data di oggi è " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

            User u = db.getUser(editTextEmail.getText().toString(), psw_encrypted);

            if(u == null || u.getEmail() == null || u.getPassword() == null)
                showAlertDialog(this, R.layout.bad_login, null);
            else
                showAlertDialog(this, R.layout.good_login, u);
        } else {
            showAlertDialog(this, R.layout.bad_login, null);
        }
    }

    public static void showAlertDialog(Activity context, int layout, User u){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        View layoutView = context.getLayoutInflater().inflate(layout, null);
        Button dialogButton = layoutView.findViewById(R.id.btnDialog);
        dialogBuilder.setView(layoutView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                if(layout == context.getResources().getIdentifier("good_login", "layout", context.getPackageName())) {
                    Intent intent = new Intent(context.getApplicationContext(), TaskListActivity.class);
                    intent.putExtra("key_object", u);
                    context.startActivity(intent);
                    context.finish();
                }
            }
        });
    }
}