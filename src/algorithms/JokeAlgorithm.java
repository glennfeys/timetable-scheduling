package algorithms;

import json.JsonModel;
import json.JsonReader;
import json.JsonWriter;
import models.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

/*
Een algoritme dat misbruik maakt van hoe de score in het begin werd berekend op het scorebord.
 */
public class JokeAlgorithm {
    private ArrayList<Course> vakken;
    private ArrayList<Classroom> rooms;
    private ArrayList<Reservation> reservaties;
    private HashMap<Classroom, Course> roomreservations;
    private HashSet<Course> availableCourses;
    private HashSet<Classroom> usedRooms;
    private HashMap<String, Site> sites;

    public void bereken(JsonModel model){
        reservaties = new ArrayList<>();

        ArrayList<TimeSlot> schedulingHours = TimeSlot.regularTimeslots();
        vakken = new ArrayList<>(model.getVakken());
        vakken.sort(Comparator.comparing(Course::getAmountOfStudents).reversed());
        rooms = new ArrayList<>(model.getRooms());
        rooms.sort(Comparator.comparing(Classroom::getCapaciteit).reversed());
        roomreservations = new HashMap<>();
        availableCourses = new HashSet<>(vakken);
        usedRooms = new HashSet<>();
        sites = new HashMap<>();
        for (Site s : model.getSites()) {
            sites.put(s.getCode(), s);
        }


        while(availableCourses.size() > 0) {
            Course vak = availableCourses.stream().max(Comparator.comparingInt(Course::getAmountOfStudents)).get();
            System.out.println(vak);

            HashSet<Classroom> available;

            if(vak.getPrograms().size()==0){
                available = new HashSet<>(rooms);
                available.removeAll(usedRooms);
            }else{
                Site site = sites.get(vak.getPrograms().get(0).getHomesite());

                available = new HashSet<>(site.getClassrooms());
                available.removeAll(usedRooms);
            }


            if (available.size() == 0) {
                availableCourses.remove(vak);
            }else{
                Classroom room = available.stream().max(Comparator.comparingInt(Classroom::getCapaciteit)).get();

                for (TimeSlot slot : schedulingHours) {
                    reservaties.add(new Reservation(vak, room, slot, Math.min(room.getCapaciteit(),vak.getAmountOfStudents())));
                }

                usedRooms.add(room);

                for (Course anderVak : vakken) {
                    if(anderVak.hasTeacherStudentsConflict(vak)){
                        availableCourses.remove(anderVak);
                    }
                }
                availableCourses.remove(vak);


            }

        }
        HashSet<Classroom> c = new HashSet<>(rooms);
        c.removeAll(usedRooms);
        System.out.println("unused: " + c.size());
    }

    public ArrayList<JsonReservation> toJsonReservations(){
        return reservaties.stream().map(Reservation::toJsonReservation).collect(Collectors.toCollection(ArrayList::new));
    }


    public static void main(String[] args) {
        JsonReader js = new JsonReader();
        JsonModel model = js.readJson("project.json");
        model.init();
        JokeAlgorithm algo = new JokeAlgorithm();
        algo.bereken(model);
        new JsonWriter().writeJsonModels(algo.toJsonReservations(), "resultSteven.json");
    }
}
