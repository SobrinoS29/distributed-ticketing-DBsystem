package edu.esi.ds.esientradas.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConfiguracionDao {

    private final JdbcTemplate jdbcTemplate;

    public ConfiguracionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String findByClave(String clave) {
        String sql = "SELECT key_value FROM Configuracion WHERE key_type = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, clave);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
