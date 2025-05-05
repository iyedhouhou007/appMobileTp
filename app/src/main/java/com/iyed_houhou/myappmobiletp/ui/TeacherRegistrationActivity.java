package com.iyed_houhou.myappmobiletp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.iyed_houhou.myappmobiletp.DatabaseHelper;
import com.iyed_houhou.myappmobiletp.R;
import com.iyed_houhou.myappmobiletp.adapter.ModuleSelectionAdapter;
import com.iyed_houhou.myappmobiletp.data.Module;
import com.iyed_houhou.myappmobiletp.viewmodel.ModuleViewModel;

import java.util.List;
import java.util.ArrayList;

public class TeacherRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "TeacherRegistration";
    private EditText editTextTeacherName;
    private RecyclerView modulesRecyclerView;
    private ModuleSelectionAdapter moduleSelectionAdapter;
    private Button btnRegisterTeacher;
    private ModuleViewModel moduleViewModel;
    private DatabaseHelper dbHelper;
    private List<Module> allModules;
    private long userId; // To store the user ID from the previous activity
    private String username; // To store the username 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_registration); 

        dbHelper = new DatabaseHelper(this);
        editTextTeacherName = findViewById(R.id.editTextTeacherName);
        modulesRecyclerView = findViewById(R.id.modulesRecyclerView);
        btnRegisterTeacher = findViewById(R.id.btnRegisterTeacher);

        modulesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        moduleViewModel = new ViewModelProvider(this).get(ModuleViewModel.class);

        // Retrieve the user ID passed from RegisterActivity
        userId = getIntent().getLongExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        
        if (userId == -1) {
            // Handle error if user ID is not passed
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        moduleViewModel.getModules().observe(this, modules -> {
            if (modules != null && !modules.isEmpty()) {
                allModules = modules;
                moduleSelectionAdapter = new ModuleSelectionAdapter(modules);
                modulesRecyclerView.setAdapter(moduleSelectionAdapter);
            } else {
                Log.e(TAG, "No modules available for selection");
                Toast.makeText(this, "Error: No modules available", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegisterTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String teacherName = editTextTeacherName.getText().toString().trim();

                if (teacherName.isEmpty()) {
                    Toast.makeText(TeacherRegistrationActivity.this, "Please enter teacher name", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (moduleSelectionAdapter == null) {
                    Toast.makeText(TeacherRegistrationActivity.this, "Module selection not available", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Integer> selectedModuleIds = moduleSelectionAdapter.getSelectedModuleIds();
                
                if (selectedModuleIds.isEmpty()) {
                    Toast.makeText(TeacherRegistrationActivity.this, "Please select at least one module", Toast.LENGTH_SHORT).show();
                    return;
                }

                long newTeacherId = saveTeacherToDatabase(teacherName, userId);

                if (newTeacherId != -1) {
                    saveTeacherModules(newTeacherId, selectedModuleIds);
                    
                    // Save username in SharedPreferences for later use
                    if (username != null) {
                        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("username", username);
                        editor.apply();
                    }
                    
                    Toast.makeText(TeacherRegistrationActivity.this, "Teacher registered successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(TeacherRegistrationActivity.this, MenuActivity.class);
                    
                    // Pass the username and role to MenuActivity
                    if (username != null) {
                        intent.putExtra("username", username);
                    }
                    intent.putExtra("userRole", "Teacher");
                    
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(TeacherRegistrationActivity.this, "Error registering teacher", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private long saveTeacherToDatabase(String teacherName, long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TEACHER_NAME, teacherName);
        values.put(DatabaseHelper.COLUMN_USER_ID, userId); // Link the teacher to the user
        long newRowId = -1;
        try {
            newRowId = db.insert(DatabaseHelper.TABLE_TEACHERS, null, values);
            Log.d(TAG, "Teacher saved with ID: " + newRowId + " for user ID: " + userId);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error saving teacher", e);
        } finally {
            db.close();
        }
        return newRowId;
    }

    private void saveTeacherModules(long teacherId, List<Integer> moduleIds) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (int moduleId : moduleIds) {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_TEACHER_MODULES_TEACHER_ID, teacherId);
                values.put(DatabaseHelper.COLUMN_TEACHER_MODULES_MODULE_ID, moduleId);
                db.insert(DatabaseHelper.TABLE_TEACHER_MODULES, null, values);
                Log.d(TAG, "Added module ID: " + moduleId + " for teacher ID: " + teacherId);
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(TAG, "Error saving teacher modules", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}