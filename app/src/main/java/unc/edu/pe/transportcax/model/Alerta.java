package unc.edu.pe.transportcax.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Alerta {
    private String lugar;
    private String gravedad; // "Leve", "Moderado", "Grave"
    private String descripcion;
    private String autor; // Campo para el nombre del usuario de Google

    // Anotación de Firebase para que el servidor le ponga la hora exacta automáticamente
    @ServerTimestamp
    private Date timestamp;

    // 1. REGLA DE ORO: Constructor vacío obligatorio para Firebase
    public Alerta() {
    }

    // 2. Constructor para cuando el usuario crea la alerta en la app
    public Alerta(String lugar, String gravedad, String descripcion, String autor) {
        this.lugar = lugar;
        this.gravedad = gravedad;
        this.descripcion = descripcion;
        this.autor = autor;
    }

    // --- GETTERS Y SETTERS---
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public String getGravedad() { return gravedad; }
    public void setGravedad(String gravedad) { this.gravedad = gravedad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    // Método auxiliar para la tarjeta visual (convierte la fecha a un texto simple)
    public String getTiempoPublicacion() {
        if (timestamp == null) {
            return "Justo ahora";
        }
        return "Hace un momento";
    }
}