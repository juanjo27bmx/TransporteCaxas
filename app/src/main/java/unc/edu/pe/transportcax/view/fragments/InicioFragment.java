package unc.edu.pe.transportcax.view.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import unc.edu.pe.transportcax.R;
import unc.edu.pe.transportcax.model.Alerta;
import unc.edu.pe.transportcax.model.Ruta;
import unc.edu.pe.transportcax.view.adapters.AlertaAdapter;
import unc.edu.pe.transportcax.view.adapters.RutaMiniAdapter;

public class InicioFragment extends Fragment {

    // --- VARIABLES ORIGINALES ---
    private EditText etOrigen, etDestino;
    private TextView tvAgregarEscala;
    private Button btnPlanificar;
    private LinearLayout contenedorDestinosDinamicos;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Location miUbicacionActual;

    // --- VARIABLES NUEVAS (DASHBOARD) ---
    private RecyclerView recyclerRutasInicio, recyclerAlertasInicio;
    private TextView tvTituloRutasInicio;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ENLACES ORIGINALES
        etOrigen = view.findViewById(R.id.etOrigen);
        etDestino = view.findViewById(R.id.etDestino);
        contenedorDestinosDinamicos = view.findViewById(R.id.contenedorDestinosDinamicos);
        tvAgregarEscala = view.findViewById(R.id.tvAgregarEscala);
        btnPlanificar = view.findViewById(R.id.btnPlanificar);

        // ENLACES NUEVOS (DASHBOARD)
        db = FirebaseFirestore.getInstance();
        tvTituloRutasInicio = view.findViewById(R.id.tvTituloRutasInicio);

        recyclerRutasInicio = view.findViewById(R.id.recyclerRutasInicio);
        if (recyclerRutasInicio != null) {
            recyclerRutasInicio.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        }

        recyclerAlertasInicio = view.findViewById(R.id.recyclerAlertasInicio);
        if (recyclerAlertasInicio != null) {
            recyclerAlertasInicio.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        }

        // CARGAMOS LA INFORMACIÓN DEL DASHBOARD
        cargarRutasInteligentes();
        cargarAlertasRecientes();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        solicitarPermisosDeUbicacion();

