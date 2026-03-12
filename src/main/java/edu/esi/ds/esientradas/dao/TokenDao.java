package edu.esi.ds.esientradas.dao;

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
    
}
