package edu.esi.ds.esientradas.services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import edu.esi.ds.esientradas.dao.ConfiguracionDao;
import edu.esi.ds.esientradas.dao.PagoDao;
import edu.esi.ds.esientradas.model.Pago;

@Service
public class PagosService {

    @Autowired
    private ConfiguracionDao configuracionDao;  // Coger la Secret Key de la BD para preparar el pago con la pasarela de pago (Stripe)
    @Autowired
    private PagoDao pagoDao;  // Guardar en la BD el pago preparado, con su ID, cantidad, etc.

    public String prepararPago(Long centimos) throws Exception {  // Cogeremos la Secret Key del a BD y nos comunicaremos con la pasarela de pago (Stripe) para preparar el pago, utilizando la información del pago que se pasa en el body del request
        Stripe.apiKey = this.configuracionDao.findByClave("STRIPE_SECRET_KEY");
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setCurrency("eur")
                .setAmount(centimos)
                .build();
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        Pago pago = estructuraPago(paymentIntent);
        if (pago != null)
            pagoDao.save(pago);
        return paymentIntent.getClientSecret();  // No guardaremos este dato en la BD, ya que es un dato que solo se necesita para el proceso de pago, y es un dato sensible que no debería almacenarse a largo plazo por razones de seguridad.
    }

    public Pago estructuraPago(PaymentIntent paymentIntent) {
        if (paymentIntent == null)
            throw new IllegalArgumentException("PaymentIntent cannot be null");
        Pago pago = new Pago();
        pago.setPaymentIntentId(paymentIntent.getId());
        pago.setCreatedAt(Instant.ofEpochSecond(paymentIntent.getCreated()));
        pago.setAmount(paymentIntent.getAmount());
        pago.setCurrency(paymentIntent.getCurrency());
        pago.setStatus(paymentIntent.getStatus());
        pago.setPaymentMethodId(paymentIntent.getPaymentMethod());
        pago.setLivemode(paymentIntent.getLivemode());
        return pago;
    }

}
