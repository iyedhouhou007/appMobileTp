package com.iyed_houhou.myappmobiletp.ui;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.iyed_houhou.myappmobiletp.DatabaseHelper;
import com.iyed_houhou.myappmobiletp.R;

public class DatabaseViewActivity extends AppCompatActivity {

    private static final String TAG = "DatabaseViewActivity";
    private DatabaseHelper dbHelper;
    private TextView tvUsersTable;
    private TextView tvTeachersTable;
    private TextView tvStudentsTable;
    private TextView tvModulesTable;
    private TextView tvTeacherModulesTable;
    private TextView tvGradesTable;
    private Button btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_view);

        dbHelper = new DatabaseHelper(this);

        // Initialize UI components
        tvUsersTable = findViewById(R.id.tvUsersTable);
        tvTeachersTable = findViewById(R.id.tvTeachersTable);
        tvStudentsTable = findViewById(R.id.tvStudentsTable);
        tvModulesTable = findViewById(R.id.tvModulesTable);
        tvTeacherModulesTable = findViewById(R.id.tvTeacherModulesTable);
        tvGradesTable = findViewById(R.id.tvGradesTable);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        // Load database data asynchronously
        new LoadDatabaseDataTask().execute();

        // Set up button click listener
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DatabaseViewActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private class LoadDatabaseDataTask extends AsyncTask<Void, Void, Void> {
        private String usersData = "No data";
        private String teachersData = "No data";
        private String studentsData = "No data";
        private String modulesData = "No data";
        private String teacherModulesData = "No data";
        private String gradesData = "No data";

        @Override
        protected Void doInBackground(Void... params) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            try {
                // Load Users table data
                usersData = getTableData(db, 
                        DatabaseHelper.TABLE_USERS,
                        new String[]{
                                DatabaseHelper.COLUMN_USER_ID,
                                DatabaseHelper.COLUMN_USERNAME,
                                DatabaseHelper.COLUMN_PASSWORD,
                                DatabaseHelper.COLUMN_ROLE
                        });
                
                // Load Teachers table data
                teachersData = getTableData(db, 
                        DatabaseHelper.TABLE_TEACHERS,
                        new String[]{
                                DatabaseHelper.COLUMN_TEACHER_ID,
                                DatabaseHelper.COLUMN_USER_ID,
                                DatabaseHelper.COLUMN_TEACHER_NAME
                        });
                
                // Load Students table data
                studentsData = getTableData(db, 
                        DatabaseHelper.TABLE_STUDENTS,
                        new String[]{
                                DatabaseHelper.COLUMN_STUDENT_ID,
                                DatabaseHelper.COLUMN_USER_ID,
                                DatabaseHelper.COLUMN_STUDENT_NAME
                        });
                
                // Load Modules table data
                modulesData = getTableData(db, 
                        DatabaseHelper.TABLE_MODULES,
                        new String[]{
                                DatabaseHelper.COLUMN_MODULE_ID,
                                DatabaseHelper.COLUMN_MODULE_NAME,
                                DatabaseHelper.COLUMN_MODULE_TD,
                                DatabaseHelper.COLUMN_MODULE_TP,
                                DatabaseHelper.COLUMN_MODULE_COEFFICIENT,
                                DatabaseHelper.COLUMN_MODULE_CREDIT
                        });
                
                // Load Teacher-Modules table data
                teacherModulesData = getTableData(db, 
                        DatabaseHelper.TABLE_TEACHER_MODULES,
                        new String[]{
                                DatabaseHelper.COLUMN_TEACHER_MODULES_TEACHER_ID,
                                DatabaseHelper.COLUMN_TEACHER_MODULES_MODULE_ID
                        });
                
                // Load Grades table data
                gradesData = getTableData(db, 
                        DatabaseHelper.TABLE_GRADES,
                        new String[]{
                                DatabaseHelper.COLUMN_GRADE_ID,
                                DatabaseHelper.COLUMN_GRADE_STUDENT_ID,
                                DatabaseHelper.COLUMN_GRADE_MODULE_ID,
                                DatabaseHelper.COLUMN_GRADE_TEACHER_ID,
                                DatabaseHelper.COLUMN_GRADE_VALUE,
                                DatabaseHelper.COLUMN_GRADE_NOTE_TD,
                                DatabaseHelper.COLUMN_GRADE_NOTE_TP,
                                DatabaseHelper.COLUMN_GRADE_NOTE_EXAM
                        });
            } catch (Exception e) {
                Log.e(TAG, "Error loading database data", e);
            } finally {
                db.close();
            }
            
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Update UI with the loaded data
            tvUsersTable.setText(usersData);
            tvTeachersTable.setText(teachersData);
            tvStudentsTable.setText(studentsData);
            tvModulesTable.setText(modulesData);
            tvTeacherModulesTable.setText(teacherModulesData);
            tvGradesTable.setText(gradesData);
        }

        /**
         * Helper method to get formatted table data
         */
        private String getTableData(SQLiteDatabase db, String tableName, String[] columns) {
            StringBuilder result = new StringBuilder();
            
            try {
                // First check if table exists
                Cursor tableCheck = db.rawQuery(
                        "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                        new String[]{tableName});
                
                if (tableCheck == null || !tableCheck.moveToFirst()) {
                    if (tableCheck != null) {
                        tableCheck.close();
                    }
                    return "Table " + tableName + " does not exist";
                }
                tableCheck.close();
                
                // Query the table
                Cursor cursor = db.query(
                        tableName,
                        columns,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                
                if (cursor == null || cursor.getCount() == 0) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return "No data in " + tableName;
                }
                
                // Add column headers
                for (String column : columns) {
                    result.append(String.format("%-15s", column));
                }
                result.append("\n");
                
                // Add separator line
                for (int i = 0; i < columns.length; i++) {
                    result.append("---------------");
                }
                result.append("\n");
                
                // Add data rows
                if (cursor.moveToFirst()) {
                    do {
                        for (String column : columns) {
                            int columnIndex = cursor.getColumnIndex(column);
                            if (columnIndex != -1) {
                                String value = cursor.getString(columnIndex);
                                result.append(String.format("%-15s", value != null ? value : "null"));
                            } else {
                                result.append(String.format("%-15s", "N/A"));
                            }
                        }
                        result.append("\n");
                    } while (cursor.moveToNext());
                }
                
                cursor.close();
            } catch (Exception e) {
                result.append("Error accessing ").append(tableName).append(": ").append(e.getMessage());
                Log.e(TAG, "Error getting data from " + tableName, e);
            }
            
            return result.toString();
        }
    }
} 