package models;

import java.util.Objects;

public class Classroom {
    private String naam;
    private String finummer;
    private int capaciteit;
    private String siteCode;
    private Site site;

    public Classroom(String naam, String finummer, int capaciteit) {
        this.naam = naam;
        this.finummer = finummer;
        this.capaciteit = capaciteit;
    }

    public String getNaam() {
        return naam;
    }

    public String getFinummer() {
        return finummer;
    }

    public int getCapaciteit() {
        return capaciteit;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public Site getSite(){
        return site;
    }

    public void setSite(Site s){
        site = s;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Classroom classroom = (Classroom) o;
        return capaciteit == classroom.capaciteit &&
                naam.equals(classroom.naam) &&
                finummer.equals(classroom.finummer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(naam, finummer, capaciteit);
    }

    @Override
    public String toString() {
        return naam;
    }
}
