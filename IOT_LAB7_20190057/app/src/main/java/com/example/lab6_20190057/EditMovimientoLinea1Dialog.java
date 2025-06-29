package com.example.lab6_20190057;

import android.app.DatePickerDialog;
import android.app.Dialog;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditMovimientoLinea1Dialog extends DialogFragment {

    private TextInputEditText etIdTarjeta, etFecha, etTiempoViaje;
    private AutoCompleteTextView spinnerEntrada, spinnerSalida;
    private Calendar selectedDate;
    private MovimientoLinea1 movimientoToEdit;

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

    public interface OnMovimientoEditedListener {
        void onMovimientoEdited();
    }

    private OnMovimientoEditedListener listener;

    public static EditMovimientoLinea1Dialog newInstance(MovimientoLinea1 movimiento) {
        EditMovimientoLinea1Dialog dialog = new EditMovimientoLinea1Dialog();
        dialog.movimientoToEdit = movimiento;
        return dialog;
    }

    public void setOnMovimientoEditedListener(OnMovimientoEditedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_movimiento_linea1, null);

        initViews(view);
        setupSpinners();
        setupDatePicker();
        fillFormWithData();

        return new AlertDialog.Builder(requireContext())
                .setTitle("Editar Movimiento Línea 1")
                .setView(view)
                .setPositiveButton("Actualizar", (dialog, which) -> updateMovimiento())
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

    private void fillFormWithData() {
        if (movimientoToEdit != null) {
            etIdTarjeta.setText(movimientoToEdit.getIdTarjeta());
            etTiempoViaje.setText(String.valueOf(movimientoToEdit.getTiempoViaje()));
            spinnerEntrada.setText(movimientoToEdit.getEstacionEntrada(), false);
            spinnerSalida.setText(movimientoToEdit.getEstacionSalida(), false);

            selectedDate.setTime(movimientoToEdit.getFechaMovimiento());
            updateDateField();
        }
    }

    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etFecha.setText(sdf.format(selectedDate.getTime()));
    }

    private void updateMovimiento() {
        String idTarjeta = etIdTarjeta.getText().toString().trim();
        String estacionEntrada = spinnerEntrada.getText().toString().trim();
        String estacionSalida = spinnerSalida.getText().toString().trim();
        String tiempoViajeStr = etTiempoViaje.getText().toString().trim();

        if (TextUtils.isEmpty(idTarjeta) || TextUtils.isEmpty(estacionEntrada) ||
                TextUtils.isEmpty(estacionSalida) || TextUtils.isEmpty(tiempoViajeStr)) {
            Toast.makeText(getContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int tiempoViaje;
        try {
            tiempoViaje = Integer.parseInt(tiempoViajeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "El tiempo de viaje debe ser un número", Toast.LENGTH_SHORT).show();
            return;
        }

        Date fechaMovimiento = selectedDate.getTime();

        System.out.println("🔄 EDITANDO MOVIMIENTO: " + movimientoToEdit.getId());

        // Actualizar el movimiento en Firestore
        FirebaseFirestore.getInstance()
                .collection("movimientos_linea1")
                .document(movimientoToEdit.getId())
                .update(
                        "idTarjeta", idTarjeta,
                        "fechaMovimiento", fechaMovimiento,
                        "estacionEntrada", estacionEntrada,
                        "estacionSalida", estacionSalida,
                        "tiempoViaje", tiempoViaje
                )
                .addOnSuccessListener(aVoid -> {
                    System.out.println("✅ MOVIMIENTO ACTUALIZADO EXITOSAMENTE");
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Movimiento actualizado exitosamente", Toast.LENGTH_SHORT).show();
                    }

                    if (listener != null) {
                        listener.onMovimientoEdited();
                    }

                    if (getDialog() != null) {
                        getDialog().dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("❌ ERROR AL ACTUALIZAR: " + e.getMessage());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}