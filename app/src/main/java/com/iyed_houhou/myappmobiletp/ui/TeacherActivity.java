package com.iyed_houhou.myappmobiletp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iyed_houhou.myappmobiletp.DatabaseHelper;
import com.iyed_houhou.myappmobiletp.R;

import java.util.ArrayList;
import java.util.List;

public class TeacherActivity extends AppCompatActivity {

    private TextView tvTeacherUsername;
    private TextView tvTeacherName;
    private TextView tvTeacherModules;
    private Button btnAddGrades;
    private Button btnLogout;
    private DatabaseHelper dbHelper;
    private static final String TAG = "TeacherActivity";
    private String username; // Store the username for passing to other activities

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        dbHelper = new DatabaseHelper(this);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Teacher Dashboard");
        }
        
        // Apply system spacing (status bar height) to the Toolbar
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        tvTeacherUsername = findViewById(R.id.tvTeacherUsername);
        tvTeacherName = findViewById(R.id.tvTeacherName);
        tvTeacherModules = findViewById(R.id.tvTeacherModules);
        btnAddGrades = findViewById(R.id.btnAddGrades);
        btnLogout = findViewById(R.id.btnLogout);

        // Get the username of the logged-in teacher from the Intent
        username = getIntent().getStringExtra("username");

        if (username != null) {
            displayTeacherDetails(username);
        } else {
            // If username not passed directly, try to get from login context
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            username = prefs.getString("username", null);
            if (username != null) {
                displayTeacherDetails(username);
            } else {
                Toast.makeText(this, "Error: User information not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        // Set up click listeners for buttons
        setupButtonListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.student_menu, menu); // Reuse student menu for now
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_logout) {
            // Clear login session and navigate to login screen
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(TeacherActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void setupButtonListeners() {
        btnAddGrades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Add Grades Activity
                Intent intent = new Intent(TeacherActivity.this, AddGradesActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear login session and navigate to login screen
                SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(TeacherActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void displayTeacherDetails(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String teacherName = "";
        List<String> modules = new ArrayList<>();
        long userId = -1;
        long teacherId = -1;

        try {
            // 1. Get the user_id from the users table using username
            String userQuery = "SELECT " + DatabaseHelper.COLUMN_USER_ID + 
                    " FROM " + DatabaseHelper.TABLE_USERS + 
                    " WHERE " + DatabaseHelper.COLUMN_USERNAME + " = ?";
            
            Cursor userCursor = db.rawQuery(userQuery, new String[]{username});
            if (userCursor != null && userCursor.moveToFirst()) {
                userId = userCursor.getLong(userCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
                userCursor.close();
                Log.d(TAG, "Found user ID: " + userId);
            } else {
                Log.e(TAG, "User not found for username: " + username);
                return;
            }

            // 2. Get the teacher details using the user_id from teachers table
            String teacherQuery = "SELECT " + DatabaseHelper.COLUMN_TEACHER_ID + ", " + 
                    DatabaseHelper.COLUMN_TEACHER_NAME + 
                    " FROM " + DatabaseHelper.TABLE_TEACHERS + 
                    " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ?";
            
            Cursor teacherCursor = db.rawQuery(teacherQuery, new String[]{String.valueOf(userId)});
            if (teacherCursor != null && teacherCursor.moveToFirst()) {
                teacherId = teacherCursor.getLong(teacherCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TEACHER_ID));
                teacherName = teacherCursor.getString(teacherCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TEACHER_NAME));
                teacherCursor.close();
                Log.d(TAG, "Found teacher: " + teacherName + " with ID: " + teacherId);
            } else {
                Log.e(TAG, "Teacher not found for user ID: " + userId);
                return;
            }

            // 3. Get the modules assigned to this teacher
            String modulesQuery = "SELECT m." + DatabaseHelper.COLUMN_MODULE_NAME + 
                    " FROM " + DatabaseHelper.TABLE_MODULES + " m" +
                    " INNER JOIN " + DatabaseHelper.TABLE_TEACHER_MODULES + " tm" +
                    " ON m." + DatabaseHelper.COLUMN_MODULE_ID + " = tm." + DatabaseHelper.COLUMN_TEACHER_MODULES_MODULE_ID +
                    " WHERE tm." + DatabaseHelper.COLUMN_TEACHER_MODULES_TEACHER_ID + " = ?";
            
            Cursor modulesCursor = db.rawQuery(modulesQuery, new String[]{String.valueOf(teacherId)});
            if (modulesCursor != null && modulesCursor.moveToFirst()) {
                do {
                    String moduleName = modulesCursor.getString(modulesCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MODULE_NAME));
                    modules.add(moduleName);
                } while (modulesCursor.moveToNext());
                modulesCursor.close();
                Log.d(TAG, "Found " + modules.size() + " modules for teacher");
            } else {
                Log.d(TAG, "No modules found for teacher ID: " + teacherId);
            }

            // Set the text views
            tvTeacherUsername.setText("Username: " + username);
            tvTeacherName.setText("Name: " + teacherName);
            
            if (modules.isEmpty()) {
                tvTeacherModules.setText("Modules: No modules assigned");
            } else {
                StringBuilder modulesText = new StringBuilder("Modules: ");
                for (int i = 0; i < modules.size(); i++) {
                    modulesText.append(modules.get(i));
                    if (i < modules.size() - 1) {
                        modulesText.append(", ");
                    }
                }
                tvTeacherModules.setText(modulesText.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying teacher details", e);
            Toast.makeText(this, "Error loading teacher details", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }
}