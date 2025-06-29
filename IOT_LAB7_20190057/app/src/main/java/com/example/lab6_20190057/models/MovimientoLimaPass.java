package com.example.lab6_20190057.models;

import java.util.Date;

public class MovimientoLimaPass {
    private String id;
    private String idTarjeta;
    private Date fechaMovimiento;
    private String paraderoEntrada;
    private String paraderoSalida;
    private int tiempoViaje; // en minutos
    private String userId;

    public MovimientoLimaPass() {
        // Constructor vacío requerido por Firestore
    }

    public MovimientoLimaPass(String idTarjeta, Date fechaMovimiento, String paraderoEntrada,
                              String paraderoSalida, int tiempoViaje, String userId) {
        this.idTarjeta = idTarjeta;
        this.fechaMovimiento = fechaMovimiento;
        this.paraderoEntrada = paraderoEntrada;
        this.paraderoSalida = paraderoSalida;
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

    public String getParaderoEntrada() { return paraderoEntrada; }
    public void setParaderoEntrada(String paraderoEntrada) { this.paraderoEntrada = paraderoEntrada; }

    public String getParaderoSalida() { return paraderoSalida; }
    public void setParaderoSalida(String paraderoSalida) { this.paraderoSalida = paraderoSalida; }

    public int getTiempoViaje() { return tiempoViaje; }
    public void setTiempoViaje(int tiempoViaje) { this.tiempoViaje = tiempoViaje; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}