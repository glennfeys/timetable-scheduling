package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import models.JsonReservation;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonReader {

    public JsonModel readJson(String fileName) {
        JsonModel jsonModel = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            Gson gson = new GsonBuilder().create();
            jsonModel = gson.fromJson(reader, JsonModel.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonModel.setInstance(jsonModel);
        return jsonModel;
    }

    public ArrayList<JsonReservation> readReservations(String fileName){

        try(BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            Type listType = new TypeToken<ArrayList<JsonReservation>>(){}.getType();
            List<JsonReservation> resList = new Gson().fromJson(reader, listType);
            return new ArrayList<>(resList);
            //wrapper = gson.fromJson(reader, ReservationsWrapper.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public JsonReservation[] readResult(String fileName) {
        JsonReservation[] res = null;
        try(BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            Gson gson = new GsonBuilder().create();
            res = gson.fromJson(reader, JsonReservation[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
