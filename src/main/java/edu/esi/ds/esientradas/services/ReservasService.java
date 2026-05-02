package edu.esi.ds.esientradas.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.TicketTokenDao;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.TicketToken;
import jakarta.transaction.Transactional;

@Service
public class ReservasService {
    
    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private TicketTokenDao ticketTokenDao;

    @Transactional
    public String reservar(Long idEntrada, String ticketToken, String userToken) {
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
        TicketToken token = new TicketToken();
        token.setEntrada(entrada);
        token.setTokenReserva(ticketToken);
        token.setUserToken(userToken);
        this.ticketTokenDao.save(token);

        this.entradaDao.updateEstado(idEntrada, Estado.RESERVADA);
        return token.getTokenReserva() + "|" + token.getUserToken();
    }

    @Transactional
    public String liberar(Long idEntrada, String ticketToken){
        if (idEntrada == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"ID de entrada no válido");
        }
        Entrada entrada = this.entradaDao.findById(idEntrada).orElseThrow(
            ()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Entrada no encontrada"));
        if (entrada.getEstado() != Estado.RESERVADA){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Entrada no reservada");
        }
        
        if(this.ticketTokenDao.deleteByEntradaIdAndTokenReserva(idEntrada, ticketToken) == 1)  // Si se eliminó el token de reserva, se libera la entrada
            this.entradaDao.updateEstado(idEntrada, Estado.DISPONIBLE);
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"No se pudo liberar la entrada");
        return ticketToken;
    }

    public List<Object[]> getTicketsFromToken(String ticketToken) {
        return this.ticketTokenDao.getTicketsFromToken(ticketToken);
    }

    @Transactional
    public int adoptReservations(String ticketToken, String newUserToken) {
        if (ticketToken == null || ticketToken.isEmpty() || newUserToken == null || newUserToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ticketToken y newUserToken son requeridos");
        }
        return this.ticketTokenDao.adoptReservations(ticketToken, newUserToken);
    }

    @Transactional
    public void cleanupExpiredReservations(String ticketToken) {
        this.ticketTokenDao.deleteExpiredTokens(ticketToken);  // Elimina tokens expirados
        this.entradaDao.liberarEntradasHuerfanas();  // Libera entradas reservadas sin token asociado (tokens expirados)
    }

    @Transactional
    public int updateTicketsAsSold(String ticketToken) {
        if (ticketToken == null || ticketToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ticketToken es requerido");
        }
        return this.entradaDao.updateEstadoByTicketToken(ticketToken);
    }
}
