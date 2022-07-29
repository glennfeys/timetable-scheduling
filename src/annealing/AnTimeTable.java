package annealing;

import json.JsonModel;
import models.*;

import java.util.*;

public class AnTimeTable {
    private JsonModel model;
    private Random random = new Random();

    private double score;

    private double lateUrenKost;
    private double afstandKost;
    private double homeSiteKost;

    private ArrayList<ArrayList<HashSet<Reservation>>> reservaties;

    private HashMap<Integer, HashMap<Program, Integer>> notHomeCounter;

    private static int days = 1*5;
    private static int hours = 8;

    public AnTimeTable(JsonModel model){
        this.model = model;
        reservaties = new ArrayList<>();
        for (int i = 0; i < 13*5; i++) {
            reservaties.add(new ArrayList<>());
            for (int j = 0; j < hours; j++) {
                reservaties.get(i).add(new HashSet<>());
            }
        }
        notHomeCounter = new HashMap<>();
        for (int i = 0; i < days; i++) {
            notHomeCounter.put(i, new HashMap<>());
            for (Program program : model.getPrograms()) {
                notHomeCounter.get(i).put(program,0);
            }
        }
        score = 0;
    }

    public AnTimeTable(JsonModel model, TimetableGreedy greedy){
        this(model);
        for (int i = 0; i < days; i++) {
            reservaties.add(new ArrayList<>());
            for (int j = 0; j < hours; j++) {
                reservaties.get(i).set(j,greedy.getReservations(new TimeSlot(i/5,i-(i/5)*5,j)));
            }
        }
        setScores();
    }

    public AnTimeTable(JsonModel model, ArrayList<JsonReservation> jsons){
        this(model);
        for (JsonReservation res : jsons) {
            if(res.day()>5 || res.hour()>9){
                continue;
            }
            Reservation reservation = new Reservation(model.getCourseById(res.getCode()),
                    model.getRoomById(res.getLokaal()),null,res.getAantal()
            );
            reservaties.get(res.day()).get(res.hour()).add(reservation);
        }
        setScores();
    }

    public AnTimeTable(AnTimeTable timeTable) {
        this(timeTable.model);
        score = timeTable.score;
        lateUrenKost = timeTable.lateUrenKost;
        afstandKost = timeTable.afstandKost;
        homeSiteKost = timeTable.homeSiteKost;

        for (int i = 0; i < days; i++) {
            reservaties.add(new ArrayList<>());
            for (int j = 0; j < hours; j++) {
                reservaties.get(i).set(j,
                        timeTable.reservaties.get(i).get(j));
            }
        }

        for (int i = 0; i < days; i++) {
            notHomeCounter.put(i, new HashMap<>(timeTable.notHomeCounter.get(i)));
        }
    }

    private void setScores(){
        afstandKost = calculateDistanceCost();
        homeSiteKost = calculateHomeCost();

        for (int i = 0; i < days; i++) {
            for (Program program : model.getPrograms()) {
                notHomeCounter.get(i).put(program, countNotInHome(program, i));
            }
        }

        lateUrenKost = calculateLateUrenCost();
        score = model.getKilometerpenalty() * afstandKost + homeSiteKost*model.getNothomepenalty() + lateUrenKost*model.getLateurenkost();
    }

    private double calculateLateUrenCost() {
        double c = 0;
        for (int i = 0; i < days; i++) {
            for (int j = JsonModel.lateHours; j < hours; j++) {
                c += reservaties.get(i).get(j).size();
            }
        }
        //inhaal week kost: voor volledige lesroosters
//        for (int i = days-5; i < days; i++) {
//            for (int j = 0; j < JsonModel.lateHours; j++) {
//                c += reservaties.get(i).get(j).size();
//            }
//        }
        return c;
    }

    public double getScore() {
        return score;
    }

    public int amountOfReservations(){
        return (int) reservaties.stream().flatMap(Collection::stream).flatMap(Collection::stream).count();
    }

    public double calculateHomeCost() {
        double cost = 0;
        for (Program program : model.getPrograms()) {
            for (int i = 0; i < days; i++) {
                if (countNotInHome(program, i)>0) {
                    cost++;
                }
            }
        }
        return cost;
    }

    public double calculateDistanceCost(){
        double cScore = 0;
        for (int i = 0; i < days; i++) {
            for (int j = 1; j < hours; j++) {
                cScore += overgangsKost(i,j,reservaties.get(i).get(j-1));
            }
        }
        return cScore;
    }


    private double programCost(Program p, int day, int hour) {
        double c = 0;
        for (Reservation reservation : reservaties.get(day).get(hour)) {
            if (reservation.getCourse().getPrograms().contains(p)) {
                for (Reservation res2 : reservaties.get(day).get(hour+1)) {
                    if (res2.getCourse().getPrograms().contains(p)) {
                        c += reservation.getClassroom().getSite().distanceTo(res2.getClassroom().getSite());
                    }
                }
            }
        }
        return c;
    }

