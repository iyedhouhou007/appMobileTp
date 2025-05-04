package com.iyed_houhou.myappmobiletp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.iyed_houhou.myappmobiletp.DatabaseHelper;
import com.iyed_houhou.myappmobiletp.R;
import com.iyed_houhou.myappmobiletp.adapter.ItemAdapter;
import com.iyed_houhou.myappmobiletp.data.Module;
import com.iyed_houhou.myappmobiletp.viewmodel.ModuleViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GradesActivity extends AppCompatActivity {
    private ItemAdapter adapter;
    private TextView overallGradeTextView;
    private ModuleViewModel viewModel;
    private DatabaseHelper dbHelper;
    private static final String TAG = "GradesActivity";
    private String username; // Store username for later use

    // Get student ID from logged-in user
    private long studentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Get logged-in username from intent
        username = getIntent().getStringExtra("username");
        Log.d(TAG, "Username from intent: " + (username != null ? username : "null"));
        
        if (username != null) {
            // Get student ID from username
            studentId = getStudentIdFromUsername(username);
            Log.d(TAG, "Student ID: " + studentId);
        }

        // Find the Toolbar and set it as ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Grades");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Apply system spacing (status bar height) to the Toolbar
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        overallGradeTextView = findViewById(R.id.tvOverallGrade);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ModuleViewModel.class);
        
        // Initialize ViewModel with student ID
        if (studentId > 0) {
            Log.d(TAG, "Initializing ViewModel with student ID: " + studentId);
            viewModel.initForStudent(studentId);
        } else {
            Log.d(TAG, "No valid student ID to initialize ViewModel");
        }

        // Initialize adapter
        adapter = new ItemAdapter(viewModel);
        recyclerView.setAdapter(adapter);

        // Observe the modules LiveData from the ViewModel
        viewModel.getModules().observe(this, modules -> {
            Log.d(TAG, "Received " + (modules != null ? modules.size() : 0) + " modules from ViewModel");
            // Update the adapter with the modules
            adapter.submitList(modules);
        });

        // Observe the overall grade LiveData
        viewModel.getOverallGrade().observe(this, grade -> {
            overallGradeTextView.setText(String.format(Locale.US, "Overall Grade: %.2f", grade));
            changeOverallGradeColor(grade);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.student_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        int id = item.getItemId();

//        if (id == R.id.menu_grades) {
//            // We're already in the grades activity
//            return true;
//        }
//        else
        if (id == R.id.menu_profile) {
            // Navigate to student profile
            Intent profileIntent = new Intent(this, StudentProfileActivity.class);
            profileIntent.putExtra("username", username);
            startActivity(profileIntent);
            return true;
        }
        else if (id == R.id.menu_settings) {
            // Show settings
            Toast.makeText(this, "Settings feature coming soon", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (id == R.id.menu_logout) {
            // Logout: Clear shared preferences and go back to login
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private long getStudentIdFromUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long studentId = -1;
        
        try {
            // First get the user ID
            String userQuery = "SELECT " + DatabaseHelper.COLUMN_USER_ID + 
                            " FROM " + DatabaseHelper.TABLE_USERS +
                            " WHERE " + DatabaseHelper.COLUMN_USERNAME + " = ?";
            Log.d(TAG, "Executing user query: " + userQuery + " with username: " + username);
            
            Cursor userCursor = db.rawQuery(userQuery, new String[]{username});
            
            if (userCursor != null && userCursor.moveToFirst()) {
                int userIdColumnIndex = userCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID);
                long userId = userCursor.getLong(userIdColumnIndex);
                Log.d(TAG, "Found user ID: " + userId);
                userCursor.close();
                
                // Then get the student ID
                String studentQuery = "SELECT " + DatabaseHelper.COLUMN_STUDENT_ID +
                                    " FROM " + DatabaseHelper.TABLE_STUDENTS +
                                    " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ?";
                Log.d(TAG, "Executing student query: " + studentQuery + " with user ID: " + userId);
                
                Cursor studentCursor = db.rawQuery(studentQuery, new String[]{String.valueOf(userId)});
                
                if (studentCursor != null && studentCursor.moveToFirst()) {
                    int studentIdColumnIndex = studentCursor.getColumnIndex(DatabaseHelper.COLUMN_STUDENT_ID);
                    studentId = studentCursor.getLong(studentIdColumnIndex);
                    Log.d(TAG, "Found student ID: " + studentId);
                    studentCursor.close();
                } else {
                    Log.d(TAG, "No student found for user ID: " + userId);
                }
            } else {
                Log.d(TAG, "No user found with username: " + username);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting student ID: " + e.getMessage());
        } finally {
            db.close();
        }
        
        return studentId;
    }

    private void changeOverallGradeColor(double grade) {
        int color = ContextCompat.getColor(this,
                grade >= 10.0 ? R.color.gradeColorGreen : R.color.gradeColorRed);
        overallGradeTextView.setTextColor(color);
    }
}