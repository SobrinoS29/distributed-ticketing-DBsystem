package edu.esi.ds.esientradas.http;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import edu.esi.ds.esientradas.services.ReservasService;
import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/reservas")
@CrossOrigin(origins = "*")
public class ReservasController {

    @Autowired
    private ReservasService reservasService;
    
    @PutMapping("/reservar")
    public String reservar(HttpSession session, @RequestBody Map<String, Object> body) {  // Devolveremos el OK y el token con body: { "entradaId" : 123, "ticketToken": "abcd" } 
        Long entradaId = ((Number) body.get("entradaId")).longValue();
        String tokenReserva = (String) body.get("tokenReserva");
        String ticketToken;

        if (tokenReserva != null && !tokenReserva.isEmpty())  // Si ya tiene token de reserva previo, agrupa bajo el mismo sessionId
            ticketToken = tokenReserva;
        else
            ticketToken = session.getId();
        
        try {
            return this.reservasService.reservar(entradaId, ticketToken);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al reservar la entrada: " + e.getMessage(), e);
        }
    }

    @PutMapping("/liberar")
    public String liberar(HttpSession session, @RequestBody Map<String, Object> body) {  // Devolveremos el OK y el ticketToken
        Long entradaId = ((Number) body.get("entradaId")).longValue();
        String tokenReserva = (String) body.get("tokenReserva");
        
        try {
            return this.reservasService.liberar(entradaId, tokenReserva);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al liberar la entrada: " + e.getMessage(), e);
        }
    }

    @GetMapping("/getTicketsFromToken")
    public List<Object[]> getTicketsFromToken(@RequestParam String token) {  // Devolveremos los tickets completos
        return this.reservasService.getTicketsFromToken(token);
    }
}

