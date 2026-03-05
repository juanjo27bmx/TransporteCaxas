package unc.edu.pe.transportcax.view.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

// Importaciones nuevas para la ubicación de alta precisión
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import unc.edu.pe.transportcax.R;

public class MapaUbicacionDialog extends DialogFragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    // 1. Herramienta de ubicación fusionada (GPS + Wi-Fi)
    private FusedLocationProviderClient fusedLocationClient;

    // 2. Lanzador automático para pedir permiso de ubicación si el usuario no lo tiene
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    obtenerUbicacionExacta();
                } else {
                    irACajamarcaPorDefecto(); // Plan B si rechaza el permiso
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen);

        // Inicializamos la herramienta de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mapa_ubicacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapaSeleccion);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnConfirmar = view.findViewById(R.id.btnConfirmarUbicacion);
        btnConfirmar.setOnClickListener(v -> {
            if (mMap != null) {
                LatLng centroDelMapa = mMap.getCameraPosition().target;
                String direccionTraducida = obtenerNombreDeCalle(centroDelMapa.latitude, centroDelMapa.longitude);

                Bundle result = new Bundle();
                result.putString("DIRECCION_SELECCIONADA", direccionTraducida);
                getParentFragmentManager().setFragmentResult("requestKeyUbicacion", result);

                dismiss();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 3. Verificamos si tenemos permiso antes de buscar la ubicación
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true); // Activa el punto azul de Google
            obtenerUbicacionExacta();
        } else {
            // Si no hay permiso, lanzamos la ventanita pidiéndolo
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    // NUEVOS MÉTODOS DE UBICACIÓN
    private void obtenerUbicacionExacta() {
        try {
            // Le pedimos la última ubicación conocida (rápida y precisa bajo techo)
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    // ¡Tenemos la ubicación exacta!
                    LatLng miPosicion = new LatLng(location.getLatitude(), location.getLongitude());
                    // Hacemos una animación fluida hacia esa calle con un zoom cercano
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 17f));
                } else {
                    irACajamarcaPorDefecto();
                }
            });
        } catch (SecurityException e) {
            irACajamarcaPorDefecto();
        }
    }

    // Plan B: Si no hay GPS o el usuario rechaza el permiso, lo enviamos al centro
    private void irACajamarcaPorDefecto() {
        LatLng cajamarca = new LatLng(-7.1617, -78.5128);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cajamarca, 15f));
    }

    private String obtenerNombreDeCalle(double lat, double lng) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> direcciones = geocoder.getFromLocation(lat, lng, 1);
            if (direcciones != null && !direcciones.isEmpty()) {
                Address direccion = direcciones.get(0);
                String nombreTraducido = direccion.getAddressLine(0);

                // Si Google nos da un código raro con un "+", lo maquillamos
                if (nombreTraducido != null && nombreTraducido.contains("+")) {
                    return "Ubicación seleccionada en el mapa";
                }

                return nombreTraducido;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Ubicación seleccionada en el mapa";
    }
}