package edu.esi.ds.esientradas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)  // Cada subclase tendrá su propia tabla, con una relación 1:1 con la tabla de Entrada
public abstract class Entrada {
    @Id @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    protected Long id;
    private Long precio;     // Ojo: en céntimos de euro

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espectaculo_id", nullable = false)
    protected Espectaculo espectaculo;

    @Enumerated(EnumType.STRING)
    protected Estado estado;

    //@OneToOne(mappedBy = "entrada", fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL)
    @Transient  // No generará una columna token en la BD
    protected TicketToken token;  // Relación 1:1 con Token

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @JsonIgnore  // Evitamos que no salga del bucle infinito al serializar la entrada, ya que el espectaculo tiene una referencia a las entradas
    public Espectaculo getEspectaculo() {
        return espectaculo;
    }
    public void setEspectaculo(Espectaculo espectaculo) {
        this.espectaculo = espectaculo;
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
    public TicketToken getToken() {
        return token;
    }
    public void setToken(TicketToken token) {
        this.token = token;
    }
}
