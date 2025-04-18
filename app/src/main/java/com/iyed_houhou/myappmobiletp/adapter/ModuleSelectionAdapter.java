package com.iyed_houhou.myappmobiletp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iyed_houhou.myappmobiletp.R;
import com.iyed_houhou.myappmobiletp.data.Module;

import java.util.ArrayList;
import java.util.List;

public class ModuleSelectionAdapter extends RecyclerView.Adapter<ModuleSelectionAdapter.ModuleViewHolder> {

    private final List<Module> modules;
    private final List<Integer> selectedModuleIds = new ArrayList<>();

    public ModuleSelectionAdapter(List<Module> modules) {
        this.modules = modules;
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_module_selection, parent, false);
        return new ModuleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        Module currentModule = modules.get(position);
        holder.moduleNameTextView.setText(currentModule.getModuleName());
        
        // Use moduleId instead of credit for uniquely identifying modules
        final int moduleId = currentModule.getModuleId();

        holder.moduleCheckBox.setOnCheckedChangeListener(null); // To avoid issues with recycling
        holder.moduleCheckBox.setChecked(selectedModuleIds.contains(moduleId));

        holder.moduleCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedModuleIds.contains(moduleId)) {
                    selectedModuleIds.add(moduleId);
                }
            } else {
                selectedModuleIds.remove(Integer.valueOf(moduleId)); // Remove by value
            }
        });
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    public List<Integer> getSelectedModuleIds() {
        return selectedModuleIds;
    }

    public static class ModuleViewHolder extends RecyclerView.ViewHolder {
        public TextView moduleNameTextView;
        public CheckBox moduleCheckBox;

        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleNameTextView = itemView.findViewById(R.id.moduleNameTextView);
            moduleCheckBox = itemView.findViewById(R.id.moduleCheckBox);
        }
    }
}