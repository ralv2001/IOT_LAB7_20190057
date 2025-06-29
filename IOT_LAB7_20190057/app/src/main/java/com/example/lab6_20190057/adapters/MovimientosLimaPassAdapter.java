package com.example.lab6_20190057.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20190057.R;
import com.example.lab6_20190057.models.MovimientoLimaPass;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovimientosLimaPassAdapter extends RecyclerView.Adapter<MovimientosLimaPassAdapter.ViewHolder> {

    private List<MovimientoLimaPass> movimientos;
    private OnMovimientoClickListener listener;

    public interface OnMovimientoClickListener {
        void onEditClick(MovimientoLimaPass movimiento, int position);
        void onDeleteClick(MovimientoLimaPass movimiento, int position);
    }

    public MovimientosLimaPassAdapter(List<MovimientoLimaPass> movimientos) {
        this.movimientos = movimientos;
    }

    public void setOnMovimientoClickListener(OnMovimientoClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movimiento_lima_pass, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        System.out.println("🎨 ADAPTER LIMA PASS: Binding view para posición: " + position);

        MovimientoLimaPass movimiento = movimientos.get(position);

        holder.tvIdTarjeta.setText("Tarjeta: " + movimiento.getIdTarjeta());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvFecha.setText("Fecha: " + sdf.format(movimiento.getFechaMovimiento()));

        holder.tvParaderos.setText(movimiento.getParaderoEntrada() + " → " + movimiento.getParaderoSalida());
        holder.tvTiempo.setText(movimiento.getTiempoViaje() + " minutos");

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

    public void updateMovimientos(List<MovimientoLimaPass> newMovimientos) {
        System.out.println("🔄 ADAPTER LIMA PASS: Actualizando movimientos");
        System.out.println("📊 ADAPTER LIMA PASS: Nuevos movimientos recibidos: " + (newMovimientos != null ? newMovimientos.size() : 0));

        this.movimientos.clear();

        if (newMovimientos != null && !newMovimientos.isEmpty()) {
            for (MovimientoLimaPass mov : newMovimientos) {
                this.movimientos.add(mov);
            }
        }

        System.out.println("📊 ADAPTER LIMA PASS: Total después de actualizar: " + this.movimientos.size());
        notifyDataSetChanged();
        System.out.println("🔔 ADAPTER LIMA PASS: notifyDataSetChanged() llamado");
    }

    public void removeMovimiento(int position) {
        movimientos.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvIdTarjeta, tvFecha, tvParaderos, tvTiempo;
        MaterialButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardMovimiento);
            tvIdTarjeta = itemView.findViewById(R.id.tvIdTarjeta);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvParaderos = itemView.findViewById(R.id.tvParaderos);
            tvTiempo = itemView.findViewById(R.id.tvTiempo);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}