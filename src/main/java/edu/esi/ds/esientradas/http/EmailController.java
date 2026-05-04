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

import edu.esi.ds.esientradas.services.EmailService;

@RestController
@RequestMapping("/email")
@CrossOrigin(origins = "*")
public class EmailController {
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/enviar-recuperacion-contrasena")
    public Map<String, String> enviarRecuperacionContrasena(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String resetToken = request.get("resetToken");
        String frontendUrl = request.get("frontendUrl");

        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error 400: Email is required");
        }
        if (resetToken == null || resetToken.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error 400: Reset token is required");
        }
        if (frontendUrl == null || frontendUrl.trim().isEmpty()) {
            frontendUrl = "http://localhost:4200";
        }

        try {
            this.emailService.enviarEmailRecuperacionContrasena(email.trim(), resetToken, frontendUrl);
            return Map.of("message", "Email de recuperación enviado correctamente");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al enviar email: " + e.getMessage(), e);
        }
    }

    @PostMapping("/enviar-cambio-contrasena")
    public Map<String, String> enviarCambioContrasena(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error 400: Email is required");
        }

        try {
            this.emailService.enviarEmailCambioContrasena(email.trim());
            return Map.of("message", "Email de confirmación enviado correctamente");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al enviar email: " + e.getMessage(), e);
        }
    }
}
