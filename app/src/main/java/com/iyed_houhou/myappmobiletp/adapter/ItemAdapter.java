package com.iyed_houhou.myappmobiletp.adapter;

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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.iyed_houhou.myappmobiletp.data.Module;
import com.iyed_houhou.myappmobiletp.R;
import com.iyed_houhou.myappmobiletp.viewmodel.ModuleViewModel;

import java.util.Locale;

public class ItemAdapter extends ListAdapter<Module, ItemAdapter.ViewHolder> {
    private final ModuleViewModel viewModel;

    public ItemAdapter(ModuleViewModel viewModel) {
        super(DIFF_CALLBACK);
        this.viewModel = viewModel;
    }

    private static final DiffUtil.ItemCallback<Module> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Module>() {
                @Override
                public boolean areItemsTheSame(@NonNull Module oldItem, @NonNull Module newItem) {
                    return oldItem.getModuleName().equals(newItem.getModuleName());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Module oldItem, @NonNull Module newItem) {
                    return oldItem.equals(newItem); // Ensure Module class implements equals()
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Module module = getItem(position);

        // Set initial values
        holder.moduleName.setText(module.getModuleName());
        holder.cof.setText(String.valueOf(module.getCoefficient()));
        holder.credit.setText(String.valueOf(module.getCredit()));
        
        // Set values in EditText fields and disable only those with values
        if (module.getNoteTD() > 0) {
            holder.td.setText(String.valueOf(module.getNoteTD()));
            holder.td.setEnabled(false);
            holder.td.setBackgroundResource(R.drawable.edit_text_disabled_background);
        } else {
            holder.td.setText("");
            holder.td.setEnabled(true);
        }
        
        if (module.getNoteTP() > 0) {
            holder.tp.setText(String.valueOf(module.getNoteTP()));
            holder.tp.setEnabled(false);
            holder.tp.setBackgroundResource(R.drawable.edit_text_disabled_background);
        } else {
            holder.tp.setText("");
            holder.tp.setEnabled(true);
        }
        
        if (module.getNoteExam() > 0) {
            holder.exam.setText(String.valueOf(module.getNoteExam()));
            holder.exam.setEnabled(false);
            holder.exam.setBackgroundResource(R.drawable.edit_text_disabled_background);
        } else {
            holder.exam.setText("");
            holder.exam.setEnabled(true);
        }

        // Set grade at the end, after all component values are set
        if (module.getGrade() > 0) {
            holder.grade.setText(String.format(Locale.US, "%.2f", module.getGrade()));
        } else {
            holder.grade.setText("");
        }
        
        // Set visibility based on module capabilities
        holder.td.setVisibility(module.getTd() == 1 ? View.VISIBLE : View.GONE);
        holder.tp.setVisibility(module.getTp() == 1 ? View.VISIBLE : View.GONE);

        changeColorOfGrade(holder, module);

        // Remove previous TextWatchers to avoid multiple attachments
        if (holder.td.getTag() instanceof TextWatcher) {
            holder.td.removeTextChangedListener((TextWatcher) holder.td.getTag());
        }
        if (holder.tp.getTag() instanceof TextWatcher) {
            holder.tp.removeTextChangedListener((TextWatcher) holder.tp.getTag());
        }
        if (holder.exam.getTag() instanceof TextWatcher) {
            holder.exam.removeTextChangedListener((TextWatcher) holder.exam.getTag());
        }

        // Attach TextWatcher to all input fields
        TextWatcher tdWatcher = createTextWatcher(module, holder, "TD", position);
        holder.td.addTextChangedListener(tdWatcher);
        holder.td.setTag(tdWatcher); // Store watcher for removal

        TextWatcher tpWatcher = createTextWatcher(module, holder, "TP", position);
        holder.tp.addTextChangedListener(tpWatcher);
        holder.tp.setTag(tpWatcher); // Store watcher for removal

        TextWatcher examWatcher = createTextWatcher(module, holder, "Exam", position);
        holder.exam.addTextChangedListener(examWatcher);
        holder.exam.setTag(examWatcher); // Store watcher for removal

        // Inside your onBindViewHolder (if using RecyclerView) or onCreate (for Activity/Fragment)
        holder.td.setFilters(new InputFilter[]{new InputFilterMax(20, 2)});
        holder.tp.setFilters(new InputFilter[]{new InputFilterMax(20, 2)});
        holder.exam.setFilters(new InputFilter[]{new InputFilterMax(20, 2)});
    }

    private TextWatcher createTextWatcher(Module module, ViewHolder holder, String fieldType, int position) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                double value = s.toString().isEmpty() ? 0.0 : Double.parseDouble(s.toString());
                // Restore database update
                viewModel.updateModuleGrade(position, fieldType, value);
                
                // Remove direct module updates
                // if (fieldType.equals("TD")) {
                //     module.setNoteTD(value);
                // } else if (fieldType.equals("TP")) {
                //     module.setNoteTP(value);
                // } else if (fieldType.equals("Exam")) {
                //     module.setNoteExam(value);
                // }
                
                // Update the displayed grade
                holder.grade.setText(String.format(Locale.US, "%.2f", module.getGrade()));
                changeColorOfGrade(holder, module);
            }
        };
    }

    private void changeColorOfGrade(TextView textView, double grade) {
        int color = ContextCompat.getColor(textView.getContext(),
                grade >= 10.0 ? R.color.gradeColorGreen : R.color.gradeColorRed);
        textView.setTextColor(color);
    }

    private void changeColorOfGrade(ViewHolder holder, Module module) {
        int color = ContextCompat.getColor(holder.itemView.getContext(),
                module.getGrade() >= 10.0 ? R.color.gradeColorGreen : R.color.gradeColorRed);
        holder.grade.setTextColor(color);
        holder.tvGrade.setTextColor(color);
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