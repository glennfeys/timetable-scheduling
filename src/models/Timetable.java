package models;

import java.util.ArrayList;

public interface Timetable {
    ArrayList<JsonReservation> toJsonReservations();
}


