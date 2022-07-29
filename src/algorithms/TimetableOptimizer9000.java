package algorithms;

import adapters.JsonAdapter;
import json.JsonModel;
import json.JsonReader;
import json.JsonWriter;
import models.*;

import java.util.*;

public class TimetableOptimizer9000 {

    private TimetableOptimize timetable;
    private JsonModel jsonModel;

    HashMap<Program, HashSet<Tuple<Integer, Integer>>> emptyDays = new HashMap<>();

    int changes = 0;

    public TimetableOptimizer9000(TimetableOptimize timetable, JsonModel jsonModel) {
        this.timetable = timetable;
        this.jsonModel = jsonModel;
    }

    public void execute() {
        int weken = 12;

        //enkele oplossen
        HashMap<TimeSlot, ArrayList<ReservationOptimize>> reservations = timetable.getReservations();

        HashMap<Course, ArrayList<ReservationOptimize>> singles = getSingles(reservations, weken);
        int sum = 0;
        for (ArrayList<ReservationOptimize> val : singles.values()) sum += val.size();
        System.out.println("aantal singles:" + sum);
        fillReservations(singles);

        HashMap<Course, ArrayList<ReservationOptimize>> lates = getLates(reservations, weken);
        sum = 0;
        for (ArrayList<ReservationOptimize> val : lates.values()) sum += val.size();
        System.out.println("aantal lates:" + sum);
        fillReservations(lates);


    }

    public void fillReservations(HashMap<Course, ArrayList<ReservationOptimize>> reservationsO) {
        HashMap<Teacher, HashMap<Tuple<Integer, Integer>, ArrayList<TimeSlot>>> timeslotsTeacher = timetable.getTimeslotsTeacher();
        HashMap<Program, HashMap<Tuple<Integer, Integer>, ArrayList<TimeSlot>>> timeslotsProgram = timetable.getTimeslotsProgram();

        for (Course course : reservationsO.keySet()) {
            ArrayList<ReservationOptimize> arr = reservationsO.get(course);

            ArrayList<ArrayList<TimeSlot>> timeslots = new ArrayList<>();

            for (Teacher teacher : course.getTeachers()) {
                ArrayList<TimeSlot> res = new ArrayList<>();
                for (Tuple<Integer, Integer> t : timeslotsTeacher.get(teacher).keySet()) {
                    res.addAll(timeslotsTeacher.get(teacher).get(t));
                }
                timeslots.add(res);
            }

            for (Program program : course.getPrograms()) {
                ArrayList<TimeSlot> res = new ArrayList<>();
                for (Tuple<Integer, Integer> t : timeslotsProgram.get(program).keySet()) {
                    res.addAll(timeslotsProgram.get(program).get(t));
                }
                timeslots.add(res);
            }

            int amountOfHours = arr.size();
            // niet direct alle reservatins vastzetten, maar 1 per 1 en kijken of je een lokaal vindt, als dit niet zo is probeer nieuwe data.
            //alternatief, alle gemeenschappelijke data en dan lokalen toekennen tot het goed is.
            ArrayList<TimeSlot> reservationTimeslots = getAvailableData(timeslots, course.getPrograms());

            List<ReservationOptimize> reservations = new ArrayList<>();

            int t = 0;
            while (t < reservationTimeslots.size()) {
                TimeSlot timeslot = reservationTimeslots.get(t);
                //TODO mogelijke optimalizatie voor P alle reservationTimeslots de classroom bepalen en dan de amountOfHours kleinste pakken.
                Tuple<Classroom, Boolean> tuple = searchClassroom(course, timeslot);

                if (tuple == null || tuple.getX() == null) {
                    //could not find classroom here we might later split in groups
                    t++;
                    continue;
                }

                Classroom classroom = tuple.getX();

                ReservationOptimize reservation = new ReservationOptimize(course, timeslot, classroom, tuple.getY(), course.getAmountOfStudents());
                reservations.add(reservation);
                t++;
            }

            reservations = getBestReservations(reservations, amountOfHours);

            for (ReservationOptimize reservation : reservations) {
                boolean worked = changeReservation(reservation, reservation.getTimeSlot(), arr, amountOfHours);
                if (worked) {
                    amountOfHours--;
                    changes++;
                    reservationTimeslots.remove(reservation.getTimeSlot());
                    if (amountOfHours <= 0) {
                        break;
                    }
                }
            }

            if (amountOfHours > 0) {
                System.out.println(amountOfHours + " uren over !");
            }
        }
    }

