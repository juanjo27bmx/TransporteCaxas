package unc.edu.pe.transportcax.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Ruta {

    @SerializedName("RutaID")
    private int rutaID;

    @SerializedName("NombreRuta")
    private String nombreRuta;

    @SerializedName("Empresa")
    private String empresa;

    @SerializedName("CostoViaje")
    private double costoViaje;

    @SerializedName("ColorHex")
    private String colorHex;

    @SerializedName("Estado")
    private String estado;

    // Aca guardará el código de las curvas
    @SerializedName("Polyline")
    private String polyline;

    @SerializedName("Paraderos")
    private List<Paradero> paraderos;

    // Constructor vacío
    public Ruta() {}

    // Constructor
    public Ruta(int rutaID, String nombreRuta, String empresa, double costoViaje, String colorHex, String estado, String polyline, List<Paradero> paraderos) {
        this.rutaID = rutaID;
        this.nombreRuta = nombreRuta;
        this.empresa = empresa;
        this.costoViaje = costoViaje;
        this.colorHex = colorHex;
        this.estado = estado;
        this.polyline = polyline;
        this.paraderos = paraderos;
    }

    // Getters y Setters

    public int getRutaID() { return rutaID; }
    public void setRutaID(int rutaID) { this.rutaID = rutaID; }

    public String getNombreRuta() { return nombreRuta; }
    public void setNombreRuta(String nombreRuta) { this.nombreRuta = nombreRuta; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public double getCostoViaje() { return costoViaje; }
    public void setCostoViaje(double costoViaje) { this.costoViaje = costoViaje; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // Getters y Setters del Polyline
    public String getPolyline() { return polyline; }
    public void setPolyline(String polyline) { this.polyline = polyline; }

    public List<Paradero> getParaderos() { return paraderos; }
    public void setParaderos(List<Paradero> paraderos) { this.paraderos = paraderos; }
}