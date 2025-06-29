package com.example.lab6_20190057.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20190057.R;
import com.example.lab6_20190057.models.MovimientoLinea1;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovimientosLinea1Adapter extends RecyclerView.Adapter<MovimientosLinea1Adapter.ViewHolder> {

    private List<MovimientoLinea1> movimientos;
    private OnMovimientoClickListener listener;

    public interface OnMovimientoClickListener {
        void onEditClick(MovimientoLinea1 movimiento, int position);
        void onDeleteClick(MovimientoLinea1 movimiento, int position);
    }

    public MovimientosLinea1Adapter(List<MovimientoLinea1> movimientos) {
        this.movimientos = movimientos;
    }

    public void setOnMovimientoClickListener(OnMovimientoClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movimiento_linea1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        System.out.println("🎨 ADAPTER: Binding view para posición: " + position);

        MovimientoLinea1 movimiento = movimientos.get(position);

        holder.tvIdTarjeta.setText("Tarjeta: " + movimiento.getIdTarjeta());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvFecha.setText("Fecha: " + sdf.format(movimiento.getFechaMovimiento()));

        holder.tvEstaciones.setText(movimiento.getEstacionEntrada() + " → " + movimiento.getEstacionSalida());
        holder.tvTiempo.setText(movimiento.getTiempoViaje() + " minutos");

        // Click listeners para los botones
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(movimiento, position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(movimiento, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movimientos.size();
    }

    public void updateMovimientos(List<MovimientoLinea1> newMovimientos) {
        System.out.println("🔄 ADAPTER: Actualizando movimientos");
        System.out.println("📊 ADAPTER: Nuevos movimientos recibidos: " + (newMovimientos != null ? newMovimientos.size() : 0));

        // Limpiar la lista actual
        this.movimientos.clear();

        // Agregar TODOS los elementos de la nueva lista
        if (newMovimientos != null && !newMovimientos.isEmpty()) {
            for (MovimientoLinea1 mov : newMovimientos) {
                this.movimientos.add(mov);
            }
        }

        System.out.println("📊 ADAPTER: Total después de actualizar: " + this.movimientos.size());

        // Notificar cambios en el hilo principal
        notifyDataSetChanged();
        System.out.println("🔔 ADAPTER: notifyDataSetChanged() llamado");
    }

    public void removeMovimiento(int position) {
        movimientos.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvIdTarjeta, tvFecha, tvEstaciones, tvTiempo;
        MaterialButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardMovimiento);
            tvIdTarjeta = itemView.findViewById(R.id.tvIdTarjeta);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvEstaciones = itemView.findViewById(R.id.tvEstaciones);
            tvTiempo = itemView.findViewById(R.id.tvTiempo);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}