package Simulation;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import org.json.JSONObject;

public class CitySettings {
    //此文件用于用户进行基础城市设定，对MainCtrl传入数据
    public static void main(String[] args){
        System.out.println("[CitySettings]Loading City Settings");
        System.out.println("[CitySettings]First let's choose a map for simulate");
        //加载地图列表等待用户选择地图，此页面需要适配GUI
        try {
            String content = new String(Files.readAllBytes(Paths.get("src/main/resources/Maps/MapLists.json")));
            java.io.File file = new java.io.File("src/main/resources/Maps/MapLists.json");
            if (!file.exists()) {
                System.out.println("[CitySettings]Map file does not exist");
                return;
            }
            CreateLogFile.getInstance().log("Successfully loaded MapLists.json");
            CreateLogFile.getInstance().flush();
            JSONObject root = new JSONObject(content);
            Set<String> keys = root.getJSONObject("Map").keySet();
            System.out.println("[CitySettings]" + keys);
            CreateLogFile.getInstance().log("[CitySettings]Load MapLists successfully");


        }catch (Exception e){
            System.out.println("[CitySettings]Failed to load MapLists.json");
        }
    }
}
