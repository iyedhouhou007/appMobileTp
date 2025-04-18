package com.iyed_houhou.myappmobiletp.viewmodel;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.iyed_houhou.myappmobiletp.DatabaseHelper;
import com.iyed_houhou.myappmobiletp.data.Module;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ModuleViewModel extends AndroidViewModel {
    private MutableLiveData<List<Module>> modules = new MutableLiveData<>();
    private MutableLiveData<Double> overallGrade = new MutableLiveData<>(0.0);
    private String apiUrl = "https://num.univ-biskra.dz/psp/formations/get_modules_json?sem=1&spec=184";
    private DatabaseHelper dbHelper;
    private long studentId = -1; // Add student ID field

    public LiveData<List<Module>> getModules() {
        return modules;
    }

    public LiveData<Double> getOverallGrade() {
        return overallGrade;
    }

    public ModuleViewModel(Application application) {
        super(application);
        dbHelper = new DatabaseHelper(application.getApplicationContext());
    }
    
    // Initialize with a specific student ID
    public void initForStudent(long studentId) {
        this.studentId = studentId;
        Log.d("ModuleViewModel", "Initializing for student ID: " + studentId);
        checkAndFetchModules();
    }

    // Check if modules exist in database, if not fetch from API
    private void checkAndFetchModules() {
        Log.d("ModuleViewModel", "Checking and fetching modules");
        new CheckDatabaseTask().execute();
    }

    public void updateModuleGrade(int position, String fieldType, double value) {
        List<Module> currentModules = modules.getValue();
        if (currentModules != null && position < currentModules.size()) {
            Module module = currentModules.get(position);
            
            // Get existing module ID
            int moduleId = module.getModuleId();
            
            switch (fieldType) {
                case "TD":
                    module.setNoteTD(value);
                    break;
                case "TP":
                    module.setNoteTP(value);
                    break;
                case "Exam":
                    module.setNoteExam(value);
                    break;
            }
            
            // Save to database with student ID
            new SaveGradeTask().execute(new GradeParams(studentId, moduleId, 
                module.getNoteTD(), module.getNoteTP(), module.getNoteExam()));
                
            modules.setValue(currentModules); // Trigger LiveData update
            calculateOverallGrade();
        }
    }
    
    // Grade parameters class for AsyncTask
    private static class GradeParams {
        long studentId;
        int moduleId;
        double tdGrade;
        double tpGrade;
        double examGrade;
        
        GradeParams(long studentId, int moduleId, double tdGrade, double tpGrade, double examGrade) {
            this.studentId = studentId;
            this.moduleId = moduleId;
            this.tdGrade = tdGrade;
            this.tpGrade = tpGrade;
            this.examGrade = examGrade;
        }
    }
    
    // AsyncTask to save grades
    private class SaveGradeTask extends AsyncTask<GradeParams, Void, Void> {
        @Override
        protected Void doInBackground(GradeParams... params) {
            if (params.length == 0) return null;
            
            GradeParams gradeParams = params[0];
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            
            try {
                // Calculate the overall grade for the module
                double grade = 0;
                int count = 0;
                
                if (gradeParams.tdGrade > 0) {
                    grade += gradeParams.tdGrade;
                    count++;
                }
                
                if (gradeParams.tpGrade > 0) {
                    grade += gradeParams.tpGrade;
                    count++;
                }
                
                if (gradeParams.examGrade > 0) {
                    grade += gradeParams.examGrade;
                    count++;
                }
                
                if (count > 0) {
                    grade = grade / count;
                }
                
                // Check if grade already exists
                String query = "SELECT * FROM " + DatabaseHelper.TABLE_GRADES + 
                               " WHERE " + DatabaseHelper.COLUMN_GRADE_STUDENT_ID + " = ? AND " +
                               DatabaseHelper.COLUMN_GRADE_MODULE_ID + " = ?";
                               
                Cursor cursor = db.rawQuery(query, new String[] {
                    String.valueOf(gradeParams.studentId),
                    String.valueOf(gradeParams.moduleId)
                });
                
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_GRADE_STUDENT_ID, gradeParams.studentId);
                values.put(DatabaseHelper.COLUMN_GRADE_MODULE_ID, gradeParams.moduleId);
                values.put(DatabaseHelper.COLUMN_GRADE_VALUE, grade);
                values.put(DatabaseHelper.COLUMN_GRADE_NOTE_TD, gradeParams.tdGrade);
                values.put(DatabaseHelper.COLUMN_GRADE_NOTE_TP, gradeParams.tpGrade);
                values.put(DatabaseHelper.COLUMN_GRADE_NOTE_EXAM, gradeParams.examGrade);
                
                if (cursor != null && cursor.moveToFirst()) {
                    // Update existing grade
                    db.update(DatabaseHelper.TABLE_GRADES, values,
                        DatabaseHelper.COLUMN_GRADE_STUDENT_ID + " = ? AND " +
                        DatabaseHelper.COLUMN_GRADE_MODULE_ID + " = ?",
                        new String[] {
                            String.valueOf(gradeParams.studentId),
                            String.valueOf(gradeParams.moduleId)
                        });
                } else {
                    // Insert new grade
                    db.insert(DatabaseHelper.TABLE_GRADES, null, values);
                }
                
                if (cursor != null) {
                    cursor.close();
                }
            } finally {
                db.close();
            }
            
            return null;
        }
    }

    private void calculateOverallGrade() {
        double totalWeightedGrade = 0;
        int totalCoefficients = 0;
        List<Module> currentModules = modules.getValue();

        if (currentModules != null) {
            for (Module module : currentModules) {
                totalWeightedGrade += module.getGrade() * module.getCoefficient();
                totalCoefficients += module.getCoefficient();
            }
            double calculatedOverallGrade = totalCoefficients > 0 ? totalWeightedGrade / totalCoefficients : 0;
            overallGrade.setValue(calculatedOverallGrade);
        } else {
            overallGrade.setValue(0.0);
        }
    }

    // New task to check if modules exist in database
    private class CheckDatabaseTask extends AsyncTask<Void, Void, List<Module>> {
        @Override
        protected List<Module> doInBackground(Void... voids) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<Module> moduleList = new ArrayList<>();
            
            // First get all modules
            Log.d("ModuleViewModel", "Loading modules from database");
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_MODULES,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MODULE_ID);
                int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MODULE_NAME);
                int tdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MODULE_TD);
                int tpIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MODULE_TP);
                int coeffIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MODULE_COEFFICIENT);
                int creditIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MODULE_CREDIT);
                
                do {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    int td = cursor.getInt(tdIndex);
                    int tp = cursor.getInt(tpIndex);
                    int coefficient = cursor.getInt(coeffIndex);
                    int credit = cursor.getInt(creditIndex);
                    
                    Module module = new Module(id, name, td, tp, coefficient, credit);
                    moduleList.add(module);
                } while (cursor.moveToNext());
                
                cursor.close();
                Log.d("ModuleViewModel", "Loaded " + moduleList.size() + " modules from database");
            } else {
                Log.d("ModuleViewModel", "No modules found in database");
            }
            
            // If student ID is valid, load their grades for each module
            if (studentId > 0 && !moduleList.isEmpty()) {
                Log.d("ModuleViewModel", "Loading grades for student ID: " + studentId);
                for (Module module : moduleList) {
                    String gradeQuery = "SELECT * FROM " + DatabaseHelper.TABLE_GRADES +
                            " WHERE " + DatabaseHelper.COLUMN_GRADE_STUDENT_ID + " = ? AND " +
                            DatabaseHelper.COLUMN_GRADE_MODULE_ID + " = ?";
                            
                    Cursor gradeCursor = db.rawQuery(gradeQuery, new String[]{
                            String.valueOf(studentId),
                            String.valueOf(module.getModuleId())
                    });
                    
                    if (gradeCursor != null && gradeCursor.moveToFirst()) {
                        // Get grade indices
                        int tdGradeIndex = gradeCursor.getColumnIndex(DatabaseHelper.COLUMN_GRADE_NOTE_TD);
                        int tpGradeIndex = gradeCursor.getColumnIndex(DatabaseHelper.COLUMN_GRADE_NOTE_TP);
                        int examGradeIndex = gradeCursor.getColumnIndex(DatabaseHelper.COLUMN_GRADE_NOTE_EXAM);
                        
                        // Set grades on the module
                        module.setNoteTD(gradeCursor.getDouble(tdGradeIndex));
                        module.setNoteTP(gradeCursor.getDouble(tpGradeIndex));
                        module.setNoteExam(gradeCursor.getDouble(examGradeIndex));
                        
                        gradeCursor.close();
                        Log.d("ModuleViewModel", "Found grades for module: " + module.getModuleName());
                    } else {
                        Log.d("ModuleViewModel", "No grades found for module: " + module.getModuleName());
                    }
                }
            } else {
                Log.d("ModuleViewModel", "Skipping grades, studentId: " + studentId + ", modules: " + moduleList.size());
            }
            
            db.close();
            return moduleList;
        }

        @Override
        protected void onPostExecute(List<Module> moduleList) {
            if (moduleList != null && !moduleList.isEmpty()) {
                // If modules exist in the database, use them
                Log.d("ModuleViewModel", "Setting " + moduleList.size() + " modules to LiveData");
                modules.setValue(moduleList);
                calculateOverallGrade();
            } else {
                // Otherwise fetch from API
                Log.d("ModuleViewModel", "No modules found, fetching from API");
                new FetchModulesTask().execute(apiUrl);
            }
        }
    }

    // Original fetch task with added database storage
    private class FetchModulesTask extends AsyncTask<String, Void, List<Module>> {
        @Override
        protected List<Module> doInBackground(String... urls) {
            List<Module> moduleList = new ArrayList<>();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(result.toString());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String moduleName = obj.getString("Nom_module");
                    int td = obj.optInt("td", 0);
                    int tp = obj.optInt("tp", 0);
                    int coefficient = obj.optInt("Coefficient", 1);
                    int credit = obj.optInt("Credit", 0);
                    
                    // Create a module object
                    Module module = new Module(moduleName, td, tp, coefficient, credit);
                    
                    // Store in database
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_MODULE_NAME, moduleName);
                    values.put(DatabaseHelper.COLUMN_MODULE_TD, td);
                    values.put(DatabaseHelper.COLUMN_MODULE_TP, tp);
                    values.put(DatabaseHelper.COLUMN_MODULE_COEFFICIENT, coefficient);
                    values.put(DatabaseHelper.COLUMN_MODULE_CREDIT, credit);
                    
                    long id = db.insert(DatabaseHelper.TABLE_MODULES, null, values);
                    
                    if (id != -1) {
                        // Set the ID from the database
                        module.setModuleId((int)id);
                    }
                    
                    moduleList.add(module);
                }
                
                db.close();
            } catch (Exception e) {
                Log.e("FetchData", "Error fetching data", e);
            }
            return moduleList;
        }

        @Override
        protected void onPostExecute(List<Module> moduleList) {
            if (moduleList != null && !moduleList.isEmpty()) {
                modules.setValue(moduleList);
                calculateOverallGrade();
            }
        }
    }
}