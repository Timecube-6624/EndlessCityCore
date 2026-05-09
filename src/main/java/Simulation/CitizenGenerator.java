package Simulation;

import org.json.JSONObject;
import java.io.IOException;
import org.json.JSONArray;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CitizenGenerator { //CitizenGenerator
    public static String SetRegion;//从Gui获得用户选择的地区
    public static void main(String[] args) {
        try {
            //read citizen name repository prepare for random generate citizen name

            //Read NameRepository.json file to String
            String NameRepositoryJson = new String(Files.readAllBytes(Paths.get("src/main/resources/PersonalityResources/NameRepository.json")));
            //Create a log massage
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO ,"Successfully loaded NameRepository.json");
            CreateLogFile.getInstance().flush();

            List<String> allNames = new ArrayList<>();
            List<Double> allWeights = new ArrayList<>();
            //逐级向下获取
            SetRegion = "EastAsia";
            JSONObject root = new JSONObject(NameRepositoryJson);
            JSONObject region = root.getJSONObject("region");
            JSONObject EastAsia = region.getJSONObject(SetRegion);
            JSONObject China = EastAsia.getJSONObject("China");
            JSONObject firstNames = China.getJSONObject("firstNames");
            //JSONObject FirstnameBlockA1 = firstNames.getJSONObject("A-1");//是否可以添加多个
            //JSONArray NameList = FirstnameBlockA1.getJSONArray("NameList");
            //JSONArray weights = FirstnameBlockA1.getJSONArray("weights");

            for (String key : firstNames.keySet()) {
                JSONObject block = firstNames.getJSONObject(key);
                System.out.println("[CitizenGenerator]:Processing:" + key);
                CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO ,"[CitizenGenerator]:Processing:" + key);
                System.out.println("[CitizenGenerator]:Keys under this key:" + block.keySet());
                CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO ,"[CitizenGenerator]:Keys under this key:" + block.keySet());
                JSONArray NameList = block.getJSONArray("NameList");
                JSONArray weights =block.getJSONArray("weights");
                //校验长度是否一致
                if (NameList.length() != weights.length()) {
                    CreateLogFile.getInstance().log(CreateLogFile.LogLevel.WARN ,"[CitizenGenerator] Name list lengths do not match");
                    throw new IllegalArgumentException("[CitizenGenerator] Name list lengths do not match");

                }
                for (int i = 0; i < NameList.length(); i++) {
                    allNames.add(NameList.getString(i));
                    allWeights.add(weights.getDouble(i));
                }
            }

            JSONArray mergedNames = new JSONArray(allNames);
            JSONArray mergedWeights = new JSONArray(allWeights);

            String selected = pickByNameAndWeights(mergedNames, mergedWeights);
            System.out.println("FirstName:" + selected);
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO ,"[CitizenGenerator]:FirstName:" + selected);
            //read name repository to a 
            JSONObject NameRepository = new JSONObject(NameRepositoryJson);
            //pick a first name randomly

        } catch (IOException e){
            e.printStackTrace();
        }





        try {
            //read and write generated citizen data

            //read CitizenData.json file to String
            String content = new String(Files.readAllBytes(Paths.get("src/main/resources/PersonalityResources/CitizenData")));
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO,"Successfully loaded CitizenData.json");
            CreateLogFile.getInstance().flush();
            //Create JSONObject from String
            JSONObject obj = new JSONObject(content);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String pickByNameAndWeights(JSONArray NameList, JSONArray weight){
        if (NameList.length() != weight.length()){//检查权重与姓氏数量是否匹配
            throw new IllegalArgumentException("[CitizenGenerator]:Names and Weights don't match");
        }
        int length = NameList.length();
        //计算总共权重
        double totalWeight = 0.0;
        for (int i = 0; i < length; i++) {
            totalWeight += weight.getDouble(i);
        }
        //生成[0,totalWeight)随机数
        Random random = new Random();
        double randomValue = random.nextDouble()*totalWeight;
        //累加权重，确定选中索引
        double cumulative = 0.0;
        for (int i = 0; i < length; i++) {
            cumulative += weight.getDouble(i);
            if (randomValue < cumulative){
                return NameList.getString(i);
            }
        }
        return NameList.getString(length-1);
    }


}
