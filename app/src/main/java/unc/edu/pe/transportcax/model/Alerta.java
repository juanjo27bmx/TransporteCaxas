package unc.edu.pe.transportcax.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Alerta {
    private String lugar;
    private String gravedad; // "Leve", "Moderado", "Grave"
    private String descripcion;
    private String autor; // Campo para el nombre del usuario de Google

    // Anotación de Firebase para que el servidor le ponga la hora exacta automáticamente
    @ServerTimestamp
    private Date timestamp;

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
        return formatoDate(timestamp);
    }
    //Metodo para dar formato a la fecha
    private String formatoDate(Date fechaAlerta){
        Date ahora = new Date();

        // Calculamos cuántas horas han pasado entre ahorita y la alerta
        long diferenciaMillis = ahora.getTime() - fechaAlerta.getTime();
        long horasPasadas = TimeUnit.MILLISECONDS.toHours(diferenciaMillis);

        if (horasPasadas < 24) {
            // Si pasaron menos de 24 horas, mostramos solo la hora (Ej: "14:30")
            SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return formatoHora.format(fechaAlerta);
        } else {
            // Si pasó más de 1 día, mostramos día y mes (Ej: "05 mar")
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd MMM", Locale.getDefault());
            return formatoFecha.format(fechaAlerta);
        }
    }
}