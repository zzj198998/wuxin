package com.egogame.vehiclehailer.model;

/**
 * 目录分类
 */
public class Catalog {
    private static int nextId = 1;
    private int id;
    private String catalog;
    private String displayName;

    public Catalog(String catalog, String displayName) {
        this.id = nextId++;
        this.catalog = catalog;
        this.displayName = displayName;
    }

    public String getCatalog() { return catalog; }
    public String getDisplayName() { return displayName; }
    public int getId() { return id; }
    public String getName() { return getDisplayName(); }
}
