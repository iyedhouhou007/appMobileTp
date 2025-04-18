package com.iyed_houhou.myappmobiletp.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.iyed_houhou.myappmobiletp.DatabaseHelper;
import com.iyed_houhou.myappmobiletp.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnViewDatabase;
    private TextView tvRegisterLink;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        dbHelper = new DatabaseHelper(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnViewDatabase = findViewById(R.id.btnViewDatabase);
        tvRegisterLink = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (authenticateUser(username, password)) {
                    // Save the username in SharedPreferences for access in other activities
                    SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", username);
                    editor.apply();
                    
                    String userRole = getUserRole(username);
                    if (userRole != null) {
                        if (userRole.equals("Student")) {
                            // Navigate to student main activity
                            Intent intent = new Intent(LoginActivity.this, GradesActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                            finish();
                        } else if (userRole.equals("Teacher")) {
                            // Navigate to teacher main activity
                            Intent intent = new Intent(LoginActivity.this, TeacherActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error retrieving user role", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnViewDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the database view activity
                Intent intent = new Intent(LoginActivity.this, DatabaseViewActivity.class);
                startActivity(intent);
            }
        });

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }


    // Authenticates the user by checking username and password
    private boolean authenticateUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {DatabaseHelper.COLUMN_USER_ID};
        String selection = DatabaseHelper.COLUMN_USERNAME + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean userExists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        Log.d(TAG, "User authentication for " + username + ": " + userExists);
        return userExists;
    }

    private String getUserRole(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {DatabaseHelper.COLUMN_ROLE};
        String selection = DatabaseHelper.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        String userRole = null;

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            int roleColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ROLE);
            if (roleColumnIndex != -1) {
                userRole = cursor.getString(roleColumnIndex);
                Log.d(TAG, "User " + username + " has role: " + userRole);
            }
            cursor.close();
        }
        db.close();
        return userRole;
    }
}