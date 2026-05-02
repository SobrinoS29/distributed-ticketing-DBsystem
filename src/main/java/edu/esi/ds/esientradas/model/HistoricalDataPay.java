package edu.esi.ds.esientradas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "historical_data_pay")
public class HistoricalDataPay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    protected Pago pago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espectaculo_id", nullable = false)
    protected Espectaculo espectaculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escenario_id", nullable = false)
    protected Escenario escenario;

    protected String userId;
    protected String userName;
    protected String userEmail;
    protected int totalCentimos;     // Ojo: en céntimos de euro
    protected String totalEntradas;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Pago getPago() {
        return pago;
    }
    public void setPago(Pago pago) {
        this.pago = pago;
    }
    public Espectaculo getEspectaculo() {
        return espectaculo;
    }
    public void setEspectaculo(Espectaculo espectaculo) {
        this.espectaculo = espectaculo;
    }
    public Escenario getEscenario() {
        return escenario;
    }
    public void setEscenario(Escenario escenario) {
        this.escenario = escenario;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getUserEmail() {
        return userEmail;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    public int getTotalCentimos() {
        return totalCentimos;
    }
    public void setTotalCentimos(int totalCentimos) {
        this.totalCentimos = totalCentimos;
    }
    public String getTotalEntradas() {
        return totalEntradas;
    }
    public void setTotalEntradas(String totalEntradas) {
        this.totalEntradas = totalEntradas;
    }
}