package edu.esi.ds.esientradas.http;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import edu.esi.ds.esientradas.services.ReservasService;
import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/reservas")
@CrossOrigin(origins = "*")
public class ReservasController {

    @Autowired
    private ReservasService service;
    
    @PutMapping("/reservar")
    public String reservar(HttpSession session, @RequestBody Map<String, Object> body) {  // Devolveremos el OK y el token con body: { "entradaId" : 123, "token": "abcd" } 
        Long entradaId = ((Number) body.get("entradaId")).longValue();
        String token = (String) body.get("token");
        String sessionId;

        if (token != null && !token.isEmpty())  // Si ya tiene token de reserva previo, agrupa bajo el mismo sessionId
            sessionId = token;
        else
            sessionId = session.getId();
        
        try {
            return this.service.reservar(entradaId, sessionId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al reservar la entrada: " + e.getMessage(), e);
        }
    }

    @PutMapping("/liberar")
    public String liberar(HttpSession session, @RequestBody Map<String, Object> body) {  // Devolveremos el OK y el token
        Long entradaId = ((Number) body.get("entradaId")).longValue();
        String sessionId = (String) body.get("token");
        
        try {
            return this.service.liberar(entradaId, sessionId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al liberar la entrada: " + e.getMessage(), e);
        }
    }
}