        // 1. Lógica para inyectar N campos dinámicos CON VALIDACIÓN (INTACTA)
        tvAgregarEscala.setOnClickListener(v -> {
            boolean puedeAgregar = true;

            // Verificamos si ya hay campos dinámicos creados
            int cantidadDinamicos = contenedorDestinosDinamicos.getChildCount();

            if (cantidadDinamicos == 0) {
                // Si no hay dinámicos, validamos el Destino 1 (el principal)
                if (etDestino.getText().toString().trim().isEmpty()) {
                    puedeAgregar = false;
                    etDestino.requestFocus(); // Hacemos que parpadee el cursor ahí
                }
            } else {
                // Si ya hay dinámicos, validamos el ÚLTIMO de la lista
                View ultimaVistaHija = contenedorDestinosDinamicos.getChildAt(cantidadDinamicos - 1);
                EditText etUltimoDestino = ultimaVistaHija.findViewById(R.id.etDestinoDinamico);

                if (etUltimoDestino.getText().toString().trim().isEmpty()) {
                    puedeAgregar = false;
                    etUltimoDestino.requestFocus();
                }
            }

            // Actuamos según la validación
            if (puedeAgregar) {
                agregarCampoDestinoDinamico();
            } else {
                Toast.makeText(requireContext(), "Llena el destino anterior antes de agregar otro", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Empaquetar TODOS los destinos (INTACTA)
        btnPlanificar.setOnClickListener(v -> {
            if (miUbicacionActual == null) {
                Toast.makeText(requireContext(), "Buscando tu ubicación GPS...", Toast.LENGTH_SHORT).show();
                return;
            }

            String destPrincipal = etDestino.getText().toString().trim();
            if (destPrincipal.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresa al menos tu primer destino", Toast.LENGTH_SHORT).show();
                return;
            }

            // Guardamos el destino 1
            ArrayList<String> listaDestinos = new ArrayList<>();
            listaDestinos.add(destPrincipal);

            // Bucle mágico: Leemos todos los destinos extra que el usuario haya agregado
            for (int i = 0; i < contenedorDestinosDinamicos.getChildCount(); i++) {
                View vistaHija = contenedorDestinosDinamicos.getChildAt(i);
                EditText etDinamico = vistaHija.findViewById(R.id.etDestinoDinamico);
                String destExtra = etDinamico.getText().toString().trim();

                if (!destExtra.isEmpty()) {
                    listaDestinos.add(destExtra);
                }
            }

            // Viajamos al mapa enviando la lista completa
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("LISTA_DESTINOS", listaDestinos);
            bundle.putDouble("ORIGEN_LAT", miUbicacionActual.getLatitude());
            bundle.putDouble("ORIGEN_LNG", miUbicacionActual.getLongitude());

            RutaDetalleFragment detalleFragment = new RutaDetalleFragment();
            detalleFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detalleFragment)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
        });
    }

    // =======================================================
    // NUEVOS MÉTODOS DEL DASHBOARD
    // =======================================================

    private void cargarRutasInteligentes() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Lógica inteligente: Si está registrado, buscamos si tiene rutas frecuentes
            db.collection("historial_rutas")
                    .whereEqualTo("usuarioId", user.getUid())
                    .get()
                    .addOnSuccessListener(query -> {
                        if (tvTituloRutasInicio != null) {
                            if (!query.isEmpty() && query.size() > 0) {
                                tvTituloRutasInicio.setText("Tus rutas frecuentes");
                                // (Futuro: cargar aquí las rutas frecuentes del usuario)
                                cargarRutasPopularesGlobales();
                            } else {
                                tvTituloRutasInicio.setText("Rutas más populares");
                                cargarRutasPopularesGlobales();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if(tvTituloRutasInicio != null) tvTituloRutasInicio.setText("Rutas sugeridas");
                        cargarRutasPopularesGlobales();
                    });
        } else {
            // Usuario invitado: Solo sugerencias globales
            if(tvTituloRutasInicio != null) tvTituloRutasInicio.setText("Sugerencias para ti");
            cargarRutasPopularesGlobales();
        }
    }

    private void cargarRutasPopularesGlobales() {
        if (recyclerRutasInicio == null) return;

        db.collection("rutas_oficiales")
                .limit(5)
                .get()
                .addOnSuccessListener(query -> {
                    List<Ruta> rutasSugeridas = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        rutasSugeridas.add(doc.toObject(Ruta.class));
                    }
                    RutaMiniAdapter miniAdapter = new RutaMiniAdapter(rutasSugeridas);
                    recyclerRutasInicio.setAdapter(miniAdapter);
                    recyclerRutasInicio.scheduleLayoutAnimation();
                });
    }

    private void cargarAlertasRecientes() {
        if (recyclerAlertasInicio == null) return;

        db.collection("alertas_trafico")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(3)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null && !value.isEmpty()) {
                        List<Alerta> ultimasAlertas = new ArrayList<>();
                        for (DocumentSnapshot doc : value) {
                            ultimasAlertas.add(doc.toObject(Alerta.class));
                        }

                        // CORRECCIÓN: Ahora solo le pasamos 1 argumento (la lista), tal como espera tu adaptador.
                        AlertaAdapter alertaAdapter = new AlertaAdapter(ultimasAlertas);
                        recyclerAlertasInicio.setAdapter(alertaAdapter);
                    }
                });
    }

    // Método para inyectar visualmente un nuevo destino
    private void agregarCampoDestinoDinamico() {
        // Inflamos (creamos) la vista desde el XML
        View vistaNueva = getLayoutInflater().inflate(R.layout.item_destino_dinamico, contenedorDestinosDinamicos, false);

        // Configuramos el botón de la "X" para que se elimine a sí mismo
        ImageButton btnQuitar = vistaNueva.findViewById(R.id.btnQuitarDestino);
        btnQuitar.setOnClickListener(v -> contenedorDestinosDinamicos.removeView(vistaNueva));

        // Lo agregamos al contenedor de la pantalla principal
        contenedorDestinosDinamicos.addView(vistaNueva);
    }

    // LÓGICA DE GPS Y GEOCODIFICACIÓN (CALLES REALES
    private void solicitarPermisosDeUbicacion() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            obtenerUbicacionActual();
        }
    }

    private void obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    miUbicacionActual = location;

                    // Magia de Geocodificación: Convertir GPS a Calle
                    Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                    try {
                        List<Address> direcciones = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (direcciones != null && !direcciones.isEmpty()) {
                            // Extraemos el nombre de la calle/avenida
                            String direccionReal = direcciones.get(0).getAddressLine(0);

                            // Limpiamos el texto (a veces dice "Cajamarca, Peru", lo dejamos más limpio)
                            if (direccionReal.contains(",")) {
                                direccionReal = direccionReal.split(",")[0];
                            }

                            etOrigen.setText("📍 " + direccionReal);
                        } else {
                            etOrigen.setText("📍 Ubicación detectada");
                        }
                    } catch (Exception e) {
                        // Si falla el internet, mostramos las coordenadas puras
                        etOrigen.setText("📍 Coordenadas: " + location.getLatitude() + ", " + location.getLongitude());
                    }
                } else {
                    etOrigen.setText("📍 Ubicación no disponible");
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual();
        } else {
            Toast.makeText(requireContext(), "Se necesita permiso de GPS", Toast.LENGTH_SHORT).show();
        }
    }
}