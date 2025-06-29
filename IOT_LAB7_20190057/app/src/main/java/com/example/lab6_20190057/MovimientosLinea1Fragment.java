package com.example.lab6_20190057;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20190057.adapters.MovimientosLinea1Adapter;
import com.example.lab6_20190057.models.MovimientoLinea1;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MovimientosLinea1Fragment extends Fragment implements MovimientosLinea1Adapter.OnMovimientoClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private MaterialButton btnFilter;
    private FirebaseFirestore db;
    private String userId;
    private List<MovimientoLinea1> movimientos;
    private MovimientosLinea1Adapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movimientos_linea1, container, false);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadMovimientos();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewMovimientos);
        fabAdd = view.findViewById(R.id.fabAddMovimiento);
        btnFilter = view.findViewById(R.id.btnFilter);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Inicializar la lista como nueva ArrayList
        movimientos = new ArrayList<>();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar con lista vacía nueva
        movimientos = new ArrayList<>();
        adapter = new MovimientosLinea1Adapter(movimientos);
        adapter.setOnMovimientoClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> {
            AddMovimientoLinea1Dialog dialog = new AddMovimientoLinea1Dialog();
            dialog.setOnMovimientoAddedListener(() -> loadMovimientos());
            dialog.show(getParentFragmentManager(), "AddMovimientoLinea1Dialog");
        });

        btnFilter.setOnClickListener(v -> {
            // Crear un dialog con opciones
            new AlertDialog.Builder(getContext())
                    .setTitle("Filtrar Movimientos")
                    .setItems(new String[]{"Filtrar por fecha", "Mostrar todos"}, (dialog, which) -> {
                        if (which == 0) {
                            showDatePicker();
                        } else {
                            showAllMovimientos();
                        }
                    })
                    .show();
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    filterMovimentosByDate(year, month, dayOfMonth);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void filterMovimentosByDate(int year, int month, int day) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, day, 0, 0, 0);
        selectedDate.set(Calendar.MILLISECOND, 0);

        Calendar endDate = Calendar.getInstance();
        endDate.set(year, month, day, 23, 59, 59);
        endDate.set(Calendar.MILLISECOND, 999);

        System.out.println("🔍 FILTRANDO LÍNEA 1 POR FECHA: " + day + "/" + (month + 1) + "/" + year);

        db.collection("movimientos_linea1")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("fechaMovimiento", selectedDate.getTime())
                .whereLessThanOrEqualTo("fechaMovimiento", endDate.getTime())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MovimientoLinea1> filteredMovimientos = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MovimientoLinea1 movimiento = document.toObject(MovimientoLinea1.class);
                            movimiento.setId(document.getId());
                            filteredMovimientos.add(movimiento);
                        }

                        System.out.println("📊 MOVIMIENTOS FILTRADOS LÍNEA 1: " + filteredMovimientos.size());
                        adapter.updateMovimientos(filteredMovimientos);

                        String dateStr = day + "/" + (month + 1) + "/" + year;
                        if (filteredMovimientos.isEmpty()) {
                            Toast.makeText(getContext(), "No hay movimientos para " + dateStr, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Mostrando " + filteredMovimientos.size() + " movimientos para " + dateStr, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        System.out.println("❌ ERROR AL FILTRAR LÍNEA 1: " + task.getException().getMessage());
                        Toast.makeText(getContext(), "Error al filtrar movimientos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAllMovimientos() {
        loadMovimientos(); // Esto recarga todos los movimientos
        Toast.makeText(getContext(), "Mostrando todos los movimientos", Toast.LENGTH_SHORT).show();
    }

    private void loadMovimientos() {
        System.out.println("🔄 CARGANDO MOVIMIENTOS...");

        db.collection("movimientos_linea1")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("✅ FIRESTORE RESPUESTA EXITOSA");

                        // Crear una NUEVA lista temporal
                        List<MovimientoLinea1> tempList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MovimientoLinea1 movimiento = document.toObject(MovimientoLinea1.class);
                            movimiento.setId(document.getId());
                            tempList.add(movimiento);
                            System.out.println("📝 MOVIMIENTO CARGADO: " + movimiento.getIdTarjeta());
                        }

                        System.out.println("📊 TOTAL MOVIMIENTOS: " + tempList.size());

                        // Actualizar el adapter con la nueva lista
                        adapter.updateMovimientos(tempList);

                        if (tempList.isEmpty()) {
                            System.out.println("⚠️ NO HAY MOVIMIENTOS");
                        }
                    } else {
                        System.out.println("❌ ERROR AL CARGAR: " + task.getException().getMessage());
                    }
                });
    }

    @Override
    public void onEditClick(MovimientoLinea1 movimiento, int position) {
        EditMovimientoLinea1Dialog dialog = EditMovimientoLinea1Dialog.newInstance(movimiento);
        dialog.setOnMovimientoEditedListener(() -> {
            System.out.println("🔄 RECARGANDO DESPUÉS DE EDITAR");
            loadMovimientos();
        });
        dialog.show(getParentFragmentManager(), "EditMovimientoLinea1Dialog");
    }

    @Override
    public void onDeleteClick(MovimientoLinea1 movimiento, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Movimiento")
                .setMessage("¿Estás seguro de que quieres eliminar este movimiento?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    deleteMovimiento(movimiento, position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteMovimiento(MovimientoLinea1 movimiento, int position) {
        db.collection("movimientos_linea1")
                .document(movimiento.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    adapter.removeMovimiento(position);
                    Toast.makeText(getContext(), "Movimiento eliminado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}