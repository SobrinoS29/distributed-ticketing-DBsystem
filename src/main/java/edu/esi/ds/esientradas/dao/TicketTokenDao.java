package edu.esi.ds.esientradas.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import edu.esi.ds.esientradas.model.TicketToken;

public interface TicketTokenDao extends JpaRepository<TicketToken, String> {

    @Transactional
    @Modifying
    @Query(value = """
        DELETE
        FROM ticket_token
        WHERE token_reserva = :ticketToken
            AND entrada_id = :idEntrada""", nativeQuery = true)
    int deleteByEntradaIdAndTokenReserva(@Param("idEntrada") Long idEntrada, @Param("ticketToken") String ticketToken);  // Método para eliminar el token de reserva cuando se libera una entrada
    
     @Query(value = """
            SELECT t.entrada_id, e.precio, z.zona, p.fila, p.columna, p.planta, e.espectaculo_id, s.escenario_id
                FROM ticket_token AS t
                JOIN entrada AS e 
                    ON t.entrada_id = e.id
                JOIN de_zona AS z 
                    ON e.id = z.id
                LEFT JOIN precisa AS p 
                    ON e.id = p.id
                JOIN espectaculo AS s 
                    ON e.espectaculo_id = s.id
                WHERE t.token_reserva = :ticketToken""", nativeQuery = true)
    List<Object[]> getTicketsFromToken(@Param("ticketToken") String ticketToken);

    @Transactional
    @Modifying
    @Query(value ="""
        UPDATE ticket_token
        SET user_token = :newUserToken
        WHERE token_reserva = :ticketToken""", nativeQuery = true)
    int adoptReservations(@Param("ticketToken") String ticketToken, @Param("newUserToken") String newUserToken);  // Adopta reservas bajo el nuevo userToken del usuario logeado

    @Transactional
    @Modifying
    @Query(value = """
        DELETE FROM ticket_token
        WHERE token_reserva = :ticketToken""", nativeQuery = true)
    int deleteExpiredTokens(@Param("ticketToken") String ticketToken);  // Elimina tokens expirados (TTL)
}