    public int countNotInHome(Program program, int day) {
        int notHome = 0;
        for (int j = 0; j < hours; j++) {
            for (Reservation reservation : reservaties.get(day).get(j)) {
                if (reservation.getCourse().getPrograms().contains(program)
                        && !reservation.isInHomeSite(program)) {
                    notHome++;
                }
            }
        }
        return notHome;
    }

    Permutation randomPerm(double randomD){
        Moment m1 = new Moment(random.nextInt(days), random.nextInt(hours));
        Moment m2 = new Moment(random.nextInt(days), random.nextInt(hours));

        return new HourPermutation(m1,m2);
    }

    //Assuming travel cost is totally symmetric
    public double overgangsKost(int dag, int uur, HashSet<Reservation> nextReservations){
        double kost = 0;
        //HashSet<Program> nextPrograms = nextReservations.stream().map(Reservation::getCourse)
        //        .flatMap(c->c.getPrograms().stream()).collect(Collectors.toCollection(HashSet::new));
        HashSet<Reservation> reservaties1 = reservaties.get(dag).get(uur);
        for (Reservation reservatie: reservaties1){
            for(Program program: reservatie.getCourse().getPrograms()){
                //if(nextPrograms.contains(program)){
                //zoek de reservatie
                for (Reservation nextReservation : nextReservations) {
                    if(nextReservation.getCourse().getPrograms().contains(program)){
                        kost += reservatie.getClassroom().getSite().distanceTo(nextReservation.getClassroom().getSite());
                    }
                }
                //}
            }
        }
        return kost;
    }

    public static class HourPermutation implements Permutation{
        private Moment hour1;
        private Moment hour2;
        private double scoreDiff;
        private double moveCostDiff = 0;
        private double lateCostDiff = 0;
        private double homeCostDiff = 0;

        public HourPermutation(Moment hour1, Moment hour2) {
            this.hour1 = hour1;
            this.hour2 = hour2;
        }

        @Override
        public double scoreDifference(AnTimeTable timeTable) {
            if(hour1.equals(hour2)){
                scoreDiff = 0;
                return 0;
            }

            double currentMoveCost = moveCost(hour1,timeTable,timeTable.reservaties.get(hour1.day()).get(hour1.hour()));
            currentMoveCost += moveCost(hour2,timeTable,timeTable.reservaties.get(hour2.day()).get(hour2.hour()));
            double newMoveCost = moveCost(hour2,timeTable,timeTable.reservaties.get(hour1.day()).get(hour1.hour()));
            newMoveCost += moveCost(hour1,timeTable,timeTable.reservaties.get(hour2.day()).get(hour2.hour()));

            //correct double counting for consecutive hours
            if(hour1.day()==hour2.day() && Math.abs(hour1.hour()-hour2.hour())==1){
                int m = Math.max(hour1.hour(),hour2.hour());
                currentMoveCost -= 2 * timeTable.overgangsKost(hour1.day(),m,timeTable.reservaties.get(hour1.day()).get(m-1));
            }
            moveCostDiff = newMoveCost-currentMoveCost;

            if(hour1.day()==hour2.day()){
                homeCostDiff = 0;
            }else{
                homeCostDiff = 0;
                reviewNotHomeCost(timeTable,hour1,hour2);
                reviewNotHomeCost(timeTable,hour2,hour1);
            }

            if(hour1.day()>=JsonModel.lateDay || hour1.hour()>=JsonModel.lateHours){
                if(hour2.day()>=JsonModel.lateDay || hour2.hour()>=JsonModel.lateHours){
                    lateCostDiff = 0;
                }else{
                    lateCostDiff = timeTable.reservaties.get(hour2.day()).get(hour2.hour()).size()
                            - timeTable.reservaties.get(hour1.day()).get(hour1.hour()).size();
                }
            }else if(hour2.day()>=JsonModel.lateDay || hour2.hour()>=JsonModel.lateHours) {
                lateCostDiff = timeTable.reservaties.get(hour1.day()).get(hour1.hour()).size()
                        - timeTable.reservaties.get(hour2.day()).get(hour2.hour()).size();
            }

            scoreDiff = homeCostDiff* timeTable.model.getNothomepenalty() + moveCostDiff*timeTable.model.getKilometerpenalty() + lateCostDiff* timeTable.model.getLateurenkost();
            return scoreDiff;
        }

