package com.iyed_houhou.myappmobiletp.data;

import java.util.Objects;

public class Module {
    private int moduleId;
    private final String moduleName;
    private final int td;
    private final int tp;
    private final int coefficient;
    private final int credit;
    private double noteTd;
    private double noteTp;
    private double noteExam;
    private double grade;

    public Module(String moduleName, int td, int tp, int coefficient, int credit) {
        this.moduleName = moduleName;
        this.td = td;
        this.tp = tp;
        this.coefficient = coefficient;
        this.credit = credit;
        this.noteTd = 0;
        this.noteTp = 0;
        this.noteExam = 0;
        calculateGrade();
    }

    public Module(int moduleId, String moduleName, int td, int tp, int coefficient, int credit) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.td = td;
        this.tp = tp;
        this.coefficient = coefficient;
        this.credit = credit;
        this.noteTd = 0;
        this.noteTp = 0;
        this.noteExam = 0;
        calculateGrade();
    }

    private void calculateGrade() {
        double moyenTdTp = 0.0;
        int count = 0;

        if (td == 1) {
            moyenTdTp += noteTd;
            count++;
        }
        if (tp == 1) {
            moyenTdTp += noteTp;
            count++;
        }

        grade = (count > 0) ? (moyenTdTp * 0.4 / count) + (noteExam * 0.6) : noteExam;
    }

    public int getTp() {
        return tp;
    }

    public int getTd() {
        return td;
    }

    public String getModuleName() {
        return moduleName;
    }

    public int getCoefficient() {
        return coefficient;
    }

    public double getNoteTD() {
        return noteTd;
    }

    public void setNoteTD(double noteTd) {
        this.noteTd = noteTd;
        calculateGrade();
    }

    public double getNoteTP() {
        return noteTp;
    }

    public void setNoteTP(double noteTp) {
        this.noteTp = noteTp;
        calculateGrade();
    }

    public double getNoteExam() {
        return noteExam;
    }

    public int getCredit() {
        return credit;
    }

    public double getGrade() {
        return grade;
    }

    public void setNoteExam(double noteExam) {
        this.noteExam = noteExam;
        calculateGrade();
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public boolean isTD() {
        return td == 1;
    }

    public boolean isTP() {
        return tp == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module = (Module) o;
        return td == module.td && tp == module.tp && coefficient == module.coefficient && credit == module.credit && Double.compare(module.noteTd, noteTd) == 0 && Double.compare(module.noteTp, noteTp) == 0 && Double.compare(module.noteExam, noteExam) == 0 && Double.compare(module.grade, grade) == 0 && moduleName.equals(module.moduleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleName, td, tp, coefficient, credit, noteTd, noteTp, noteExam, grade);
    }

}