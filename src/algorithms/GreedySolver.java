package algorithms;

import adapters.JsonAdapter;
import json.JsonModel;
import json.JsonReader;
import json.JsonWriter;
import models.*;

import java.util.*;

public class GreedySolver {
    //per timeslot welke classrooms van welke sites er nog vrij zijn
    private HashMap<TimeSlot, HashMap<String, ArrayList<Classroom>>> classroomsTimeslot = new HashMap<>();
    //per teacher welke timeslots hij nog vrij is
    private HashMap<Teacher, ArrayList<TimeSlot>> timeslotsTeacher = new HashMap<>();
    //per program welke tijdslots hij nog vrij is
    private HashMap<Program, ArrayList<TimeSlot>> timeslotsProgram = new HashMap<>();
    //per programma hoeveel uren we nog niet gescheduled hebben
    private HashMap<Course, Double> hoursLeft = new HashMap<>();

    //lijst van alle cursussen,classrooms en sites
    private ArrayList<Course> courses;
    private ArrayList<Classroom> classrooms;
    private ArrayList<Site> sites;

    //De timetable die we invullen
    private TimetableGreedy timetable;

    //het model van de gelezen json
    private JsonModel jsonModel;

    //counter om aantal niet geschedulde lessen bij te houden
    private int aantalNiet = 0;

    //map van alle sites en zijn bijhorende classrooms
    HashMap<String, ArrayList<Classroom>> fullMap = new HashMap<>();

    public GreedySolver(TimetableGreedy timetable, JsonModel jsonModel) {
        this.jsonModel = jsonModel;
        this.courses = timetable.getCourses();
        this.classrooms = timetable.getClassrooms();
        this.sites = timetable.getSites();
        this.timetable = timetable;
        //sorteer de lessen en lokalen van groot naar klein
        courses.sort(Comparator.comparingInt(Course::getAmountOfStudents).reversed());
        classrooms.sort(Comparator.comparingInt(Classroom::getCapaciteit).reversed());

        // vul fullmap met alle classrooms per site
        for (Site site : sites) {
            ArrayList<Classroom> classrooms1 = site.getClassrooms();
            classrooms1.sort(Comparator.comparingInt(Classroom::getCapaciteit).reversed());
            fullMap.put(site.getCode(), classrooms1);
        }
    }

    public void execute() {
        int counter = 0;
        int i = -1;
        //ga alle lessen af van groot naar klein
        while (i < courses.size() - 1) {
            int countBef = counter;
            i++;
            Course course = courses.get(i);
            if (course.getCode().equals("F000863A")) {
                System.out.println("yeet");
            }

            //lijst van alle vogelijkheden van de verschillende factoren
            ArrayList<ArrayList<TimeSlot>> timeslots = new ArrayList<>();
            //vrije momenten van teachers verzamelen
            for (Teacher teacher : course.getTeachers()) {
                if (!timeslotsTeacher.containsKey(teacher)) timeslotsTeacher.put(teacher, TimeSlot.weekTimeSlots());
                timeslots.add(timeslotsTeacher.get(teacher));
            }
            //vrije momenten van programma's verzamelen
            for (Program program : course.getPrograms()) {
                if (!timeslotsProgram.containsKey(program)) timeslotsProgram.put(program, TimeSlot.weekTimeSlots());
                timeslots.add(timeslotsProgram.get(program));
            }
            //aantal uren voor een week voor de les nemen
            int amountOfHours = (int) Math.ceil(course.getContacturen());
            //kijk welke data past voor alle facroten en stop dit in de gereturnde arraylist
            ArrayList<TimeSlot> reservationTimeslots = getAvailableData(timeslots, amountOfHours);

            //lijst van mogelijke reservaties
            List<Reservation> reservations = new ArrayList<>();

            int t = 0;
            //voor alle mogelijke timeslots
            while (t < reservationTimeslots.size()) {
                TimeSlot timeslot = reservationTimeslots.get(t);
                //zoek de best passende classroom geeft tuple terug met classroom en of deze al dan niet in de site ligt
                Tuple<Classroom, Boolean> tuple = searchClassroom(course, timeslot);

                if (tuple == null || tuple.getX() == null) {
                    //could not find classroom here we might later split in groups
                    t++;
                    continue;
                }

                Classroom classroom = tuple.getX();
                //mogelijke reservatie maken
                Reservation reservation = new Reservation(course, classroom, timeslot, course.getAmountOfStudents());
                reservation.setInSite(tuple.getY());
                reservations.add(reservation);
                t++;
            }

            //sorteer de reservaties van goed naar slecht
            reservations = getBestReservations(reservations);

            //voor elke reservatie die mogelijk is voeg deze toe.
            for (Reservation reservation : reservations) {
                if (course.getContacturen() == 0) continue;
                addReservation(reservation, reservation.getTimeSlot());
                counter++;
                amountOfHours--;
                reservationTimeslots.remove(reservation.getTimeSlot());
                //als we genoeg reservaties hebben gescheduled stop
                if (amountOfHours <= 0) {
                    break;
                }
            }

            //als we niet genoeg reservaties hebben gevonden, split
            if (amountOfHours > 0) {
                //split in groups
                System.out.println("split");
                counter -= amountOfHours;
                if (course.getAmountOfStudents() > 5) {
                    splitIntoGroups(course, amountOfHours, reservationTimeslots);
                } else {
                    //not enough people to split into groups
                    aantalNiet += amountOfHours;
                }
            }
            if (counter - countBef != (int) Math.ceil(course.getContacturen())) {
                System.out.println(course.getCursusnaam());
            }
        }
        System.out.println("amount scheduled: " + counter);
        System.out.println("amount not scheduled: " + aantalNiet);
    }

