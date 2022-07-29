package models;

import java.util.*;

public class TimetableGreedy implements Timetable {

    private static final String[] days = new String[]{"ma", "di", "wo", "do", "vr"};
    private static final int amountOfWeeks = 13;
    private static final int amountOfDays = 5;
    private static final int amountOfHours = 8;


    private ArrayList<Course> courses;
    private ArrayList<Site> sites;
    private HashMap<TimeSlot, ArrayList<Reservation>> reservations;

    public TimetableGreedy(ArrayList<Course> courses, ArrayList<Site> sites) {
        this.courses = courses;
        this.sites = sites;
        reservations = new HashMap<>();
    }

    public HashSet<Reservation> getReservations(TimeSlot timeSlot) {
        if(reservations.get(timeSlot)==null){
            return new HashSet<>();
        }
        return new HashSet<>(reservations.get(timeSlot));
    }

    public void addReservation(Reservation reservation, TimeSlot timeSlot) {
        ArrayList<Reservation> timeReservations = reservations.getOrDefault(timeSlot, new ArrayList<>());
        timeReservations.add(reservation);
        reservations.put(timeSlot, timeReservations);

        //OF:
        //reservations.putIfAbsent(timeSlot, new ArrayList<>());
        //reservations.get(timeSlot).add(reservation);
    }

    public boolean isReservationPossible(Reservation reservation) {
        TimeSlot time = reservation.getTimeSlot();
        for (Reservation reserv : reservations.getOrDefault(time, new ArrayList<>())) {
            if (reserv.getClassroom() == reservation.getClassroom()) {
                return false;
            }
            if (reserv.getCourse().hasTeacherStudentsConflict(reservation.getCourse())) {
                return false;
            }
        }
        return true;
    }

    public int amountCorrect() {
        //TODO nog niet correct omdat we timeslot.getNext() doen maar deze gaat enkel goede uren doen
        int counter = 0;
        TimeSlot timeSlot = new TimeSlot(0,0,0);
        while (timeSlot.getWeek() != -1) {
            if (!reservations.containsKey(timeSlot)) {
                timeSlot = timeSlot.getNext();
                continue;
            }
            HashSet<Program> programs = new HashSet<>();
            HashSet<Teacher> teachers = new HashSet<>();
            HashSet<Classroom> classrooms = new HashSet<>();
            for (Reservation reservation : reservations.get(timeSlot)){
                boolean counts = true;
                for (Program program : reservation.getCourse().getPrograms()) {
                    if (programs.contains(program)) {
                        System.out.println("PROGRAM");
                        continue;
                    } else {
                        programs.add(program);
                    }
                }
                for (Teacher teacher : reservation.getCourse().getTeachers()) {
                    if (teachers.contains(teacher)){
                        System.out.println("TEACHER");
                        continue;
                    } else {
                        teachers.add(teacher);
                    }
                }
                if (classrooms.contains(reservation.getClassroom())) {
                    System.out.println("CLASSROOM");
                    continue;
                } else {
                    classrooms.add(reservation.getClassroom());
                }

                if (reservation.getAmount() > reservation.getClassroom().getCapaciteit()) {
                    System.out.println("CLASSROOM FULL");
                    continue;
                }
                counter++;
            }
            timeSlot = timeSlot.getNext();
        }
        return counter;
    }

    //gives a random combination of this timetable combinated with a different one
    public TimetableGreedy crossover(TimetableGreedy timetableGreedy) {
        //TODO
        return null;
    }

    public Reservation getRandomReservation() {
        Random random = new Random();
        Course course = courses.get(random.nextInt(courses.size()));
        Site site = sites.get(random.nextInt(sites.size()));
        Classroom classroom = site.getClassrooms().get(site.getClassrooms().size());
        TimeSlot timeSlot = new TimeSlot(random.nextInt(13), random.nextInt(5), random.nextInt(8));
        return new Reservation(course, classroom, timeSlot, course.getAmountOfStudents());
    }

    //makes json file of the timetable for submitting
    public void toJson(String fileName) {
        //TODO
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public ArrayList<Site> getSites() {
        return sites;
    }

    public ArrayList<Classroom> getClassrooms() {
        ArrayList<Classroom> classrooms = new ArrayList<>();
        for (Site site : sites) {
            classrooms.addAll(site.getClassrooms());
        }
        return classrooms;
    }

    public HashMap<TimeSlot, ArrayList<Reservation>> getReservations() {
        return reservations;
    }

    public void setReservations(HashMap<TimeSlot, ArrayList<Reservation>> reservations) {
        this.reservations = reservations;
    }

    @Override
    public ArrayList<JsonReservation> toJsonReservations() {
        ArrayList<JsonReservation> jsonReservations = new ArrayList<>();

        for (ArrayList<Reservation> timeReservations : reservations.values()) {
            for (Reservation reservation : timeReservations) {

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
