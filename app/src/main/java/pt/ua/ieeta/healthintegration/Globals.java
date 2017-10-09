package pt.ua.ieeta.healthintegration;

import org.json.JSONObject;

import java.util.HashMap;

public class Globals{
    private static Globals instance;

    // Global variable
    private String token;
    private String username;
    private String env = "omh";
    private HashMap< Integer, HashMap< String, HashMap< Integer, JSONObject>>> allDataBySession;
    private int currentSessionSelected;


    // Restrict the constructor from being instantiated
    private Globals(){}

    public void setData(String t){
        this.token=t;
    }
    public String getToken(){
        return this.token;
    }

    public String getUsername(){
        return this.username;
    }

    public void setUsername(String t){
        this.username=t;
    }

    public static synchronized Globals getInstance(){
        if(instance==null){
            instance=new Globals();
        }
        return instance;
    }

    public String getEnv() { return this.env; }
    public void setEnv(String t){
        this.env=t;
    }

    public HashMap<Integer, HashMap<String, HashMap<Integer, JSONObject>>> getAllDataBySession() {
        return allDataBySession;
    }

    public void setAllDataBySession(HashMap<Integer, HashMap<String, HashMap<Integer, JSONObject>>> allDataBySession) {
        this.allDataBySession = allDataBySession;
    }


    public int getCurrentSessionSelected() {
        return currentSessionSelected;
    }

    public void setCurrentSessionSelected(int currentSessionSelected) {
        this.currentSessionSelected = currentSessionSelected;
    }
}