    public void splitIntoGroups(Course course, int hours, ArrayList<TimeSlot> available) {
        //grootst mogelijk lokaal dat we kunnen vinden in de site of als er geen is erbuiten
        Classroom biggest = getBiggestClassroom(course, available);
        //als we er geen gevonden hebben
        if (biggest == null) {
            aantalNiet += hours;
            return;
        }
        //spits groep op in capaciteit grootste lokaal en de rest
        int group1 = biggest.getCapaciteit();
        int group2 = course.getAmountOfStudents() - biggest.getCapaciteit();

        //bereken aantal uren dat er nog les moet zijn in de groepen
        double newHours = (course.getContacturenReal() - ((Math.ceil(course.getContacturen()) - hours) * 12)) * 1.25;

        //zet aantal uren van huidige les naar aantal ingeplande uren
        course.setContacturen((Math.ceil(course.getContacturen()) - hours) * 12 * 1.25);

        //maak nieuwe lessen
        Course course1 = new Course(
                course.getCursusnaam(),
                course.getCode(),
                course.getPrograms(),
                course.getTeachers(),
                newHours,
                group1,
                course.hashCode() + 1);
        Course course2 = new Course(
                course.getCursusnaam(),
                course.getCode(),
                course.getPrograms(),
                course.getTeachers(),
                newHours,
                group2,
                course.hashCode() + 2);
        //voeg de nieuwe lessen toe in de lessen arrayList
        int index = Collections.binarySearch(courses, course1, Comparator.comparingInt(Course::getAmountOfStudents).reversed());
        if (index < 0) courses.add(-index - 1, course1);
        else courses.add(index, course1);
        index = Collections.binarySearch(courses, course2, Comparator.comparingInt(Course::getAmountOfStudents).reversed());
        if (index < 0) courses.add(-index - 1, course2);
        else courses.add(index, course2);
    }

    //zoek grootste lokaal in site of erbuiten als er geen meer is in de site
    public Classroom getBiggestClassroom(Course course, ArrayList<TimeSlot> available) {
        Classroom biggest = null;
        for (TimeSlot timeSlot : available) {
            for (Program program : course.getPrograms()) {
                String siteCode = program.getHomesite();
                if (!classroomsTimeslot.containsKey(timeSlot)) classroomsTimeslot.put(timeSlot, fullMapClone());
                if (classroomsTimeslot.get(timeSlot).get(siteCode).size() == 0) break;
                Classroom siteBiggest = classroomsTimeslot.get(timeSlot).get(siteCode).get(0);
                if (biggest == null || biggest.getCapaciteit() < siteBiggest.getCapaciteit()) biggest = siteBiggest;
            }
        }
        if (biggest == null) System.out.println("not schedulable");
        return biggest;
    }

