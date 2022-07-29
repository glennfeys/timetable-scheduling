package adapters;

import json.JsonModel;
import json.JsonResultModel;
import models.JsonReservation;
import models.TimetableGreedy;
import models.TimetableOptimize;

public class JsonAdapter {
    public TimetableGreedy toGreedyTimetable(JsonModel jsonModel) {
        return new TimetableGreedy(jsonModel.getVakken(), jsonModel.getSites());
    }
}
