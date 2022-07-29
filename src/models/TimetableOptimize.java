package models;

import json.JsonModel;

import java.util.*;

public class TimetableOptimize implements Timetable {
    private HashMap<TimeSlot, ArrayList<ReservationOptimize>> reservations = new HashMap<>();

    HashMap<TimeSlot, HashMap<String, ArrayList<Classroom>>> classroomsTimeslot = new HashMap<>();
    HashMap<Teacher, HashMap<Tuple<Integer, Integer>, ArrayList<TimeSlot>>> timeslotsTeacher = new HashMap<>();
    HashMap<Program, HashMap<Tuple<Integer, Integer>, ArrayList<TimeSlot>>> timeslotsProgram = new HashMap<>();

    private static final HashMap<String, Integer> dayMap = new HashMap<>();
    static {
        dayMap.put("ma", 0);
        dayMap.put("di", 1);
        dayMap.put("wo", 2);
        dayMap.put("do", 3);
        dayMap.put("vr", 4);
    }

    ArrayList<Site> sites;
    HashSet<Program> programs;
    HashMap<String, ArrayList<Classroom>> fullMap = new HashMap<>();
    HashMap<Tuple<Integer, Integer>, ArrayList<TimeSlot>> fullTimeMap = new HashMap<>();

    public TimetableOptimize(JsonModel jsonModel, JsonReservation[] resultModel) {
        this.programs = jsonModel.getPrograms();
        this.sites = jsonModel.getSites();
        //make fullMap
        for (Site site : sites) {
            ArrayList<Classroom> classrooms1 = site.getClassrooms();
            classrooms1.sort(Comparator.comparingInt(Classroom::getCapaciteit).reversed());
            fullMap.put(site.getCode(), classrooms1);
        }

        //make fullTimeMap
        for (int day=0; day<5; day++) {
            for (int hour=0; hour<8; hour++) {
                ArrayList<TimeSlot> timeSlots = new ArrayList<>();
                for (int week=0; week<12; week++) {
                    timeSlots.add(new TimeSlot(week, day, hour));
                }
                fullTimeMap.put(new Tuple<Integer, Integer>(day, hour), timeSlots);
            }
        }

        //init all hashmaps

        for (JsonReservation reservation : resultModel) {
            TimeSlot timeSlot = new TimeSlot(reservation.getWeken().get(0)-1, dayMap.get(reservation.getDagen().get(0)), reservation.getUren().get(0)-1);
            Course course = jsonModel.getCourseByCode(reservation.getCode());
            Classroom classroom = jsonModel.getClassroomByCode(reservation.getLokaal());
            Tuple<Integer, Integer> t = new Tuple<>(timeSlot.getDay(), timeSlot.getHour());
            boolean isInSite = false;
            //TODO dit kan problemen geven
            if (course == null) {
                System.out.println("error");
                continue;
            }
            course.setAmountOfStudents(reservation.getAantal());

            classroomsTimeslot.computeIfAbsent(timeSlot, k -> fullMapClone());
            classroomsTimeslot.get(timeSlot).get(classroom.getSiteCode()).remove(classroom);
            for (Teacher teacher : course.getTeachers()) {
                timeslotsTeacher.computeIfAbsent(teacher, k -> fullTimeMapClone());
                timeslotsTeacher.get(teacher).get(t).remove(timeSlot);
            }
            for (Program program : course.getPrograms()) {
                if (program.getHomesite().equals(classroom.getSiteCode())) isInSite = true;
                timeslotsProgram.computeIfAbsent(program, k -> fullTimeMapClone());
                timeslotsProgram.get(program).get(t).remove(timeSlot);
            }
            reservations.computeIfAbsent(timeSlot, k-> new ArrayList<>());
            reservations.get(timeSlot).add(new ReservationOptimize(course, timeSlot, classroom, isInSite, reservation.getAantal()));
        }
    }

    public Site getSiteFromString(String string) {
        for (Site site : sites) {
            if (site.getCode().equals(string)) return site;
        }
        return null;
    }

    public HashMap<TimeSlot, ArrayList<ReservationOptimize>> getReservations() {
        return reservations;
    }

    public HashMap<TimeSlot, HashMap<String, ArrayList<Classroom>>> getClassroomsTimeslot() {
        return classroomsTimeslot;
    }

    public HashMap<Tuple<Integer, Integer>, ArrayList<TimeSlot>> fullTimeMapClone() {
        HashMap<Tuple<Integer, Integer>, ArrayList<TimeSlot>> result = new HashMap<>();
        for (Tuple<Integer, Integer> t : fullTimeMap.keySet()) {
            result.put(t, (ArrayList<TimeSlot>) fullTimeMap.get(t).clone());
        }
        return result;
    }

    public HashMap<String, ArrayList<Classroom>> fullMapClone() {
        HashMap<String, ArrayList<Classroom>> result = new HashMap<>();
        for (String s : fullMap.keySet()) {
            result.put(s, (ArrayList<Classroom>) fullMap.get(s).clone());
        }
        return result;
    }

    public HashMap<Teacher, HashMap<Tuple<Integer, Integer>, ArrayList<TimeSlot>>> getTimeslotsTeacher() {
        return timeslotsTeacher;
    }

    public HashMap<Program, HashMap<Tuple<Integer, Integer>, ArrayList<TimeSlot>>> getTimeslotsProgram() {
        return timeslotsProgram;
    }

    public void changeReservation(ReservationOptimize reservation, ReservationOptimize old) {
        TimeSlot newT = reservation.getTimeSlot();
        TimeSlot oldT = old.getTimeSlot();
        reservations.get(newT).add(reservation);
        reservations.get(oldT).remove(old);
    }

    public HashSet<Program> getPrograms() {
        return programs;
    }

    @Override
    public ArrayList<JsonReservation> toJsonReservations() {
        ArrayList<JsonReservation> jsonReservations = new ArrayList<>();

        for (ArrayList<ReservationOptimize> timeReservations : reservations.values()) {
            for (ReservationOptimize reservation : timeReservations){
                jsonReservations.add(new JsonReservation(
                        reservation.getClassroom().getFinummer(),
                        reservation.getCourse().getCode(),
                        reservation.getAmount(),
                        new ArrayList<>(Collections.singletonList(reservation.getTimeSlot().getDayName())),
                        new ArrayList<>(Collections.singletonList(reservation.getTimeSlot().getWeek()+1)),
                        new ArrayList<>(Collections.singletonList(reservation.getTimeSlot().getHour()+1))
                ));
            }
        }
        return jsonReservations;
    }
}
