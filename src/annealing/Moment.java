package annealing;

import java.util.Objects;
/*
A combination of day and hour, eliminating weeks
 */
public class Moment {
    private int day;
    private int hour;

    public Moment(int day, int hour) {
        this.day = day;
        this.hour = hour;
    }

    public int day() {
        return day;
    }

    public int hour() {
        return hour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Moment moment = (Moment) o;
        return day == moment.day &&
                hour == moment.hour;
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, hour);
    }

    @Override
    public String toString() {
        return "(" + day + "," + hour + ")";
    }
}
