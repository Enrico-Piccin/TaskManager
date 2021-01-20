package com.example.taskmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

import model.User;

public class RegisterActivity extends AppCompatActivity {
    EditText editTextName, editTextEmail, editTextPhone, editTextPassword;
    Button btnRegister;
    TextView txtViewLogin;
    TextInputLayout errName, errEmail, errPhone, errPassword;
    boolean nameValido, emailValida, phoneValido, pswValida;
    private final int PSW_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextName = (EditText) findViewById(R.id.name);
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPhone = (EditText) findViewById(R.id.phone);
        editTextPassword = (EditText) findViewById(R.id.password);
        txtViewLogin = (TextView) findViewById(R.id.login);
        btnRegister = (Button) findViewById(R.id.register);
        errName= (TextInputLayout) findViewById(R.id.nameError);
        errEmail = (TextInputLayout) findViewById(R.id.emailError);
        errPhone = (TextInputLayout) findViewById(R.id.phoneError);
        errPassword = (TextInputLayout) findViewById(R.id.passError);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convalidaRegister();
            }
        });

        txtViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reindirizzamento alla LoginActivity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    public void convalidaRegister() {
        // Verifica della validità del nome
        if (editTextName.getText().toString().isEmpty()) {
            errName.setError(getResources().getString(R.string.name_error));
            nameValido = false;
        } else  {
            nameValido = true;
            errName.setErrorEnabled(false);
        }

        // Check for a valid email address.
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

        // Check for a valid phone number.
        if (editTextPhone.getText().toString().isEmpty()) {
            errPhone.setError(getResources().getString(R.string.phone_error));
            phoneValido = false;
        } else  {
            phoneValido = true;
            errPhone.setErrorEnabled(false);
        }

        // Check for a valid password.
        if (editTextPassword.getText().toString().isEmpty()) {
            errPassword.setError(getResources().getString(R.string.password_error));
            pswValida = false;
        } else if (editTextPassword.getText().length() < 6) {
            errPassword.setError(getResources().getString(R.string.error_invalid_password));
            pswValida = false;
        } else  {
            pswValida = true;
            errPassword.setErrorEnabled(false);
        }

        if (nameValido && emailValida && phoneValido && pswValida) {
            DatabaseHelper db = new DatabaseHelper(this);
            String psw_encrypted = null;
            try {
                psw_encrypted = new Cryptograph(this).encrypt(this, editTextPassword.getText().toString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d("Errore", "La password criptata in fase fi REGISTRAZIONE è: " + psw_encrypted);

            User u = db.getUser(editTextEmail.getText().toString(), psw_encrypted);

            if(u == null || u.getEmail() == null || u.getPassword() == null) {
                User newU = new User(editTextName.getText().toString(), editTextEmail.getText().toString(),
                        editTextPhone.getText().toString(), psw_encrypted, getRandomMaterialColor(Math.random() < 0.50 ? "400" : "500"), 0, new Date(Integer.parseInt(new SimpleDateFormat("yyyy-MM-dd").format(new Date()).split("-")[0]), Integer.parseInt(new SimpleDateFormat("yyyy-MM-dd").format(new Date()).split("-")[1]), Integer.parseInt(new SimpleDateFormat("yyyy-MM-dd").format(new Date()).split("-")[2])));
                db.addUser(newU);
                getSharedPreferences(newU.getEmail(), Context.MODE_PRIVATE).edit().putInt("DAILY_TASKS", 5).apply();
                LoginActivity.showAlertDialog(this, R.layout.good_login, newU);
            }
            else
                LoginActivity.showAlertDialog(this, R.layout.bad_login, null);
        } else
            LoginActivity.showAlertDialog(this, R.layout.bad_login, null);
    }

    /**
     * Viene scelto un colore random dal file array.xml
     */
    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }
}