package com.iyed_houhou.myappmobiletp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnGradeChangeListener {
    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Module> moduleList;
    private TextView overallGradeTextView; // Add a TextView in your layout for overall grade

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the Toolbar and set it as ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Apply system spacing (status bar height) to the Toolbar
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        overallGradeTextView = findViewById(R.id.tvOverallGrade); // Ensure it's in your layout

        // Initialize adapter
        moduleList = new ArrayList<>();
        adapter = new ItemAdapter(moduleList, this); // Pass the listener
        recyclerView.setAdapter(adapter);

        onGradeChanged();

        // Fetch data
        new FetchData().execute("https://num.univ-biskra.dz/psp/formations/get_modules_json?sem=1&spec=184");
    }



    @Override
    public void onGradeChanged() {
        updateOverallGrade();
        changeColorOfGrade();
    }

    private void changeColorOfGrade() {
        int color = ContextCompat.getColor(this,
        Module.getOverAllGrade() >= 10.0 ? R.color.gradeColorGreen : R.color.gradeColorRed);
        overallGradeTextView.setTextColor(color);
    }

    private void updateOverallGrade() {
        double totalWeightedGrade = 0;
        int totalCoefficients = 0;

        for (Module module : moduleList) {
            totalWeightedGrade += module.getGrade() * module.getCoefficient();
            totalCoefficients += module.getCoefficient();
        }

        double overallGrade = totalCoefficients > 0 ? totalWeightedGrade / totalCoefficients : 0;
        Module.setOverAllGrade(overallGrade);
        overallGradeTextView.setText(String.format("Overall Grade: %.2f", overallGrade));
    }



    private class FetchData extends AsyncTask<String, Void, List<Module>> {
        @Override
        protected List<Module> doInBackground(String... urls) {
            List<Module> modules = new ArrayList<>();
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
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    modules.add(new Module(
                            obj.getString("Nom_module"),
                            obj.optInt("td", 0),  // Handle missing keys safely
                            obj.optInt("tp", 0),
                            obj.optInt("Coefficient", 1),
                            obj.optInt("Credit", 0)
                    ));
                }
            } catch (Exception e) {
                Log.e("FetchData", "Error fetching data", e);
            }
            return modules;
        }

        @Override
        protected void onPostExecute(List<Module> modules) {
            if (modules != null && !modules.isEmpty()) {
                moduleList.clear();
                moduleList.addAll(modules);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
