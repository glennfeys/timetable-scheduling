package models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JsonReservation {
    private String lokaal;
    private String code;
    private int aantal;
    private ArrayList<String> dagen;
    private ArrayList<Integer> weken;
    private ArrayList<Integer> uren;

    private static final List<String> days = List.of("ma", "di", "wo", "do", "vr");

    public JsonReservation(String lokaal, String code, int aantal, ArrayList<String> dagen, ArrayList<Integer> weken, ArrayList<Integer> uren) {
        this.lokaal = lokaal;
        this.code = code;
        this.aantal = aantal;
        this.dagen = dagen;
        this.weken = weken;
        this.uren = uren;
    }

    public String getLokaal() {
        return lokaal;
    }

    public void setLokaal(String lokaal) {
        this.lokaal = lokaal;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getAantal() {
        return aantal;
    }

    public void setAantal(int aantal) {
        this.aantal = aantal;
    }

    public ArrayList<String> getDagen() {
        return dagen;
    }

    public void setDagen(ArrayList<String> dagen) {
        this.dagen = dagen;
    }

    public ArrayList<Integer> getWeken() {
        return weken;
    }

    public void setWeken(ArrayList<Integer> weken) {
        this.weken = weken;
    }

    public ArrayList<Integer> getUren() {
        return uren;
    }

    public void setUren(ArrayList<Integer> uren) {
        this.uren = uren;
    }

    public int hour(){
        if(uren.size()>1){
            throw new IllegalStateException("meerdere uren");
        }
        return uren.iterator().next()-1;
    }

    public int day(){
        if(weken.size()>1){
            throw new IllegalStateException("meerdere uren");
        }
        int week = (weken.iterator().next()-1);
        int dag = days.indexOf(dagen.iterator().next());
        return week * 5 + dag;
    }
}