    public HashMap<Course, ArrayList<ReservationOptimize>> getSingles(HashMap<TimeSlot, ArrayList<ReservationOptimize>> reservations, int weken) {
        HashMap<Course, ArrayList<ReservationOptimize>> singles = new HashMap<>();
        HashSet<Program> seen;
        HashMap<Program, ReservationOptimize> single;

        HashSet<Program> programs = timetable.getPrograms();

        for (int week = 0; week < weken; week++) {
            for (int day = 0; day < 5; day++) {
                seen = new HashSet<>();
                single = new HashMap<>();
                for (int hour = 0; hour < 8; hour++) {
                    TimeSlot timeSlot = new TimeSlot(week, day, hour);
                    ArrayList<ReservationOptimize> ress = reservations.get(timeSlot);
                    if (ress == null) continue;
                    for (ReservationOptimize res : ress) {
                        for (Program program : res.getCourse().getPrograms()) {
                            if (!seen.contains(program)) {
                                if (single.containsKey(program)) {
                                    single.remove(program);
                                    seen.add(program);
                                } else {
                                    single.put(program, res);
                                }
                            }
                        }

                    }
                }
                HashSet<ReservationOptimize> used = new HashSet<>();
                for (ReservationOptimize reservationOptimize : single.values()) {
                    if (used.contains(reservationOptimize)) continue;
                    used.add(reservationOptimize);
                    Course course = reservationOptimize.getCourse();
                    singles.computeIfAbsent(course, k -> new ArrayList<>());
                    singles.get(course).add(reservationOptimize);
                }
                Tuple<Integer, Integer> t = new Tuple<>(week, day);
                for (Program program : programs) {
                    if (!seen.contains(program) && !single.containsKey(program)) {
                        emptyDays.computeIfAbsent(program, k -> new HashSet<>());
                        emptyDays.get(program).add(t);
                    }
                }
            }
        }


        return singles;
    }

    public HashMap<Course, ArrayList<ReservationOptimize>> getLates(HashMap<TimeSlot, ArrayList<ReservationOptimize>> reservations, int weken) {
        HashMap<Course, ArrayList<ReservationOptimize>> lates = new HashMap<>();

        for (int week = 0; week < weken; week++) {
            for (int day = 0; day < 5; day++) {
                for (int hour : new int[]{6, 7, 3}) {
                    TimeSlot timeSlot = new TimeSlot(week, day, hour);
                    ArrayList<ReservationOptimize> ress = reservations.get(timeSlot);
                    if (ress == null) continue;
                    for (ReservationOptimize reservationOptimize : ress) {
                        Course course = reservationOptimize.getCourse();
                        lates.computeIfAbsent(course, k -> new ArrayList<>());
                        lates.get(course).add(reservationOptimize);
                    }
                }
            }
        }

        return lates;
    }

