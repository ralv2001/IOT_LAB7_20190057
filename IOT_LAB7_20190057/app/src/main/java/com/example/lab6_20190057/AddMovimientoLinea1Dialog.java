package com.example.lab6_20190057;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.lab6_20190057.models.MovimientoLinea1;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddMovimientoLinea1Dialog extends DialogFragment {

    private TextInputEditText etIdTarjeta, etFecha, etTiempoViaje;
    private AutoCompleteTextView spinnerEntrada, spinnerSalida;
    private Calendar selectedDate;

    // Estaciones de la Línea 1
    private String[] estaciones = {
            "Villa El Salvador", "Parque Industrial", "Pumuy", "Puente Los Reyes",
            "SJM", "Atocongo", "Jorge Chávez", "Ayacucho", "Cabitos", "San Borja Sur",
            "Angamos", "San Borja Norte", "Javier Prado", "Esther Ballivián",
            "Domingo Orué", "María Auxiliadora", "Nicolás Arriola", "Gamarra",
            "La Cultura", "San Carlos", "Pirámide del Sol", "Los Jardines",
            "El Ángel", "Presbítero Maestro", "Caja de Agua", "Bayóvar",
            "Honorio Delgado", "UNI", "Parque del Trabajo", "Caquetá",
            "Tacna", "Jorge Chávez", "Grau", "Villa María del Triunfo"
    };

    public interface OnMovimientoAddedListener {
        void onMovimientoAdded();
    }

    private OnMovimientoAddedListener listener;

    public void setOnMovimientoAddedListener(OnMovimientoAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_movimiento_linea1, null);

        initViews(view);
        setupSpinners();
        setupDatePicker();

        return new AlertDialog.Builder(requireContext())
                .setTitle("Agregar Movimiento Línea 1")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> saveMovimiento())
                .setNegativeButton("Cancelar", null)
                .create();
    }

    private void initViews(View view) {
        etIdTarjeta = view.findViewById(R.id.etIdTarjeta);
        etFecha = view.findViewById(R.id.etFecha);
        etTiempoViaje = view.findViewById(R.id.etTiempoViaje);
        spinnerEntrada = view.findViewById(R.id.spinnerEntrada);
        spinnerSalida = view.findViewById(R.id.spinnerSalida);

        selectedDate = Calendar.getInstance();
        updateDateField();
    }

    private void setupSpinners() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                estaciones
        );

        spinnerEntrada.setAdapter(adapter);
        spinnerSalida.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etFecha.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        updateDateField();
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etFecha.setText(sdf.format(selectedDate.getTime()));
    }

    private void saveMovimiento() {
        String idTarjeta = etIdTarjeta.getText().toString().trim();
        String estacionEntrada = spinnerEntrada.getText().toString().trim();
        String estacionSalida = spinnerSalida.getText().toString().trim();
        String tiempoViajeStr = etTiempoViaje.getText().toString().trim();

        if (TextUtils.isEmpty(idTarjeta)) {
            return;
        }

        if (TextUtils.isEmpty(estacionEntrada)) {
            return;
        }

        if (TextUtils.isEmpty(estacionSalida)) {
            return;
        }

        if (TextUtils.isEmpty(tiempoViajeStr)) {
            return;
        }

        int tiempoViaje;
        try {
            tiempoViaje = Integer.parseInt(tiempoViajeStr);
        } catch (NumberFormatException e) {
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Date fechaMovimiento = selectedDate.getTime();

        MovimientoLinea1 movimiento = new MovimientoLinea1(
                idTarjeta, fechaMovimiento, estacionEntrada, estacionSalida, tiempoViaje, userId
        );

        FirebaseFirestore.getInstance()
                .collection("movimientos_linea1")
                .add(movimiento)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("✅ MOVIMIENTO GUARDADO EXITOSAMENTE");

                    // Verificar que el contexto no sea null antes de mostrar Toast
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Movimiento guardado exitosamente", Toast.LENGTH_SHORT).show();
                    }

                    // Llamar al listener para actualizar la lista
                    if (listener != null) {
                        System.out.println("✅ LLAMANDO AL LISTENER");
                        listener.onMovimientoAdded();
                    }

                    // Cerrar el diálogo
                    if (getDialog() != null) {
                        getDialog().dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("❌ ERROR AL GUARDAR: " + e.getMessage());

                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}