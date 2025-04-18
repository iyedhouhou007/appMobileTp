package com.iyed_houhou.myappmobiletp.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.iyed_houhou.myappmobiletp.DatabaseHelper;
import com.iyed_houhou.myappmobiletp.R;
import com.google.android.material.textfield.TextInputEditText;

public class StudentRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "StudentRegistration";
    private TextInputEditText etStudentName;
    private TextInputEditText etStudentGroup;
    private TextInputEditText etStudentYear;
    private Button btnRegisterStudent;
    private DatabaseHelper dbHelper;
    private long userId; // To store the user ID from the previous activity
    private String username; // To store the username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_registration);

        dbHelper = new DatabaseHelper(this);
        etStudentName = findViewById(R.id.etStudentName);
        etStudentGroup = findViewById(R.id.etStudentGroup);
        etStudentYear = findViewById(R.id.etStudentYear);
        btnRegisterStudent = findViewById(R.id.btnRegisterStudent);

        // Retrieve the user ID passed from RegisterActivity
        userId = getIntent().getLongExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        
        if (userId == -1) {
            // Handle error if user ID is not passed
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnRegisterStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentName = etStudentName.getText().toString().trim();
                String studentGroup = etStudentGroup.getText().toString().trim();
                String studentYear = etStudentYear.getText().toString().trim();

                if (studentName.isEmpty()) {
                    Toast.makeText(StudentRegistrationActivity.this, "Please enter your full name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save student details to database
                long newStudentId = saveStudentToDatabase(studentName, userId, studentGroup, studentYear);

                if (newStudentId != -1) {
                    // Save username in SharedPreferences for later use
                    if (username != null) {
                        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("username", username);
                        editor.apply();
                    }
                    
                    Toast.makeText(StudentRegistrationActivity.this, "Registration completed successfully", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to GradesActivity
                    Intent intent = new Intent(StudentRegistrationActivity.this, GradesActivity.class);
                    if (username != null) {
                        intent.putExtra("username", username);
                    }
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(StudentRegistrationActivity.this, "Error completing registration", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private long saveStudentToDatabase(String studentName, long userId, String studentGroup, String studentYear) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_STUDENT_NAME, studentName);
        values.put(DatabaseHelper.COLUMN_USER_ID, userId);
        
        // Add student group and year to the values if the columns exist
        // You might need to add these columns to your table schema if they don't exist
        // For now, we'll just store the main required information
        
        long newRowId = -1;
        try {
            newRowId = db.insert(DatabaseHelper.TABLE_STUDENTS, null, values);
            Log.d(TAG, "Student saved with ID: " + newRowId + " for user ID: " + userId);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error saving student", e);
        } finally {
            db.close();
        }
        return newRowId;
    }
} 