    //vul lessen van een week over het volledige semester
    public void multiplyWeekby13() {
        int[] weeks = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        for (TimeSlot timeSloth : TimeSlot.weekTimeSlots()) {

            ArrayList<Reservation> reservations = timetable.getReservations().get(timeSloth);
            int oldLength = reservations.size();
            for (int j = 0; j < oldLength; j++) {
                Reservation reservation = reservations.get(j);
                Course course = reservation.getCourse();
                if (!hoursLeft.containsKey(course)) hoursLeft.put(course, Math.ceil(course.getContacturenReal()));
                hoursLeft.put(course, hoursLeft.get(reservation.getCourse()) - 1);
                for (int i : weeks) {
                    if (hoursLeft.get(course) <= 0) break;
                    TimeSlot resTime = reservation.getTimeSlot();
                    TimeSlot timeSlot = new TimeSlot(i, resTime.getDay(), resTime.getHour());
                    Reservation reservation1 = new Reservation(course, reservation.getClassroom(), timeSlot, reservation.getAmount());

                    addReservation(reservation1, resTime);


                    hoursLeft.put(course, hoursLeft.get(reservation.getCourse()) - 1);
                }
            }
        }
        System.out.println("done");
    }

    //zoek het beste classroom
    public Tuple<Classroom, Boolean> searchClassroom(Course course, TimeSlot timeSlot) {

        if (course.getAmountOfStudents() > classrooms.get(0).getCapaciteit()) {
            return null;
        }

        if (!classroomsTimeslot.containsKey(timeSlot)) classroomsTimeslot.put(timeSlot, fullMapClone());
        //zoek mogelijk lokaal in de site dat zo goed mogelijk gevuld is
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
        //sorteer sites van dicht naar ver
        ArrayList<String> mySites = new ArrayList<>(classroomsTimeslot.get(timeSlot).keySet());
        mySites.sort((o1, o2) -> {
            double d1 = 0;
            double d2 = 0;
            for (Program program : course.getPrograms()) {
                Site home = jsonModel.getSiteByCode(program.getHomesite());
                Site other1 = jsonModel.getSiteByCode(o1);
                Site other2 = jsonModel.getSiteByCode(o2);
                d1 += home.distance(other1);
                d2 += home.distance(other2);
            }
            return (int) Math.ceil(d2 - d1);
        });
        //voor elke site van dicht naar ver kijk naar het lokaal dat het beste gevult wordt
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

    //voeg een reservatie toe en pas alle dingen die de mogelijkheden bijhouden
    public void addReservation(Reservation reservation, TimeSlot timeSlot) {
        for (Teacher teacher : reservation.getCourse().getTeachers()) {
            timeslotsTeacher.get(teacher).remove(timeSlot);
        }
        for (Program program : reservation.getCourse().getPrograms()) {
            timeslotsProgram.get(program).remove(timeSlot);
        }
        if (!classroomsTimeslot.containsKey(timeSlot)) classroomsTimeslot.put(timeSlot, fullMapClone());
        classroomsTimeslot.get(timeSlot).get(reservation.getClassroom().getSiteCode()).remove(reservation.getClassroom());
        timetable.addReservation(reservation, timeSlot);
    }

    public ArrayList<TimeSlot> getAvailableData(ArrayList<ArrayList<TimeSlot>> data, int amount) {
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
                result.add(timeSlot);
            }
        }