    public ArrayList<TimeSlot> getAvailableData(ArrayList<ArrayList<TimeSlot>> data, ArrayList<Program> programs) {
        //get smallest array
        ArrayList<TimeSlot> smallest = new ArrayList<>();
        int smallestSize = -1;
        for (ArrayList<TimeSlot> a : data) {
            if (smallestSize == -1 || a.size() < smallestSize) {
                smallestSize = a.size();
                smallest = a;
            }
        }

        ArrayList<TimeSlot> result = new ArrayList<>();

        boolean found = true;
        for (TimeSlot timeSlot : smallest) {
            found = true;
            for (ArrayList a : data) {
                if (!a.contains(timeSlot)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                boolean isEmpty = false;
                for (Program program : programs) {
                    if (emptyDays.containsKey(program) && emptyDays.get(program).contains(new Tuple<>(timeSlot.getWeek(), timeSlot.getDay()))) {
                        isEmpty = true;
                        break;
                    }
                }
                if (!isEmpty && timeSlot.isGood()) result.add(timeSlot);
            }
        }

        return result;
    }

    public Tuple<Classroom, Boolean> searchClassroom(Course course, TimeSlot timeSlot) {

        HashMap<TimeSlot, HashMap<String, ArrayList<Classroom>>> classroomsTimeslot = timetable.getClassroomsTimeslot();

        for (Program program : course.getPrograms()) {
            String siteCode = program.getHomesite();
            ArrayList<Classroom> siteClassrooms = classroomsTimeslot.get(timeSlot).get(siteCode);
            // can become binSearch if slow
            for (int i = siteClassrooms.size() - 1; i >= 0; i--) {
                Classroom classroom = siteClassrooms.get(i);
                if (classroom.getCapaciteit() > course.getAmountOfStudents()) {
                    classroom.setSiteCode(siteCode);
                    return new Tuple<>(classroom, true);
                }
            }
        }


        Classroom closest = null;

        ArrayList<String> mySites = new ArrayList<>(classroomsTimeslot.get(timeSlot).keySet());
        mySites.sort((o1, o2) -> {
            double d1 = 0;
            double d2 = 0;
            for (Program program : course.getPrograms()) {
                Site home = timetable.getSiteFromString(program.getHomesite());
                Site other1 = timetable.getSiteFromString(o1);
                Site other2 = timetable.getSiteFromString(o2);
                d1 += home.distance(other1);
                d2 += home.distance(other2);
            }
            return (int) Math.ceil(d2 - d1);
        });
        for (String siteCode : mySites) {
            ArrayList<Classroom> classrooms1 = classroomsTimeslot.get(timeSlot).get(siteCode);
            for (int i = classrooms1.size() - 1; i >= 0; i--) {
                Classroom classroom = classrooms1.get(i);
                if (classroom.getCapaciteit() > course.getAmountOfStudents()) {
                    classroom.setSiteCode(siteCode);
                    return new Tuple<>(classroom, false);
                }
            }
        }
        return new Tuple<>(null, false);
    }

    public List<ReservationOptimize> getBestReservations(List<ReservationOptimize> reservations, int amount) {

        reservations.sort((o1, o2) -> {
            if (o1.isInSite() && !o2.isInSite()) return -1;
            if (!o1.isInSite() && o2.isInSite()) return 1;
            if (o1.getTimeSlot().isGood() && !o2.getTimeSlot().isGood()) return -1;
            if (!o1.getTimeSlot().isGood() && o2.getTimeSlot().isGood()) return 1;
            return o1.getClassroom().getCapaciteit() - o2.getClassroom().getCapaciteit();
        });

        ArrayList<ReservationOptimize> results = new ArrayList<>();

        int counter = 0;
        if (reservations.size() == 0) return new ArrayList<>();
        int amountOfHours = amount;
        for (ReservationOptimize reservation : reservations) {
            if (reservation.getClassroom() == null) continue;
            results.add(reservation);
            counter++;
            //if (counter >= amountOfHours) return results;

        }
        return results;
    }

    public boolean changeReservation(ReservationOptimize reservation, TimeSlot timeSlot, ArrayList<ReservationOptimize> resO, int pos) {
        HashMap<Teacher, HashMap<Tuple<Integer, Integer>, ArrayList<TimeSlot>>> timeslotsTeacher = timetable.getTimeslotsTeacher();
        HashMap<Program, HashMap<Tuple<Integer, Integer>, ArrayList<TimeSlot>>> timeslotsProgram = timetable.getTimeslotsProgram();
        HashMap<TimeSlot, HashMap<String, ArrayList<Classroom>>> classroomsTimeslot = timetable.getClassroomsTimeslot();

        ReservationOptimize old = resO.get(pos-1);
        double debug2 = (getDistance(reservation) - getDistance(old))*4/75;
        if ((getDistance(reservation) - getDistance(old))*4/75 > 2) {
            return false;
        }

        Tuple<Integer, Integer> t = new Tuple<>(timeSlot.getDay(), timeSlot.getHour());

        for (Teacher teacher : reservation.getCourse().getTeachers()) {
            ArrayList<TimeSlot> debug = timeslotsTeacher.get(teacher).get(t);
            timeslotsTeacher.get(teacher).get(t).remove(timeSlot);
        }
        for (Program program : reservation.getCourse().getPrograms()) {
            timeslotsProgram.get(program).get(t).remove(timeSlot);
        }
        classroomsTimeslot.get(timeSlot).get(reservation.getClassroom().getSiteCode()).remove(reservation.getClassroom());

        //remove old reservation
        TimeSlot oldTime = old.getTimeSlot();

        Tuple<Integer, Integer> oldT = new Tuple<>(oldTime.getDay(), oldTime.getHour());

        for (Teacher teacher : old.getCourse().getTeachers()) {
            timeslotsTeacher.get(teacher).get(oldT).add(oldTime);
        }
        for (Program program : old.getCourse().getPrograms()) {
            timeslotsProgram.get(program).get(oldT).add(oldTime);
        }
        ArrayList<Classroom> debug = classroomsTimeslot.get(oldTime).get(old.getClassroom().getSiteCode());
        classroomsTimeslot.get(oldTime).get(old.getClassroom().getSiteCode()).add(old.getClassroom());

        timetable.changeReservation(reservation, old);
        return true;
    }

    public double getDistance(ReservationOptimize reservation) {
        double result = 0;
        HashMap<TimeSlot, ArrayList<ReservationOptimize>> reservations = timetable.getReservations();
        TimeSlot timeSlot = reservation.getTimeSlot();

        ArrayList<Program> programs = reservation.getCourse().getPrograms();

        if (timeSlot.getHour() != 0) {
            TimeSlot prev = new TimeSlot(timeSlot.getWeek(), timeSlot.getDay(), timeSlot.getHour()-1);
            if (reservations.get(prev) != null) {
                for (ReservationOptimize reservationOptimize : reservations.get(prev)) {
                    for (Program program : reservationOptimize.getCourse().getPrograms()) {
                        if (programs.contains(program)) {
                            result += getDistanceReservation(reservation, reservationOptimize);
                        }
                    }
                }
            }
        }

        if (timeSlot.getHour() != 7) {
            TimeSlot next = new TimeSlot(timeSlot.getWeek(), timeSlot.getDay(), timeSlot.getHour()+1);
            if (reservations.get(next) != null) {
                for (ReservationOptimize reservationOptimize : reservations.get(next)) {
                    for (Program program : reservationOptimize.getCourse().getPrograms()) {
                        if (programs.contains(program)) {
                            result += getDistanceReservation(reservation, reservationOptimize);
                        }
                    }
                }
            }
        }

        return result;
    }

    public double getDistance2(ReservationOptimize reservation) {
        double result = 0;

        ArrayList<Program> programs = reservation.getCourse().getPrograms();

        for (Program program : programs) {
            Site home = jsonModel.getSiteByCode(program.getHomesite());
            Site s1 = jsonModel.getSiteByCode(reservation.getClassroom().getSiteCode());
            result += s1.distance(home);
        }

        return result;
    }

    public double getDistanceReservation(ReservationOptimize r1, ReservationOptimize r2) {
        Site s1 = jsonModel.getSiteByCode(r1.getClassroom().getSiteCode());
        Site s2 = jsonModel.getSiteByCode(r2.getClassroom().getSiteCode());

        return s1.distance(s2);
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();

        JsonReader jsonReader = new JsonReader();
        JsonModel jsonModel = jsonReader.readJson("project.json");
        jsonModel.init();
        JsonReservation[] resultModel = jsonReader.readResult("result0.json");

        TimetableOptimize timetable = new TimetableOptimize(jsonModel, resultModel);

        TimetableOptimizer9000 optimizer = new TimetableOptimizer9000(timetable, jsonModel);

        optimizer.execute();
        optimizer.printWeek(0, "CBINFO", 3);
        //System.out.println("amount of changes: "+optimizer.changes);

        Collection<JsonReservation> jsonReservations = timetable.toJsonReservations();
        new JsonWriter().writeJsonModels(jsonReservations, "resultNew.json");
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000;
        System.out.println("duration: " + duration);
    }

    public void printWeek(int week, String programStr, int mt) {
        Program program = jsonModel.getProgramByCode(programStr, mt);

        System.out.println("-----------------------------------------");
        System.out.println("\\begin{table}[h!]");
        System.out.println("\\centering");
        System.out.println("\\begin{tabular}{|c|c|c|c|c|c|}");
        System.out.println("\\hline");
        System.out.println(" & ma & di & wo & do & vr \\\\ \\hline");
        HashMap<TimeSlot, ArrayList<ReservationOptimize>> reservations = timetable.getReservations();

        for (int hour = 0; hour < 8; hour++) {
            System.out.print((hour+1) + " ");
            for (int day = 0; day < 5; day++) {
                TimeSlot timeSlot = new TimeSlot(week, day, hour);
                ArrayList<ReservationOptimize> reservationArrayList = reservations.get(timeSlot);
                boolean found = false;
                if (reservationArrayList == null) {
                    System.out.print(" & ");
                    continue;
                }
                for (ReservationOptimize reservation : reservationArrayList) {
                    if (reservation.getCourse().getPrograms().contains(program)) {
                        found = true;

                        System.out.print("& ");
                        System.out.print("\\shortstack{");
                        String str1 = reservation.getCourse().getCursusnaam();
                        System.out.print(str1.substring(0,Math.min(10, str1.length())));
                        System.out.print(" \\\\ ");
                        String str2 = reservation.getClassroom().getNaam();
                        System.out.print(str2.substring(0,Math.min(10, str2.length())));
                        System.out.print("} ");
                        break;
                    }
                }
                if (!found) {
                    System.out.print(" & ");
                }

            }
            System.out.println(" \\\\ \\hline");
        }
        System.out.println("\\end{tabular}");
        System.out.println("\\caption{CAPTION}");
        System.out.println("\\label{tab:my_label}");
        System.out.println("\\end{table}");
    }
}


