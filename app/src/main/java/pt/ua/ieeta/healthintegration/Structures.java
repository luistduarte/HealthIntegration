package pt.ua.ieeta.healthintegration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Structures {


    private HashMap< Integer, JSONObject> dataSessionNumber;
    private HashMap< String, HashMap< Integer, JSONObject>> dataSessionType;
    private HashMap< Integer, HashMap< String, HashMap< Integer, JSONObject>>> allDataBySession;


    public Structures(){
        allDataBySession  = new HashMap<>();
    }


    public void fillDataWithHR(String data) {
        try {
            JSONArray fromServerHR = new JSONArray(data);

            int hrDataLength = fromServerHR.length();
            int currentSession = 0;

            if (hrDataLength > 0) {
                currentSession = fromServerHR.getJSONObject(0).getJSONObject("body").getJSONObject("heart_rate").getInt("session");
            }
            dataSessionNumber = new HashMap<>();
            dataSessionType = new HashMap<>();

            for (int i = 0; i<hrDataLength; i++) {
                JSONObject dataPoint = fromServerHR.getJSONObject(i);
                JSONObject body = dataPoint.getJSONObject("body");
                int current_number = body.getJSONObject("heart_rate").getInt("part_number");

                if (current_number == 1 && !dataSessionNumber.isEmpty()) {
                    dataSessionType = new HashMap<>();

                    if (getAllDataBySession().containsKey(currentSession)){
                        dataSessionType = getAllDataBySession().get(currentSession);
                        dataSessionType.put("heart-rate", dataSessionNumber);
                        getAllDataBySession().remove(currentSession);
                        getAllDataBySession().put(currentSession, dataSessionType);
                    } else {
                        dataSessionType.put("heart-rate", dataSessionNumber);
                        getAllDataBySession().put(currentSession, dataSessionType);
                    }
                    dataSessionNumber = new HashMap<>();

                    currentSession = body.getJSONObject("heart_rate").getInt("session");
                    dataSessionNumber.put(current_number, body);

                } else {
                    dataSessionNumber.put(current_number, body);
                }

            }
            dataSessionType = new HashMap<>();
            if (hrDataLength > 0){
                if (getAllDataBySession().containsKey(currentSession)){
                    dataSessionType = getAllDataBySession().get(currentSession);
                    dataSessionType.put("heart-rate", dataSessionNumber);
                    getAllDataBySession().remove(currentSession);
                    getAllDataBySession().put(currentSession, dataSessionType);
                } else {
                    dataSessionType.put("heart-rate", dataSessionNumber);
                    getAllDataBySession().put(currentSession, dataSessionType);
                }
            }


            System.out.println("all filled"+ getAllDataBySession().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void fillDataWithACC(String data) {
        try {
            System.out.println("ALL DATA "+ getAllDataBySession());
            JSONArray fromServerACC = new JSONArray(data);

            int accDataLength = fromServerACC.length();
            int currentSession = 0;

            if (accDataLength > 0) {
                currentSession = fromServerACC.getJSONObject(0).getJSONObject("body").getJSONObject("accelerometer").getInt("session");
            }
            dataSessionNumber = new HashMap<>();
            dataSessionType = new HashMap<>();

            for (int i = 0; i<accDataLength; i++) {
                JSONObject dataPoint = fromServerACC.getJSONObject(i);
                JSONObject body = dataPoint.getJSONObject("body");
                int current_number = body.getJSONObject("accelerometer").getInt("part_number");

                if (current_number == 1 && !dataSessionNumber.isEmpty()) {
                    dataSessionType = new HashMap<>();

                    if (getAllDataBySession().containsKey(currentSession)){
                        dataSessionType = getAllDataBySession().get(currentSession);
                        dataSessionType.put("accelerometer", dataSessionNumber);
                        getAllDataBySession().remove(currentSession);
                        getAllDataBySession().put(currentSession, dataSessionType);
                    } else {
                        dataSessionType.put("accelerometer", dataSessionNumber);
                        getAllDataBySession().put(currentSession, dataSessionType);
                    }
                    dataSessionNumber = new HashMap<>();

                    currentSession = body.getJSONObject("accelerometer").getInt("session");
                    dataSessionNumber.put(current_number, body);

                } else {
                    dataSessionNumber.put(current_number, body);
                }

            }
            dataSessionType = new HashMap<>();
            if (accDataLength > 0){
                if (getAllDataBySession().containsKey(currentSession)){
                    dataSessionType = getAllDataBySession().get(currentSession);
                    dataSessionType.put("accelerometer", dataSessionNumber);
                    getAllDataBySession().remove(currentSession);
                    getAllDataBySession().put(currentSession, dataSessionType);
                } else {
                    dataSessionType.put("accelerometer", dataSessionNumber);
                    getAllDataBySession().put(currentSession, dataSessionType);
                }

            }

            System.out.println("all filled"+ getAllDataBySession().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void fillDataWithECG(String data) {
        try {
            JSONArray fromServerECG = new JSONArray(data);

            int ecgDataLength = fromServerECG.length();
            int currentSession = 0;

            if (ecgDataLength > 0) {
                currentSession = fromServerECG.getJSONObject(0).getJSONObject("body").getJSONObject("ecg").getInt("session");
            }
            dataSessionNumber = new HashMap<>();
            dataSessionType = new HashMap<>();

            for (int i = 0; i<ecgDataLength; i++) {
                JSONObject dataPoint = fromServerECG.getJSONObject(i);
                JSONObject body = dataPoint.getJSONObject("body");
                int current_number = body.getJSONObject("ecg").getInt("part_number");

                if (current_number == 1 && !dataSessionNumber.isEmpty()) {
                    dataSessionType = new HashMap<>();

                    if (getAllDataBySession().containsKey(currentSession)){
                        dataSessionType = getAllDataBySession().get(currentSession);
                        dataSessionType.put("ecg", dataSessionNumber);
                        getAllDataBySession().remove(currentSession);
                        getAllDataBySession().put(currentSession, dataSessionType);
                    } else {
                        dataSessionType.put("ecg", dataSessionNumber);
                        getAllDataBySession().put(currentSession, dataSessionType);
                    }
                    dataSessionNumber = new HashMap<>();

                    currentSession = body.getJSONObject("ecg").getInt("session");
                    dataSessionNumber.put(current_number, body);

                } else {
                    dataSessionNumber.put(current_number, body);
                }
            }
            dataSessionType = new HashMap<>();
            if (ecgDataLength > 0) {
                if (getAllDataBySession().containsKey(currentSession)){
                    dataSessionType = getAllDataBySession().get(currentSession);
                    dataSessionType.put("ecg", dataSessionNumber);
                    getAllDataBySession().remove(currentSession);
                    getAllDataBySession().put(currentSession, dataSessionType);
                } else {
                    dataSessionType.put("ecg", dataSessionNumber);
                    getAllDataBySession().put(currentSession, dataSessionType);
                }
            }

            System.out.println("ecg all filled"+ getAllDataBySession().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, HashMap<String, HashMap<Integer, JSONObject>>> getAllDataBySession() {
        return allDataBySession;
    }

}