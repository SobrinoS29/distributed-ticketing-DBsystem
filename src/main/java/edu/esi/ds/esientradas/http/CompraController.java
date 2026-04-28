package edu.esi.ds.esientradas.http;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.esi.ds.esientradas.services.EmailService;
import edu.esi.ds.esientradas.services.UsuariosService;

@RestController
@RequestMapping("/compra")
@CrossOrigin(origins = "*")  // Acepta todas las peticiones vengan de donde vengan "*", pero podríamos establecer las url que queramos
public class CompraController {

    @Autowired
    private UsuariosService usuariosService;
    @Autowired
    private EmailService emailService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostMapping("/comprar")  // POST /compra/comprar
    public void comprar(@RequestBody Map<String, Object> payload) {  // Lo pasaremos por el body (POST)
        String sessionId = (String) payload.get("sessionId");  // El sessionId se pasa en el body entre misma sesión
        Long idEntrada = ((Number) payload.get("idEntrada")).longValue();
        // Aquí iría la lógica para procesar la compra utilizando el sessionId y el idEntrada,
        // por ejemplo, llamando a un servicio que se encargue de la compra
        
    }
/* @PutMapping("/comprar")  // POST /compra/comprar
    public String comprar(HttpSession session, HttpServerResponse response, @RequestParam String sessionToken) throws IOException {
    String sessionId = session.getId();
        if(sessionToken == null || sessionToken.isEmpty()) {
            response.sendRedirect("http://www.uclm.es/")
            return null;
        }
        return this.usuariosService.checkToken(sessionToken);
        
        
*/     // Aquí iría la lógica para procesar la compra utilizando el sessionId y el sessionToken,

    @GetMapping("/checkUserToken")
    public String checkToken(@RequestParam String sessionToken) {
        if(sessionToken == null || sessionToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error 400: Token is required");
        }
        String userCheck = this.usuariosService.checkUserToken(sessionToken);
        if(userCheck == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Error 401: Invalid token");
        }
        return userCheck;
    }

    @PostMapping("/enviarEmailCompra")
    public void enviarEmailCompra(@RequestBody Map<String, Object> payload) throws JsonProcessingException {
        String tokenUser = (String) payload.get("sessionToken");
        Object entradasSeleccionadas = payload.get("ticketsSeleccionados");
        
        String entradasJson = objectMapper.writeValueAsString(entradasSeleccionadas);

        Object[] userInfoEmail = this.usuariosService.getUserInfoEmail(tokenUser);  // Obtener la información para enviar el email del usuario a partir de su token para enviarlo al servicio de email
        this.emailService.enviarEmailCompra(userInfoEmail, entradasJson);
    }
}