        public void reviewNotHomeCost(AnTimeTable timeTable, Moment hourStart, Moment hourEnd){
            for (Reservation reservation : timeTable.reservaties.get(hourStart.day()).get(hourStart.hour())) {
                for(Program program: reservation.getCourse().getPrograms()){
                    if (!reservation.isInHomeSite(program)) {
                        if (timeTable.notHomeCounter.get(hourEnd.day()).get(program) == 0) {
                            homeCostDiff++;
                        }
                        if (timeTable.notHomeCounter.get(hour1.day()).get(program) == 0) {
                            homeCostDiff--;
                        }
                    }
                }
            }
        }

        public void updateNotHomeCounts(AnTimeTable timeTable, Moment hourStart, Moment hourEnd){
            for (Reservation reservation : timeTable.reservaties.get(hourStart.day()).get(hourStart.hour())) {
                for(Program program: reservation.getCourse().getPrograms()){
                    if (!reservation.isInHomeSite(program)) {
                        timeTable.notHomeCounter.get(hourEnd.day()).compute(program,(k,v)->v+1);
                        timeTable.notHomeCounter.get(hourStart.day()).compute(program,(k,v)->v-1);
                    }
                }
            }
        }

        /*
        Total cost moving from and to the given moment, with newRes Reservations on moment itself.
         */
        public double moveCost(Moment moment, AnTimeTable timeTable, HashSet<Reservation> newRes){
            double cost = 0;
            if (moment.hour() > 0) {
                cost += timeTable.overgangsKost(moment.day(),moment.hour()-1,newRes);
            }
            if(moment.hour()<7){
                cost += timeTable.overgangsKost(moment.day(),moment.hour()+1,newRes);
            }
            return cost;
        }

        public void permute(AnTimeTable timeTable) {
            updateNotHomeCounts(timeTable,hour1,hour2);
            updateNotHomeCounts(timeTable,hour2,hour1);
            //Swap
            HashSet<Reservation> temp = timeTable.reservaties.get(hour1.day()).get(hour1.hour());
            timeTable.reservaties.get(hour1.day()).set(hour1.hour(),
                    timeTable.reservaties.get(hour2.day()).get(hour2.hour()));
            timeTable.reservaties.get(hour2.day()).set(hour2.hour(),temp);

            timeTable.homeSiteKost += homeCostDiff;
            timeTable.lateUrenKost += lateCostDiff;
            timeTable.afstandKost += moveCostDiff;
            timeTable.score += scoreDiff;
        }
    }

    public ArrayList<JsonReservation> toJsonReservations() {
        ArrayList<JsonReservation> jsonReservations = new ArrayList<>();

        for (int i = 0; i < 5*13; i++) {
            for (int j = 0; j < hours; j++) {
                for(Reservation reservation: reservaties.get(i).get(j)){
                    TimeSlot timeSlot = new TimeSlot(i/5, i-(i/5)*5, j);
                    jsonReservations.add(new JsonReservation(
                            reservation.getClassroom().getFinummer(),
                            reservation.getCourse().getCode(),
                            reservation.getAmount(),
                            new ArrayList<>(Collections.singletonList(timeSlot.getDayName())),
                            new ArrayList<>(Collections.singletonList(timeSlot.getWeek()+1)),
                            new ArrayList<>(Collections.singletonList(j+1))
                    ));
                }
            }
        }

        return jsonReservations;
    }

    public void printScores(){
        System.out.println("distance cost: " + afstandKost);
        System.out.println("late hour cost: " + lateUrenKost);
        System.out.println("not home cost: " + homeSiteKost);
        System.out.println("total cost: " + score);
    }

    public void multiplyWeekby13() {
        int[] weeks = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        HashMap<Course, Double> hoursLeft = new HashMap<>();
        for (TimeSlot timeSloth : TimeSlot.weekTimeSlots()) {

            ArrayList<Reservation> reservations = new ArrayList<>(reservaties.get(timeSloth.getDay()).get(timeSloth.getHour()));
            int oldLength = reservations.size();
            for (int j = 0; j < oldLength; j++) {
                Reservation reservation = reservations.get(j);
                Course course = reservation.getCourse();
                if (!hoursLeft.containsKey(course)) hoursLeft.put(course, Math.ceil(course.getContacturenReal()));
                hoursLeft.put(course, hoursLeft.get(reservation.getCourse()) - 1);
                for (int i : weeks) {
                    if (hoursLeft.get(course) <= 0) break;
                    TimeSlot timeSlot = null;
                    Reservation reservation1 = new Reservation(course, reservation.getClassroom(), timeSlot, reservation.getAmount());

                    reservaties.get(i*5+timeSloth.getDay()).get(timeSloth.getHour()).add(reservation1);

                    hoursLeft.put(course, hoursLeft.get(reservation.getCourse()) - 1);
                }
            }
        }
        System.out.println("Done multiplying");
    }

}