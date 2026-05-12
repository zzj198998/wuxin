package com.egogame.vehiclehailer.model;

/**
 * 车型配置
 */
public class CarModel {
    private int id;
    private String modelName;
    private String regFileName;

    public CarModel(int id, String modelName, String regFileName) {
        this.id = id;
        this.modelName = modelName;
        this.regFileName = regFileName;
    }

    public int getId() { return id; }
    public String getModelName() { return modelName; }
    public String getRegFileName() { return regFileName; }

    public String getDisplayName() {
        switch (modelName) {
            case "Car_Deepal": return "深蓝";
            case "Car_OuShang": return "欧尚";
            case "Car_Jili": return "吉利";
            case "Car_Mazda": return "马自达";
            case "Car_QiYuan": return "启源Q05";
            case "Car_YiPai007": return "奕派007";
            default: return modelName;
        }
    }
}
