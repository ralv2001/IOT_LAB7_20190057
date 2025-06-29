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

import com.example.lab6_20190057.models.MovimientoLimaPass;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditMovimientoLimaPassDialog extends DialogFragment {

    private TextInputEditText etIdTarjeta, etFecha, etTiempoViaje;
    private AutoCompleteTextView spinnerEntrada, spinnerSalida;
    private Calendar selectedDate;
    private MovimientoLimaPass movimientoToEdit;

    // Paraderos principales del Metropolitano y Corredores
    private String[] paraderos = {
            "Naranjal", "Izaguirre", "Pacífico", "Independencia", "Los Jazmines",
            "Tupac Amaru", "Caquetá", "Ramón Castilla", "Tacna", "Jiménez Pimentel",
            "Colmena", "España", "Central", "Estadio Nacional", "México",
            "Canada", "Javier Prado", "Canaval Moreyra", "Aramburú", "Domingo Orué",
            "Angamos", "Ricardo Palma", "Benavides", "28 de Julio", "Balta",
            "Barranco", "Bulevar", "Estación Barranco", "Chorrillos"
    };

    public interface OnMovimientoEditedListener {
        void onMovimientoEdited();
    }

    private OnMovimientoEditedListener listener;

    public static EditMovimientoLimaPassDialog newInstance(MovimientoLimaPass movimiento) {
        EditMovimientoLimaPassDialog dialog = new EditMovimientoLimaPassDialog();
        dialog.movimientoToEdit = movimiento;
        return dialog;
    }

    public void setOnMovimientoEditedListener(OnMovimientoEditedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_movimiento_lima_pass, null);

        initViews(view);
        setupSpinners();
        setupDatePicker();
        fillFormWithData();

        return new AlertDialog.Builder(requireContext())
                .setTitle("Editar Movimiento Lima Pass")
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
                paraderos
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
            spinnerEntrada.setText(movimientoToEdit.getParaderoEntrada(), false);
            spinnerSalida.setText(movimientoToEdit.getParaderoSalida(), false);

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
        String paraderoEntrada = spinnerEntrada.getText().toString().trim();
        String paraderoSalida = spinnerSalida.getText().toString().trim();
        String tiempoViajeStr = etTiempoViaje.getText().toString().trim();

        if (TextUtils.isEmpty(idTarjeta) || TextUtils.isEmpty(paraderoEntrada) ||
                TextUtils.isEmpty(paraderoSalida) || TextUtils.isEmpty(tiempoViajeStr)) {
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

        System.out.println("🔄 EDITANDO MOVIMIENTO LIMA PASS: " + movimientoToEdit.getId());

        // Actualizar el movimiento en Firestore
        FirebaseFirestore.getInstance()
                .collection("movimientos_lima_pass")
                .document(movimientoToEdit.getId())
                .update(
                        "idTarjeta", idTarjeta,
                        "fechaMovimiento", fechaMovimiento,
                        "paraderoEntrada", paraderoEntrada,
                        "paraderoSalida", paraderoSalida,
                        "tiempoViaje", tiempoViaje
                )
                .addOnSuccessListener(aVoid -> {
                    System.out.println("✅ MOVIMIENTO LIMA PASS ACTUALIZADO EXITOSAMENTE");
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
                    System.out.println("❌ ERROR AL ACTUALIZAR LIMA PASS: " + e.getMessage());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}