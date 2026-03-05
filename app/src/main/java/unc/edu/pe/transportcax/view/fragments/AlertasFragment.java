package unc.edu.pe.transportcax.view.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// Librerías de Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import unc.edu.pe.transportcax.R;
import unc.edu.pe.transportcax.model.Alerta;
import unc.edu.pe.transportcax.view.adapters.AlertaAdapter;

public class AlertasFragment extends Fragment {

    private RecyclerView recyclerAlertas;
    private AlertaAdapter alertaAdapter;
    private List<Alerta> listaAlertas;

    // Herramientas de Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Guardamos la referencia de la cajita de texto para poder pegarle la dirección del mapa
    private EditText etLugarActual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alertas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicializamos Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerAlertas = view.findViewById(R.id.recycler_alertas);
        FloatingActionButton fabAgregar = view.findViewById(R.id.fabAgregarAlerta);

        // Configuramos el RecyclerView
        recyclerAlertas.setLayoutManager(new LinearLayoutManager(getContext()));
        listaAlertas = new ArrayList<>();
        alertaAdapter = new AlertaAdapter(listaAlertas);
        recyclerAlertas.setAdapter(alertaAdapter);

        // 2. Cargamos los datos reales desde la nube
        escucharAlertasDesdeFirebase();

        // 3. Escucha cuando el mapa se cierra y atrapa la dirección
        getParentFragmentManager().setFragmentResultListener("requestKeyUbicacion", getViewLifecycleOwner(), (requestKey, bundle) -> {
            String direccionResultante = bundle.getString("DIRECCION_SELECCIONADA");
            if (etLugarActual != null) {
                etLugarActual.setText(direccionResultante);
            }
        });

        // 4. Configurar el botón flotante con el candado de seguridad
        fabAgregar.setOnClickListener(v -> {
            FirebaseUser usuarioActual = mAuth.getCurrentUser();
            if (usuarioActual != null) {
                mostrarDialogoAgregarAlerta();
            } else {
                Toast.makeText(requireContext(), "Inicia sesión en la pestaña Perfil para reportar el tráfico.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void escucharAlertasDesdeFirebase() {
        db.collection("alertas_trafico")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firebase", "Error al conectar con Firestore", error);
                        return;
                    }

                    if (value != null) {
                        listaAlertas.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Alerta alertaDescargada = doc.toObject(Alerta.class);
                            listaAlertas.add(alertaDescargada);
                        }
                        alertaAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void mostrarDialogoAgregarAlerta() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View viewDialogo = getLayoutInflater().inflate(R.layout.dialog_agregar_alerta, null);
        dialog.setContentView(viewDialogo);

        // Referencias a los campos del XML
        etLugarActual = viewDialogo.findViewById(R.id.etLugarAlerta);
        ImageButton btnAbrirMapa = viewDialogo.findViewById(R.id.btnAbrirMapa); // Nuestro botón de GPS
        Spinner spinnerGravedad = viewDialogo.findViewById(R.id.spinnerGravedad);
        EditText etDescripcion = viewDialogo.findViewById(R.id.etDescripcionAlerta);
        Button btnPublicar = viewDialogo.findViewById(R.id.btnPublicarAlerta);

        // Poblar el Spinner
        String[] opcionesGravedad = {"🟡 Leve (Tráfico lento)", "🟠 Moderado (Vía parcialmente cerrada)", "🔴 Grave (Choque / Cierre total)"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, opcionesGravedad);
        spinnerGravedad.setAdapter(adapterSpinner);

        // ABRIR EL MAPA
        if (btnAbrirMapa != null) {
            btnAbrirMapa.setOnClickListener(v -> {
                MapaUbicacionDialog mapaDialog = new MapaUbicacionDialog();
                mapaDialog.show(getParentFragmentManager(), "MapaDialog");
            });
        }

        // PUBLICAR EN LA NUBE
        btnPublicar.setOnClickListener(v -> {
            String lugar = etLugarActual.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();
            String gravedad = spinnerGravedad.getSelectedItem().toString();

            if (lugar.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            btnPublicar.setEnabled(false);

            FirebaseUser user = mAuth.getCurrentUser();
            String nombreAutor = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Conductor Anónimo";

            Alerta nuevaAlerta = new Alerta(lugar, gravedad, descripcion, nombreAutor);

            db.collection("alertas_trafico")
                    .add(nuevaAlerta)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(requireContext(), "¡Alerta publicada!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error de red. Intenta nuevamente.", Toast.LENGTH_SHORT).show();
                        btnPublicar.setEnabled(true);
                    });
        });

        dialog.show();
    }

    // INYECTAR DATOS DE PRUEBA
    private void inyectarDatosDePrueba() {
        Alerta alerta1 = new Alerta(
                "Av. Atahualpa (Frente a la UNC)",
                "🟡 Leve (Tráfico lento)",
                "Mucha congestión vehicular por el horario de salida de los estudiantes. Avanza lento pero seguro.",
                "Clever"
        );

        Alerta alerta2 = new Alerta(
                "Av. Vía de Evitamiento (Óvalo Musical)",
                "🔴 Grave (Choque / Cierre total)",
                "Fuerte choque entre una combi y un auto particular. La vía está completamente bloqueada, tomar desvíos urgentes.",
                "Juan Pérez"
        );

        Alerta alerta3 = new Alerta(
                "Vía a Baños del Inca",
                "🟠 Moderado (Vía parcialmente cerrada)",
                "Trabajos de mantenimiento en el carril derecho. Hay maquinaria pesada en la zona trabajando.",
                "Lidia Garay"
        );

        db.collection("alertas_trafico").add(alerta1);
        db.collection("alertas_trafico").add(alerta2);
        db.collection("alertas_trafico").add(alerta3);

        Toast.makeText(requireContext(), "Datos de prueba inyectados a Firebase", Toast.LENGTH_SHORT).show();
    }
}