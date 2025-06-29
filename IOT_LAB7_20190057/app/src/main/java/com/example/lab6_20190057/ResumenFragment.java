package com.example.lab6_20190057;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResumenFragment extends Fragment {

    private BarChart barChart;
    private PieChart pieChart;
    private MaterialButton btnFilterDate;
    private TextView tvDateRange;
    private FirebaseFirestore db;
    private String userId;

    private Calendar startDate, endDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resumen, container, false);

        initViews(view);
        setupCharts();
        setupDateFilter();
        loadData();

        return view;
    }

    private void initViews(View view) {
        barChart = view.findViewById(R.id.barChart);
        pieChart = view.findViewById(R.id.pieChart);
        btnFilterDate = view.findViewById(R.id.btnFilterDate);
        tvDateRange = view.findViewById(R.id.tvDateRange);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Inicializar fechas (último mes)
        endDate = Calendar.getInstance();
        startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);

        updateDateRangeText();
    }

    private void setupCharts() {
        // Configurar BarChart
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);

        Description barDesc = new Description();
        barDesc.setText("Movimientos por Sistema");
        barChart.setDescription(barDesc);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        // Configurar PieChart
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(35f);
        pieChart.setTransparentCircleRadius(40f);

        Description pieDesc = new Description();
        pieDesc.setText("Uso de Sistemas");
        pieChart.setDescription(pieDesc);
    }

    private void setupDateFilter() {
        btnFilterDate.setOnClickListener(v -> showDateRangePicker());
    }

    private void showDateRangePicker() {
        // Selector de fecha de inicio
        DatePickerDialog startDatePicker = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    startDate.set(year, month, dayOfMonth);
                    // Después de seleccionar fecha de inicio, mostrar selector de fecha final
                    showEndDatePicker();
                },
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH)
        );
        startDatePicker.setTitle("Seleccionar fecha de inicio");
        startDatePicker.show();
    }

    private void showEndDatePicker() {
        DatePickerDialog endDatePicker = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    endDate.set(year, month, dayOfMonth);
                    updateDateRangeText();
                    loadData(); // Recargar datos con nuevo rango
                },
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH)
        );
        endDatePicker.setTitle("Seleccionar fecha final");
        endDatePicker.show();
    }

    private void updateDateRangeText() {
        String rangeText = "Rango: " + dateFormat.format(startDate.getTime()) +
                " - " + dateFormat.format(endDate.getTime());
        tvDateRange.setText(rangeText);
    }

    private void loadData() {
        loadMovimientosLinea1();
        loadMovimientosLimaPass();
    }

    private int linea1Count = 0;
    private int limaPassCount = 0;

    private void loadMovimientosLinea1() {
        Date start = startDate.getTime();
        Date end = endDate.getTime();

        db.collection("movimientos_linea1")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("fechaMovimiento", start)
                .whereLessThanOrEqualTo("fechaMovimiento", end)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        linea1Count = task.getResult().size();
                        System.out.println("📊 LÍNEA 1 COUNT: " + linea1Count);
                        updateCharts();
                    }
                });
    }

    private void loadMovimientosLimaPass() {
        Date start = startDate.getTime();
        Date end = endDate.getTime();

        db.collection("movimientos_lima_pass")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("fechaMovimiento", start)
                .whereLessThanOrEqualTo("fechaMovimiento", end)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        limaPassCount = task.getResult().size();
                        System.out.println("📊 LIMA PASS COUNT: " + limaPassCount);
                        updateCharts();
                    }
                });
    }

    private void updateCharts() {
        updateBarChart();
        updatePieChart();
    }

    private void updateBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, linea1Count));
        entries.add(new BarEntry(1f, limaPassCount));

        BarDataSet dataSet = new BarDataSet(entries, "Movimientos");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        // Etiquetas del eje X
        String[] labels = {"Línea 1", "Lima Pass"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.length);

        barChart.setData(barData);
        barChart.invalidate(); // refresh
    }

    private void updatePieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();

        int total = linea1Count + limaPassCount;
        if (total > 0) {
            entries.add(new PieEntry(linea1Count, "Línea 1"));
            entries.add(new PieEntry(limaPassCount, "Lima Pass"));
        } else {
            entries.add(new PieEntry(1, "Sin datos"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Uso de Sistemas");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate(); // refresh
    }
}