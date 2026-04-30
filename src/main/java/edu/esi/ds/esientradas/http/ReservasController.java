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
    public String reservar(HttpSession session, @RequestBody Map<String, Object> body) {  // Traemos body: { "entradaId" : 123, "ticketToken": "abcd", "userToken": "efgh" }, y devolveremos el OK y el ticketToken + userToken
        Long entradaId = ((Number) body.get("entradaId")).longValue();
        String tokenReserva = (String) body.get("tokenReserva");
        String ticketToken;
        String userTokenReserva = (String) body.get("userTokenReserva");  // Recibimos el token de sesión del usuario para asociarlo a la reserva y poder mostrar sus reservas activas posteriormente
        String userToken;

        if (tokenReserva != null && !tokenReserva.isEmpty())  // Si ya tiene token de reserva previo, agrupa bajo el mismo sessionId
            ticketToken = tokenReserva;
        else
            ticketToken = java.util.UUID.randomUUID().toString();  // Generamos un nuevo token de reserva para la nueva reserva
        
        if(userTokenReserva != null && !userTokenReserva.isEmpty())  // Si el usuario ha iniciado sesión, se le asigna su token de sesión en lugar del sessionId para que pueda recuperar sus reservas desde cualquier dispositivo
            userToken = userTokenReserva;
        else
            userToken = session.getId();

        try {
            return this.reservasService.reservar(entradaId, ticketToken, userToken);
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
    public List<Object[]> getTicketsFromToken(@RequestParam String ticketToken) {  // Devolveremos los tickets completos
        return this.reservasService.getTicketsFromToken(ticketToken);
    }

    @PutMapping("/adoptReservations")
    public int adoptReservations(@RequestBody Map<String, Object> body) {  // Adoptar reservas anónimas bajo el nuevo userToken del usuario logeado
        String ticketToken = (String) body.get("ticketToken");
        String newUserToken = (String) body.get("newUserToken");
        
        try {
            return this.reservasService.adoptReservations(ticketToken, newUserToken);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al adoptar las reservas: " + e.getMessage(), e);
        }
    }

    @PutMapping("/cleanupExpiredReservations")
    public void cleanupExpiredReservations(@RequestBody Map<String, Object> body) {  // Endpoint para limpiar reservas expiradas TTL
        String ticketToken = (String) body.get("ticketToken");
        this.reservasService.cleanupExpiredReservations(ticketToken);
    }
}

