package edu.esi.ds.esientradas.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.dao.EscenarioDao;
import edu.esi.ds.esientradas.dao.EspectaculoDao;
import edu.esi.ds.esientradas.dto.DtoEntrada;
import edu.esi.ds.esientradas.dao.EntradaDao;

@Service
public class BusquedaService {

    @Autowired
    private EscenarioDao dao;

    @Autowired
    private EspectaculoDao espectuloDao;

    @Autowired
    private EntradaDao entradaDao;

    public List<Escenario> getEscenarios() {
        return this.dao.findAll();
    }

    public List<Espectaculo> getEspectaculos(String artista) {
        return this.espectuloDao.findByArtista(artista);
    }

    public List<Espectaculo> getEspectaculos(Long idEscenario) {
        return this.espectuloDao.findByEscenarioId(idEscenario);
    }

    public List<Entrada> getEntradas(Long espectaculoId) {
        return this.entradaDao.findByEspectaculoId(espectaculoId);
    }

    /*
    public Integer getNumeroDeEntradas(Long espectaculoId) {
        return this.entradaDao.countByEspectaculoId(espectaculoId);
    }

    public Integer getEntradasLibres(Long espectaculoId) {
        return this.entradaDao.countByEspectaculoIdAndEstado(espectaculoId, Estado.DISPONIBLE);
    }
    */

    public DtoEntrada getNumeroDeEntradasComoDto(Long espectaculoId) {
        List<Object[]> resultado = this.entradaDao.getNumeroDeEntradasComoDto(espectaculoId);
        if (resultado.isEmpty()) {
            return new DtoEntrada(0, 0, 0, 0);
        }
        Object[] obj = resultado.get(0);
        DtoEntrada dto = new DtoEntrada(
            ((Number) obj[0]).intValue(),  // total
            ((Number) obj[1]).intValue(),  // libres
            ((Number) obj[2]).intValue(),  // reservadas
            ((Number) obj[3]).intValue()   // vendidas
        );
        return dto;
    }
}