package com.susiturva.susicam.model;

public class LocationModel {
    public String session_id;
    public Double lat;
    public Double lng;
    public Double alt;
    public Double speed;
    public Double bearing;
    public int bat_soc;
    public Long uptime;
    public String last_update;
    public int active_streams;
    public int ir_active;
    public String ip_address;


    public LocationModel(){}

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public Double getAlt() {
        return alt;
    }

    public void setAlt(Double alt) {
        this.alt = alt;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getBearing() {
        return bearing;
    }

    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }

    public int getBat_soc() {
        return bat_soc;
    }

    public void setBat_soc(int bat_soc) {
        this.bat_soc = bat_soc;
    }

    public Long getUptime() {
        return uptime;
    }

    public void setUptime(Long uptime) {
        this.uptime = uptime;
    }

    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    public int getActive_streams() {
        return active_streams;
    }

    public void setActive_streams(int active_streams) {
        this.active_streams = active_streams;
    }

    public int getIr_active() {
        return ir_active;
    }

    public void setIr_active(int ir_active) {
        this.ir_active = ir_active;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }
}
