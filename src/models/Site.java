package models;

import java.util.ArrayList;
import java.util.HashMap;

public class Site {
    private String code;
    private String naam;
    private double xcoord;
    private double ycoord;
    private ArrayList<Classroom> lokalen;

    private HashMap<Site, Double> distances = new HashMap<>();

    public String getCode() {
        return code;
    }

    public String getName() {
        return naam;
    }

    public double getXcoord() {
        return xcoord;
    }

    public double getYcoord() {
        return ycoord;
    }

    public ArrayList<Classroom> getClassrooms() {
        return lokalen;
    }

    public double distanceTo(Site other){
        if(equals(other)){
            return 0;
        }
        return distances.get(other);
    }

    public void calculateDistances(ArrayList<Site> sites){
        for (Site s : sites) {
            //distances.put(s, 10* Math.sqrt(Math.pow(xcoord - s.getXcoord(),2) + Math.pow(ycoord - getYcoord(),2)));
            distances.put(s, Haversine.distance(xcoord, ycoord, s.getXcoord(), s.getYcoord()));
        }
    }

    public double distance(Site site) {
        return Haversine.distance(xcoord, ycoord, site.getXcoord(), site.getYcoord());
    }

    @Override
    public String toString() {
        return naam;
    }
}
