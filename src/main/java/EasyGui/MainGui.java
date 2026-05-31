package EasyGui;

import Simulation.CreateLogFile;
import Simulation.FindSaves;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import  javafx.collections.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;


import java.util.ArrayList;
import java.util.List;

import static Simulation.FindSaves.DoNeedCreateNewSimulation;
import static Simulation.FindSaves.SaveList;
import static javafx.application.Application.launch;
import static Simulation.CitizenGenerator.SetRegion;

public class MainGui extends Application {
    public static boolean NewSimulationClicked;
    private static boolean SimulationViewOpen = false;//跟踪主模拟窗口是否已经打开
    public void start(Stage primaryStage) {
        //创建菜单栏
        BorderPane borderPane = new BorderPane();
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");//定义选项卡名称
        Menu AboutMenu = new Menu("About");
        //选项卡菜单名称
        MenuItem ContinueSimulation = new MenuItem("Continue Simulation");
        MenuItem NewSimulation = new MenuItem("New Simulation");
        MenuItem LoadSimulation = new MenuItem("Load Simulation");
        //选项事件
        ContinueSimulation.setOnAction(e -> {
            System.out.println("Continue Simulation");
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Continue Simulation clicked");
            //读取上一次关闭时的存档到配置文件
        });
        NewSimulation.setOnAction(e -> {
            System.out.println("New Simulation");
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:New Simulation clicked");

            if (SimulationViewOpen) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm New Simulation");
                confirmAlert.setHeaderText(null);
                confirmAlert.setContentText("A simulation is already running. Are you sure you want to create a new one?");
                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        SimulationViewOpen = false;
                        NewSimulationClicked = true;
                        NewSimulationSettingWindow();
                    }
                });
            } else {
                NewSimulationClicked = true;
                NewSimulationSettingWindow();
            }
        });
        LoadSimulation.setOnAction(e -> {
            System.out.println("Load Simulation");
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Load Simulation clicked");
            //接下来选择需要读取的存档
            SavesChoosingWindow();
        });
        //检查是否必须先创建新的地图
        FindSaves.main(null);
        if (DoNeedCreateNewSimulation == true) {
            //使功能按钮变灰
            ContinueSimulation.setDisable(true);
            NewSimulation.setDisable(false);
            Platform.runLater(() ->{
                ContinueSimulation.setDisable(true);
                menuBar.requestLayout();
            });
        }
        //将选项添加到界面中
        fileMenu.getItems().addAll(
                NewSimulation,
                ContinueSimulation,
                LoadSimulation
        );
        //定义窗口基本信息
        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(AboutMenu);
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        Scene scene = new Scene(root ,800,600);

        //定义窗口名
        primaryStage.setTitle("EndlessCity-InternalVer.");
        primaryStage.setScene(scene);
        primaryStage.show();
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO,"[MainGui]:Create window 'EndlessCity-InternalVer.' Successfully");

    }
    public void SavesChoosingWindow() {

        ObservableList<String> items = FXCollections.observableArrayList();
        if (DoNeedCreateNewSimulation == true) {
            //判断是否有存档
            System.out.println("[MainGui]:SavesChoosingWindow: There're NO saves,please create a new simulation]");
        }else {
            //接收传入文件列表字符串 rawLine为未处理的字符串 cleanedLine为已被处理的字符串
            List<String> fileNames = new ArrayList<>();
            for (String rawLine : SaveList.split("\n")) {
                String cleanedLine = rawLine.trim();
                if (cleanedLine.isEmpty()) continue;
                if (cleanedLine.startsWith("- ")) {
                    cleanedLine = cleanedLine.substring(2).trim();
                }
                fileNames.add(cleanedLine);
            }
            //逻辑混乱点，需要理解逻辑后重新编写
            //创建可观察列表并绑定到ListView
            ObservableList<String> ListItems = FXCollections.observableArrayList(fileNames);
            ListView<String> listView = new ListView<>(ListItems);
            listView.setPrefSize(400,300);
            //添加Choose a Save窗口中ListView底部按钮，默认禁用
            Button confirmButton = new Button("Confirm");
            Button DetailButton = new Button("Detail");
            confirmButton.setDisable(true);
            DetailButton.setDisable(true);
            //设置底部按钮布局
            HBox buttonBar = new HBox(15,confirmButton,DetailButton);
            buttonBar.setAlignment(Pos.CENTER);
            buttonBar.setPadding(new Insets(10));
            //监听ListView变化
            listView.getSelectionModel().selectedItemProperty().addListener((obs,oldVal,newVal)->{
                boolean ButtonIsSelected = newVal != null;
                confirmButton.setDisable(!ButtonIsSelected);
                DetailButton.setDisable(!ButtonIsSelected);
            });
            //添加底部按钮事件
            confirmButton.setOnAction(e -> {
               String selectedSave = listView.getSelectionModel().getSelectedItem();
               if (selectedSave != null) {
                   System.out.println("[MainGui]:Loading Save:" + selectedSave);
                   CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Loading Save:" + selectedSave);
                //创建新窗口并且关闭存档加载窗口
                   Stage saveListStage = (Stage) confirmButton.getScene().getWindow();
                   saveListStage.close();

                   Platform.runLater(() -> openSimulationMainView());
               }
            });
            //处理detailButton的按钮事件
            DetailButton.setOnAction(e -> {
               String selectedSave = listView.getSelectionModel().getSelectedItem();
               if (selectedSave != null) {
                   System.out.println("[MainGui]:Save detail:" + selectedSave);
                   CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Save detail:" + selectedSave);
                   DetailInspectWindow(selectedSave);

               }
            });
            //添加ListView存档列表的选择事件
            listView.getSelectionModel().selectedItemProperty().addListener((obs,oldVal,newVal)->{
                if (newVal != null) {
                    System.out.println("[MainGui]:SavesChoosingWindow: Selected a Save: " + newVal);
                    CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:SavesChoosingWindow: Selected a Save: " + newVal);
                }
            });
            //使用BorderPane进行布局
            BorderPane SaveListBorderPane = new BorderPane();
            SaveListBorderPane.setTop(new Label("Choose a save"));
            SaveListBorderPane.setCenter(listView);
            SaveListBorderPane.setBottom(buttonBar);
            //创建舞台并显示
            Stage SaveListStage = new Stage();
            SaveListStage.setTitle("Choose your save");
            SaveListStage.setScene(new Scene(SaveListBorderPane,500,400));
            SaveListStage.show();

            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO,"[MainGui]:Save window 'Choose your save'");
        }
    }

    public void NewSimulationSettingWindow() {
        //用于创建一个窗口，调整新模拟的基本信息

        //创建模拟选项窗口的布局
        BorderPane simulationOptionsBorderPane = new BorderPane();

        //顶部标题
        Label titleLabel = new Label("Simulation options");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        BorderPane.setMargin(titleLabel, new Insets(10));
        simulationOptionsBorderPane.setTop(titleLabel);

        //中心区域 - 模拟参数设置
        VBox centerPanel = new VBox(15);
        centerPanel.setPadding(new Insets(20));

        //模拟名称设置
        HBox nameSetting = new HBox(10);
        Label nameLabel = new Label("Simulation Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter simulation name");
        nameSetting.getChildren().addAll(nameLabel, nameField);

        //星球选择设置
        HBox planetSetting = new HBox(10);
        Label planetLabel = new Label("Planet:");
        ComboBox<String> planetComboBox = new ComboBox<>();
        planetComboBox.getItems().addAll("Earth", "Mars", "Moon", "Titan", "Europa");
        planetComboBox.setValue("Earth");
        planetLabel.setPrefWidth(120);
        planetSetting.getChildren().addAll(planetLabel, planetComboBox);

        //区域选择设置
        HBox regionSetting = new HBox(10);
        Label regionLabel = new Label("Region:");
        ComboBox<String> regionComboBox = new ComboBox<>();
        regionLabel.setPrefWidth(120);
        regionSetting.getChildren().addAll(regionLabel, regionComboBox);

        //区域数据定义 - 每个星球对应不同区域和经纬度
        //地球区域
        ObservableList<String> earthRegions = FXCollections.observableArrayList(
            "North America - East Coast", "North America - West Coast", "North America - Central",
            "South America - North", "South America - South", "Europe - West", "Europe - East",
            "Europe - North", "Asia - East", "Asia - South", "Asia - Central", "Asia - Southeast",
            "Africa - North", "Africa - Central", "Africa - South", "Oceania - Australia",
            "Oceania - New Zealand", "Middle East - West", "Middle East - East"
        );

        //火星区域
        ObservableList<String> marsRegions = FXCollections.observableArrayList(
            "Valles Marineris North", "Valles Marineris South", "Olympus Mons Foothills",
            "Hellas Planitia", "Arabia Terra", "Noctis Labyrinthus", "Acidalia Planitia",
            "Utopia Planitia", "Hellas Basin Rim", "Syrtis Major"
        );

        //月球区域
        ObservableList<String> moonRegions = FXCollections.observableArrayList(
            "Mare Imbrium", "Mare Serenitatis", "Mare Tranquillitatis", "Mare Crisium",
            "Mare Nubium", "Mare Fecunditatis", "Oceanus Procellarum", "Lacus Somniorum",
            "Sinus Iridum", "Tycho Crater Region"
        );

        //土卫六区域
        ObservableList<String> titanRegions = FXCollections.observableArrayList(
            "Shangri-La Dune Sea", "Kraken Mare Coast", "Ligeia Mare Shore",
            "Ontario Lacus Region", "Polonia Dune Field", "Sentinels Range"
        );

        //木卫二区域
        ObservableList<String> europaRegions = FXCollections.observableArrayList(
            "Conamara Chaos", "Thera Regio", "Minos Linea", "Powys Regio",
            "Annwn Regio", "Atlantis Linea", "Murray Linea"
        );

        //经纬度显示
        HBox coordinatesSetting = new HBox(10);
        Label coordinatesLabel = new Label("Coordinates:");
        Label latitudeValueLabel = new Label("N/A");
        Label longitudeValueLabel = new Label("N/A");
        coordinatesLabel.setPrefWidth(120);
        coordinatesSetting.getChildren().addAll(coordinatesLabel, latitudeValueLabel, longitudeValueLabel);

        //用于存储当前选中的经纬度坐标，供后续气候计算模块使用
        double[] coords = {0, 0};

        //根据星球选择更新区域列表
        planetComboBox.setOnAction(e -> {
            String selectedPlanet = planetComboBox.getValue();
            regionComboBox.getItems().clear();

            switch (selectedPlanet) {
                case "Earth":
                    regionComboBox.getItems().addAll(earthRegions);
                    break;
                case "Mars":
                    regionComboBox.getItems().addAll(marsRegions);
                    break;
                case "Moon":
                    regionComboBox.getItems().addAll(moonRegions);
                    break;
                case "Titan":
                    regionComboBox.getItems().addAll(titanRegions);
                    break;
                case "Europa":
                    regionComboBox.getItems().addAll(europaRegions);
                    break;
            }

            if (!regionComboBox.getItems().isEmpty()) {
                regionComboBox.setValue(regionComboBox.getItems().get(0));
            }

            latitudeValueLabel.setText("N/A");
            longitudeValueLabel.setText("N/A");
            coords[0] = 0;
            coords[1] = 0;
        });

        //根据区域选择更新经纬度
        regionComboBox.setOnAction(e -> {
            String selectedRegion = regionComboBox.getValue();
            String selectedPlanet = planetComboBox.getValue();

            if (selectedRegion != null && selectedPlanet != null) {
                double[] calculatedCoords = calculateCoordinates(selectedPlanet, selectedRegion);
                coords[0] = calculatedCoords[0];
                coords[1] = calculatedCoords[1];
                latitudeValueLabel.setText(String.format("%.0f°", coords[0]));
                longitudeValueLabel.setText(String.format("%.0f°", coords[1]));
            }
        });

        //地图大小设置
        HBox mapSizeSetting = new HBox(10);
        Label sizeLabel = new Label("Map Size:");
        ComboBox<String> sizeComboBox = new ComboBox<>();
        sizeComboBox.getItems().addAll("Small (256x256)", "Medium (512x512)", "Large (1024x1024)");
        sizeComboBox.setValue("Medium (512x512)");
        sizeLabel.setPrefWidth(120);
        mapSizeSetting.getChildren().addAll(sizeLabel, sizeComboBox);

        //地形类型设置
        HBox terrainSetting = new HBox(10);
        Label terrainLabel = new Label("Terrain Type:");
        ComboBox<String> terrainComboBox = new ComboBox<>();
        
        Map<String, Geo.TerrainConfig.TerrainType> terrainMap = new java.util.LinkedHashMap<>();
        terrainMap.put("No Map", null);
        for (Geo.TerrainConfig.TerrainType type : Geo.TerrainConfig.TerrainType.values()) {
            terrainMap.put(type.getDisplayName(), type);
        }
        
        terrainComboBox.getItems().add("No Map");
        for (Geo.TerrainConfig.TerrainType type : Geo.TerrainConfig.TerrainType.values()) {
            String displayName = type.getDisplayName();
            terrainComboBox.getItems().add(displayName);
        }
        
        terrainComboBox.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setDisable(true);
                    } else {
                        setText(item);
                        if (item.equals("No Map")) {
                            setDisable(false);
                        } else {
                            Geo.TerrainConfig.TerrainType type = Geo.TerrainConfig.TerrainType.fromDisplayName(item);
                            boolean hasFile = Geo.TerrainConfig.hasGeoJsonFile(type);
                            setDisable(!hasFile);
                        }
                    }
                }
            };
            return cell;
        });
        
        terrainComboBox.setValue("No Map");
        terrainLabel.setPrefWidth(120);
        terrainSetting.getChildren().addAll(terrainLabel, terrainComboBox);

        //难度设置
        HBox difficultySetting = new HBox(10);
        Label difficultyLabel = new Label("Difficulty:");
        ToggleGroup difficultyGroup = new ToggleGroup();
        RadioButton easyButton = new RadioButton("Easy");
        RadioButton normalButton = new RadioButton("Normal");
        RadioButton hardButton = new RadioButton("Hard");
        easyButton.setToggleGroup(difficultyGroup);
        normalButton.setToggleGroup(difficultyGroup);
        hardButton.setToggleGroup(difficultyGroup);
        normalButton.setSelected(true);
        difficultyLabel.setPrefWidth(120);
        difficultySetting.getChildren().addAll(difficultyLabel, easyButton, normalButton, hardButton);

        //将所有设置添加到中心面板
        centerPanel.getChildren().addAll(nameSetting, planetSetting, regionSetting,
            coordinatesSetting, mapSizeSetting, terrainSetting, difficultySetting);
        simulationOptionsBorderPane.setCenter(centerPanel);

        //底部按钮区域
        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(10));

        Button confirmButton = new Button("Confirm");
        Button cancelButton = new Button("Cancel");

        //确认按钮事件
        confirmButton.setOnAction(e -> {
            String simulationName = nameField.getText().trim();
            if (simulationName.isEmpty()) {
                simulationName = "New Simulation";
            }

            String planet = planetComboBox.getValue();
            String region = regionComboBox.getValue();
            double latitude = coords[0];
            double longitude = coords[1];
            String mapSize = sizeComboBox.getValue();
            String terrain = terrainComboBox.getValue();
            String difficulty = "Normal";
            if (easyButton.isSelected()) difficulty = "Easy";
            if (hardButton.isSelected()) difficulty = "Hard";

            System.out.println("[MainGui]:Creating new simulation with settings:");
            System.out.println("[MainGui]:Name: " + simulationName);
            System.out.println("[MainGui]:Planet: " + planet);
            System.out.println("[MainGui]:Region: " + region);
            System.out.println("[MainGui]:Coordinates: " + String.format("%.0f", latitude) + "°, " + String.format("%.0f", longitude) + "°");
            System.out.println("[MainGui]:Map Size: " + mapSize);
            System.out.println("[MainGui]:Terrain: " + terrain);
            System.out.println("[MainGui]:Difficulty: " + difficulty);

            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Creating new simulation - Name: " + simulationName +
                ", Planet: " + planet + ", Region: " + region + ", Coords: " + String.format("%.0f", latitude) + "°/" + String.format("%.0f", longitude) + "°" +
                ", Map Size: " + mapSize + ", Terrain: " + terrain + ", Difficulty: " + difficulty);

            // 加载地形数据（按需转换为 HDF5）
            Geo.TerrainConfig.TerrainType terrainType = Geo.TerrainConfig.TerrainType.fromDisplayName(terrain);
            Geo.GeoJsonImportor geoJsonImportor = new Geo.GeoJsonImportor();
            
            if (terrainType != null) {
                try {
                    geoJsonImportor.ensureTerrainDataAvailable(terrainType);
                    
                    if (geoJsonImportor.needsHdf5Conversion(terrainType)) {
                        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Terrain " + terrain + " will be converted to HDF5");
                    } else {
                        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Terrain " + terrain + " using HDF5 cache");
                    }
                    
                    // 加载地图数据
                    java.util.List<Geo.GeoJsonImportor.FeatureData> terrainFeatures = geoJsonImportor.loadMapByTerrain(terrainType);
                    CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Loaded " + terrainFeatures.size() + " features for terrain: " + terrain);
                    
                } catch (Exception ex) {
                    CreateLogFile.getInstance().log(CreateLogFile.LogLevel.ERROR, "[MainGui]:Failed to load terrain data: " + ex.getMessage());
                }
            } else {
                CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:No map selected, skipping terrain loading");
            }
            
            // 调用存档创建功能
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Starting save creation for simulation: " + simulationName);
            Simulation.SimulationSaver saver = new Simulation.SimulationSaver();
            saver.setBasicMapInfo(simulationName, "Player", region);
            saver.setTerrainType(terrain);
            boolean saveSuccess = saver.saveToFile(simulationName);

            if (saveSuccess) {
                System.out.println("[MainGui]:Save file created successfully");
                CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Save creation completed successfully");

                // 等待文件系统刷新（处理文件创建延迟）
                String expectedFileName = simulationName + ".json";
                FindSaves.waitForFileCreation(expectedFileName, 2000);

                // 刷新存档列表
                boolean refreshSuccess = FindSaves.refreshSaveList();
                if (refreshSuccess) {
                    System.out.println("[MainGui]:Save list refreshed successfully");
                    CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Save list refreshed successfully");
                } else {
                    System.err.println("[MainGui]:Failed to refresh save list");
                    CreateLogFile.getInstance().log(CreateLogFile.LogLevel.WARN, "[MainGui]:Failed to refresh save list");
                }

                // 更新菜单栏按钮状态
                NewSimulationClicked = false;
            } else {
                System.err.println("[MainGui]:Failed to create save file");
                CreateLogFile.getInstance().log(CreateLogFile.LogLevel.ERROR, "[MainGui]:Save creation failed");
            }

            //关闭窗口
            Stage currentStage = (Stage) confirmButton.getScene().getWindow();
            currentStage.close();

            //打开模拟主界面
            if (saveSuccess) {
                Platform.runLater(() -> openSimulationMainView());
            }

        });

        //取消按钮事件
        cancelButton.setOnAction(e -> {
            System.out.println("[MainGui]:Simulation options cancelled");
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Simulation options cancelled");

            //关闭窗口
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.close();
        });

        buttonBar.getChildren().addAll(confirmButton, cancelButton);
        simulationOptionsBorderPane.setBottom(buttonBar);

        //创建舞台并显示
        Stage simulationOptionsStage = new Stage();
        simulationOptionsStage.setTitle("Simulation options");
        simulationOptionsStage.setScene(new Scene(simulationOptionsBorderPane, 600, 500));
        simulationOptionsStage.show();

        //初始化默认星球的区域列表
        planetComboBox.fireEvent(new javafx.event.ActionEvent());

        //初始化默认区域的经纬度显示
        if (regionComboBox.getValue() != null && planetComboBox.getValue() != null) {
            double[] initialCoords = calculateCoordinates(planetComboBox.getValue(), regionComboBox.getValue());
            coords[0] = initialCoords[0];
            coords[1] = initialCoords[1];
            latitudeValueLabel.setText(String.format("%.0f°", coords[0]));
            longitudeValueLabel.setText(String.format("%.0f°", coords[1]));
        }

        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Create window 'Simulation options' successfully");
    }

    private double[] calculateCoordinates(String planet, String region) {
        //根据星球和区域返回对应的经纬度坐标（仅精确到度）
        double[] coords = {0, 0};

        switch (planet) {
            case "Earth":
                switch (region) {
                    case "North America - East Coast": coords = new double[]{40, -74}; break;
                    case "North America - West Coast": coords = new double[]{37, -122}; break;
                    case "North America - Central": coords = new double[]{41, -89}; break;
                    case "South America - North": coords = new double[]{5, -74}; break;
                    case "South America - South": coords = new double[]{-35, -65}; break;
                    case "Europe - West": coords = new double[]{48, 2}; break;
                    case "Europe - East": coords = new double[]{55, 37}; break;
                    case "Europe - North": coords = new double[]{59, 18}; break;
                    case "Asia - East": coords = new double[]{35, 139}; break;
                    case "Asia - South": coords = new double[]{20, 79}; break;
                    case "Asia - Central": coords = new double[]{41, 69}; break;
                    case "Asia - Southeast": coords = new double[]{14, 101}; break;
                    case "Africa - North": coords = new double[]{31, 10}; break;
                    case "Africa - Central": coords = new double[]{0, 20}; break;
                    case "Africa - South": coords = new double[]{-26, 28}; break;
                    case "Oceania - Australia": coords = new double[]{-33, 151}; break;
                    case "Oceania - New Zealand": coords = new double[]{-41, 175}; break;
                    case "Middle East - West": coords = new double[]{30, 32}; break;
                    case "Middle East - East": coords = new double[]{35, 51}; break;
                }
                break;
            case "Mars":
                switch (region) {
                    case "Valles Marineris North": coords = new double[]{-1, -65}; break;
                    case "Valles Marineris South": coords = new double[]{-15, -70}; break;
                    case "Olympus Mons Foothills": coords = new double[]{19, -133}; break;
                    case "Hellas Planitia": coords = new double[]{-42, 70}; break;
                    case "Arabia Terra": coords = new double[]{22, -15}; break;
                    case "Noctis Labyrinthus": coords = new double[]{-5, -95}; break;
                    case "Acidalia Planitia": coords = new double[]{50, 20}; break;
                    case "Utopia Planitia": coords = new double[]{48, 120}; break;
                    case "Hellas Basin Rim": coords = new double[]{-25, 80}; break;
                    case "Syrtis Major": coords = new double[]{9, 70}; break;
                }
                break;
            case "Moon":
                switch (region) {
                    case "Mare Imbrium": coords = new double[]{33, -19}; break;
                    case "Mare Serenitatis": coords = new double[]{28, 18}; break;
                    case "Mare Tranquillitatis": coords = new double[]{1, 23}; break;
                    case "Mare Crisium": coords = new double[]{17, 59}; break;
                    case "Mare Nubium": coords = new double[]{-15, -17}; break;
                    case "Mare Fecunditatis": coords = new double[]{-3, 53}; break;
                    case "Oceanus Procellarum": coords = new double[]{5, -45}; break;
                    case "Lacus Somniorum": coords = new double[]{38, 29}; break;
                    case "Sinus Iridum": coords = new double[]{45, -32}; break;
                    case "Tycho Crater Region": coords = new double[]{-43, -11}; break;
                }
                break;
            case "Titan":
                switch (region) {
                    case "Shangri-La Dune Sea": coords = new double[]{-10, -165}; break;
                    case "Kraken Mare Coast": coords = new double[]{65, -30}; break;
                    case "Ligeia Mare Shore": coords = new double[]{75, -40}; break;
                    case "Ontario Lacus Region": coords = new double[]{-70, -110}; break;
                    case "Polonia Dune Field": coords = new double[]{5, -150}; break;
                    case "Sentinels Range": coords = new double[]{20, -130}; break;
                }
                break;
            case "Europa":
                switch (region) {
                    case "Conamara Chaos": coords = new double[]{10, -85}; break;
                    case "Thera Regio": coords = new double[]{40, 180}; break;
                    case "Minos Linea": coords = new double[]{-5, 75}; break;
                    case "Powys Regio": coords = new double[]{15, -170}; break;
                    case "Annwn Regio": coords = new double[]{-25, -90}; break;
                    case "Atlantis Linea": coords = new double[]{5, -160}; break;
                    case "Murray Linea": coords = new double[]{20, -145}; break;
                }
                break;
        }

        return coords;
    }

    // Detail按钮打开的窗口
    // EXTENSION_INTERFACE: 添加新信息项时，请在对应的Category下添加新的infoItems.add()调用
    public static void DetailInspectWindow(String saveFileName) {
        if (saveFileName == null || saveFileName.trim().isEmpty()) {
            System.err.println("[MainGui]:DetailInspectWindow: Invalid save file name");
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.ERROR, "[MainGui]:DetailInspectWindow: Invalid save file name");
            return;
        }

        String savesFolder = "src/main/resources/Saves";
        String jsonFileName = saveFileName;
        if (!jsonFileName.endsWith(".json")) {
            jsonFileName = jsonFileName + ".json";
        }
        String fullPath = savesFolder + "/" + jsonFileName;

        org.json.JSONObject saveData = loadSaveFile(fullPath);
        if (saveData == null) {
            showErrorDialog("Failed to load save file: " + saveFileName);
            return;
        }

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab basicInfoTab = new Tab("Basic Info");
        basicInfoTab.setContent(createBasicInfoContent(saveData, saveFileName));
        tabPane.getTabs().add(basicInfoTab);

        Tab mapTab = new Tab("Map Data");
        mapTab.setContent(createMapDataContent(saveData));
        tabPane.getTabs().add(mapTab);

        Tab citizenTab = new Tab("Citizen Data");
        citizenTab.setContent(createCitizenDataContent(saveData));
        tabPane.getTabs().add(citizenTab);

        Tab objectTab = new Tab("Object Data");
        objectTab.setContent(createObjectDataContent(saveData));
        tabPane.getTabs().add(objectTab);

        // EXTENSION_POINT: 在此处添加新的选项卡
        // 示例: Tab customTab = new Tab("Custom"); customTab.setContent(createCustomContent(saveData)); tabPane.getTabs().add(customTab);

        BorderPane rootPane = new BorderPane();
        rootPane.setTop(new Label("Save Details: " + saveFileName));
        rootPane.setCenter(tabPane);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });

        HBox bottomBar = new HBox(closeButton);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(10));
        rootPane.setBottom(bottomBar);

        BorderPane.setMargin(tabPane, new Insets(10));
        BorderPane.setAlignment(rootPane.getTop(), Pos.CENTER);

        Stage detailStage = new Stage();
        detailStage.setTitle("Save Details");
        detailStage.setScene(new Scene(rootPane, 600, 500));
        detailStage.show();

        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:DetailInspectWindow opened for: " + saveFileName);
    }

    private static JSONObject loadSaveFile(String filePath) {
        try {
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                System.err.println("[MainGui]:Save file not found: " + filePath);
                return null;
            }
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            return new org.json.JSONObject(content);
        } catch (Exception e) {
            System.err.println("[MainGui]:Failed to load save file: " + e.getMessage());
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.ERROR, "[MainGui]:Failed to load save file: " + e.getMessage());
            return null;
        }
    }

    private static VBox createBasicInfoContent(JSONObject saveData, String fileName) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label titleLabel = new Label("Basic Map Information");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);

        int row = 0;
        grid.add(new Label("File Name:"), 0, row);
        grid.add(new Label(fileName), 1, row++);

        try {
            JSONObject basicInfo = saveData.getJSONObject("MapRelatedData").getJSONObject("BasicMapInformation");
            addInfoRow(grid, "Map Name:", getJsonString(basicInfo, "MapName"), row++);
            addInfoRow(grid, "Creator:", getJsonString(basicInfo, "MapCreator"), row++);
            addInfoRow(grid, "Region:", getJsonString(basicInfo, "MapRegion"), row++);
            addInfoRow(grid, "Create Date:", getJsonString(basicInfo, "MapCreateDate"), row++);
            addInfoRow(grid, "Season:", getJsonString(basicInfo, "CurrentMapSeason"), row++);
        } catch (Exception e) {
            grid.add(new Label("Error loading basic info"), 1, row);
        }

        content.getChildren().addAll(titleLabel, grid);
        return content;
    }

    private static VBox createMapDataContent(JSONObject saveData) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label titleLabel = new Label("Map Related Data");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TitledPane terrainPane = new TitledPane("Terrain Data", createTerrainContent(saveData));
        TitledPane hydrologyPane = new TitledPane("Hydrology Data", createHydrologyContent(saveData));

        terrainPane.setExpanded(true);
        hydrologyPane.setExpanded(true);

        content.getChildren().addAll(titleLabel, terrainPane, hydrologyPane);
        return content;
    }

    private static VBox createTerrainContent(JSONObject saveData) {
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        try {
            JSONObject terrain = saveData.getJSONObject("MapRelatedData").getJSONObject("TerrainData");
            addInfoRow(content, "Contour Line:", getJsonString(terrain, "CurrentMapContourLineData"));
            addInfoRow(content, "River Bank:", getJsonString(terrain, "CurrentRiverBankData"));
            addInfoRow(content, "Vegetation:", getJsonString(terrain, "CurrentVegetationData"));
            addInfoRow(content, "Wind Direction:", getJsonString(terrain, "CurrentWindDirectionData"));
            addInfoRow(content, "Temperature:", getJsonString(terrain, "CurrentTemperatureData"));
        } catch (Exception e) {
            content.getChildren().add(new Label("No terrain data available"));
        }
        return content;
    }

    private static VBox createHydrologyContent(JSONObject saveData) {
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        try {
            JSONObject hydrology = saveData.getJSONObject("MapRelatedData").getJSONObject("HydrologyData");
            addInfoRow(content, "River Speed:", getJsonString(hydrology, "CurrentRiverFlowingSpeed"));
            addInfoRow(content, "River Pollution:", getJsonString(hydrology, "CurrentRiverPollution"));
            addInfoRow(content, "River Temperature:", getJsonString(hydrology, "CurrentRiverTemperature"));
            addInfoRow(content, "Ocean Current:", getJsonString(hydrology, "OceanCurrentData"));
            addInfoRow(content, "Ocean Temperature:", getJsonString(hydrology, "CurrentOceanTemperatureData"));
        } catch (Exception e) {
            content.getChildren().add(new Label("No hydrology data available"));
        }
        return content;
    }

    private static VBox createCitizenDataContent(JSONObject saveData) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label titleLabel = new Label("Citizen Information");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TitledPane basicPane = new TitledPane("Basic Data", createCitizenBasicContent(saveData));
        TitledPane societyPane = new TitledPane("Society Data", createCitizenSocietyContent(saveData));

        basicPane.setExpanded(true);

        content.getChildren().addAll(titleLabel, basicPane, societyPane);
        return content;
    }

    private static VBox createCitizenBasicContent(JSONObject saveData) {
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        try {
            JSONObject citizen = saveData.getJSONObject("CreatureData").getJSONObject("CitizenData");
            JSONObject basic = citizen.getJSONObject("CitizenBasicData");
            addInfoRow(content, "Citizen ID:", getJsonString(basic, "CitizenID"));
            addInfoRow(content, "Name:", getJsonString(basic, "CitizenName"));
            addInfoRow(content, "Sex:", getJsonString(basic, "CitizenSex"));
            addInfoRow(content, "Age:", getJsonString(basic, "CurrentCitizenAges"));
            addInfoRow(content, "Personality:", getJsonString(basic, "CurrentCitizenPersonality"));
            addInfoRow(content, "Height:", getJsonString(basic, "CurrentCitizenHeight"));
            addInfoRow(content, "Weight:", getJsonString(basic, "CurrentCitizenWeight"));
        } catch (Exception e) {
            content.getChildren().add(new Label("No citizen basic data available"));
        }
        return content;
    }

    private static VBox createCitizenSocietyContent(JSONObject saveData) {
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        try {
            JSONObject citizen = saveData.getJSONObject("CreatureData").getJSONObject("CitizenData");
            JSONObject society = citizen.getJSONObject("CitizenSocietyData");
            addInfoRow(content, "Employment:", getJsonString(society, "CurrentCitizenEmployment"));
            addInfoRow(content, "Education:", getJsonString(society, "CurrentCitizenEducationalBackground"));
            addInfoRow(content, "Family:", getJsonString(society, "CurrentCitizenFamily"));
            addInfoRow(content, "Disease:", getJsonString(society, "CurrentCitizenDisease"));
        } catch (Exception e) {
            content.getChildren().add(new Label("No citizen society data available"));
        }
        return content;
    }

    private static VBox createObjectDataContent(JSONObject saveData) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label titleLabel = new Label("Object Information");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TitledPane roadPane = new TitledPane("Road Data", createRoadContent(saveData));
        TitledPane buildingPane = new TitledPane("Building Data", createBuildingContent(saveData));

        roadPane.setExpanded(true);
        buildingPane.setExpanded(true);

        content.getChildren().addAll(titleLabel, roadPane, buildingPane);
        return content;
    }

    private static VBox createRoadContent(JSONObject saveData) {
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        try {
            JSONObject object = saveData.getJSONObject("ObjectData");
            JSONObject road = object.getJSONObject("RoadRelatedData");
            addInfoRow(content, "Road Line:", getJsonString(road, "CurrentRoadLine"));
            addInfoRow(content, "Road Node:", getJsonString(road, "CurrentRodeNode"));
            addInfoRow(content, "Road Type:", getJsonString(road, "CurrentRoadType"));
            addInfoRow(content, "Allow Vehicle:", getJsonString(road, "CurrentRoadAllowVehicleType"));
        } catch (Exception e) {
            content.getChildren().add(new Label("No road data available"));
        }
        return content;
    }

    private static VBox createBuildingContent(JSONObject saveData) {
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        try {
            JSONObject object = saveData.getJSONObject("ObjectData");
            JSONObject building = object.getJSONObject("BuildingRelatedData");
            if (building.length() == 0) {
                content.getChildren().add(new Label("No building data available"));
            } else {
                building.keys().forEachRemaining(key ->
                    addInfoRow(content, key + ":", getJsonString(building, key))
                );
            }
        } catch (Exception e) {
            content.getChildren().add(new Label("No building data available"));
        }
        return content;
    }

    private static void addInfoRow(VBox parent, String label, String value) {
        HBox row = new HBox(10);
        Label lbl = new Label(label);
        lbl.setPrefWidth(140);
        lbl.setStyle("-fx-font-weight: bold;");
        Label val = new Label(value != null && !value.isEmpty() ? value : "N/A");
        val.setStyle("-fx-text-fill: #666666;");
        row.getChildren().addAll(lbl, val);
        parent.getChildren().add(row);
    }

    private static void addInfoRow(GridPane grid, String label, String value, int row) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold;");
        Label val = new Label(value != null && !value.isEmpty() ? value : "N/A");
        val.setStyle("-fx-text-fill: #666666;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private static String getJsonString(JSONObject obj, String key) {
        try {
            if (obj.has(key) && !obj.isNull(key)) {
                String val = obj.get(key).toString();
                return val.isEmpty() ? "N/A" : val;
            }
        } catch (Exception e) {
            // ignore
        }
        return "N/A";
    }

    private static void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

    }

    private static void openSimulationMainView() {
        if (SimulationViewOpen) return;

        SimulationMainView mainView = new SimulationMainView();
        Stage mainViewStage = new Stage();
        mainViewStage.setOnCloseRequest(e -> SimulationViewOpen = false);
        mainView.start(mainViewStage);
        SimulationViewOpen = true;

        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:SimulationMainView opened");
    }

    public static void main(String[] args) {
        Simulation.FindSaves.main(null);
        launch(args);

    }
}
