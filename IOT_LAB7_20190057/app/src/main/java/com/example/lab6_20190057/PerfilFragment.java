package com.example.lab6_20190057;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.InputStream;

public class PerfilFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1001;

    // Views principales
    private MaterialButton btnConectar, btnSeleccionarImagen, btnSubirImagen, btnDescargarImagen, btnCerrarSesion, btnCopiarUrl;
    private ImageView ivImagenSeleccionada, ivImagenDescargada;
    private TextInputEditText etS3Key;
    private TextView tvEstadoConexion, tvUrlCompleta;
    private LinearLayout layoutImagenDescargada;

    // Variables de estado
    private CloudStorage cloudStorage;
    private Bitmap imagenSeleccionada;
    private boolean conectadoAWS = false;
    private FirebaseAuth mAuth;
    private String ultimaUrlCompleta = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sistema_laboratorio7, container, false);

        initViews(view);
        setupClickListeners();
        initCloudStorage();

        // Conectar automáticamente al inicio (invisible para el usuario)
        conectarAWSInternamente();

        return view;
    }

    private void initViews(View view) {
        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Views del laboratorio 7
        tvEstadoConexion = view.findViewById(R.id.tvEstadoConexion);
        btnConectar = view.findViewById(R.id.btnConectar);
        btnSeleccionarImagen = view.findViewById(R.id.btnSeleccionarImagen);
        btnSubirImagen = view.findViewById(R.id.btnSubirImagen);
        btnDescargarImagen = view.findViewById(R.id.btnDescargarImagen);
        ivImagenSeleccionada = view.findViewById(R.id.ivImagenSeleccionada);
        ivImagenDescargada = view.findViewById(R.id.ivImagenDescargada);
        etS3Key = view.findViewById(R.id.etS3Key);
        layoutImagenDescargada = view.findViewById(R.id.layoutImagenDescargada);
        tvUrlCompleta = view.findViewById(R.id.tvUrlCompleta);
        btnCopiarUrl = view.findViewById(R.id.btnCopiarUrl);

        // Crear botón de cerrar sesión dinámicamente
        crearBotonCerrarSesion(view);

        // Mostrar info del usuario
        mostrarInfoUsuario();

        // Inicialmente deshabilitar botón de descarga
        btnDescargarImagen.setEnabled(false);
        btnDescargarImagen.setAlpha(0.5f);
    }

    private void crearBotonCerrarSesion(View view) {
        // Encontrar el LinearLayout principal dentro del ScrollView
        LinearLayout mainLinearLayout = null;

        // Buscar el LinearLayout principal que contiene todos los elementos
        ViewGroup parent = view.findViewById(R.id.tvEstadoConexion).getParent().getParent() instanceof LinearLayout
                ? (LinearLayout) view.findViewById(R.id.tvEstadoConexion).getParent().getParent()
                : null;

        if (parent instanceof LinearLayout) {
            mainLinearLayout = (LinearLayout) parent;
        } else {
            // Buscar recursivamente el LinearLayout principal
            ViewGroup scrollContent = (ViewGroup) view;
            mainLinearLayout = findMainLinearLayout(scrollContent);
        }

        // Crear el botón de cerrar sesión
        btnCerrarSesion = new MaterialButton(getContext());
        btnCerrarSesion.setText("Cerrar Sesión");
        btnCerrarSesion.setTextSize(16);
        btnCerrarSesion.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        btnCerrarSesion.setTextColor(getResources().getColor(android.R.color.white));

        // Configurar layout params
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (52 * getResources().getDisplayMetrics().density)
        );
        params.setMargins(
                (int) (16 * getResources().getDisplayMetrics().density),
                (int) (32 * getResources().getDisplayMetrics().density), // Más margen superior
                (int) (16 * getResources().getDisplayMetrics().density),
                (int) (24 * getResources().getDisplayMetrics().density)  // Margen inferior
        );
        btnCerrarSesion.setLayoutParams(params);

        // Agregar al FINAL del LinearLayout principal
        if (mainLinearLayout != null) {
            mainLinearLayout.addView(btnCerrarSesion); // Se agregará al final automáticamente
            System.out.println("✅ Botón cerrar sesión agregado al final del layout");
        } else {
            System.out.println("❌ No se encontró el LinearLayout principal");
        }
    }

    // Método auxiliar para encontrar el LinearLayout principal
    private LinearLayout findMainLinearLayout(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof LinearLayout) {
                return (LinearLayout) child;
            } else if (child instanceof ViewGroup) {
                LinearLayout found = findMainLinearLayout((ViewGroup) child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void mostrarInfoUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String displayName = user.getDisplayName();

            System.out.println("👤 Usuario logueado: " + (displayName != null ? displayName : email));
        }
    }

    private void setupClickListeners() {
        btnConectar.setOnClickListener(v -> conectarAWSInternamente());
        btnSeleccionarImagen.setOnClickListener(v -> seleccionarImagen());
        btnSubirImagen.setOnClickListener(v -> subirImagen());
        btnDescargarImagen.setOnClickListener(v -> descargarImagen());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
        btnCopiarUrl.setOnClickListener(v -> copiarUrlAlPortapapeles());
    }

    private void initCloudStorage() {
        cloudStorage = new CloudStorage(getContext());
        System.out.println("☁️ CloudStorage inicializado");
    }

    private void conectarAWSInternamente() {
        System.out.println("🔗 Conectando a AWS internamente...");

        cloudStorage.conectarServicioAlmacenamiento(new CloudStorage.ConnectionCallback() {
            @Override
            public void onConnected() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        conectadoAWS = true;
                        System.out.println("✅ AWS conectado internamente");
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        conectadoAWS = false;
                        System.out.println("❌ Error conexión AWS: " + error);
                    });
                }
            }
        });
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
                imagenSeleccionada = BitmapFactory.decodeStream(inputStream);

                // Mostrar preview
                ivImagenSeleccionada.setImageBitmap(imagenSeleccionada);

                // Habilitar botón de subida con animación
                btnSubirImagen.setEnabled(true);
                btnSubirImagen.setAlpha(1.0f);

                Toast.makeText(getContext(), "✅ Imagen seleccionada", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                Toast.makeText(getContext(), "❌ Error cargando imagen", Toast.LENGTH_SHORT).show();
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void subirImagen() {
        if (!conectadoAWS) {
            Toast.makeText(getContext(), "⚠️ Conectando a AWS...", Toast.LENGTH_SHORT).show();
            conectarAWSInternamente();
            return;
        }

        if (imagenSeleccionada == null) {
            Toast.makeText(getContext(), "⚠️ Selecciona una imagen primero", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "❌ Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón durante subida
        btnSubirImagen.setEnabled(false);
        btnSubirImagen.setText("Subiendo...");
        btnSubirImagen.setAlpha(0.5f);

        String fileName = "perfil_" + System.currentTimeMillis() + ".jpg";
        String userId = user.getUid();

        cloudStorage.guardarArchivo(imagenSeleccionada, fileName, userId, new CloudStorage.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Restaurar botón
                        btnSubirImagen.setEnabled(true);
                        btnSubirImagen.setText("Carga");
                        btnSubirImagen.setAlpha(1.0f);

                        // Extraer S3 key y guardar URL completa
                        String s3Key = CloudStorage.extraerS3KeyDeUrl(downloadUrl);
                        ultimaUrlCompleta = downloadUrl;

                        Toast.makeText(getContext(),
                                "✅ Imagen subida exitosamente a la nube!",
                                Toast.LENGTH_LONG).show();

                        // Auto-llenar campo interno (oculto) y habilitar descarga
                        etS3Key.setText(s3Key);
                        btnDescargarImagen.setEnabled(true);
                        btnDescargarImagen.setAlpha(1.0f);

                        System.out.println("✅ URL completa disponible en navegador: " + downloadUrl);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnSubirImagen.setEnabled(true);
                        btnSubirImagen.setText("Carga");
                        btnSubirImagen.setAlpha(1.0f);
                        Toast.makeText(getContext(), "❌ Error subiendo: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void descargarImagen() {
        if (!conectadoAWS) {
            Toast.makeText(getContext(), "⚠️ Conectando a AWS...", Toast.LENGTH_SHORT).show();
            conectarAWSInternamente();
            return;
        }

        String s3Key = etS3Key.getText().toString().trim();
        if (s3Key.isEmpty()) {
            Toast.makeText(getContext(), "⚠️ Primero sube una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón durante descarga
        btnDescargarImagen.setEnabled(false);
        btnDescargarImagen.setText("Descargando...");
        btnDescargarImagen.setAlpha(0.5f);

        cloudStorage.obtenerArchivo(s3Key, new CloudStorage.DownloadCallback() {
            @Override
            public void onSuccess(String imagePath) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Restaurar botón
                        btnDescargarImagen.setEnabled(true);
                        btnDescargarImagen.setText("Descarga");
                        btnDescargarImagen.setAlpha(1.0f);

                        // Mostrar imagen descargada
                        Glide.with(getContext())
                                .load(imagePath)
                                .into(ivImagenDescargada);

                        // Mostrar sección completa
                        layoutImagenDescargada.setVisibility(View.VISIBLE);

                        // Mostrar URL completa
                        tvUrlCompleta.setText(ultimaUrlCompleta);

                        Toast.makeText(getContext(),
                                "✅ Imagen descargada y guardada en tu galería!",
                                Toast.LENGTH_LONG).show();

                        System.out.println("✅ Imagen guardada en: " + imagePath);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnDescargarImagen.setEnabled(true);
                        btnDescargarImagen.setText("Descarga");
                        btnDescargarImagen.setAlpha(1.0f);
                        Toast.makeText(getContext(), "❌ Error descargando: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void copiarUrlAlPortapapeles() {
        if (ultimaUrlCompleta.isEmpty()) {
            Toast.makeText(getContext(), "⚠️ No hay URL para copiar", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("URL de imagen", ultimaUrlCompleta);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(getContext(), "📋 URL copiada al portapapeles", Toast.LENGTH_SHORT).show();
    }

    private void cerrarSesion() {
        new AlertDialog.Builder(getContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
                    mAuth.signOut();

                    // Regresar a MainActivity
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    if (getActivity() != null) {
                        getActivity().finish();
                    }

                    Toast.makeText(getContext(), "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}