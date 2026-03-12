package edu.esi.ds.esientradas.dao;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;

public interface EntradaDao extends JpaRepository<Entrada, Long> {
    List<Entrada> findByEspectaculoId(Long espectaculoId);

    @Query(value = "UPDATE Entrada e SET e.estado = :estado WHERE e.id = :idEntrada")
    @Modifying
    void updateEstado(@Param("idEntrada") Long idEntrada, @Param("estado")Estado estado);

    /*
    Integer countByEspectaculoId(Long espectaculoId);

    Integer countByEspectaculoIdAndEstado(Long espectaculoId, Estado estado);
        // findByEspectaculo(id).stream().filter(e -> e.getEstado() == Estado.DISPONIBLE).count();  Dejamos que sea la base de datos quien se encargue de contar las entradas disponibles, es más eficiente que traer todas las entradas y luego filtrarlas en memoria.
    */

    @Query(value = """
            SELECT
                CAST(COUNT(*) AS SIGNED) as total,  # COUNT(*) devuelve un Long, pero el DtoEntrada espera un Integer, por lo que hacemos un CAST a SIGNED para que devuelva un Integer. Esto es específico de MySQL, en otros motores de base de datos el CAST puede ser diferente.
                CAST(SUM(CASE WHEN estado='DISPONIBLE' THEN 1 ELSE 0 END) AS SIGNED) AS libres,
                CAST(SUM(CASE WHEN estado='RESERVADA' THEN 1 ELSE 0 END) AS SIGNED) AS reservadas,
                CAST(SUM(CASE WHEN estado='VENDIDA' THEN 1 ELSE 0 END) AS SIGNED) AS vendidas
            FROM entrada
            WHERE espectaculo_id = :espectaculoId""", nativeQuery = true)
    Object getNumeroDeEntradasComoDto(@Param("espectaculoId") Long espectaculoId);

    @Query(value = """
            SELECT e.id, e.precio, e.espectaculo_id, z.zona
                FROM entrada AS e, de_zona AS z
                WHERE e.espectaculo_id = :espectaculoId
                    AND e.estado = 'DISPONIBLE'
                    AND z.zona = :zona
                    AND e.id = z.id""", nativeQuery = true)
    List<Object[]> findByEspectaculoIdByZonaAndEstado(@Param("espectaculoId") Long espectaculoId, @Param("zona") Integer zona);
}
