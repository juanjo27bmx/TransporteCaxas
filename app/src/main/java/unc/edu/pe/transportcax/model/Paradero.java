package unc.edu.pe.transportcax.model;

import com.google.gson.annotations.SerializedName;

public class Paradero {

    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("lat")
    private double lat;

    @SerializedName("lng")
    private double lng;

    // Constructor vacío
    public Paradero() {}

    // Constructor con parámetros
    public Paradero(int id, String nombre, double lat, double lng) {
        this.id = id;
        this.nombre = nombre;
        this.lat = lat;
        this.lng = lng;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
}