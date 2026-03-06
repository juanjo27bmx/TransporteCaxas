package unc.edu.pe.transportcax.view.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Importamos Firebase para la lectura Offline/Online
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import unc.edu.pe.transportcax.R;
import unc.edu.pe.transportcax.model.Ruta;
import unc.edu.pe.transportcax.view.adapters.RutaAdapter;

public class RutasFragment extends Fragment {

    private RecyclerView recyclerRutas;
    private RutaAdapter rutaAdapter;

    // Elementos de la cabecera
    private SearchView searchViewRutas;
    private TextView tvTituloRutas;

    // Lista de respaldo para restaurar los datos cuando borramos la búsqueda
    private List<Ruta> listaOriginalDeRutas = new ArrayList<>();

    // Herramienta de Firebase
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rutas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicializamos Firebase
        db = FirebaseFirestore.getInstance();

        // Enlazamos las vistas
        recyclerRutas = view.findViewById(R.id.recycler_rutas);
        searchViewRutas = view.findViewById(R.id.searchViewRutas);
        tvTituloRutas = view.findViewById(R.id.tvTituloRutas);

        recyclerRutas.setLayoutManager(new LinearLayoutManager(getContext()));

        // Configuramos el clic en cada tarjeta de la lista
        rutaAdapter = new RutaAdapter(rutaSeleccionada -> {
            Bundle bundle = new Bundle();

            // Pasamos el ID exacto de la ruta
            bundle.putString("ID_RUTA", String.valueOf(rutaSeleccionada.getRutaID()));

            bundle.putString("NOMBRE_RUTA", rutaSeleccionada.getNombreRuta());
            try {
                bundle.putString("COLOR_RUTA", rutaSeleccionada.getColorHex());
            } catch (Exception e){}

            RutaCompletaFragment fragment = new RutaCompletaFragment();
            fragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerRutas.setAdapter(rutaAdapter);

        // Aplicamos toda la lógica del buscador
        configurarBuscadorYAnimacion();

        // 2. LEEMOS DESDE DE FIREBASE (OFFLINE/ONLINE)
        // Reemplazamos la lectura del MapViewModel por la base de datos de Firestore
        cargarRutasDesdeFirebase();
    }

    private void cargarRutasDesdeFirebase() {
        db.collection("rutas_oficiales")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firebase", "Error al cargar las rutas", error);
                        return;
                    }

                    if (value != null) {
                        List<Ruta> nuevasRutas = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Ruta ruta = doc.toObject(Ruta.class);
                            nuevasRutas.add(ruta);
                        }

                        // Guardamos la lista original para que el buscador funcione
                        listaOriginalDeRutas = nuevasRutas;
                        // Mostramos las rutas iniciales en la pantalla
                        rutaAdapter.setRutas(nuevasRutas);

                        if (nuevasRutas.isEmpty() && getContext() != null) {
                            Toast.makeText(getContext(), "Esperando rutas de la API...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void configurarBuscadorYAnimacion() {
        // 1. Efecto Visual: Ocultar título al abrir el buscador
        searchViewRutas.setOnSearchClickListener(v -> tvTituloRutas.setVisibility(View.GONE));

        // 2. Efecto Visual: Mostrar título al cerrar el buscador
        searchViewRutas.setOnCloseListener(() -> {
            tvTituloRutas.setVisibility(View.VISIBLE);
            return false;
        });

        // 3. Lógica de Filtrado en tiempo real
        searchViewRutas.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarLista(newText);
                return true;
            }
        });
    }

    private void filtrarLista(String textoBuscado) {
        List<Ruta> listaFiltrada = new ArrayList<>();
        String query = textoBuscado.toLowerCase().trim();

        if (query.isEmpty()) {
            rutaAdapter.setRutas(listaOriginalDeRutas);
            return;
        }

        // Intentamos convertir el texto a número para filtrar por costo máximo
        Double precioMaximo = null;
        try {
            precioMaximo = Double.parseDouble(query);
        } catch (NumberFormatException ignored) {}

        for (Ruta ruta : listaOriginalDeRutas) {
            String nombre = (ruta.getNombreRuta() != null) ? ruta.getNombreRuta().toLowerCase() : "";
            double costoRuta = ruta.getCostoViaje();

            boolean coincideNombre = nombre.contains(query);
            // Si el texto es numérico, comparamos si el costo es menor o igual al ingresado
            boolean coincidePrecio = (precioMaximo != null && costoRuta <= precioMaximo);

            if (coincideNombre || coincidePrecio) {
                listaFiltrada.add(ruta);
            }
        }

        // Actualizamos el adaptador con la nueva lista filtrada
        rutaAdapter.setRutas(listaFiltrada);
    }
}
