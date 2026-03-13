package edu.esi.ds.esientradas.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Token;
import jakarta.transaction.Transactional;

@Service
public class ReservasService {
    
    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private TokenDao tokenDao;

    @Transactional
    public String reservar(Long idEntrada, String sessionId){
        if (idEntrada == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"ID de entrada no válido");
        }
        Entrada entrada = this.entradaDao.findById(idEntrada).orElseThrow(
            ()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Entrada no encontrada"));
        if (entrada.getEstado() != Estado.DISPONIBLE){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Entrada no disponible");
        }
        //entrada.setEstado(Estado.RESERVADA);
        //this.dao.save(entrada);
        Token token = new Token();
        token.setEntrada(entrada);
        token.setSessionId(sessionId);
        this.tokenDao.save(token);

        this.entradaDao.updateEstado(idEntrada, Estado.RESERVADA);
        return token.getSessionId();
    }

    @Transactional
    public String liberar(Long idEntrada, String sessionId){
        if (idEntrada == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"ID de entrada no válido");
        }
        Entrada entrada = this.entradaDao.findById(idEntrada).orElseThrow(
            ()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Entrada no encontrada"));
        if (entrada.getEstado() != Estado.RESERVADA){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Entrada no reservada");
        }
        
        if(this.tokenDao.deleteByEntradaIdAndSessionId(idEntrada, sessionId) == 1)  // Si se eliminó el token de reserva, se libera la entrada
            this.entradaDao.updateEstado(idEntrada, Estado.DISPONIBLE);
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"No se pudo liberar la entrada");
        return sessionId;
    }

    public List<Object[]> getTicketsFromToken(String token) {
        return this.tokenDao.getTicketsFromToken(token);
    }
}
