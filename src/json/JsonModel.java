package json;

import models.*;

import java.util.ArrayList;
import java.util.HashSet;

public class JsonModel {
    private ArrayList<Course> vakken;
    private ArrayList<Site> sites;
    private double nothomepenalty;
    private double kilometerpenalty;
    private double lateurenkost;
    private int minimaalStudentenaantal;

    public static int lateHours = 6;
    public static int lateDay = 12*5;

    private HashSet<Teacher> teachers;
    private HashSet<Classroom> rooms;
    private HashSet<Program> programs;

    private static JsonModel instance;

    public static JsonModel getInstance() {
        return instance;
    }

    public static void setInstance(JsonModel instance) {
        JsonModel.instance = instance;
    }

    public ArrayList<Course> getVakken() {
        return vakken;
    }

    public ArrayList<Site> getSites() {
        return sites;
    }

    public double getNothomepenalty() {
        return nothomepenalty;
    }

    public double getKilometerpenalty() {
        return kilometerpenalty;
    }

    public double getLateurenkost() {
        return lateurenkost;
    }

    public int getMinimaalStudentenaantal() {
        return minimaalStudentenaantal;
    }

    public void init(){
        teachers = new HashSet<>();
        rooms = new HashSet<>();
        programs = new HashSet<>();

        for(Course course: getVakken()){
            teachers.addAll(course.getTeachers());
            programs.addAll(course.getPrograms());
        }

        for (Site s : sites) {
            rooms.addAll(s.getClassrooms());
            for (Classroom r : s.getClassrooms()) {
                r.setSite(s);
            }
            s.calculateDistances(sites);
        }
    }

    public HashSet<Teacher> getTeachers(){
        return teachers;
    }
    public HashSet<Classroom> getRooms() {
        return rooms;
    }
    public HashSet<Program> getPrograms() {
        return programs;
    }

    public Course getCourseById(String id){
        for (Course c : vakken) {
            if(c.getCode().equals(id)){
                return c;
            }
        }
        throw new IllegalArgumentException("course bestaat niet");
//        return null;
    }

    public Classroom getRoomById(String id) {
        for (Classroom r : rooms) {
            if (r.getFinummer().equals(id)) {
                return r;
            }
        }
        throw new IllegalArgumentException("room bestaat niet");
    }

    public Site getSiteByCode(String code){
        for (Site site: sites) {
            if (site.getCode().equals(code)) {
                return site;
            }
        }
        return null;
    }

    public Program getProgramByCode(String code, int mt){
        for (Program program: programs) {
            if (program.getCode().equals(code) && program.getMt1() == mt) {
                return program;
            }
        }
        return null;
    }

    public Course getCourseByCode(String str) {
        for (Course course : vakken) {
            if (course.getCode().equals(str)) {
                return course;
            }
        }
        return null;
    }

    public Classroom getClassroomByCode(String str) {
        for (Site site : sites) {
            for (Classroom classroom : site.getClassrooms()) {
                if (classroom.getFinummer().equals(str)) {
                    classroom.setSiteCode(site.getCode());
                    return classroom;
                }
            }

        }
        return null;
    }

}
