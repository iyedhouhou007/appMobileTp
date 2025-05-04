package com.iyed_houhou.myappmobiletp.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iyed_houhou.myappmobiletp.DatabaseHelper;
import com.iyed_houhou.myappmobiletp.R;
import com.iyed_houhou.myappmobiletp.data.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddGradesActivity extends AppCompatActivity {

    private static final String TAG = "AddGradesActivity";
    private DatabaseHelper dbHelper;
    private TextView tvTeacherInfo;
    private Spinner spinnerStudents;
    private Spinner spinnerModules;
    private EditText etTDGrade;
    private EditText etTPGrade;
    private EditText etExamGrade;
    private LinearLayout layoutTDGrade;
    private LinearLayout layoutTPGrade;
    private Button btnSaveGrades;
    private Button btnBack;

    private long teacherId = -1;
    private String teacherName = "";
    private List<StudentItem> studentList;
    private List<ModuleItem> moduleList;
    private Map<Long, Boolean> moduleTDMap = new HashMap<>();
    private Map<Long, Boolean> moduleTPMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_grades);

        dbHelper = new DatabaseHelper(this);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add Student Grades");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Apply system spacing (status bar height) to the Toolbar
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // Initialize UI components
        tvTeacherInfo = findViewById(R.id.tvTeacherInfo);
        spinnerStudents = findViewById(R.id.spinnerStudents);
        spinnerModules = findViewById(R.id.spinnerModules);
        etTDGrade = findViewById(R.id.etTDGrade);
        etTPGrade = findViewById(R.id.etTPGrade);
        etExamGrade = findViewById(R.id.etExamGrade);
        layoutTDGrade = findViewById(R.id.layoutTDGrade);
        layoutTPGrade = findViewById(R.id.layoutTPGrade);
        btnSaveGrades = findViewById(R.id.btnSaveGrades);
        btnBack = findViewById(R.id.btnBack);

        // Get the username from intent or SharedPreferences
        String username = getIntent().getStringExtra("username");
        if (username == null) {
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            username = prefs.getString("username", null);
        }

        if (username != null) {
            // Get teacher ID and name from database
            loadTeacherInfo(username);
            
            // Load students and modules data
            loadStudents();
            loadTeacherModules();
            
            // Set up spinners
            setupSpinners();
        } else {
            Toast.makeText(this, "Error: Teacher information not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Module selection listener to toggle TD/TP fields based on the module capabilities
        spinnerModules.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < moduleList.size()) {
                    ModuleItem selectedModule = moduleList.get(position);
                    
                    // Show/hide TD grade input based on module TD capability
                    layoutTDGrade.setVisibility(selectedModule.hasTD ? View.VISIBLE : View.GONE);
                    if (!selectedModule.hasTD) {
                        etTDGrade.setText("");
                    }
                    
                    // Show/hide TP grade input based on module TP capability
                    layoutTPGrade.setVisibility(selectedModule.hasTP ? View.VISIBLE : View.GONE);
                    if (!selectedModule.hasTP) {
                        etTPGrade.setText("");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Save button click listener
        btnSaveGrades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGrades();
            }
        });

        // Back button click listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddGradesActivity.this, TeacherActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadTeacherInfo(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            // Get user ID from username
            String userQuery = "SELECT " + DatabaseHelper.COLUMN_USER_ID + 
                    " FROM " + DatabaseHelper.TABLE_USERS + 
                    " WHERE " + DatabaseHelper.COLUMN_USERNAME + " = ?";
            Cursor userCursor = db.rawQuery(userQuery, new String[]{username});
            
            if (userCursor != null && userCursor.moveToFirst()) {
                long userId = userCursor.getLong(userCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
                userCursor.close();
                
                // Get teacher details using user ID
                String teacherQuery = "SELECT " + DatabaseHelper.COLUMN_TEACHER_ID + ", " + 
                        DatabaseHelper.COLUMN_TEACHER_NAME + 
                        " FROM " + DatabaseHelper.TABLE_TEACHERS + 
                        " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ?";
                Cursor teacherCursor = db.rawQuery(teacherQuery, new String[]{String.valueOf(userId)});
                
                if (teacherCursor != null && teacherCursor.moveToFirst()) {
                    teacherId = teacherCursor.getLong(teacherCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TEACHER_ID));
                    teacherName = teacherCursor.getString(teacherCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TEACHER_NAME));
                    tvTeacherInfo.setText("Teacher: " + teacherName);
                    teacherCursor.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading teacher info", e);
        } finally {
            db.close();
        }
    }

    private void loadStudents() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        studentList = new ArrayList<>();
        
        try {
            // Query to get all students
            String query = "SELECT " + DatabaseHelper.COLUMN_STUDENT_ID + ", " + 
                    DatabaseHelper.COLUMN_STUDENT_NAME + ", " +
                    DatabaseHelper.COLUMN_USER_ID +
                    " FROM " + DatabaseHelper.TABLE_STUDENTS;
            Cursor cursor = db.rawQuery(query, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long studentId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STUDENT_ID));
                    String studentName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STUDENT_NAME));
                    long userId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
                    
                    studentList.add(new StudentItem(studentId, studentName, userId));
                } while (cursor.moveToNext());
                cursor.close();
            } else {
                Log.d(TAG, "No students found in database");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading students", e);
        } finally {
            db.close();
        }
    }

    private void loadTeacherModules() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        moduleList = new ArrayList<>();
        
        try {
            // Query to get modules assigned to this teacher
            String query = "SELECT m." + DatabaseHelper.COLUMN_MODULE_ID + ", " +
                    "m." + DatabaseHelper.COLUMN_MODULE_NAME + ", " +
                    "m." + DatabaseHelper.COLUMN_MODULE_TD + ", " +
                    "m." + DatabaseHelper.COLUMN_MODULE_TP + ", " +
                    "m." + DatabaseHelper.COLUMN_MODULE_COEFFICIENT + ", " +
                    "m." + DatabaseHelper.COLUMN_MODULE_CREDIT +
                    " FROM " + DatabaseHelper.TABLE_MODULES + " m" +
                    " INNER JOIN " + DatabaseHelper.TABLE_TEACHER_MODULES + " tm" +
                    " ON m." + DatabaseHelper.COLUMN_MODULE_ID + " = tm." + DatabaseHelper.COLUMN_TEACHER_MODULES_MODULE_ID +
                    " WHERE tm." + DatabaseHelper.COLUMN_TEACHER_MODULES_TEACHER_ID + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(teacherId)});
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long moduleId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MODULE_ID));
                    String moduleName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MODULE_NAME));
                    boolean hasTD = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MODULE_TD)) == 1;
                    boolean hasTP = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MODULE_TP)) == 1;
                    int coefficient = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MODULE_COEFFICIENT));
                    int credit = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MODULE_CREDIT));
                    
                    moduleList.add(new ModuleItem(moduleId, moduleName, hasTD, hasTP, coefficient, credit));
                    moduleTDMap.put(moduleId, hasTD);
                    moduleTPMap.put(moduleId, hasTP);
                } while (cursor.moveToNext());
                cursor.close();
            } else {
                Log.d(TAG, "No modules found for teacher ID: " + teacherId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading teacher modules", e);
        } finally {
            db.close();
        }
    }

    private void setupSpinners() {
        // Set up students spinner
        List<String> studentNames = new ArrayList<>();
        for (StudentItem student : studentList) {
            studentNames.add(student.name);
        }
        
        ArrayAdapter<String> studentsAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, studentNames);
        studentsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudents.setAdapter(studentsAdapter);
        
        // Set up modules spinner
        List<String> moduleNames = new ArrayList<>();
        for (ModuleItem module : moduleList) {
            moduleNames.add(module.name);
        }
        
        ArrayAdapter<String> modulesAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, moduleNames);
        modulesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModules.setAdapter(modulesAdapter);

        // Initial visibility of TD/TP fields based on first module if available
        if (moduleList.size() > 0) {
            ModuleItem firstModule = moduleList.get(0);
            layoutTDGrade.setVisibility(firstModule.hasTD ? View.VISIBLE : View.GONE);
            layoutTPGrade.setVisibility(firstModule.hasTP ? View.VISIBLE : View.GONE);
        }
    }

    private void saveGrades() {
        // Validate input
        if (spinnerStudents.getSelectedItemPosition() == -1 || studentList.isEmpty()) {
            Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (spinnerModules.getSelectedItemPosition() == -1 || moduleList.isEmpty()) {
            Toast.makeText(this, "Please select a module", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get selected student and module
        StudentItem selectedStudent = studentList.get(spinnerStudents.getSelectedItemPosition());
        ModuleItem selectedModule = moduleList.get(spinnerModules.getSelectedItemPosition());
        
        // Validate grade inputs
        String tdGradeStr = etTDGrade.getText().toString().trim();
        String tpGradeStr = etTPGrade.getText().toString().trim();
        String examGradeStr = etExamGrade.getText().toString().trim();
        
        double tdGrade = 0;
        double tpGrade = 0;
        double examGrade = 0;
        double finalGrade = 0;
        
        // Remove mandatory validation - instead, just check if at least one grade is entered
        boolean hasAtLeastOneGrade = false;
        
        if (!tdGradeStr.isEmpty() || !tpGradeStr.isEmpty() || !examGradeStr.isEmpty()) {
            hasAtLeastOneGrade = true;
        }
        
        if (!hasAtLeastOneGrade) {
            Toast.makeText(this, "Please enter at least one grade (TD, TP, or Exam)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Parse grades
        try {
            // Parse TD grade if entered
            if (!tdGradeStr.isEmpty()) {
                tdGrade = Double.parseDouble(tdGradeStr);
                if (tdGrade < 0 || tdGrade > 20) {
                    Toast.makeText(this, "TD grade must be between 0 and 20", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Parse TP grade if entered
            if (!tpGradeStr.isEmpty()) {
                tpGrade = Double.parseDouble(tpGradeStr);
                if (tpGrade < 0 || tpGrade > 20) {
                    Toast.makeText(this, "TP grade must be between 0 and 20", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Parse exam grade if entered
            if (!examGradeStr.isEmpty()) {
                examGrade = Double.parseDouble(examGradeStr);
                if (examGrade < 0 || examGrade > 20) {
                    Toast.makeText(this, "Exam grade must be between 0 and 20", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Calculate final grade based on available components
            double tdTpSum = 0;
            int tdTpCount = 0;
            
            // Count TD if it has a value
            if (!tdGradeStr.isEmpty() && selectedModule.hasTD) {
                tdTpSum += tdGrade;
                tdTpCount++;
            }
            
            // Count TP if it has a value
            if (!tpGradeStr.isEmpty() && selectedModule.hasTP) {
                tdTpSum += tpGrade;
                tdTpCount++;
            }
            
            // Calculate final grade
            if (tdTpCount > 0 && !examGradeStr.isEmpty()) {
                // If we have both TD/TP and exam grades
                double tdTpAverage = tdTpSum / tdTpCount;
                finalGrade = (tdTpAverage * 0.4) + (examGrade * 0.6);
            } else if (tdTpCount > 0 && examGradeStr.isEmpty()) {
                // If we only have TD/TP grades
                finalGrade = tdTpSum / tdTpCount;
            } else if (tdTpCount == 0 && !examGradeStr.isEmpty()) {
                // If we only have exam grade
                finalGrade = examGrade;
            }
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid grade format", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if grade already exists for this student and module
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long gradeId = -1;
        
        try {
            String checkQuery = "SELECT " + DatabaseHelper.COLUMN_GRADE_ID + 
                    " FROM " + DatabaseHelper.TABLE_GRADES + 
                    " WHERE " + DatabaseHelper.COLUMN_GRADE_STUDENT_ID + " = ? AND " + 
                    DatabaseHelper.COLUMN_GRADE_MODULE_ID + " = ?";
            Cursor cursor = db.rawQuery(checkQuery, new String[]{
                    String.valueOf(selectedStudent.id),
                    String.valueOf(selectedModule.id)
            });
            
            if (cursor != null && cursor.moveToFirst()) {
                // Grade exists, get its ID for update
                gradeId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRADE_ID));
                cursor.close();
            }
            
            // Prepare values to save
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_GRADE_STUDENT_ID, selectedStudent.id);
            values.put(DatabaseHelper.COLUMN_GRADE_MODULE_ID, selectedModule.id);
            values.put(DatabaseHelper.COLUMN_GRADE_TEACHER_ID, teacherId);
            values.put(DatabaseHelper.COLUMN_GRADE_VALUE, finalGrade);
            values.put(DatabaseHelper.COLUMN_GRADE_NOTE_TD, tdGrade);
            values.put(DatabaseHelper.COLUMN_GRADE_NOTE_TP, tpGrade);
            values.put(DatabaseHelper.COLUMN_GRADE_NOTE_EXAM, examGrade);
            
            if (gradeId != -1) {
                // Update existing grade
                db.update(DatabaseHelper.TABLE_GRADES, values, 
                        DatabaseHelper.COLUMN_GRADE_ID + " = ?", 
                        new String[]{String.valueOf(gradeId)});
                Toast.makeText(this, "Grade updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Insert new grade
                db.insert(DatabaseHelper.TABLE_GRADES, null, values);
                Toast.makeText(this, "Grade saved successfully", Toast.LENGTH_SHORT).show();
            }
            
            // Clear input fields
            etTDGrade.setText("");
            etTPGrade.setText("");
            etExamGrade.setText("");
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving grades", e);
            Toast.makeText(this, "Error saving grades: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    // Helper classes for storing data
    private static class StudentItem {
        public long id;
        public String name;
        public long userId;
        
        public StudentItem(long id, String name, long userId) {
            this.id = id;
            this.name = name;
            this.userId = userId;
        }
    }
    
    private static class ModuleItem {
        public long id;
        public String name;
        public boolean hasTD;
        public boolean hasTP;
        public int coefficient;
        public int credit;
        
        public ModuleItem(long id, String name, boolean hasTD, boolean hasTP, int coefficient, int credit) {
            this.id = id;
            this.name = name;
            this.hasTD = hasTD;
            this.hasTP = hasTP;
            this.coefficient = coefficient;
            this.credit = credit;
        }
    }
} 