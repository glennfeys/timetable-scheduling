package json;

import models.JsonReservation;

import java.util.ArrayList;

public class JsonResultModel {

    private ArrayList<JsonReservation> jsonReservations;

    private static JsonResultModel instance;

    public static JsonResultModel getInstance() {
        return instance;
    }

    public static void setInstance(JsonResultModel instance) {
        JsonResultModel.instance = instance;
    }

    public ArrayList<JsonReservation> getJsonReservations() {
        return jsonReservations;
    }

    public void setJsonReservations(ArrayList<JsonReservation> jsonReservations) {
        this.jsonReservations = jsonReservations;
    }
}
