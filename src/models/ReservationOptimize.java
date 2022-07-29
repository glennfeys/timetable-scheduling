package models;

import java.util.Objects;

public class ReservationOptimize {
    Course course;
    TimeSlot timeSlot;
    Classroom classroom;
    boolean inSite;
    int amount;

    public ReservationOptimize(Course course, TimeSlot timeSlot, Classroom classroom, boolean inSite, int amount) {
        this.course = course;
        this.timeSlot = timeSlot;
        this.classroom = classroom;
        this.inSite = inSite;
        this.amount = amount;
    }

    public Course getCourse() {
        return course;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public void setInSite(boolean inSite) {
        this.inSite = inSite;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isInSite() {
        return inSite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationOptimize that = (ReservationOptimize) o;
        return inSite == that.inSite &&
                amount == that.amount &&
                Objects.equals(course, that.course) &&
                Objects.equals(timeSlot, that.timeSlot) &&
                Objects.equals(classroom, that.classroom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(course, timeSlot, classroom, inSite, amount);
    }
}
