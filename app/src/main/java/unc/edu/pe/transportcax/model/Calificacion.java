package unc.edu.pe.transportcax.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Calificacion {
    private String rutaID;
    private float puntajeSeguridad;
    private float puntajeTiempo;
    private String comentario;
    private String autorNombre; // Viene del usuario de Google
    private String autorEmail;  // Viene del usuario de Google

    @ServerTimestamp
    private Date fechaCalificacion;

    // Constructor vacío obligatorio para Firebase
    public Calificacion() {}

    public Calificacion(String rutaID, float puntajeSeguridad, float puntajeTiempo, String comentario, String autorNombre, String autorEmail) {
        this.rutaID = rutaID;
        this.puntajeSeguridad = puntajeSeguridad;
        this.puntajeTiempo = puntajeTiempo;
        this.comentario = comentario;
        this.autorNombre = autorNombre;
        this.autorEmail = autorEmail;
    }

    // Getters y Setters
    public String getRutaID() { return rutaID; }
    public void setRutaID(String rutaID) { this.rutaID = rutaID; }

    public float getPuntajeSeguridad() { return puntajeSeguridad; }
    public void setPuntajeSeguridad(float puntajeSeguridad) { this.puntajeSeguridad = puntajeSeguridad; }

    public float getPuntajeTiempo() { return puntajeTiempo; }
    public void setPuntajeTiempo(float puntajeTiempo) { this.puntajeTiempo = puntajeTiempo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getAutorNombre() { return autorNombre; }
    public void setAutorNombre(String autorNombre) { this.autorNombre = autorNombre; }

    public String getAutorEmail() { return autorEmail; }
    public void setAutorEmail(String autorEmail) { this.autorEmail = autorEmail; }

    public Date getFechaCalificacion() { return fechaCalificacion; }
    public void setFechaCalificacion(Date fechaCalificacion) { this.fechaCalificacion = fechaCalificacion; }
}