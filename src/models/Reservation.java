package models;

import java.util.ArrayList;
import java.util.Collections;

public class Reservation {
    private Course course;
    private Classroom classroom;
    private TimeSlot timeSlot;
    private int amount;
    private boolean inSite;

    public Reservation(Course course, Classroom classroom, TimeSlot timeSlot, int amount) {
        this.course = course;
        this.classroom = classroom;
        this.timeSlot = timeSlot;
        this.amount = amount;
    }

    public Course getCourse() {
        return course;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isInSite() {
        return inSite;
    }

    public void setInSite(boolean inSite) {
        this.inSite = inSite;
    }

    public boolean isInHomeSite(Program program) {
        return classroom.getSite().getCode().equals(program.getHomesite());
    }

    public JsonReservation toJsonReservation(){
        return new JsonReservation(
                classroom.getFinummer(),
                course.getCode(),
                amount,
                new ArrayList<>(Collections.singletonList(getTimeSlot().getDayName())),
                new ArrayList<>(Collections.singletonList(getTimeSlot().getWeek())),
                new ArrayList<>(Collections.singletonList(getTimeSlot().getHour()))
                );
    }

    @Override
    public String toString() {
        return "(" + course.getPrograms() + ","  + classroom.getSite() + ")";
    }
}
