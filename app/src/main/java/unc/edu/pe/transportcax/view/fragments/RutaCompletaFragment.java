package unc.edu.pe.transportcax.view.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.PolyUtil;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import unc.edu.pe.transportcax.R;
import unc.edu.pe.transportcax.model.Calificacion;
import unc.edu.pe.transportcax.model.DirectionsResponse;
import unc.edu.pe.transportcax.model.Paradero;
import unc.edu.pe.transportcax.model.Ruta;
import unc.edu.pe.transportcax.network.GoogleMapsApi;
import unc.edu.pe.transportcax.view.adapters.CalificacionAdapter;

public class RutaCompletaFragment extends Fragment implements OnMapReadyCallback {

    private String idRuta = "";
    private String nombreRutaSeleccionada = "";
    private String colorRuta = "#1A73E8";

    private TextView tvTituloRutaMapa;
    private FirebaseFirestore db;
    private RecyclerView recyclerCalificaciones;
    private CalificacionAdapter calificacionAdapter;
    private TextView tvPromedioEstrellas;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ruta_completa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        tvTituloRutaMapa = view.findViewById(R.id.tvTituloRutaMapa);
        ImageView btnVolver = view.findViewById(R.id.btnVolver);

        if (getArguments() != null) {
            idRuta = getArguments().getString("ID_RUTA", "");
            nombreRutaSeleccionada = getArguments().getString("NOMBRE_RUTA", "");
            colorRuta = getArguments().getString("COLOR_RUTA", "#1A73E8");
            tvTituloRutaMapa.setText(nombreRutaSeleccionada);
        }
        cargarCalificaciones();
        btnVolver.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // AQUÍ ENLAZAMOS EL BOTÓN DE LA ESTRELLA
        FloatingActionButton fabCalificar = view.findViewById(R.id.fabCalificarRuta);
        fabCalificar.setOnClickListener(v -> {
            FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();

            if (usuarioActual != null) {
                mostrarDialogoCalificacion(usuarioActual);
            } else {
                // AQUÍ CREAMOS EL EMERGENTE ELEGANTE EN LUGAR DEL TOAST
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("⚠️ Inicio de sesión requerido")
                        .setMessage("Para mantener la veracidad de las calificaciones en nuestra comunidad, necesitas iniciar sesión antes de publicar tu opinión.\n\nVe a la pestaña 'Perfil' para conectarte rápidamente con Google.")
                        .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_ruta_completa);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void cargarCalificaciones() {
        tvPromedioEstrellas = getView().findViewById(R.id.tvPromedioEstrellas);
        recyclerCalificaciones = getView().findViewById(R.id.recyclerCalificaciones);

        // Inicializamos el Adapter vacío
        calificacionAdapter = new CalificacionAdapter(new java.util.ArrayList<>());
        recyclerCalificaciones.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        recyclerCalificaciones.setAdapter(calificacionAdapter);

        // Escuchamos a Firebase
        db.collection("calificaciones_rutas")
                .whereEqualTo("rutaID", idRuta) // Solo las calificaciones de ESTA combi
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        tvPromedioEstrellas.setText("⭐ Error al cargar.");
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        java.util.List<Calificacion> lista = new java.util.ArrayList<>();
                        float sumaTotal = 0;

                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                            Calificacion calif = doc.toObject(Calificacion.class);
                            lista.add(calif);

                            // Promediamos Seguridad y Tiempo para esta persona
                            float promedioUsuario = (calif.getPuntajeSeguridad() + calif.getPuntajeTiempo()) / 2.0f;
                            sumaTotal += promedioUsuario;
                        }

                        // Calculamos el promedio de todos
                        float promedioGlobal = sumaTotal / lista.size();

                        // Mostramos el texto en la parte colapsada
                        tvPromedioEstrellas.setText(String.format(java.util.Locale.getDefault(), "⭐ %.1f de 5 (%d opiniones)", promedioGlobal, lista.size()));

                        // Mandamos la lista al adaptador para que llene el menú deslizable
                        calificacionAdapter.setCalificaciones(lista);
                    } else {
                        tvPromedioEstrellas.setText("⭐ Sin calificaciones. ¡Sé el primero!");
                        calificacionAdapter.setCalificaciones(new java.util.ArrayList<>()); // Lista vacía
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Leemos la ruta específica desde Firebase
        db.collection("rutas_oficiales").document(idRuta).get().addOnSuccessListener(doc -> {
            Ruta ruta = doc.toObject(Ruta.class);
            if (ruta == null) return;

            googleMap.clear();

            // 1. Dibujamos los paraderos (Esto siempre funciona offline)
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            if (ruta.getParaderos() != null && !ruta.getParaderos().isEmpty()) {
                for (Paradero p : ruta.getParaderos()) {
                    LatLng pos = new LatLng(p.getLat(), p.getLng());
                    boundsBuilder.include(pos);
                    googleMap.addMarker(new MarkerOptions().position(pos).title(p.getNombre()));
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150));
            }

            // 2. ¿TENEMOS LA CURVA EXACTA GUARDADA?
            if (ruta.getPolyline() != null && ruta.getPolyline().length() > 50) {
                // MODO OFFLINE: Ya la tenemos, la dibujamos al instante
                dibujarCurvaEnMapa(googleMap, ruta.getPolyline());
            } else {
                // MODO ONLINE (Solo la primera vez): Preguntamos a Google y la guardamos
                if (ruta.getParaderos() != null && ruta.getParaderos().size() >= 2) {
                    calcularCurvaConGoogleYGuardar(googleMap, ruta);
                }
            }
        });
    }