        return result;
    }

    public TimetableGreedy getTimetable() {
        return timetable;
    }

    //geeft clone van fullMap
    public HashMap<String, ArrayList<Classroom>> fullMapClone() {
        HashMap<String, ArrayList<Classroom>> result = new HashMap<>();
        for (String s : fullMap.keySet()) {
            result.put(s, (ArrayList<Classroom>) fullMap.get(s).clone());
        }
        return result;
    }

    //sorteert reservaties van goed naar slecht
    public List<Reservation> getBestReservations(List<Reservation> reservations) {
        //sorteer de reservaties volgens geldigheid, insite, goede uren en capaciteit
        reservations.sort((o1, o2) -> {
            if (o1.getClassroom() == null) return 1;
            if (o2.getClassroom() == null) return -1;
            if (o1.isInSite() && !o2.isInSite()) return -1;
            if (!o1.isInSite() && o2.isInSite()) return 1;
            if (o1.getTimeSlot().isGood() && !o2.getTimeSlot().isGood()) return -1;
            if (!o1.getTimeSlot().isGood() && o2.getTimeSlot().isGood()) return 1;
            return o1.getClassroom().getCapaciteit() - o2.getClassroom().getCapaciteit();
        });

        ArrayList<Reservation> results = new ArrayList<>();
        ArrayList<Reservation> others = new ArrayList<>();

        int counter = 0;
        if (reservations.size() == 0) return new ArrayList<>();
        int amountOfHours = (int) Math.ceil(reservations.get(0).getCourse().getContacturen());
        for (Reservation reservation : reservations) {
            if (reservation.getClassroom()==null) continue;
            if (reservation.getTimeSlot().getHour() == 7) {
                others.add(reservation);
                continue;
            }
            results.add(reservation);
            counter++;
            if (counter >= amountOfHours) return results;
        }

        for (Reservation reservation : others) {
            results.add(reservation);
            counter++;
            if (counter >= amountOfHours) return results;
        }

        return results;
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        JsonReader jsonReader = new JsonReader();
        JsonModel jsonModel = jsonReader.readJson("project.json");
        jsonModel.init();
        JsonAdapter jsonAdapter = new JsonAdapter();
        TimetableGreedy timetable = jsonAdapter.toGreedyTimetable(jsonModel);
        GreedySolver greedyAlgorithm = new GreedySolver(timetable, jsonModel);
        //greedyAlgorithm.test();

        greedyAlgorithm.execute();
        greedyAlgorithm.multiplyWeekby13();
        greedyAlgorithm.printWeek(0, "CBINFO", 3);

        TimetableGreedy resultTimetable = greedyAlgorithm.getTimetable();
        Collection<JsonReservation> jsonReservations = resultTimetable.toJsonReservations();
        System.out.println("###########################");
        //greedyAlgorithm.checkCorrect(jsonReservations, jsonModel);
        new JsonWriter().writeJsonModels(jsonReservations, "result0.json");
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000;
        System.out.println("duration: " + duration);
    }

    public void test() {
        int tot = 0;
        for (Course course : courses) {
            if (course.getPrograms().size() > 1) tot++;
        }
        System.out.println("aantal uren: " + tot);
    }

    public void printWeek(int week, String programStr, int mt) {
        Program program = jsonModel.getProgramByCode(programStr, mt);

        System.out.println("-----------------------------------------");
        System.out.println("\\begin{table}[h!]");
        System.out.println("\\centering");
        System.out.println("\\begin{tabular}{|c|c|c|c|c|c|}");
        System.out.println("\\hline");
        System.out.println(" & ma & di & wo & do & vr \\\\ \\hline");
        HashMap<TimeSlot, ArrayList<Reservation>> reservations = timetable.getReservations();

        for (int hour = 0; hour < 8; hour++) {
            System.out.print((hour+1) + " ");
            for (int day = 0; day < 5; day++) {
                TimeSlot timeSlot = new TimeSlot(week, day, hour);
                ArrayList<Reservation> reservationArrayList = reservations.get(timeSlot);
                boolean found = false;
                if (reservationArrayList == null) {
                    System.out.print(" & ");
                    continue;
                }
                for (Reservation reservation : reservationArrayList) {
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
        System.out.println("\\caption{Uurrooster 3e Bach informatica voor 1 week uit gretig algoritme}");
        System.out.println("\\label{tab:my_label}");
        System.out.println("\\end{table}");
    }

    public void checkCorrect(Collection<JsonReservation> jsonReservations, JsonModel jsonModel) {
        HashMap<String, Double> hours = new HashMap<>();

        //fill
        for (Course course : jsonModel.getVakken()) {
            hours.put(course.getCode(), course.getContacturenReal());
        }

        for (JsonReservation jsonReservation : jsonReservations) {
            String code = jsonReservation.getCode();
            hours.put(code, hours.get(code) - (jsonReservation.getAantal() / jsonModel.getCourseByCode(code).getAmountOfStudents()));
        }

        for (String str : hours.keySet()) {
            if (hours.get(str) > 0) System.out.println(str + " heeft te weinig : " + hours.get(str));
        }

    }
}