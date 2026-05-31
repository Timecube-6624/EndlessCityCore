package Geo;

import java.nio.file.*;
import java.util.*;

public class TerrainConfig {

    public enum TerrainType {
        FLAT_PLAINS("Flat Plains", "flat_plains.geojson"),
        ROLLING_HILLS("Rolling Hills", "rolling_hills.geojson"),
        MOUNTAINOUS("Mountainous", "mountainous.geojson"),
        MIXED("Mixed", "mixed_terrain.geojson"),
        COASTAL("Coastal", "coastal.geojson"),
        DESERT("Desert", "desert.geojson");

        private final String displayName;
        private final String fileName;

        TerrainType(String displayName, String fileName) {
            this.displayName = displayName;
            this.fileName = fileName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getFileName() {
            return fileName;
        }

        public static TerrainType fromDisplayName(String displayName) {
            for (TerrainType type : values()) {
                if (type.displayName.equals(displayName)) {
                    return type;
                }
            }
            return MIXED;
        }
    }

    private static final Map<TerrainType, TerrainInfo> terrainMap = new EnumMap<>(TerrainType.class);

    static {
        terrainMap.put(TerrainType.FLAT_PLAINS, new TerrainInfo(
            "平坦平原",
            "适合农业和城市扩张",
            0.8f,    // 建造效率
            0.6f,    // 资源丰富度
            0.9f     // 交通便利性
        ));
        
        terrainMap.put(TerrainType.ROLLING_HILLS, new TerrainInfo(
            "连绵丘陵",
            "平衡的地形，适合多样化发展",
            0.7f,
            0.75f,
            0.8f
        ));
        
        terrainMap.put(TerrainType.MOUNTAINOUS, new TerrainInfo(
            "多山地形",
            "丰富的矿产资源，但建造困难",
            0.4f,
            0.9f,
            0.5f
        ));
        
        terrainMap.put(TerrainType.MIXED, new TerrainInfo(
            "混合地形",
            "包含多种地形特征",
            0.7f,
            0.8f,
            0.75f
        ));
        
        terrainMap.put(TerrainType.COASTAL, new TerrainInfo(
            "沿海地区",
            "贸易便利，渔业资源丰富",
            0.75f,
            0.85f,
            0.95f
        ));
        
        terrainMap.put(TerrainType.DESERT, new TerrainInfo(
            "沙漠",
            "资源稀缺，但阳光充足",
            0.5f,
            0.4f,
            0.7f
        ));
    }

    public static TerrainInfo getTerrainInfo(TerrainType type) {
        return terrainMap.get(type);
    }

    public static boolean hasGeoJsonFile(TerrainType type) {
        Path filePath = Paths.get(GeoJsonImportor.GEOJSON_DIRECTORY, type.getFileName());
        return Files.exists(filePath);
    }

    public static boolean hasHdf5File(TerrainType type) {
        String hdf5Name = type.getFileName().replace(".geojson", ".h5").replace(".json", ".h5");
        Path filePath = Paths.get(GeoJsonImportor.HDF5_OUTPUT_DIRECTORY, hdf5Name);
        return Files.exists(filePath);
    }

    public static String getGeoJsonFilePath(TerrainType type) {
        return GeoJsonImportor.GEOJSON_DIRECTORY + "/" + type.getFileName();
    }

    public static String getHdf5FilePath(TerrainType type) {
        String hdf5Name = type.getFileName().replace(".geojson", ".h5").replace(".json", ".h5");
        return GeoJsonImportor.HDF5_OUTPUT_DIRECTORY + "/" + hdf5Name;
    }

    public static List<TerrainType> getAvailableTerrains() {
        List<TerrainType> available = new ArrayList<>();
        for (TerrainType type : TerrainType.values()) {
            if (hasGeoJsonFile(type)) {
                available.add(type);
            }
        }
        return available;
    }

    public static class TerrainInfo {
        public final String chineseName;
        public final String description;
        public final float buildEfficiency;
        public final float resourceRichness;
        public final float transportationAccessibility;

        public TerrainInfo(String chineseName, String description, 
                          float buildEfficiency, float resourceRichness, float transportationAccessibility) {
            this.chineseName = chineseName;
            this.description = description;
            this.buildEfficiency = buildEfficiency;
            this.resourceRichness = resourceRichness;
            this.transportationAccessibility = transportationAccessibility;
        }
    }
}