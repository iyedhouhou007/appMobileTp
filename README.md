# MyAppMobileTp

MyAppMobileTp is an Android application for managing student grades, modules, and user roles (Student/Teacher). It features user registration, login, profile management, grade entry (by teachers), and grade viewing (by students). The app uses an SQLite database and follows the MVVM architecture with LiveData and ViewModel.

## Features

- **User Registration & Login:** Register as a Student or Teacher, login with credentials.
- **Role-based Menu:** Students and Teachers see different menu options.
- **Student Profile:** View personal info and navigate to grades.
- **Teacher Registration:** Assign modules to teachers during registration.
- **Grade Management:** Teachers can add/update grades for students in their modules.
- **Grade Viewing:** Students can view their grades and overall average.
- **Module Management:** Modules are fetched from a remote API and stored locally.
- **Database Viewer:** For debugging, view all database tables.

## Project Structure

```
app/
  src/
    main/
      java/com/iyed_houhou/myappmobiletp/
        ui/           # Activities (Login, Register, Menu, Grades, AddGrades, StudentProfile, TeacherRegistration)
        data/         # Data models (e.g., Module.java)
        adapter/      # RecyclerView adapters
        viewmodel/    # ViewModels (e.g., ModuleViewModel.java)
        DatabaseHelper.java
      res/
        layout/       # XML layouts for activities/fragments
        values/       # Strings, colors, styles, dimens
        drawable/     # Images and icons
        menu/         # Menu XMLs
        xml/          # Backup rules, etc.
      AndroidManifest.xml
  build.gradle.kts
```

## Main Components

- **Activities:**

  - `LoginActivity`: User authentication.
  - `RegisterActivity`: User registration.
  - `MenuActivity`: Main menu, role-based navigation.
  - `StudentProfileActivity`: Student info.
  - `GradesActivity`: View grades.
  - `AddGradesActivity`: Teachers add/update grades.
  - `TeacherRegistrationActivity`: Assign modules to teachers.
  - `StudentRegistrationActivity`: Complete student profile after registration.
  - `DatabaseViewActivity`: View database contents (debugging).

- **Database:**

  - `DatabaseHelper`: Manages SQLite tables for users, students, teachers, modules, grades, and teacher-module assignments.

- **ViewModel:**

  - `ModuleViewModel`: Handles module and grade data, fetching from API, and grade calculations.

- **Data Model:**
  - `Module`: Represents a module with TD/TP/exam grades and coefficient.

## How to Build & Run

1. **Clone the repository.**
2. Open the project in Android Studio.
3. Sync Gradle and build the project.
4. Run on an emulator or Android device.

## Usage

1. **Register** as a Student or Teacher.
2. **Login** with your credentials.
3. **Students:** Complete profile, view grades.
4. **Teachers:** Assign modules, add grades for students.
5. **Logout** from the menu.

## Notes

- Modules are fetched from a remote API on first launch and stored locally.
- Teachers can only add grades for modules assigned to them.
- Students can only view their own grades.
- The app uses SharedPreferences for session management.

---

**License:** For educational/demo use.
