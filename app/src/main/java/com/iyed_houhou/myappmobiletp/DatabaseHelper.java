package com.iyed_houhou.myappmobiletp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "StudentGradesApp.db";
    private static final int DATABASE_VERSION = 7; // Increment version after adding new table

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_TEACHERS = "teachers";
    public static final String TABLE_STUDENTS = "students";
    public static final String TABLE_MODULES = "modules";
    public static final String TABLE_GRADES = "grades";
    public static final String TABLE_TEACHER_MODULES = "teacher_modules";

    // Common column names
    public static final String COLUMN_ID = "id";

    // Users table columns
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROLE = "role";

    // Teachers table columns
    public static final String COLUMN_TEACHER_ID = "teacher_id";
    public static final String COLUMN_TEACHER_NAME = "teacher_name";
    // Add other teacher columns if needed

    // Students table columns
    public static final String COLUMN_STUDENT_ID = "student_id";
    public static final String COLUMN_STUDENT_NAME = "student_name";
    // Add other student columns if needed

    // Modules table columns
    public static final String COLUMN_MODULE_ID = "module_id"; // Assuming you'll add this
    public static final String COLUMN_MODULE_NAME = "Nom_module";
    public static final String COLUMN_MODULE_TD = "td";
    public static final String COLUMN_MODULE_TP = "tp";
    public static final String COLUMN_MODULE_COEFFICIENT = "Coefficient";
    public static final String COLUMN_MODULE_CREDIT = "Credit";

    // Grades table columns
    public static final String COLUMN_GRADE_ID = "grade_id";
    public static final String COLUMN_GRADE_STUDENT_ID = "student_id";
    public static final String COLUMN_GRADE_MODULE_ID = "module_id";
    public static final String COLUMN_GRADE_TEACHER_ID = "teacher_id";
    public static final String COLUMN_GRADE_VALUE = "grade_value";
    public static final String COLUMN_GRADE_NOTE_TD = "noteTD";
    public static final String COLUMN_GRADE_NOTE_TP = "noteTP";
    public static final String COLUMN_GRADE_NOTE_EXAM = "noteExam";

    // Teacher-Modules table columns
    public static final String COLUMN_TEACHER_MODULES_TEACHER_ID = "teacher_id";
    public static final String COLUMN_TEACHER_MODULES_MODULE_ID = "module_id";

    // SQL commands to create tables
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + "(" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_ROLE + " TEXT NOT NULL" +
                    ")";

    private static final String CREATE_TABLE_TEACHERS =
            "CREATE TABLE " + TABLE_TEACHERS + "(" +
                    COLUMN_TEACHER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " INTEGER NOT NULL UNIQUE, " + // Add user_id, make it unique
                    COLUMN_TEACHER_NAME + " TEXT NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_STUDENTS =
            "CREATE TABLE " + TABLE_STUDENTS + "(" +
                    COLUMN_STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " INTEGER NOT NULL UNIQUE, " + // Add user_id, make it unique
                    COLUMN_STUDENT_NAME + " TEXT NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_MODULES =
            "CREATE TABLE " + TABLE_MODULES + "(" +
                    COLUMN_MODULE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // Added module_id
                    COLUMN_MODULE_NAME + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_MODULE_TD + " INTEGER, " +
                    COLUMN_MODULE_TP + " INTEGER, " +
                    COLUMN_MODULE_COEFFICIENT + " INTEGER, " +
                    COLUMN_MODULE_CREDIT + " INTEGER" +
                    ")";

    private static final String CREATE_TABLE_GRADES =
            "CREATE TABLE " + TABLE_GRADES + "(" +
                    COLUMN_GRADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_GRADE_STUDENT_ID + " INTEGER, " +
                    COLUMN_GRADE_MODULE_ID + " INTEGER, " +
                    COLUMN_GRADE_TEACHER_ID + " INTEGER, " +
                    COLUMN_GRADE_VALUE + " REAL, " +
                    COLUMN_GRADE_NOTE_TD + " REAL, " +
                    COLUMN_GRADE_NOTE_TP + " REAL, " +
                    COLUMN_GRADE_NOTE_EXAM + " REAL, " +
                    "FOREIGN KEY(" + COLUMN_GRADE_STUDENT_ID + ") REFERENCES " + TABLE_STUDENTS + "(" + COLUMN_STUDENT_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_GRADE_MODULE_ID + ") REFERENCES " + TABLE_MODULES + "(" + COLUMN_MODULE_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_GRADE_TEACHER_ID + ") REFERENCES " + TABLE_TEACHERS + "(" + COLUMN_TEACHER_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_TEACHER_MODULES =
            "CREATE TABLE " + TABLE_TEACHER_MODULES + "(" +
                    COLUMN_TEACHER_MODULES_TEACHER_ID + " INTEGER, " +
                    COLUMN_TEACHER_MODULES_MODULE_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_TEACHER_MODULES_TEACHER_ID + ") REFERENCES " + TABLE_TEACHERS + "(" + COLUMN_TEACHER_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_TEACHER_MODULES_MODULE_ID + ") REFERENCES " + TABLE_MODULES + "(" + COLUMN_MODULE_ID + "), " +
                    "PRIMARY KEY(" + COLUMN_TEACHER_MODULES_TEACHER_ID + ", " + COLUMN_TEACHER_MODULES_MODULE_ID + ")" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("db_test_query", "is it creating ?");
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_TEACHERS);
        db.execSQL(CREATE_TABLE_STUDENTS);
        db.execSQL(CREATE_TABLE_MODULES);
        db.execSQL(CREATE_TABLE_GRADES);
        db.execSQL(CREATE_TABLE_TEACHER_MODULES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This is a basic implementation that drops all tables and recreates them
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODULES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHER_MODULES);
        onCreate(db);
    }
}