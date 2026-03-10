package edu.esi.ds.esientradas.http;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.esi.ds.esientradas.services.UsuariosService;

@RestController
@RequestMapping("/compra")
@CrossOrigin(origins = "*")  // Acepta todas las peticiones vengan de donde vengan "*", pero podríamos establecer las url que queramos
public class CompraController {

    @Autowired
    private UsuariosService usuariosService;
    
    @PostMapping("/comprar")  // POST /compra/comprar
    public void comprar(@RequestBody Map<String, Object> payload) {  // Lo pasaremos por el body (POST)
        String sessionId = (String) payload.get("sessionId");  // El sessionId se pasa en el body entre misma sesión
        Long idEntrada = ((Number) payload.get("idEntrada")).longValue();
        // Aquí iría la lógica para procesar la compra utilizando el sessionId y el idEntrada,
        // por ejemplo, llamando a un servicio que se encargue de la compra
        
    }
/* @PutMapping("/comprar")  // POST /compra/comprar
    public String comprar(HttpSession session, HttpServerResponse response, @RequestParam String userToken) throws IOException {
    String sessionId = session.getId();
        if(userToken == null || userToken.isEmpty()) {
            response.sendRedirect("http://www.uclm.es/")
            return null;
        }
        return this.usuariosService.checkToken(userToken);
        
        
*/     // Aquí iría la lógica para procesar la compra utilizando el sessionId y el userToken,
}
