package unc.edu.pe.transportcax.view.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import unc.edu.pe.transportcax.R;

public class UsuarioFragment extends Fragment {

    // Vistas del XML
    private ImageView ivPerfilFoto;
    private TextView tvNombreUsuario, tvCorreoUsuario;
    private SignInButton btnGoogleLogin;
    private Button btnCerrarSesion;

    // Herramientas de Firebase y Google
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    // Lanzador moderno para la ventana de Google
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        // Cuenta de Google obtenida correctamente, ahora la conectamos con Firebase
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.w("GoogleAuth", "El inicio de sesión falló", e);
                        Toast.makeText(requireContext(), "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_usuario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Enlazamos los elementos visuales
        ivPerfilFoto = view.findViewById(R.id.ivPerfilFoto);
        tvNombreUsuario = view.findViewById(R.id.tvNombreUsuario);
        tvCorreoUsuario = view.findViewById(R.id.tvCorreoUsuario);
        btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);

        // 2. Inicializamos Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 3. Configuramos cómo queremos que Google inicie sesión (Pedimos Correo y Token)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Este ID lo genera tu google-services.json automáticamente
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // 4. Qué pasa al hacer clic en "Iniciar Sesión"
        btnGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // 5. Qué pasa al hacer clic en "Cerrar Sesión"
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        // 6. Actualizar la pantalla si el usuario ya había iniciado sesión antes
        actualizarUI(mAuth.getCurrentUser());
    }

    private void firebaseAuthWithGoogle(String idToken) {
        // Intercambiamos la credencial de Google por una sesión oficial de Firebase
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "¡Bienvenido a TransportCax!", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        actualizarUI(user);
                    } else {
                        Toast.makeText(requireContext(), "Autenticación de Firebase fallida.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cerrarSesion() {
        // Desconectamos de Firebase
        mAuth.signOut();
        // Desconectamos la cuenta de Google del dispositivo
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            actualizarUI(null);
            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();
        });
    }

    private void actualizarUI(FirebaseUser user) {
        if (user != null) {
            // Usuario Logeado
            tvNombreUsuario.setText(user.getDisplayName());
            tvCorreoUsuario.setText(user.getEmail());

            // Escondemos el botón de login y mostramos el de cerrar sesión
            btnGoogleLogin.setVisibility(View.GONE);
            btnCerrarSesion.setVisibility(View.VISIBLE);

        } else {
            // Usuario Invitado
            tvNombreUsuario.setText("Invitado");
            tvCorreoUsuario.setText("Inicia sesión para reportar alertas");

            // Mostramos el botón de login y escondemos el de cerrar sesión
            btnGoogleLogin.setVisibility(View.VISIBLE);
            btnCerrarSesion.setVisibility(View.GONE);
        }
    }
}