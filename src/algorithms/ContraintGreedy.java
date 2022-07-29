package algorithms;

import heap.Element;
import heap.EmptyHeapException;
import heap.Heap;
import heap.Heaps;
import json.JsonModel;
import json.JsonReader;
import json.JsonWriter;
import models.*;

import java.util.*;
import java.util.stream.Collectors;

/*
*
* Een gretig algoritme dat voor elk vak exact bijhoudt op hoeveel mogelijke manieren het nog gescheduld kan worden.
* Dan wordt telkens de meest geconstrainde ingepland.
* Voor effeciëntie maakt dit gebruik van een heap met change-key operaties.
* Toch draait dit algoritme ongeveer 40 minuten, en hebben we het daardoor achterwege gelaten.
*
* */

public class ContraintGreedy {
    private JsonModel database;
    private ArrayList<TimeSlot> allHours;
    private HashSet<Classroom> allRooms;
    private HashSet<CourseC> allCourses;

    private HashMap<CourseC, HashSet<Classroom>> consideredRooms = new HashMap<>();

    private HashMap<CourseC, Integer> hoursNeeded = new HashMap<>();
    private HashMap<CourseC, ArrayList<PossibleRoom>> availabilities;
    private HashMap<CourseC, Double> urgency;
    private HashMap<TimeSlot, HashSet<Classroom>> availableRooms = new HashMap<>();
    private HashMap<TimeSlot, HashSet<CourseC>> coursesWantingHour = new HashMap<>();

    private HashSet<Reservation> timetable = new HashSet<>();

    private Heap<CourseC> courseHeap;
    private HashMap<CourseC, Element<CourseC>> heapElements = new HashMap<>();

    private int counter = 0;

    public ContraintGreedy(JsonModel model) {
        database = model;
    }


    public void initialize(){
        database.init();
        allHours = TimeSlot.allTimeslots();
        allRooms = database.getRooms();
        allCourses = new HashSet<>();

        timetable = new HashSet<>();
        availabilities = new HashMap<>();
        courseHeap = Heaps.newBinaryHeap();
        urgency = new HashMap<>();
        availableRooms = new HashMap<>();

        HashMap<String, Site> siteName = new HashMap<>();
        for(Site s: database.getSites()){
            siteName.put(s.getCode(),s);
        }

        for (TimeSlot slot : allHours) {
            availableRooms.put(slot, new HashSet<>(allRooms));
            coursesWantingHour.put(slot, new HashSet<>());
        }

        for(Course course: database.getVakken()){
            CourseC cc = new CourseC(course);
            allCourses.add(cc);
            hoursNeeded.put(cc,(int) Math.ceil(course.getContacturen()));
            availabilities.put(cc, new ArrayList<>());
            consideredRooms.put(cc,new HashSet<>());

            if(course.getPrograms().size()==0){
                for (Classroom room : allRooms) {
                    if (room.getCapaciteit() >= course.getAmountOfStudents()) {
                        consideredRooms.get(cc).add(room);
                    }
                }
            }else{
                for(Program p: course.getPrograms()){
                    Site s = siteName.get(p.getHomesite());
                    for (Classroom room : s.getClassrooms()) {
                        if (room.getCapaciteit() >= course.getAmountOfStudents()) {
                            consideredRooms.get(cc).add(room);
                        }
                    }
                }
            }

            int counter = consideredRooms.get(cc).size();

            for (TimeSlot slot: allHours) {
                if (counter > 0) {
                    PossibleRoom possibleRoom = new PossibleRoom(slot, counter);
                    availabilities.get(cc).add(possibleRoom);
                    //is sowieso gesorteerd

                    coursesWantingHour.get(slot).add(cc);
                }
            }

            urgency.put(cc,getUrgency(cc));
            Element<CourseC> el = courseHeap.insert(cc);
            heapElements.put(cc, el);
        }
    }

    public double getUrgency(CourseC course){
        double counter = 0;
        int needed =  course.hoursNeeded();
        ArrayList<PossibleRoom> ar = availabilities.get(course);

        if (ar.size() < needed) {
            counter = -needed;
            return counter;
        }
        for (int i = 0; i < course.hoursNeeded(); i++) {
            counter += Math.log(ar.get(i).numberOfPoss);
        }
        return counter;
    }

    public void updateUrgency(CourseC course){
        double newUrgency = getUrgency(course);
        urgency.put(course, newUrgency);
        heapElements.get(course).update(course);
    }

    public void calculate() throws EmptyHeapException {

        while(!courseHeap.isEmpty()){

            CourseC nextCourse = courseHeap.findMin().value();

            if(counter%1000==0) System.out.println("count: " + counter);
            counter++;
            if (availabilities.get(nextCourse).size() == 0 || hoursNeeded.get(nextCourse)==0) {
                //Vak onmogelijk te schedulen
                courseHeap.removeMin();
            }else {
                //Kies het volgende uur en lokaal om voor dit vak in te plannen
                //bijvoorbeeld uur met de meeste mogelijkheden
                TimeSlot nextHour = availabilities.get(nextCourse).get(0).lesuur;
                Classroom nextRoom = consideredRooms.get(nextCourse).stream()
                        .filter(availableRooms.get(nextHour)::contains)
                        .filter(room-> room.getCapaciteit()>=nextCourse.course.getAmountOfStudents())
                        .min(Comparator.comparingInt(Classroom::getCapaciteit)).get();
//                System.out.println(nextRoom);
                makeReservation(nextCourse,nextHour,nextRoom);
            }
        }

    }

