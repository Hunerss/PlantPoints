package com.example.plantpoints;

public class AreasView {

    private String name;
    private String desc;
    private String dist;

    public AreasView(String tname, String tdesc, String tdist){
        name = tname;
        desc = tdesc;
        dist = tdist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDist() {
        return dist;
    }

    public void setDist(String dist) {
        this.dist = dist;
    }
}
