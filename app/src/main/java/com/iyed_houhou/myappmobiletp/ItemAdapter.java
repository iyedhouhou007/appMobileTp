package com.iyed_houhou.myappmobiletp;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private final List<Module> moduleList;
    private final OnGradeChangeListener gradeChangeListener;

    public ItemAdapter(List<Module> moduleList, OnGradeChangeListener listener) {
        this.moduleList = moduleList;
        this.gradeChangeListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Module module = moduleList.get(position);

        // Set initial values
        holder.moduleName.setText(module.getModuleName());
        holder.cof.setText(String.valueOf(module.getCoefficient()));
        holder.credit.setText(String.valueOf(module.getCredit()));
        holder.grade.setText(String.format(Locale.US, "%.2f", module.getGrade()));


        // Set previous values in EditTexts
        holder.td.setText(module.getNoteTD() == 0 ? "" : String.valueOf(module.getNoteTD()));
        holder.tp.setText(module.getNoteTP() == 0 ? "" : String.valueOf(module.getNoteTP()));
        holder.exam.setText(module.getNoteExam() == 0 ? "" : String.valueOf(module.getNoteExam()));

        holder.td.setVisibility(module.getTd() == 1? View.VISIBLE : View.GONE);
        holder.tp.setVisibility(module.getTp() == 1? View.VISIBLE : View.GONE);

        changeColorOfGrade(holder, module);

        // Attach TextWatcher to all input fields
        holder.td.addTextChangedListener(createTextWatcher(module, holder, "TD"));
        holder.tp.addTextChangedListener(createTextWatcher(module, holder, "TP"));
        holder.exam.addTextChangedListener(createTextWatcher(module, holder, "Exam"));

        // Inside your onBindViewHolder (if using RecyclerView) or onCreate (for Activity/Fragment)
        holder.td.setFilters(new InputFilter[]{new InputFilterMax(20, 2)});
        holder.tp.setFilters(new InputFilter[]{new InputFilterMax(20, 2)});
        holder.exam.setFilters(new InputFilter[]{new InputFilterMax(20, 2)});
    }

    private TextWatcher createTextWatcher(Module module, ViewHolder holder, String fieldType) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                double value = s.toString().isEmpty() ? 0.0 : Double.parseDouble(s.toString());

                // Update corresponding value
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

                // Update grade and UI
                holder.grade.setText(String.format(Locale.US, "%.2f", module.getGrade()));

                changeColorOfGrade(holder, module);

                // Notify listener
                if (gradeChangeListener != null) {
                    gradeChangeListener.onGradeChanged();
                }
            }
        };
    }

    private void changeColorOfGrade(ViewHolder holder, Module module) {
        int color = ContextCompat.getColor(holder.itemView.getContext(),
                module.getGrade() >= 10.0 ? R.color.gradeColorGreen : R.color.gradeColorRed);
        holder.grade.setTextColor(color);
        holder.tvGrade.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return moduleList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView moduleName, cof, credit, grade, tvGrade;
        EditText td, tp, exam;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleName = itemView.findViewById(R.id.moduleName);
            td = itemView.findViewById(R.id.td);
            tp = itemView.findViewById(R.id.tp);
            exam = itemView.findViewById(R.id.exam);
            cof = itemView.findViewById(R.id.cof);
            credit = itemView.findViewById(R.id.credit);
            grade = itemView.findViewById(R.id.grade);
            tvGrade = itemView.findViewById(R.id.tvGrade);
        }
    }

    public static class InputFilterMax implements InputFilter {
        private final double maxValue;
        private final int decimalPlaces;

        public InputFilterMax(double maxValue, int decimalPlaces) {
            this.maxValue = maxValue;
            this.decimalPlaces = decimalPlaces;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                // Construct the new text after input change
                String newText = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

                // Check if input is a valid number
                double input = Double.parseDouble(newText);
                if (input > maxValue) {
                    return ""; // Reject input if greater than max
                }

                // Check decimal places
                if (newText.contains(".")) {
                    String[] split = newText.split("\\.");
                    if (split.length > 1 && split[1].length() > decimalPlaces) {
                        return ""; // Reject input if decimal places exceed limit
                    }
                }

            } catch (NumberFormatException e) {
                // Ignore invalid input (like just ".")
            }
            return null; // Accept input
        }
    }
}

