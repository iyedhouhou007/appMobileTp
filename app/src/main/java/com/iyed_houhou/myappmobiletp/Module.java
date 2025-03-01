package com.iyed_houhou.myappmobiletp;

public class Module {
    private final String moduleName;

    private final int td;
    private final int tp;
    private final int coefficient;
    private final int credit;

    private double noteTd;
    private double noteTp;
    private double noteExam;

    private static double overAllGrade;
    private double grade;

    public Module(String moduleName, int td, int tp, int coefficient, int credit) {
        this.moduleName = moduleName;
        this.td = td;
        this.tp = tp;
        this.coefficient = coefficient;
        this.credit = credit;
        noteTd = 0 ;
        noteTp = 0;
        noteExam = 0;
        calculateGrade();
        overAllGrade = 0;
    }

    public static double getOverAllGrade() {
        return overAllGrade;
    }

    public static void setOverAllGrade(double overAllGrade) {
        Module.overAllGrade = overAllGrade;
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
}
