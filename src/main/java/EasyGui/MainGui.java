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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import java.util.ArrayList;
import java.util.List;

import static Simulation.FindSaves.DoNeedCreateNewSimulation;
import static Simulation.FindSaves.SaveList;
import static javafx.application.Application.launch;
import static Simulation.CitizenGenerator.SetRegion;

public class MainGui extends Application {
    public static boolean NewSimulationClicked;
    public void start(Stage primaryStage) {
        //创建菜单栏
        BorderPane borderPane = new BorderPane();
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");//定义选项卡名称
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
            NewSimulationClicked = true;
            //创建一个窗口，设定新模拟的基本信息
            NewSimulationSettingWindow();

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
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        Scene scene = new Scene(root ,800,600);

        //定义窗口名
        primaryStage.setTitle("FunTamyCity-InternalVer.");
        primaryStage.setScene(scene);
        primaryStage.show();
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO,"[MainGui]:Create window 'FunTamyCity-InternalVer.' Successfully");

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
                   //后续添加实际的存档加载逻辑代码，并关闭这个窗口
               }
            });
            //处理detailButton的按钮事件
            DetailButton.setOnAction(e -> {
               String selectedSave = listView.getSelectionModel().getSelectedItem();
               if (selectedSave != null) {
                   System.out.println("[MainGui]:Save detail:" + selectedSave);
                   CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Save detail:" + selectedSave);
                   //后续添加打开存档详情的窗口
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
        terrainComboBox.getItems().addAll("Flat Plains", "Rolling Hills", "Mountainous", "Mixed", "Coastal", "Desert");
        terrainComboBox.setValue("Mixed");
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
            
            // 调用存档创建功能
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[MainGui]:Starting save creation for simulation: " + simulationName);
            Simulation.SimulationSaver saver = new Simulation.SimulationSaver();
            saver.setBasicMapInfo(simulationName, "Player", region);
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
            
            //后续添加实际的新模拟创建逻辑
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
    public static void main(String[] args) {
        Simulation.FindSaves.main(null);
        launch(args);
    }
}