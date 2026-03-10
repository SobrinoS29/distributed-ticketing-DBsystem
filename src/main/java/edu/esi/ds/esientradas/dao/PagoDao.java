package edu.esi.ds.esientradas.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import edu.esi.ds.esientradas.model.Pago;

public interface PagoDao extends JpaRepository<Pago, Long> {

    Pago findByPaymentIntentId(String paymentIntentId);

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE pago
            SET status = :#{#pago.status}, payment_method_id = :#{#pago.paymentMethodId}, livemode = :#{#pago.livemode}
            WHERE payment_intent_id = :#{#pago.paymentIntentId}""", nativeQuery = true)
    void updatePagoState(Pago pago);  // Método para actualizar el estado del pago en la BD después de confirmarlo con Stripe
}
