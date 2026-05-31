package Geo;

import Simulation.CreateLogFile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GeoJsonImportor {
    //定义文件路径
    public static final String GEOJSON_DIRECTORY = "src/main/resources/Maps/GeoJsonFiles";
    public static final String HDF5_OUTPUT_DIRECTORY = "src/main/resources/Maps/Hdf5Files";

    public static void main(String[] args) {
        GeoJsonImportor importer = new GeoJsonImportor();

        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:=== GeoJsonImportor Initialization ===");
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Initialization started");
        ensureDirectoriesExist();
        //列出可用文件列表
        List<String> availableMaps = importer.listAvailableMaps();
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Available GeoJSON maps:");
        for (String map : availableMaps) {
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:  - " + map);
        }
    }

    public static void ensureDirectoriesExist() {
        try {
            Path geoJsonPath = Paths.get(GEOJSON_DIRECTORY);
            Path hdf5Path = Paths.get(HDF5_OUTPUT_DIRECTORY);
            //确定GeoJson文件是否被转化为HDF5文件
            if (!Files.exists(geoJsonPath)) {
                Files.createDirectories(geoJsonPath);
                CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Created GeoJSON directory: " + geoJsonPath.toAbsolutePath());
            }
            
            if (!Files.exists(hdf5Path)) {
                Files.createDirectories(hdf5Path);
                CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Created HDF5 directory: " + hdf5Path.toAbsolutePath());
            }
            
        } catch (IOException e) {
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.ERROR, "[GeoJsonImportor]:Failed to create directories: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<String> listAvailableMaps() {
        List<String> mapFiles = new ArrayList<>();

        try {//查找是否存在这样的文件
            Path dir = Paths.get(GEOJSON_DIRECTORY);
            if (!Files.exists(dir)) {
                CreateLogFile.getInstance().log(CreateLogFile.LogLevel.WARN, "[GeoJsonImportor]:GeoJSON directory does not exist");
                return mapFiles;
            }
            
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{geojson,json}")) {
                for (Path file : stream) {
                    mapFiles.add(file.getFileName().toString());
                }
            }
            
            Collections.sort(mapFiles);
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Listed " + mapFiles.size() + " map files");
        } catch (IOException e) {
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.ERROR, "[GeoJsonImportor]:Error listing map files: " + e.getMessage());
        }
        
        return mapFiles;
    }

    public List<FeatureData> loadMap(String fileName) throws Exception {
        ensureDirectoriesExist();
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Loading map: " + fileName);
        
        Path filePath = Paths.get(GEOJSON_DIRECTORY, fileName);
        
        if (!Files.exists(filePath)) {
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.ERROR, "[GeoJsonImportor]:Map file not found: " + filePath);
            throw new FileNotFoundException("Map file not found: " + filePath);
        }
        
        return readGeoJson(filePath.toString());
    }

    public List<FeatureData> readGeoJson(String fullPath) throws Exception {
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Reading GeoJSON from: " + fullPath);
        List<FeatureData> results = new ArrayList<>();
        
        File file = new File(fullPath);
        if (!file.exists()) {
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.ERROR, "[GeoJsonImportor]:GeoJSON file not found: " + fullPath);
            throw new FileNotFoundException("GeoJSON file not found: " + fullPath);
        }
        
        try (Reader reader = new FileReader(file)) {
            JSONObject json = new JSONObject(new JSONTokener(reader));
            
            String type = json.optString("type", "");
            
            if ("FeatureCollection".equals(type)) {
                JSONArray features = json.optJSONArray("features");
                if (features != null) {
                    for (int i = 0; i < features.length(); i++) {
                        JSONObject featureJson = features.optJSONObject(i);
                        if (featureJson != null) {
                            results.add(parseFeature(featureJson));
                        }
                    }
                }
            } else if ("Feature".equals(type)) {
                results.add(parseFeature(json));
            }
            
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Parsed " + results.size() + " features from GeoJSON");
        }
        
        return results;
    }

    private FeatureData parseFeature(JSONObject featureJson) {
        FeatureData data = new FeatureData();

        // Parse geometry
        JSONObject geometryJson = featureJson.optJSONObject("geometry");
        if (geometryJson != null) {
            data.geometryType = geometryJson.optString("type", "Unknown");
            data.coordinates = parseCoordinates(geometryJson.optJSONArray("coordinates"));
            data.bounds = calculateBounds(data.coordinates);
        }

        // Parse properties
        JSONObject propertiesJson = featureJson.optJSONObject("properties");
        if (propertiesJson != null) {
            Iterator<String> keys = propertiesJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    data.properties.put(key, propertiesJson.get(key));
                } catch (JSONException e) {
                    data.properties.put(key, propertiesJson.optString(key));
                }
            }
        }
        
        return data;
    }

    private double[][] parseCoordinates(JSONArray coordsArray) {
        if (coordsArray == null) {
            return new double[0][0];
        }
        
        try {
            // Try as LineString or Point (1D array of coordinates)
            if (coordsArray.length() > 0 && coordsArray.get(0) instanceof JSONArray) {
                List<double[]> coordsList = new ArrayList<>();
                for (int i = 0; i < coordsArray.length(); i++) {
                    JSONArray coord = coordsArray.optJSONArray(i);
                    if (coord != null) {
                        double[] point = new double[coord.length()];
                        for (int j = 0; j < coord.length(); j++) {
                            point[j] = coord.optDouble(j);
                        }
                        coordsList.add(point);
                    }
                }
                return coordsList.toArray(new double[0][]);
            } else {
                // Single point
                double[] point = new double[coordsArray.length()];
                for (int i = 0; i < coordsArray.length(); i++) {
                    point[i] = coordsArray.optDouble(i);
                }
                return new double[][]{point};
            }
        } catch (Exception e) {
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.WARN, "[GeoJsonImportor]:Failed to parse coordinates: " + e.getMessage());
            return new double[0][0];
        }
    }

    private double[] calculateBounds(double[][] coordinates) {
        if (coordinates == null || coordinates.length == 0) {
            return new double[]{0, 0, 0, 0};
        }
        
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        
        for (double[] coord : coordinates) {
            if (coord.length >= 2) {
                minX = Math.min(minX, coord[0]);
                minY = Math.min(minY, coord[1]);
                maxX = Math.max(maxX, coord[0]);
                maxY = Math.max(maxY, coord[1]);
            }
        }
        
        return new double[]{minX, minY, maxX, maxY};
    }

    public void convertToHdf5(String geoJsonFileName) throws Exception {
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Starting HDF5 conversion for: " + geoJsonFileName);
        List<FeatureData> features = loadMap(geoJsonFileName);
        String hdf5FileName = geoJsonFileName.replace(".geojson", ".h5").replace(".json", ".h5");
        
        convertFeaturesToHdf5(features, hdf5FileName);
    }

    private void convertFeaturesToHdf5(List<FeatureData> features, String outputFileName) {
        Path outputPath = Paths.get(HDF5_OUTPUT_DIRECTORY, outputFileName);
        
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Converting to HDF5: " + outputPath);
        
        try {
            String placeholderContent = "HDF5 conversion placeholder\n";
            placeholderContent += "Features count: " + features.size() + "\n";
            placeholderContent += "Conversion timestamp: " + new java.util.Date() + "\n";
            
            Files.write(outputPath, placeholderContent.getBytes());
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:HDF5 placeholder created successfully: " + outputPath);
            
        } catch (IOException e) {
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.ERROR, "[GeoJsonImportor]:Error creating HDF5 placeholder: " + e.getMessage());
        }
    }

    public List<FeatureData> loadMapByTerrain(TerrainConfig.TerrainType terrainType) throws Exception {
        ensureDirectoriesExist();
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Loading map by terrain: " + terrainType.getDisplayName());
        
        String fileName = terrainType.getFileName();
        String geoJsonPath = GEOJSON_DIRECTORY + "/" + fileName;
        String hdf5Path = HDF5_OUTPUT_DIRECTORY + "/" + fileName.replace(".geojson", ".h5").replace(".json", ".h5");
        
        if (Files.exists(Paths.get(hdf5Path))) {
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Loading from HDF5 cache: " + hdf5Path);
            return loadFromHdf5(hdf5Path);
        }
        
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Loading from GeoJSON: " + geoJsonPath);
        List<FeatureData> features = readGeoJson(geoJsonPath);
        
        scheduleHdf5Conversion(fileName, features);
        
        return features;
    }

    private List<FeatureData> loadFromHdf5(String hdf5Path) throws Exception {
        List<FeatureData> features = new ArrayList<>();
        
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Loading HDF5 file: " + hdf5Path);
        
        String content = new String(Files.readAllBytes(Paths.get(hdf5Path)));
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:HDF5 content loaded, length: " + content.length());
        
        return features;
    }

    private void scheduleHdf5Conversion(String fileName, List<FeatureData> features) {
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Scheduling background HDF5 conversion for: " + fileName);
        new Thread(() -> {
            try {
                String hdf5FileName = fileName.replace(".geojson", ".h5").replace(".json", ".h5");
                convertFeaturesToHdf5(features, hdf5FileName);
                CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Background HDF5 conversion completed for: " + hdf5FileName);
            } catch (Exception e) {
                CreateLogFile.getInstance().log(CreateLogFile.LogLevel.ERROR, "[GeoJsonImportor]:Background HDF5 conversion failed: " + e.getMessage());
            }
        }, "HDF5-Conversion-Worker").start();
    }

    public boolean needsHdf5Conversion(TerrainConfig.TerrainType terrainType) {
        String hdf5Name = terrainType.getFileName().replace(".geojson", ".h5").replace(".json", ".h5");
        Path hdf5Path = Paths.get(HDF5_OUTPUT_DIRECTORY, hdf5Name);
        
        boolean needsConversion = !Files.exists(hdf5Path);
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Checking HDF5 conversion need for " + terrainType.getDisplayName() + ": " + (needsConversion ? "needs conversion" : "cache exists"));
        return needsConversion;
    }

    public void ensureTerrainDataAvailable(TerrainConfig.TerrainType terrainType) throws Exception {
        ensureDirectoriesExist();
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Ensuring terrain data available for: " + terrainType.getDisplayName());
        
        String geoJsonPath = GEOJSON_DIRECTORY + "/" + terrainType.getFileName();
        if (!Files.exists(Paths.get(geoJsonPath))) {
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Terrain data not found, creating default: " + terrainType.getFileName());
            createDefaultTerrainData(terrainType);
        } else {
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Terrain data already exists: " + geoJsonPath);
        }
    }

    private void createDefaultTerrainData(TerrainConfig.TerrainType terrainType) throws Exception {
        Path filePath = Paths.get(GEOJSON_DIRECTORY, terrainType.getFileName());
        
        String defaultGeoJson = generateDefaultTerrainGeoJson(terrainType);
        Files.write(filePath, defaultGeoJson.getBytes());
        
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Created default terrain data: " + filePath);
    }

    private String generateDefaultTerrainGeoJson(TerrainConfig.TerrainType terrainType) {
        return "{\n" +
               "  \"type\": \"FeatureCollection\",\n" +
               "  \"name\": \"" + terrainType.getDisplayName() + "\",\n" +
               "  \"features\": []\n" +
               "}";
    }

    public boolean hasMapFile(String fileName) {
        Path filePath = Paths.get(GEOJSON_DIRECTORY, fileName);
        boolean exists = Files.exists(filePath);
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Checking map file existence: " + fileName + " - " + exists);
        return exists;
    }

    public void saveGeoJson(List<FeatureData> features, String fileName) throws Exception {
        ensureDirectoriesExist();
        
        Path filePath = Paths.get(GEOJSON_DIRECTORY, fileName);
        
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[GeoJsonImportor]:Saving GeoJSON to: " + filePath);
    }

    public static class FeatureData {
        public String geometryType;
        public double[][] coordinates;
        public double[] bounds;
        public Map<String, Object> properties = new HashMap<>();
    }

    public static class MapMetadata {
        public String fileName;
        public String displayName;
        public int featureCount;
        public double[] bounds;
        public Date lastModified;
        public boolean hasHdf5Version;
    }
}