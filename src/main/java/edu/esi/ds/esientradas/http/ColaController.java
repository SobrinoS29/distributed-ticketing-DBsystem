package edu.esi.ds.esientradas.http;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.esi.ds.esientradas.dto.ColaStatusDto;
import edu.esi.ds.esientradas.services.ColaEsperaService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/cola")
@CrossOrigin(origins = "*")
public class ColaController {

    @Autowired
    private ColaEsperaService colaEsperaService;

    @PostMapping("/unirse")
    public ColaStatusDto unirse(HttpSession session, @RequestBody Map<String, Object> body) {
        Long espectaculoId = ((Number) body.get("espectaculoId")).longValue();
        String userToken = (String) body.get("userToken");
        String participante = (userToken != null && !userToken.isBlank()) ? userToken : session.getId();

        return this.colaEsperaService.unirse(espectaculoId, participante);
    }

    @GetMapping("/estado")
    public ColaStatusDto estado(@RequestParam String queueToken) {
        return this.colaEsperaService.obtenerEstado(queueToken);
    }

    @PostMapping("/salir")
    public ColaStatusDto salir(@RequestBody Map<String, Object> body) {
        String queueToken = (String) body.get("queueToken");
        return this.colaEsperaService.salir(queueToken);
    }
}