    // Metodo que traza la línea en pantalla
    private void dibujarCurvaEnMapa(GoogleMap googleMap, String polylineString) {
        List<LatLng> puntosDeRuta = PolyUtil.decode(polylineString);
        if (puntosDeRuta != null && !puntosDeRuta.isEmpty()) {
            PolylineOptions lineaRuta = new PolylineOptions()
                    .width(12f)
                    .color(Color.parseColor(colorRuta))
                    .addAll(puntosDeRuta);
            googleMap.addPolyline(lineaRuta);
        }
    }

    // Metodo que se conecta a Google solo si no conocemos la curva
    private void calcularCurvaConGoogleYGuardar(GoogleMap googleMap, Ruta ruta) {
        List<Paradero> paraderos = ruta.getParaderos();
        String origen = paraderos.get(0).getLat() + "," + paraderos.get(0).getLng();
        String destino = paraderos.get(paraderos.size() - 1).getLat() + "," + paraderos.get(paraderos.size() - 1).getLng();

        StringBuilder waypoints = new StringBuilder();
        for (int i = 1; i < paraderos.size() - 1; i++) {
            waypoints.append(paraderos.get(i).getLat()).append(",").append(paraderos.get(i).getLng());
            if (i < paraderos.size() - 2) {
                waypoints.append("|");
            }
        }

        String TU_API_KEY = "AIzaSyCjDzqbvtc89eZ_IJZyEe7EO0aPiuriR_8";

        Retrofit retrofitGoogle = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GoogleMapsApi googleApi = retrofitGoogle.create(GoogleMapsApi.class);

        Toast.makeText(requireContext(), "Calculando curva precisa por primera vez...", Toast.LENGTH_SHORT).show();

        googleApi.obtenerRutaPorCalles(origen, destino, waypoints.toString(), TU_API_KEY)
                .enqueue(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                        if (response.isSuccessful() && response.body() != null && !response.body().getRoutes().isEmpty()) {
                            String polylineExacto = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();
                            dibujarCurvaEnMapa(googleMap, polylineExacto);

                            db.collection("rutas_oficiales").document(String.valueOf(ruta.getRutaID()))
                                    .update("polyline", polylineExacto)
                                    .addOnSuccessListener(aVoid -> Log.d("OfflineMap", "¡Curva guardada para modo offline!"));

                        } else {
                            String errorMsg = "Error de Google: " + response.code() + " " + response.message();
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                            Log.e("RutaCompleta", errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Toast.makeText(requireContext(), "Sin internet para calcular la curva", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // METODO PARA MOSTRAR EL MENÚ DESLIZABLE DE CALIFICACION
    private void mostrarDialogoCalificacion(FirebaseUser usuarioActual) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View viewDialogo = getLayoutInflater().inflate(R.layout.dialog_calificar_ruta, null);
        dialog.setContentView(viewDialogo);

        TextView tvTitulo = viewDialogo.findViewById(R.id.tvTituloCalificar);
        RatingBar rbSeguridad = viewDialogo.findViewById(R.id.ratingSeguridad);
        RatingBar rbTiempo = viewDialogo.findViewById(R.id.ratingTiempo);
        EditText etComentario = viewDialogo.findViewById(R.id.etComentarioRuta);
        Button btnEnviar = viewDialogo.findViewById(R.id.btnEnviarCalificacion);

        tvTitulo.setText("Calificar " + nombreRutaSeleccionada);

        btnEnviar.setOnClickListener(v -> {
            float puntajeSeg = rbSeguridad.getRating();
            float puntajeTie = rbTiempo.getRating();
            String comentario = etComentario.getText().toString().trim();

            if (puntajeSeg == 0 || puntajeTie == 0) {
                Toast.makeText(requireContext(), "Por favor, asigna estrellas a ambos rubros", Toast.LENGTH_SHORT).show();
                return;
            }

            btnEnviar.setEnabled(false);

            // Armar el objeto usando modelo y los datos de Google
            String nombre = usuarioActual.getDisplayName() != null ? usuarioActual.getDisplayName() : "Usuario";
            String email = usuarioActual.getEmail() != null ? usuarioActual.getEmail() : "Sin correo";

            Calificacion nuevaCalificacion = new Calificacion(idRuta, puntajeSeg, puntajeTie, comentario, nombre, email);

            // Guardar en Firebase
            db.collection("calificaciones_rutas")
                    .add(nuevaCalificacion)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(requireContext(), "¡Gracias por tu calificación!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error al enviar la calificación", Toast.LENGTH_SHORT).show();
                        btnEnviar.setEnabled(true);
                    });
        });

        dialog.show();
    }
}