package com.example.lab6_20190057.models;

import java.util.Date;

public class MovimientoLinea1 {
    private String id;
    private String idTarjeta;
    private Date fechaMovimiento;
    private String estacionEntrada;
    private String estacionSalida;
    private int tiempoViaje; // en minutos
    private String userId;

    public MovimientoLinea1() {
        // Constructor vacío requerido por Firestore
    }

    public MovimientoLinea1(String idTarjeta, Date fechaMovimiento, String estacionEntrada,
                            String estacionSalida, int tiempoViaje, String userId) {
        this.idTarjeta = idTarjeta;
        this.fechaMovimiento = fechaMovimiento;
        this.estacionEntrada = estacionEntrada;
        this.estacionSalida = estacionSalida;
        this.tiempoViaje = tiempoViaje;
        this.userId = userId;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdTarjeta() { return idTarjeta; }
    public void setIdTarjeta(String idTarjeta) { this.idTarjeta = idTarjeta; }

    public Date getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(Date fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }

    public String getEstacionEntrada() { return estacionEntrada; }
    public void setEstacionEntrada(String estacionEntrada) { this.estacionEntrada = estacionEntrada; }

    public String getEstacionSalida() { return estacionSalida; }
    public void setEstacionSalida(String estacionSalida) { this.estacionSalida = estacionSalida; }

    public int getTiempoViaje() { return tiempoViaje; }
    public void setTiempoViaje(int tiempoViaje) { this.tiempoViaje = tiempoViaje; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}