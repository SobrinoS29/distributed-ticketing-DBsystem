package edu.esi.ds.esientradas.dto;

import edu.esi.ds.esientradas.model.Estado;

public class DtoEntrada {

    private Estado estado;
    private Long precio;
    private Long espectaculo_id;
    private Long id;

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