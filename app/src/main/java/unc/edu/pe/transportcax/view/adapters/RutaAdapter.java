package unc.edu.pe.transportcax.view.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import unc.edu.pe.transportcax.R;
import unc.edu.pe.transportcax.model.Ruta;

public class RutaAdapter extends RecyclerView.Adapter<RutaAdapter.RutaViewHolder> {

    private List<Ruta> listaRutas = new ArrayList<>();
    private final OnItemClickListener listener;

    // 1. Interfaz para el clic
    public interface OnItemClickListener {
        void onItemClick(Ruta ruta);
    }

    // 2. Constructor que pide el Listener
    public RutaAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setRutas(List<Ruta> rutas) {
        this.listaRutas = rutas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RutaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ruta, parent, false);
        return new RutaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutaViewHolder holder, int position) {
        Ruta ruta = listaRutas.get(position);

        // 1. Mostramos los datos reales del modelo
        holder.tvNombreRuta.setText(ruta.getNombreRuta());
        holder.tvTrayecto.setText(ruta.getEmpresa()); // Muestra la empresa de transporte
        holder.tvPrecio.setText(String.format("S/ %.2f", ruta.getCostoViaje())); // Muestra el costo real con 2 decimales

        // 2. Colorear el icono usando el metodo : getColorHex()
        try {
            holder.imgIconoRuta.setColorFilter(Color.parseColor(ruta.getColorHex()));
        } catch (Exception e) {
            // Color azul por defecto si la API devuelve un color inválido o vacío
            holder.imgIconoRuta.setColorFilter(Color.parseColor("#1A73E8"));
        }

        // 3. Activar el clic en toda la tarjeta
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(ruta);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaRutas != null ? listaRutas.size() : 0;
    }

    static class RutaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreRuta, tvTrayecto, tvPrecio;
        ImageView imgIconoRuta; // Agregamos el ImageView para pintarlo

        public RutaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreRuta = itemView.findViewById(R.id.tvNombreRuta);
            tvTrayecto = itemView.findViewById(R.id.tvTrayecto);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            imgIconoRuta = itemView.findViewById(R.id.imgIconoRuta);
        }
    }
}