package edu.esi.ds.esientradas.dto;

import edu.esi.ds.esientradas.model.Estado;

public class DtoEntrada {

    private Integer total;
    private Integer libres;
    private Integer reservadas;
    private Integer vendidas;
    
    private Estado estado;
    private Long precio;
    private Long espectaculo_id;
    private Long id;

    public DtoEntrada(Integer total, Integer libres, Integer reservadas, Integer vendidas) {
        this.total = total;
        this.libres = libres;
        this.reservadas = reservadas;
        this.vendidas = vendidas;
    }
    
    public Integer getTotal() {
        return total;
    }
    public void setTotal(Integer total) {
        this.total = total;
    }
    public Integer getLibres() {
        return libres;
    }
    public void setLibres(Integer libres) {
        this.libres = libres;
    }
    public Integer getReservadas() {
        return reservadas;
    }
    public void setReservadas(Integer reservadas) {
        this.reservadas = reservadas;
    }
    public Integer getVendidas() {
        return vendidas;
    }
    public void setVendidas(Integer vendidas) {
        this.vendidas = vendidas;
    }

    
    public Estado getEstado() {
        return estado;
    }
    public void setEstado(Estado estado) {
        this.estado = estado;
    }
    public Long getPrecio() {
        return precio;
    }
    public void setPrecio(Long precio) {
        this.precio = precio;
    }
    public Long getEspectaculo_id() {
        return espectaculo_id;
    }
    public void setEspectaculo_id(Long espectaculo_id) {
        this.espectaculo_id = espectaculo_id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return this.id;
    }

}