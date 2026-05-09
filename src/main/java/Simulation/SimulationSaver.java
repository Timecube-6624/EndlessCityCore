package Simulation;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimulationSaver {
    
    private JSONObject saveData;
    private static final String SAVES_DIRECTORY = "src/main/resources/Saves";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public SimulationSaver() {
        saveData = new JSONObject();
        
        // MapRelatedData
        JSONObject mapRelatedData = new JSONObject();
        
        // BasicMapInformation
        JSONObject basicMapInfo = new JSONObject();
        basicMapInfo.put("MapName", "");
        basicMapInfo.put("MapCreateDate", "");
        basicMapInfo.put("MapCreator", "");
        basicMapInfo.put("CurrentMapSeason", "");
        basicMapInfo.put("MapRegion", "");
        mapRelatedData.put("BasicMapInformation", basicMapInfo);
        
        // TerrainData
        JSONObject terrainData = new JSONObject();
        terrainData.put("CurrentMapContourLineData", "");
        terrainData.put("CurrentRiverBankData", "");
        terrainData.put("CurrentVegetationData", "");
        terrainData.put("CurrentWindDirectionData", "");
        terrainData.put("CurrentTemperatureData", "");
        mapRelatedData.put("TerrainData", terrainData);
        
        // HydrologyData
        JSONObject hydrologyData = new JSONObject();
        hydrologyData.put("CurrentRiverFlowingSpeed", "");
        hydrologyData.put("CurrentRiverPollution", "");
        hydrologyData.put("CurrentRiverTemperature", "");
        hydrologyData.put("OceanCurrentData", "");
        hydrologyData.put("CurrentOceanTemperatureData", "");
        mapRelatedData.put("HydrologyData", hydrologyData);
        
        saveData.put("MapRelatedData", mapRelatedData);
        
        // CreatureData
        JSONObject creatureData = new JSONObject();
        
        // CitizenData
        JSONObject citizenData = new JSONObject();
        
        // CitizenBasicData
        JSONObject citizenBasicData = new JSONObject();
        citizenBasicData.put("CitizenID", "");
        citizenBasicData.put("CitizenName", "");
        citizenBasicData.put("CitizenSex", "");
        citizenBasicData.put("CurrentCitizenAges", "");
        citizenBasicData.put("CurrentCitizenPersonality", "");
        citizenBasicData.put("CurrentCitizenHeight", "");
        citizenBasicData.put("CurrentCitizenWeight", "");
        citizenData.put("CitizenBasicData", citizenBasicData);
        
        // CitizenSocietyData
        JSONObject citizenSocietyData = new JSONObject();
        citizenSocietyData.put("CurrentCitizenEmployment", "");
        citizenSocietyData.put("CurrentCitizenEducationalBackground", "");
        citizenSocietyData.put("CurrentCitizenFamily", "");
        citizenSocietyData.put("CurrentCitizenDisease", "");
        citizenData.put("CitizenSocietyData", citizenSocietyData);
        
        // EntityData
        JSONObject entityData = new JSONObject();
        entityData.put("CurrentNaturalCreature", "");
        entityData.put("CurrentVegetationGrow", "");
        citizenData.put("EntityData", entityData);
        
        creatureData.put("CitizenData", citizenData);
        saveData.put("CreatureData", creatureData);
        
        // ObjectData
        JSONObject objectData = new JSONObject();
        
        // RoadRelatedData
        JSONObject roadRelatedData = new JSONObject();
        roadRelatedData.put("CurrentRoadLine", "");
        roadRelatedData.put("CurrentRodeNode", "");
        roadRelatedData.put("CurrentRoadType", "");
        roadRelatedData.put("CurrentRoadAllowVehicleType", "");
        objectData.put("RoadRelatedData", roadRelatedData);
        
        // BuildingRelatedData
        JSONObject buildingRelatedData = new JSONObject();
        objectData.put("BuildingRelatedData", buildingRelatedData);
        
        saveData.put("ObjectData", objectData);
    }
    
    public void setBasicMapInfo(String mapName, String creator, String region) {
        JSONObject basicInfo = saveData.getJSONObject("MapRelatedData").getJSONObject("BasicMapInformation");
        basicInfo.put("MapName", mapName);
        basicInfo.put("MapCreateDate", LocalDateTime.now().format(DATE_FORMATTER));
        basicInfo.put("MapCreator", creator);
        basicInfo.put("MapRegion", region);
    }
    
    public void setSeason(String season) {
        saveData.getJSONObject("MapRelatedData").getJSONObject("BasicMapInformation")
            .put("CurrentMapSeason", season);
    }
    
    public void setTerrainData(String contourLine, String riverBank, String vegetation,
                               String windDirection, String temperature) {
        JSONObject terrain = saveData.getJSONObject("MapRelatedData").getJSONObject("TerrainData");
        terrain.put("CurrentMapContourLineData", contourLine);
        terrain.put("CurrentRiverBankData", riverBank);
        terrain.put("CurrentVegetationData", vegetation);
        terrain.put("CurrentWindDirectionData", windDirection);
        terrain.put("CurrentTemperatureData", temperature);
    }
    
    public void setHydrologyData(String riverSpeed, String riverPollution, String riverTemp,
                                 String oceanCurrent, String oceanTemp) {
        JSONObject hydrology = saveData.getJSONObject("MapRelatedData").getJSONObject("HydrologyData");
        hydrology.put("CurrentRiverFlowingSpeed", riverSpeed);
        hydrology.put("CurrentRiverPollution", riverPollution);
        hydrology.put("CurrentRiverTemperature", riverTemp);
        hydrology.put("OceanCurrentData", oceanCurrent);
        hydrology.put("CurrentOceanTemperatureData", oceanTemp);
    }
    
    public void setCitizenBasicData(String citizenId, String name, String sex, String age,
                                    String personality, String height, String weight) {
        JSONObject citizen = saveData.getJSONObject("CreatureData").getJSONObject("CitizenData");
        JSONObject basic = citizen.getJSONObject("CitizenBasicData");
        basic.put("CitizenID", citizenId);
        basic.put("CitizenName", name);
        basic.put("CitizenSex", sex);
        basic.put("CurrentCitizenAges", age);
        basic.put("CurrentCitizenPersonality", personality);
        basic.put("CurrentCitizenHeight", height);
        basic.put("CurrentCitizenWeight", weight);
    }
    
    public void setCitizenSocietyData(String employment, String education, String family, String disease) {
        JSONObject citizen = saveData.getJSONObject("CreatureData").getJSONObject("CitizenData");
        JSONObject society = citizen.getJSONObject("CitizenSocietyData");
        society.put("CurrentCitizenEmployment", employment);
        society.put("CurrentCitizenEducationalBackground", education);
        society.put("CurrentCitizenFamily", family);
        society.put("CurrentCitizenDisease", disease);
    }
    
    public void setEntityData(String naturalCreature, String vegetationGrow) {
        JSONObject citizen = saveData.getJSONObject("CreatureData").getJSONObject("CitizenData");
        JSONObject entity = citizen.getJSONObject("EntityData");
        entity.put("CurrentNaturalCreature", naturalCreature);
        entity.put("CurrentVegetationGrow", vegetationGrow);
    }
    
    public void setRoadData(String roadLine, String roadNode, String roadType, String allowVehicleType) {
        JSONObject road = saveData.getJSONObject("ObjectData").getJSONObject("RoadRelatedData");
        road.put("CurrentRoadLine", roadLine);
        road.put("CurrentRodeNode", roadNode);
        road.put("CurrentRoadType", roadType);
        road.put("CurrentRoadAllowVehicleType", allowVehicleType);
    }
    
    public String getJsonString() {
        return saveData.toString(4);
    }
    
    public JSONObject getSaveData() {
        return saveData;
    }
    
    public boolean saveToFile(String fileName) {
        CreateLogFile logger = CreateLogFile.getInstance();
        
        try {
            logger.log(CreateLogFile.LogLevel.INFO, "[SimulationSaver]: Starting save operation for file: " + fileName);
            
            File savesDir = new File(SAVES_DIRECTORY);
            if (!savesDir.exists()) {
                savesDir.mkdirs();
            }
            
            String filePath = SAVES_DIRECTORY + File.separator + fileName + ".json";
            File saveFile = new File(filePath);
            
            try (FileWriter writer = new FileWriter(saveFile)) {
                writer.write(getJsonString());
            }
            
            logger.log(CreateLogFile.LogLevel.INFO, "[SimulationSaver]: Save operation completed successfully");
            return true;
            
        } catch (IOException e) {
            logger.log(CreateLogFile.LogLevel.ERROR, "[SimulationSaver]: Failed to save file - " + e.getMessage());
            return false;
        }
    }
}