package models;

import java.util.ArrayList;
import java.util.Objects;

public class Course {
    private static final int minStudentsAmount = 5;

    private String cursusnaam;
    private String code;
    private ArrayList<Program> programmas;
    private ArrayList<Teacher> lesgevers;
    private double contacturen;
    private String studenten;
    private int amountOfStudents = -1;
    private int group = 0;

    public Course(String cursusnaam, String code, ArrayList<Program> programmas, ArrayList<Teacher> lesgevers, double contacturen, int amountOfStudents, int group) {
        this.cursusnaam = cursusnaam;
        this.code = code;
        this.programmas = programmas;
        this.lesgevers = lesgevers;
        this.contacturen = contacturen;
        this.amountOfStudents = amountOfStudents;
        this.group = group;
    }

    public Course() {
    }

    public boolean hasTeacherStudentsConflict(Course course) {
        for (Teacher teacher1 : lesgevers) {
            for (Teacher teacher2 : course.getTeachers()) {
                if (teacher1.equals(teacher2)) {
                    return true;
                }
            }
        }
        for (Program program1 : programmas) {
            for (Program program2 : course.getPrograms()) {
                if (program1.equals(program2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getCursusnaam() {
        return cursusnaam;
    }

    public String getCode() {
        return code;
    }

    public ArrayList<Program> getPrograms() {
        return programmas;
    }

    public ArrayList<Teacher> getTeachers() {
        return lesgevers;
    }

    // devide by 1.5 because contacthour != lesson block
    public double getContacturen() {
        return contacturen/(1.25 * 12);
    }

    public double getContacturenReal() {
        return contacturen/(1.25);
    }

    public int getAmountOfStudents() {
        if (amountOfStudents == -1) {
            amountOfStudents = Math.max(Integer.parseInt(studenten), minStudentsAmount);
        }
        return amountOfStudents;
    }

    public int getGroup() {
        return group;
    }

    public void setContacturen(double contacturen) {
        this.contacturen = contacturen;
    }

    public void setAmountOfStudents(int amountOfStudents) {
        this.amountOfStudents = amountOfStudents;
    }

    @Override
    public String toString() {
        return cursusnaam;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return  group == course.group &&
                cursusnaam.equals(course.cursusnaam) &&
                code.equals(course.code) &&
                programmas.equals(course.programmas) &&
                lesgevers.equals(course.lesgevers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cursusnaam, code, programmas, lesgevers, contacturen, group);
    }
}
