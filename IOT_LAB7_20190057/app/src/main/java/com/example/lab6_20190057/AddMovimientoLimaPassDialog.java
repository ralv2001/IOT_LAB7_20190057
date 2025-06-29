package com.example.lab6_20190057;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.lab6_20190057.models.MovimientoLimaPass;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddMovimientoLimaPassDialog extends DialogFragment {

    private TextInputEditText etIdTarjeta, etFecha, etTiempoViaje;
    private AutoCompleteTextView spinnerEntrada, spinnerSalida;
    private Calendar selectedDate;

    // Paraderos principales del Metropolitano y Corredores
    private String[] paraderos = {
            "Naranjal", "Izaguirre", "Pacífico", "Independencia", "Los Jazmines",
            "Tupac Amaru", "Caquetá", "Ramón Castilla", "Tacna", "Jiménez Pimentel",
            "Colmena", "España", "Central", "Estadio Nacional", "México",
            "Canada", "Javier Prado", "Canaval Moreyra", "Aramburú", "Domingo Orué",
            "Angamos", "Ricardo Palma", "Benavides", "28 de Julio", "Balta",
            "Barranco", "Bulevar", "Estación Barranco", "Chorrillos"
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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_movimiento_lima_pass, null);

        initViews(view);
        setupSpinners();
        setupDatePicker();

        return new AlertDialog.Builder(requireContext())
                .setTitle("Agregar Movimiento Lima Pass")
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

    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etFecha.setText(sdf.format(selectedDate.getTime()));
    }

    private void saveMovimiento() {
        String idTarjeta = etIdTarjeta.getText().toString().trim();
        String paraderoEntrada = spinnerEntrada.getText().toString().trim();
        String paraderoSalida = spinnerSalida.getText().toString().trim();
        String tiempoViajeStr = etTiempoViaje.getText().toString().trim();

        if (TextUtils.isEmpty(idTarjeta) || TextUtils.isEmpty(paraderoEntrada) ||
                TextUtils.isEmpty(paraderoSalida) || TextUtils.isEmpty(tiempoViajeStr)) {
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

        MovimientoLimaPass movimiento = new MovimientoLimaPass(
                idTarjeta, fechaMovimiento, paraderoEntrada, paraderoSalida, tiempoViaje, userId
        );

        FirebaseFirestore.getInstance()
                .collection("movimientos_lima_pass")
                .add(movimiento)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("✅ MOVIMIENTO LIMA PASS GUARDADO EXITOSAMENTE");

                    if (listener != null) {
                        listener.onMovimientoAdded();
                    }

                    if (getDialog() != null) {
                        getDialog().dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("❌ ERROR AL GUARDAR LIMA PASS: " + e.getMessage());
                });
    }
}