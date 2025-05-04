package com.iyed_houhou.myappmobiletp.ui;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iyed_houhou.myappmobiletp.DatabaseHelper;
import com.iyed_houhou.myappmobiletp.R;


public class StudentProfileActivity extends AppCompatActivity {
    private static final String TAG = "StudentProfileActivity";
    private DatabaseHelper dbHelper;
    private TextView tvStudentName, tvStudentId, tvUsername;
    private Button btnViewGrades;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);
        
        // Initialize views
        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentId = findViewById(R.id.tvStudentId);
        tvUsername = findViewById(R.id.tvUsername);
        btnViewGrades = findViewById(R.id.btnViewGrades);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Apply system spacing (status bar height) to the Toolbar
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        btnViewGrades.setOnClickListener((e) -> {
            Intent intent = new Intent(StudentProfileActivity.this, GradesActivity.class);
            startActivity(intent);

        });
        
        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        
        // Get username from intent
        String username = getIntent().getStringExtra("username");
        if (username != null) {
            // Load and display student information
            loadStudentInfo(username);
        }
    }
    
    private void loadStudentInfo(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            // Get user ID from username
            String userQuery = "SELECT " + DatabaseHelper.COLUMN_USER_ID +
                    " FROM " + DatabaseHelper.TABLE_USERS +
                    " WHERE " + DatabaseHelper.COLUMN_USERNAME + " = ?";
            
            Cursor userCursor = db.rawQuery(userQuery, new String[]{username});
            
            if (userCursor != null && userCursor.moveToFirst()) {
                int userIdColumnIndex = userCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID);
                long userId = userCursor.getLong(userIdColumnIndex);
                userCursor.close();
                
                // Get student info using user ID
                String studentQuery = "SELECT * FROM " + DatabaseHelper.TABLE_STUDENTS +
                        " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ?";
                
                Cursor studentCursor = db.rawQuery(studentQuery, new String[]{String.valueOf(userId)});
                
                if (studentCursor != null && studentCursor.moveToFirst()) {
                    int studentIdColumnIndex = studentCursor.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_ID);
                    int studentNameColumnIndex = studentCursor.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_NAME);
                    
                    long studentId = studentCursor.getLong(studentIdColumnIndex);
                    String studentName = studentCursor.getString(studentNameColumnIndex);
                    
                    // Display student information
                    tvStudentName.setText("Name: " + studentName);
                    tvStudentId.setText("Student ID: " + studentId);
                    tvUsername.setText("Username: " + username);
                    
                    studentCursor.close();
                } else {
                    Log.d(TAG, "No student found with user ID: " + userId);
                }
            } else {
                Log.d(TAG, "No user found with username: " + username);
            }
        } finally {
            db.close();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 