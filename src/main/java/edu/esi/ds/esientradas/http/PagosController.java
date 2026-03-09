package edu.esi.ds.esientradas.http;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import edu.esi.ds.esientradas.services.PagosService;

@RestController
@RequestMapping("/pago")
public class PagosController {
    
    @Autowired
    private PagosService pagosService;
    
    @PostMapping("/prepararPago")  // POST /pago/prepararPago
    public void prepararPago(@RequestBody Map<String, Object> infoPago) {  // Nos comunicaremos con el PagoService para preparar el pago utilizando la información del pago que se pasa en el body
        Long centimos = ((Number) infoPago.get("centimos")).longValue();
        try {
            this.pagosService.prepararPago(centimos);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