    public void makeReservation(CourseC courseC, TimeSlot hour, Classroom room){
        Course course = courseC.course;
        timetable.add(new Reservation(course, room, hour, course.getAmountOfStudents()));
        hoursNeeded.put(courseC,hoursNeeded.get(courseC)-1);

        availableRooms.get(hour).remove(room);

        //Update de availabilities
        HashSet<CourseC> coursesToUpdate = new HashSet<>(coursesWantingHour.get(hour));

        for(CourseC cc: coursesToUpdate){
            if (cc.course.hasTeacherStudentsConflict(course)) {
                //cc cannot use this timeslot anymore
                coursesWantingHour.get(hour).remove(cc);
                removePossibleHour(cc, hour);
                updateUrgency(cc);
            }else{
                removePossibleRoom(cc,hour,room);
            }
        }

        if(hoursNeeded.get(courseC)==0){
            for (TimeSlot s : allHours) {
                coursesWantingHour.get(s).remove(courseC);
            }
        }
    }

    private void removePossibleHour(CourseC course, TimeSlot hour) {
        ArrayList<PossibleRoom> possibleRooms = availabilities.get(course);
        Iterator<PossibleRoom> iterator = possibleRooms.iterator();
        boolean found = false;
        PossibleRoom pr;
        while(!found && iterator.hasNext()){
            pr = iterator.next();
            if(pr.lesuur.equals(hour)){
                found = true;
            }
        }

        if (!found) {
            throw new IllegalStateException("ergens zit een fout");
        }
        iterator.remove();
        //andere volgorde blijft bewaard
    }

    private void removePossibleRoom(CourseC course, TimeSlot hour, Classroom room){
        ArrayList<PossibleRoom> possibleRooms = availabilities.get(course);
        Iterator<PossibleRoom> iterator = possibleRooms.iterator();
        boolean found = false;
        int i = -1;
        PossibleRoom pr = null;
        while(!found && iterator.hasNext()){
            pr = iterator.next();
            i++;
            if(pr.lesuur.equals(hour)){
                found = true;
            }
        }

        if (!found) {
            System.out.println(course.course);
            throw new IllegalStateException("ergens zit een fout");
        }


        int newRoomsnumber = getRoomsFittingCourse(course, hour);
        int oldRoomsnumber = pr.numberOfPoss;

        if (newRoomsnumber == 0) {
            //TODO
            coursesWantingHour.get(hour).remove(course);
            iterator.remove();
        }else{
            pr.numberOfPoss = newRoomsnumber;
            int newPos = fixOrder(possibleRooms,i,newRoomsnumber);
            updateUrgency(course);
        }
    }

    private int getRoomsFittingCourse(CourseC course, TimeSlot hour) {
        return (int) consideredRooms.get(course).stream()
                .filter(availableRooms.get(hour)::contains)
                .filter(room -> room.getCapaciteit() >= course.course.getAmountOfStudents()).count();
    }

    private int fixOrder(ArrayList<PossibleRoom> list, int index, int newValue){
        boolean ok = false;
        while (!ok) {

            if (index > 0 && list.get(index - 1).numberOfPoss < newValue) {
                Collections.swap(list, index - 1, index);
                index--;
            } else if (index < list.size() - 1 && list.get(index + 1).numberOfPoss > newValue) {
                Collections.swap(list, index, index + 1);
                index++;
            } else {
                ok = true;
            }
        }
        return index;
    }

    public Collection<JsonReservation> toJsonReservations(){
        return timetable.stream().map(Reservation::toJsonReservation).collect(Collectors.toSet());
    }


    public static class PossibleRoom {
        private TimeSlot lesuur;
        private int numberOfPoss;

        public PossibleRoom(TimeSlot lesuur, int number) {
            this.lesuur = lesuur;
            numberOfPoss = number;
        }

        @Override
        public String toString() {
            return "(uur: " + lesuur.getHour() + ", nr: " + numberOfPoss + ")";
        }
    }

    public class CourseC implements Comparable<CourseC>{
        public Course course;

        public CourseC(Course c){
            course = c;
        }

        @Override
        public int compareTo(CourseC o) {
            return urgency.get(this).compareTo(urgency.get(o));
        }

        public int hoursNeeded(){
            return hoursNeeded.get(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CourseC courseC = (CourseC) o;
            return course.equals(courseC.course);
        }

        @Override
        public int hashCode() {
            return Objects.hash(course);
        }

        @Override
        public String toString() {
            return course.toString();
        }
    }

    public static void main(String[] args) throws EmptyHeapException {
        JsonReader js = new JsonReader();
        JsonModel model = js.readJson("project.json");
        ContraintGreedy algo = new ContraintGreedy(model);
        algo.initialize();
        algo.calculate();
        new JsonWriter().writeJsonModels(algo.toJsonReservations(), "resultConstraint.json");
    }

}

