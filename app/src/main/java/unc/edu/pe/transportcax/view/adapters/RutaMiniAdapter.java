package unc.edu.pe.transportcax.view.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import unc.edu.pe.transportcax.R;
import unc.edu.pe.transportcax.model.Ruta;
import unc.edu.pe.transportcax.view.fragments.RutaCompletaFragment;

public class RutaMiniAdapter extends RecyclerView.Adapter<RutaMiniAdapter.RutaMiniViewHolder> {

    private List<Ruta> listaRutas;

    public RutaMiniAdapter(List<Ruta> listaRutas) {
        this.listaRutas = listaRutas;
    }

    @NonNull
    @Override
    public RutaMiniViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ruta_mini, parent, false);
        return new RutaMiniViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutaMiniViewHolder holder, int position) {
        Ruta ruta = listaRutas.get(position);

        holder.tvNombre.setText(ruta.getNombreRuta());
        // Mostramos el destino final de la combi como subtítulo
        if (ruta.getParaderos() != null && !ruta.getParaderos().isEmpty()) {
            String destinoFinal = ruta.getParaderos().get(ruta.getParaderos().size() - 1).getNombre();
            holder.tvDestino.setText(destinoFinal);
        } else {
            holder.tvDestino.setText("Ruta circular");
        }

        // Si el usuario toca la tarjetita, lo mandamos al mapa de esa ruta
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("ID_RUTA", String.valueOf(ruta.getRutaID()));
            bundle.putString("NOMBRE_RUTA", ruta.getNombreRuta());
            bundle.putString("COLOR_RUTA", ruta.getColorHex());

            RutaCompletaFragment fragment = new RutaCompletaFragment();
            fragment.setArguments(bundle);

            ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return listaRutas != null ? listaRutas.size() : 0;
    }

    static class RutaMiniViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDestino;

        public RutaMiniViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreRutaMini);
            tvDestino = itemView.findViewById(R.id.tvDestinoRutaMini);
        }
    }
}