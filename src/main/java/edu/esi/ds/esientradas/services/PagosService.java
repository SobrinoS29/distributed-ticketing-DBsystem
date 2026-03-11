package edu.esi.ds.esientradas.services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
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

    @Autowired
    private PDFService pdfService;
    @Autowired
    private EmailService emailService;

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

    public int confirmarPago(String idPago) throws StripeException {  // Nos comunicaremos con la pasarela de pago (Stripe) para confirmar el pago, utilizando el ID del pago que se pasa en el body del request
        int intConfirm = -1;
        Stripe.apiKey = this.configuracionDao.findByClave("STRIPE_SECRET_KEY");
        PaymentIntent paymentIntent = PaymentIntent.retrieve(idPago);  // Recuperamos el PaymentIntent de Stripe utilizando el ID del pago para comprobar su estado real

        Pago pago = estructuraPago(paymentIntent);
        if (pago != null  && "succeeded".equals(paymentIntent.getStatus())) {
            intConfirm = pagoDao.updatePagoState(pago);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al confirmar el pago con Stripe Id: " + paymentIntent.getId() + ", estado: " + paymentIntent.getStatus());
        }
        
        //this.pdfService.confirmarPago(idPago);
        //this.emailService.enviarConfirmacionPago(pago);
        return intConfirm;
    }

}
