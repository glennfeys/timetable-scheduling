package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

public class JsonWriter {
    public void writeJsonModels(Collection jsonReservations, String fileName) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName);
            gson.toJson(jsonReservations, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
