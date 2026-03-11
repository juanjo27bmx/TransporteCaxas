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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private ProgressBar pbAlertasMain;
    private AlertaAdapter alertaAdapter;
    private List<Alerta> listaAlertas;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText etLugarActual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alertas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerAlertas = view.findViewById(R.id.recycler_alertas);
        pbAlertasMain = view.findViewById(R.id.pbAlertasMain);
        FloatingActionButton fabAgregar = view.findViewById(R.id.fabAgregarAlerta);

        recyclerAlertas.setLayoutManager(new LinearLayoutManager(getContext()));
        listaAlertas = new ArrayList<>();
        alertaAdapter = new AlertaAdapter(listaAlertas);
        recyclerAlertas.setAdapter(alertaAdapter);

        escucharAlertasDesdeFirebase();

        getParentFragmentManager().setFragmentResultListener("requestKeyUbicacion", getViewLifecycleOwner(), (requestKey, bundle) -> {
            String direccionResultante = bundle.getString("DIRECCION_SELECCIONADA");
            if (etLugarActual != null) {
                etLugarActual.setText(direccionResultante);
            }
        });

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
                        if (pbAlertasMain != null) pbAlertasMain.setVisibility(View.GONE);
                        return;
                    }

                    if (value != null) {
                        listaAlertas.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Alerta alertaDescargada = doc.toObject(Alerta.class);
                            listaAlertas.add(alertaDescargada);
                        }
                        alertaAdapter.notifyDataSetChanged();

                        if (pbAlertasMain != null) pbAlertasMain.setVisibility(View.GONE);
                        if (recyclerAlertas != null) recyclerAlertas.setVisibility(View.VISIBLE);
                        if (recyclerAlertas != null) recyclerAlertas.scheduleLayoutAnimation();
                    }
                });
    }

    private void mostrarDialogoAgregarAlerta() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View viewDialogo = getLayoutInflater().inflate(R.layout.dialog_agregar_alerta, null);
        dialog.setContentView(viewDialogo);

        etLugarActual = viewDialogo.findViewById(R.id.etLugarAlerta);
        ImageButton btnAbrirMapa = viewDialogo.findViewById(R.id.btnAbrirMapa);
        Spinner spinnerGravedad = viewDialogo.findViewById(R.id.spinnerGravedad);
        EditText etDescripcion = viewDialogo.findViewById(R.id.etDescripcionAlerta);
        Button btnPublicar = viewDialogo.findViewById(R.id.btnPublicarAlerta);

        String[] opcionesGravedad = {"🟡 Leve (Tráfico lento)", "🟠 Moderado (Vía parcialmente cerrada)", "🔴 Grave (Choque / Cierre total)"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, opcionesGravedad);
        spinnerGravedad.setAdapter(adapterSpinner);

        if (btnAbrirMapa != null) {
            btnAbrirMapa.setOnClickListener(v -> {
                MapaUbicacionDialog mapaDialog = new MapaUbicacionDialog();
                mapaDialog.show(getParentFragmentManager(), "MapaDialog");
            });
        }

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
}