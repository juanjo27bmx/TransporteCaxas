package unc.edu.pe.transportcax.view.fragments;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import unc.edu.pe.transportcax.R;
import unc.edu.pe.transportcax.model.Paradero;
import unc.edu.pe.transportcax.model.Ruta;
import unc.edu.pe.transportcax.viewmodel.MapViewModel;

public class RutaDetalleFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapViewModel mapViewModel;

    // Variables recibidas
    private double origenLat = 0.0;
    private double origenLng = 0.0;
    private ArrayList<String> listaDestinos = new ArrayList<>();

    // Elementos de la UI
    private TextView tvOrigenTop, tvInstruccionPasos, tvTiempoCaminata;
    private Button btnFinalizarViaje;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ruta_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvOrigenTop = view.findViewById(R.id.tvOrigenTop);
        tvInstruccionPasos = view.findViewById(R.id.tvInstruccionPasos);
        tvTiempoCaminata = view.findViewById(R.id.tvTiempoCaminata);
        btnFinalizarViaje = view.findViewById(R.id.btnFinalizarViaje);

        // Extraemos LA LISTA de destinos de la mochila
        if (getArguments() != null) {
            listaDestinos = getArguments().getStringArrayList("LISTA_DESTINOS");
            origenLat = getArguments().getDouble("ORIGEN_LAT", 0.0);
            origenLng = getArguments().getDouble("ORIGEN_LNG", 0.0);

            tvOrigenTop.setText("Planificador Multi-Destino");
        }

        mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment_detalle);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnFinalizarViaje.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        calcularCalculadoraDeRutas();
    }

    private void calcularCalculadoraDeRutas() {
        if (origenLat == 0.0 || origenLng == 0.0 || listaDestinos == null || listaDestinos.isEmpty()) return;

        mapViewModel.getListaRutasLiveData().observe(getViewLifecycleOwner(), rutasDeLaApi -> {
            if (rutasDeLaApi == null || rutasDeLaApi.isEmpty()) return;

            mMap.clear();
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

            double costoTotalPasajes = 0.0;
            StringBuilder ticketResumen = new StringBuilder();

            // El punto de partida inicial es tu GPS
            LatLng puntoDePartidaActual = new LatLng(origenLat, origenLng);
            boundsBuilder.include(puntoDePartidaActual);

            mMap.addMarker(new MarkerOptions().position(puntoDePartidaActual).title("📍 Tu Inicio"));

            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

            // Preparar conexión a Google Maps API
            retrofit2.Retrofit retrofitGoogle = new retrofit2.Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/")
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .build();
            unc.edu.pe.transportcax.network.GoogleMapsApi googleApi = retrofitGoogle.create(unc.edu.pe.transportcax.network.GoogleMapsApi.class);
            String TU_API_KEY = "AIzaSyCjDzqbvtc89eZ_IJZyEe7EO0aPiuriR_8"; // Tu API Key

            // BUCLE MAESTRO: Caminata -> Combi -> Caminata
            for (int i = 0; i < listaDestinos.size(); i++) {
                String destinoObjetivo = listaDestinos.get(i);
                LatLng coordenadasDestinoFinal = null;

                // 1. Buscamos la coordenada exacta del lugar a donde quiere ir
                try {
                    List<Address> addresses = geocoder.getFromLocationName(destinoObjetivo + ", Cajamarca, Perú", 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        coordenadasDestinoFinal = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Ruta rutaOptima = null;
                Paradero paraderoSubidaOptimo = null;
                Paradero paraderoBajadaOptimo = null;
                float distanciaMinimaTotalCaminada = Float.MAX_VALUE;

                // 2. Buscamos la mejor ruta combinada
                for (Ruta ruta : rutasDeLaApi) {
                    Paradero posibleBajada = null;
                    float distanciaCaminataBajada = Float.MAX_VALUE;

                    // PASO A: Buscar el paradero donde debe bajarse
                    if (coordenadasDestinoFinal != null) {
                        for (Paradero p : ruta.getParaderos()) {
                            float[] res = new float[1];
                            Location.distanceBetween(coordenadasDestinoFinal.latitude, coordenadasDestinoFinal.longitude, p.getLat(), p.getLng(), res);
                            if (res[0] < distanciaCaminataBajada) {
                                distanciaCaminataBajada = res[0];
                                posibleBajada = p;
                            }
                        }
                        if (distanciaCaminataBajada > 1500) {
                            posibleBajada = null;
                        }
                    } else {
                        for (Paradero p : ruta.getParaderos()) {
                            if (p.getNombre().toLowerCase().contains(destinoObjetivo.toLowerCase())) {
                                posibleBajada = p;
                                distanciaCaminataBajada = 0;
                                break;
                            }
                        }
                    }

                    // PASO B: Buscar el paradero donde debe subirse
                    if (posibleBajada != null) {
                        Paradero posibleSubida = null;
                        float distanciaCaminataSubida = Float.MAX_VALUE;

                        for (Paradero p : ruta.getParaderos()) {
                            float[] res = new float[1];
                            Location.distanceBetween(puntoDePartidaActual.latitude, puntoDePartidaActual.longitude, p.getLat(), p.getLng(), res);

                            if (res[0] < distanciaCaminataSubida) {
                                distanciaCaminataSubida = res[0];
                                posibleSubida = p;
                            }
                        }

                        // PASO C: Sumamos ambas caminatas
                        float caminataTotalRuta = distanciaCaminataSubida + distanciaCaminataBajada;

                        if (caminataTotalRuta < distanciaMinimaTotalCaminada) {
                            distanciaMinimaTotalCaminada = caminataTotalRuta;
                            paraderoSubidaOptimo = posibleSubida;
                            paraderoBajadaOptimo = posibleBajada;
                            rutaOptima = ruta;
                        }
                    }
                }

                // 3. Evaluamos resultados y dibujamos
                if (rutaOptima != null && paraderoSubidaOptimo != null && paraderoBajadaOptimo != null) {

                    costoTotalPasajes += rutaOptima.getCostoViaje();

                    ticketResumen.append("<b>• Tramo ").append(i + 1).append(":</b><br>")
                            .append("&nbsp;&nbsp;🚶 <font color='#4CAF50'><b>Camina a:</b> ").append(paraderoSubidaOptimo.getNombre()).append("</font><br>")
                            .append("&nbsp;&nbsp;🚌 <b>Toma:</b> ").append(rutaOptima.getNombreRuta()).append(" (S/ ").append(String.format(Locale.getDefault(), "%.2f", rutaOptima.getCostoViaje())).append(")<br>")
                            .append("&nbsp;&nbsp;🛑 <font color='#FF9800'><b>Bájate en:</b> ").append(paraderoBajadaOptimo.getNombre()).append("</font><br>");

                    LatLng posSubida = new LatLng(paraderoSubidaOptimo.getLat(), paraderoSubidaOptimo.getLng());
                    LatLng posBajada = new LatLng(paraderoBajadaOptimo.getLat(), paraderoBajadaOptimo.getLng());

                    boundsBuilder.include(posSubida);
                    boundsBuilder.include(posBajada);

                    mMap.addMarker(new MarkerOptions().position(posSubida)
                            .title("Sube aquí: " + paraderoSubidaOptimo.getNombre())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    mMap.addMarker(new MarkerOptions().position(posBajada)
                            .title("Bájate aquí: " + paraderoBajadaOptimo.getNombre())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                    // 1er Trazado: Caminata inicial (Gris)
                    mMap.addPolyline(new PolylineOptions().add(puntoDePartidaActual, posSubida)
                            .color(Color.GRAY).width(8f).pattern(Arrays.asList(new Dash(20), new Gap(20))));

                    // --- 2do Trazado: La Combi usando Google Maps Directions (Con Calles Exactas y Color API) ---
                    String colorApi = rutaOptima.getColorHex() != null ? rutaOptima.getColorHex() : "#1A73E8";

                    // Extraemos los paraderos intermedios
                    java.util.List<Paradero> paraderosRuta = rutaOptima.getParaderos();
                    int idxSubida = paraderosRuta.indexOf(paraderoSubidaOptimo);
                    int idxBajada = paraderosRuta.indexOf(paraderoBajadaOptimo);

                    StringBuilder waypoints = new StringBuilder();
                    if (idxSubida != -1 && idxBajada != -1 && idxSubida != idxBajada) {
                        int step = (idxSubida < idxBajada) ? 1 : -1;
                        for (int j = idxSubida + step; j != idxBajada; j += step) {
                            Paradero intermedio = paraderosRuta.get(j);
                            waypoints.append(intermedio.getLat()).append(",").append(intermedio.getLng()).append("|");
                        }
                        if (waypoints.length() > 0 && waypoints.charAt(waypoints.length() - 1) == '|') {
                            waypoints.setLength(waypoints.length() - 1);
                        }
                    }

                    String origenGoogle = posSubida.latitude + "," + posSubida.longitude;
                    String destinoGoogle = posBajada.latitude + "," + posBajada.longitude;

                    // Obtenemos la ruta por las calles desde Google
                    googleApi.obtenerRutaPorCalles(origenGoogle, destinoGoogle, waypoints.toString(), TU_API_KEY)
                            .enqueue(new retrofit2.Callback<unc.edu.pe.transportcax.model.DirectionsResponse>() {
                                @Override
                                public void onResponse(retrofit2.Call<unc.edu.pe.transportcax.model.DirectionsResponse> call,
                                                       retrofit2.Response<unc.edu.pe.transportcax.model.DirectionsResponse> response) {
                                    if (response.isSuccessful() && response.body() != null && response.body().getRoutes() != null && !response.body().getRoutes().isEmpty()) {
                                        String polylineExacto = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();
                                        mMap.addPolyline(new PolylineOptions()
                                                .width(12f)
                                                .color(android.graphics.Color.parseColor(colorApi))
                                                .addAll(com.google.maps.android.PolyUtil.decode(polylineExacto)));
                                    } else {
                                        // Plan B (Línea recta)
                                        mMap.addPolyline(new PolylineOptions().add(posSubida, posBajada)
                                                .color(android.graphics.Color.parseColor(colorApi)).width(12f));
                                    }
                                }

                                @Override
                                public void onFailure(retrofit2.Call<unc.edu.pe.transportcax.model.DirectionsResponse> call, Throwable t) {
                                    // Plan B (Línea recta)
                                    mMap.addPolyline(new PolylineOptions().add(posSubida, posBajada)
                                            .color(android.graphics.Color.parseColor(colorApi)).width(12f));
                                }
                            });

                    // 3er Trazado: Caminata Final (De bajada al destino)
                    if (coordenadasDestinoFinal != null) {
                        ticketResumen.append("&nbsp;&nbsp;🚶 <font color='#E91E63'><b>Camina hasta:</b> ").append(destinoObjetivo).append("</font><br><br>");

                        mMap.addMarker(new MarkerOptions().position(coordenadasDestinoFinal)
                                .title("Destino: " + destinoObjetivo)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

                        mMap.addPolyline(new PolylineOptions().add(posBajada, coordenadasDestinoFinal)
                                .color(Color.GRAY).width(8f).pattern(Arrays.asList(new Dash(20), new Gap(20))));

                        boundsBuilder.include(coordenadasDestinoFinal);

                        // El próximo destino inicia desde el lugar físico
                        puntoDePartidaActual = coordenadasDestinoFinal;
                    } else {
                        ticketResumen.append("<br>");
                        // El próximo viaje inicia desde el paradero de bajada
                        puntoDePartidaActual = posBajada;
                    }

                } else {
                    if (i == 0) {
                        ticketResumen.append("<font color='#F44336'>❌ No encontramos rutas cercanas a '")
                                .append(destinoObjetivo).append("'.</font><br><br>")
                                .append("<i>💡 Sugerencia: Verifica la ortografía.</i><br>");
                    } else {
                        ticketResumen.append("<b>• Tramo ").append(i + 1).append(":</b> '").append(destinoObjetivo)
                                .append("' <font color='#F44336'>❌ Destino inaccesible.</font><br>")
                                .append("<i>⚠️ Tu viaje se interrumpió aquí.</i><br><br>");
                    }
                    Toast.makeText(requireContext(), "Destino fuera de alcance: " + destinoObjetivo, Toast.LENGTH_LONG).show();
                    break;
                }
            }

            // Actualizamos la Tarjeta Visual
            tvInstruccionPasos.setSingleLine(false);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                tvInstruccionPasos.setText(android.text.Html.fromHtml(ticketResumen.toString(), android.text.Html.FROM_HTML_MODE_LEGACY));
            } else {
                tvInstruccionPasos.setText(android.text.Html.fromHtml(ticketResumen.toString()));
            }

            tvTiempoCaminata.setTextColor(Color.parseColor("#4CAF50"));
            tvTiempoCaminata.setText("💰 TOTAL PASAJES: S/ " + String.format(Locale.getDefault(), "%.2f", costoTotalPasajes));

            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 200));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}