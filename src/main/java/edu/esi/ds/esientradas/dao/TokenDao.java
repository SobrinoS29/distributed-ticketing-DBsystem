package edu.esi.ds.esientradas.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import edu.esi.ds.esientradas.model.Token;

public interface TokenDao extends JpaRepository<Token, String> {

    @Transactional
    @Modifying
    @Query(value = """
        DELETE
        FROM token
        WHERE session_id = :sessionId
            AND entrada_id = :idEntrada""", nativeQuery = true)
    int deleteByEntradaIdAndSessionId(@Param("idEntrada") Long idEntrada, @Param("sessionId") String sessionId);  // Método para eliminar el token de reserva cuando se libera una entrada
    
     @Query(value = """
            SELECT t.entrada_id, e.precio, z.zona, p.fila, p.columna, p.planta, e.espectaculo_id, s.escenario_id
                FROM token AS t
                JOIN entrada AS e 
                    ON t.entrada_id = e.id
                JOIN de_zona AS z 
                    ON e.id = z.id
                LEFT JOIN precisa AS p 
                    ON e.id = p.id
                JOIN espectaculo AS s 
                    ON e.espectaculo_id = s.id
                WHERE t.session_id = :token""", nativeQuery = true)
    List<Object[]> getTicketsFromToken(@Param("token") String token);
}
