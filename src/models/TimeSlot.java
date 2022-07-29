package models;

import java.util.ArrayList;
import java.util.Objects;

public class TimeSlot {
    private static final String[] days = new String[]{"ma", "di", "wo", "do", "vr"};
    private static final int weekAmount = 13;
    private static final int dayAmount = 5;
    private static final int hourAmount = 8;

    private int week;
    private int day;
    private int hour;

    public TimeSlot(int week, int day, int hour) {
        this.week = week;
        this.day = day;
        this.hour = hour;
    }

    public TimeSlot getNext() {
        if (week == -1) {
            return this;
        }
        int n_hour = hour;
        int n_day = day;
        int n_week = week;

        n_hour++;
        //prevent 4 lessons in a row
        if (n_hour == 3) {
            n_hour++;
        }
        if (n_hour == 6) {
            n_hour = 0;
            n_day++;
            if (n_day == 5) {
                n_day = 0;
                n_week++;
                if (n_week == 12) {
                    //if week is -1 it means there are no more available next dates
                    n_week = -1;
                }
            }
        }
        if (n_week == 0 && n_day==0 && n_hour==0) {
            System.out.println("help");
        }
        return new TimeSlot(n_week, n_day, n_hour);
    }

    public TimeSlot getNextAll() {
        if (week == -1) {
            return this;
        }
        int n_hour = hour;
        int n_day = day;
        int n_week = week;

        n_hour++;
        //prevent 4 lessons in a row
        if (n_hour == 8) {
            n_hour = 0;
            n_day++;
            if (n_day == 5) {
                n_day = 0;
                n_week++;
                if (n_week == 13) {
                    //if week is -1 it means there are no more available next dates
                    n_week = -1;
                }
            }
        }
        return new TimeSlot(n_week, n_day, n_hour);
    }

    public int getWeek() {
        return week;
    }

    public int getDay() {
        return day;
    }

    public String getDayName() {
        return days[day];
    }

    public int getHour() {
        return hour;
    }

    public static ArrayList<TimeSlot> regularTimeslots(){
        //TODO
        int[] hours = new int[]{1,2,3,5,6};
        ArrayList<TimeSlot> array = new ArrayList<>();
        for (int week = 1; week < 13; week++) {
            for (int day = 0; day < 5; day++) {
                for (int hour : hours) {
                    array.add(new TimeSlot(week,day,hour));
                }
            }
        }
        return array;
    }

    public static ArrayList<TimeSlot> regularTimeslots2(){
        int[] hours = new int[]{0,1,2,4,5};
        ArrayList<TimeSlot> array = new ArrayList<>();
        for (int week = 0; week < 12; week++) {
            for (int day = 0; day < 5; day++) {
                for (int hour : hours) {
                    array.add(new TimeSlot(week,day,hour));
                }
            }
        }
        return array;
    }

    public static ArrayList<TimeSlot> allTimeslots(){
        int[] hours = new int[]{0,1,2,3,4,5,6,7};
        ArrayList<TimeSlot> array = new ArrayList<>();
        for (int week = 0; week < 13; week++) {
            for (int day = 0; day < 5; day++) {
                for (int hour : hours) {
                    array.add(new TimeSlot(week,day,hour));
                }
            }
        }
        return array;
    }

    public static ArrayList<TimeSlot> allTimeslots2(){
        int[] hours = new int[]{0,1,2,4,5};
        ArrayList<TimeSlot> array = new ArrayList<>();
        for (int week = 0; week < 13; week++) {
            for (int day = 0; day < 5; day++) {
                for (int hour : hours) {
                    array.add(new TimeSlot(week,day,hour));
                }
            }
        }

        hours = new int[]{3,6,7};
        for (int week = 0; week < 13; week++) {
            for (int day = 0; day < 5; day++) {
                for (int hour : hours) {
                    array.add(new TimeSlot(week,day,hour));
                }
            }
        }
        return array;
    }

    public static ArrayList<TimeSlot> weekTimeSlots() {
        int[] hours = new int[]{0,1,2,4,5};
        ArrayList<TimeSlot> array = new ArrayList<>();
        int week = 0;
        for (int day = 0; day < 5; day++) {
            for (int hour : hours) {
                array.add(new TimeSlot(week,day,hour));
            }
        }


        hours = new int[]{3,6,7};

        for (int day = 0; day < 5; day++) {
            for (int hour : hours) {
                array.add(new TimeSlot(week,day,hour));
            }
        }

        return array;
    }

    public static TimeSlot random() {
        int week = (int) (Math.random()*weekAmount);
        int day = (int) (Math.random()*dayAmount);
        int hour = (int) (Math.random()*hourAmount);

        return new TimeSlot(week, day, hour);
    }

    public boolean isGood(){
        return week < 12 && hour != 3 && hour <= 5;
    }

    @Override
    public String toString() {
        return "<" + week + "/" + day + "/" + hour + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return week == timeSlot.week &&
                day == timeSlot.day &&
                hour == timeSlot.hour;
    }

    @Override
    public int hashCode() {
        return 10000 + 100*week+10*day+hour;
    }
}
