/*
    Periodicamente comprobaremos si el token es válido (fecha de creación < ...),
    Si el token es inválido, se eliminará de la base de datos y se actualizará el estado de la entrada.

    Podemos asignarle el mismo token a todas las entradas que compre el mismo user.
*/

package edu.esi.ds.esientradas.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Column;

@Entity
public class Token {
    @Id @Column(length = 36)
    private String valor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrada_id", referencedColumnName = "id")
    private Entrada entrada;
    
    private String sessionId;
    private java.time.LocalDateTime hora;  // Hora de creación del token

    public Token() {
        this.valor = UUID.randomUUID().toString();  // Con @Id creamos nosotros el valor del token, sin embargo con @GeneratedValue sería la BD quien lo haría
        this.hora = java.time.LocalDateTime.now();
    }

    public String getValor() {
        return valor;
    }
    public void setValor(String valor) {
        this.valor = valor;
    }
    public Entrada getEntrada() {
        return entrada;
    }
    public void setEntrada(Entrada entrada) {
        this.entrada = entrada;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public java.time.LocalDateTime getHora() {
        return hora;
    }
    public void setHora(java.time.LocalDateTime hora) {
        this.hora = hora;
    }

}
