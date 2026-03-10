package unc.edu.pe.transportcax.view.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import unc.edu.pe.transportcax.R;
import unc.edu.pe.transportcax.model.Calificacion;

public class CalificacionAdapter extends RecyclerView.Adapter<CalificacionAdapter.CalificacionViewHolder> {

    private List<Calificacion> listaCalificaciones;

    public CalificacionAdapter(List<Calificacion> listaCalificaciones) {
        this.listaCalificaciones = listaCalificaciones;
    }

    public void setCalificaciones(List<Calificacion> nuevasCalificaciones) {
        this.listaCalificaciones = nuevasCalificaciones;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalificacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calificacion, parent, false);
        return new CalificacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalificacionViewHolder holder, int position) {
        Calificacion calif = listaCalificaciones.get(position);

        // APLICAMOS LA LÓGICA DE ABREVIATURA AQUÍ
        String nombreAbreviado = formatearNombre(calif.getAutorNombre());
        holder.tvAutor.setText(nombreAbreviado);

        String textoComentario = calif.getComentario();
        if (textoComentario == null || textoComentario.trim().isEmpty()) {
            holder.tvComentario.setText("El usuario no dejó ningún comentario escrito.");
            holder.tvComentario.setTextColor(android.graphics.Color.parseColor("#999999")); // Gris clarito
            holder.tvComentario.setTypeface(null, android.graphics.Typeface.ITALIC);
        } else {
            holder.tvComentario.setText(textoComentario);
            holder.tvComentario.setTextColor(android.graphics.Color.parseColor("#4D4D4D")); // Gris oscuro normal
            holder.tvComentario.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        // Promediamos la seguridad y el tiempo para mostrar una sola calificación en la tarjeta
        float promedio = (calif.getPuntajeSeguridad() + calif.getPuntajeTiempo()) / 2.0f;
        holder.rbEstrellas.setRating(promedio);
    }

    // EL MÉTODO MÁGICO QUE ABREVIA LOS NOMBRES
    private String formatearNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) return "Usuario";

        // Separamos el nombre por espacios
        String[] partes = nombreCompleto.trim().split("\\s+");

        if (partes.length == 1) {
            return partes[0];
        } else if (partes.length == 2) {
            return partes[0] + " " + partes[1];
        } else if (partes.length == 3) {
            // Ejemplo: "Diego Flores Quispe" -> "Diego Flores"
            return partes[0] + " " + partes[1];
        } else {
            // Ejemplo: "Juan Jose Albitres Moreno" -> "Juan J. Albitres"
            String primerNombre = partes[0];
            String inicialSegundoNombre = partes[1].substring(0, 1).toUpperCase() + ".";
            String primerApellido = partes[2];
            return primerNombre + " " + inicialSegundoNombre + " " + primerApellido;
        }
    }

    @Override
    public int getItemCount() {
        return listaCalificaciones != null ? listaCalificaciones.size() : 0;
    }

    static class CalificacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvAutor, tvComentario;
        RatingBar rbEstrellas;

        public CalificacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAutor = itemView.findViewById(R.id.tvAutorCalificacion);
            tvComentario = itemView.findViewById(R.id.tvComentarioCalificacion);
            rbEstrellas = itemView.findViewById(R.id.rbEstrellasCalificacion);
        }
    }
}