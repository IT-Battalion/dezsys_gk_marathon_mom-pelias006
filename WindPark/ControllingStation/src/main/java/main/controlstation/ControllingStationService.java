package main.controlstation;

import main.model.TimingstationData;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service("controllingStationService")
public class ControllingStationService {
    private static final HashMap<String, TimingstationData> data = new HashMap<>();

    public void updateData(TimingstationData timingstationData) {
        data.put(timingstationData.getTimingstationID(), timingstationData);
    }

    public TimingstationData getTimingstationData(String inTimingstationID) {
        return data.get(inTimingstationID);
    }

    public HashMap<String, TimingstationData> getData() {
        return data;
    }
}
