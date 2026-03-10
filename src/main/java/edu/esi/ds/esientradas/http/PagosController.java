package edu.esi.ds.esientradas.http;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import edu.esi.ds.esientradas.services.PagosService;

@RestController
@RequestMapping("/pago")
@CrossOrigin(origins = "*")
public class PagosController {
    
    @Autowired
    private PagosService pagosService;
    
    @PostMapping("/prepararPago")  // POST /pago/prepararPago
    public String prepararPago(@RequestBody Map<String, Object> infoPago) {  // Nos comunicaremos con el PagoService para preparar el pago utilizando la información del pago que se pasa en el body
        Long centimos = ((Number) infoPago.get("centimos")).longValue();
        try {
            return this.pagosService.prepararPago(centimos);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al preparar el pago: " + e.getMessage(), e);
        }
    }

    @PostMapping("/confirmarPago")  // POST /pago/confirmarPago
    public void confirmarPago(@RequestBody Map<String, Object> paymentIntent) {  // Confirmar el pago con Stripe y actualizar la BD
        String idPago = (String) paymentIntent.get("id");
        try {
            this.pagosService.confirmarPago(idPago);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al confirmar el pago: " + e.getMessage(), e);
        }
    }

}
