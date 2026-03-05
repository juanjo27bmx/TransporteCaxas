package unc.edu.pe.transportcax.view.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import unc.edu.pe.transportcax.R;
import unc.edu.pe.transportcax.model.Alerta;

public class AlertaAdapter extends RecyclerView.Adapter<AlertaAdapter.AlertaViewHolder> {

    private List<Alerta> listaAlertas;

    public AlertaAdapter(List<Alerta> listaAlertas) {
        this.listaAlertas = listaAlertas;
    }

    @NonNull
    @Override
    public AlertaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alerta, parent, false);
        return new AlertaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertaViewHolder holder, int position) {
        Alerta alerta = listaAlertas.get(position);
        holder.tvLugar.setText(alerta.getLugar());
        holder.tvDescripcion.setText(alerta.getDescripcion());
        holder.tvTiempo.setText(alerta.getTiempoPublicacion());
        holder.tvLugar.setText(alerta.getLugar());
        holder.tvDescripcion.setText(alerta.getDescripcion());
        holder.tvTiempo.setText(alerta.getTiempoPublicacion());

        // Mostrar el nombre del usuario de Google
        holder.tvAutor.setText("Reportado por: " + alerta.getAutor());

        // Cambiar el color del ícono según la gravedad
        if (alerta.getGravedad().contains("Leve")) {
            holder.ivIcono.setColorFilter(Color.parseColor("#FFC107")); // Amarillo
        } else if (alerta.getGravedad().contains("Moderado")) {
            holder.ivIcono.setColorFilter(Color.parseColor("#FF9800")); // Naranja
        } else {
            holder.ivIcono.setColorFilter(Color.parseColor("#F44336")); // Rojo
        }
    }

    @Override
    public int getItemCount() {
        return listaAlertas.size();
    }

    public static class AlertaViewHolder extends RecyclerView.ViewHolder {
        TextView tvLugar, tvDescripcion, tvTiempo, tvAutor; // <-- Agrega tvAutor
        ImageView ivIcono;

        public AlertaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLugar = itemView.findViewById(R.id.tvLugarAlerta);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionAlerta);
            tvTiempo = itemView.findViewById(R.id.tvTiempoAlerta);
            tvAutor = itemView.findViewById(R.id.tvAutorAlerta); // <-- Enlázalo aquí
            ivIcono = itemView.findViewById(R.id.ivIconoAlerta);
        }
    }
}