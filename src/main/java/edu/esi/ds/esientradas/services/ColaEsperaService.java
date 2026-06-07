package edu.esi.ds.esientradas.services;

import java.util.ArrayDeque;
import java.util.Deque;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dto.ColaStatusDto;

@Service
@CrossOrigin(origins = "*")
public class ColaEsperaService {

    // Configurable: espectáculo objetivo e hora de apertura
    private static final Long TARGET_ESPECTACULO_ID = 1L;  // Igual que escenarios.irASeleccionarEntradas()
    private static final LocalTime QUEUE_OPEN_TIME = LocalTime.of(8, 43); // hh:mm

    private static final long VENTANA_TURNO_MS = 5 * 60 * 1000L; // 5 minutos por turno = TTL (seleccionar-entradas y pago)

    private final Map<Long, Deque<ParticipanteCola>> colasPorEspectaculo = new HashMap<>();
    private final Map<String, ParticipanteCola> participantesPorToken = new HashMap<>();

    public synchronized ColaStatusDto unirse(Long espectaculoId, String participanteClave) {
        validarEspectaculo(espectaculoId);
        validarParticipante(participanteClave);

        // Si este espectáculo no usa cola, devolvemos estado de bypass
        if (!usaCola(espectaculoId)) {
            return construirEstadoAccesoLibre(espectaculoId);
        }

        limpiarColasCaducadas();

        String queueToken = UUID.randomUUID().toString();
        EstadoCola estadoInicial = isQueueOpenNow() ? EstadoCola.ESPERANDO : EstadoCola.PREOPEN;
        ParticipanteCola nuevo = new ParticipanteCola(queueToken, espectaculoId, participanteClave, estadoInicial);

        participantesPorToken.put(queueToken, nuevo);
        obtenerCola(espectaculoId).addLast(nuevo);

        // Activamos si procede (si la cola ya está abierta y es el primero)
        activarSiguienteSiCorresponde(espectaculoId);
        return construirEstado(nuevo);
    }

    public synchronized ColaStatusDto obtenerEstado(String queueToken) {
        validarToken(queueToken);
        limpiarColasCaducadas();

        ParticipanteCola participante = participantesPorToken.get(queueToken);
        if (participante == null) {
            return construirEstadoTokenNoEncontrado(queueToken);
        }

        return construirEstado(participante);
    }

    public synchronized ColaStatusDto salir(String queueToken) {
        validarToken(queueToken);

        ParticipanteCola participante = participantesPorToken.get(queueToken);
        if (participante == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Turno no encontrado");
        }

        expulsarParticipante(participante, EstadoCola.SALIDO, "Has salido de la cola");
        activarSiguienteSiCorresponde(participante.getEspectaculoId());
        return construirEstado(participante);
    }

    private void limpiarColasCaducadas() {
        long ahora = System.currentTimeMillis();

        Iterator<Map.Entry<Long, Deque<ParticipanteCola>>> iter = colasPorEspectaculo.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long, Deque<ParticipanteCola>> entry = iter.next();
            Deque<ParticipanteCola> cola = entry.getValue();

            while (!cola.isEmpty()) {
                ParticipanteCola primero = cola.peekFirst();
                if (primero == null) break;

                // Si estaba en PREOPEN y la cola no ha abierto, dejamos en sitio
                if (primero.getEstado() == EstadoCola.PREOPEN) {
                    if (!isQueueOpenNow()) break;
                    // pasa a ESPERANDO cuando la cola abre
                    primero.setEstado(EstadoCola.ESPERANDO);
                    primero.setTurnoExpiraEn(null);
                    // reevaluar en siguiente iteración
                    continue;
                }

                if (primero.getEstado() == EstadoCola.ESPERANDO) {
                    primero.setEstado(EstadoCola.TURNO_ACTIVO);
                    primero.setTurnoExpiraEn(ahora + VENTANA_TURNO_MS);
                    break;
                }

                if (primero.getEstado() == EstadoCola.TURNO_ACTIVO) {
                    Long exp = primero.getTurnoExpiraEn();
                    if (exp != null && exp <= ahora) {
                        expulsarParticipante(primero, EstadoCola.EXPIRADO, "Tu turno ha expirado");
                        continue;
                    }
                    break;
                }

                // si está en otro estado se elimina
                cola.pollFirst();
            }

            if (cola.isEmpty()) iter.remove();
        }
    }

    private void activarSiguienteSiCorresponde(Long espectaculoId) {
        Deque<ParticipanteCola> cola = colasPorEspectaculo.get(espectaculoId);
        if (cola == null || cola.isEmpty()) return;

        ParticipanteCola primero = cola.peekFirst();
        if (primero != null && primero.getEstado() == EstadoCola.ESPERANDO) {
            primero.setEstado(EstadoCola.TURNO_ACTIVO);
            primero.setTurnoExpiraEn(System.currentTimeMillis() + VENTANA_TURNO_MS);
        }
    }

    private ColaStatusDto construirEstado(ParticipanteCola participante) {
        Deque<ParticipanteCola> cola = colasPorEspectaculo.get(participante.getEspectaculoId());
        int posicion = -1;
        if (cola != null) {
            int idx = 0;
            for (ParticipanteCola c : cola) {
                if (c.getQueueToken().equals(participante.getQueueToken())) { posicion = idx; break; }
                idx++;
            }
        }

        ColaStatusDto estado = new ColaStatusDto();
        estado.setQueueToken(participante.getQueueToken());
        estado.setEspectaculoId(participante.getEspectaculoId());
        estado.setStatus(participante.getEstado().name());
        estado.setPosition(posicion >= 0 ? posicion + 1 : null);
        estado.setAheadCount(Math.max(posicion, 0));

        if (participante.getEstado() == EstadoCola.TURNO_ACTIVO) {
            estado.setCanProceed(true);
            estado.setTurnExpiresAt(participante.getTurnoExpiraEn());
            estado.setMessage("Tu turno está activo. Ya puedes continuar con la selección de entradas.");
        } else if (participante.getEstado() == EstadoCola.PREOPEN) {
            estado.setCanProceed(false);
            estado.setTurnExpiresAt(fechaAperturaActualEnMillis());
            estado.setMessage("La cola todavía no ha abierto. Se abrirá a las " + QUEUE_OPEN_TIME.toString() + ".");
        } else if (participante.getEstado() == EstadoCola.ESPERANDO) {
            estado.setCanProceed(false);
            estado.setTurnExpiresAt(participante.getTurnoExpiraEn());
            estado.setMessage(posicion > 0 ? "Hay " + posicion + " persona(s) delante de ti." : "Tu turno se está preparando.");
        } else if (participante.getEstado() == EstadoCola.EXPIRADO) {
            estado.setCanProceed(false);
            estado.setMessage("Tu turno ha expirado. Debes volver a entrar en la cola.");
        } else {
            estado.setCanProceed(false);
            estado.setMessage("Has abandonado la cola.");
        }

        return estado;
    }

    private void expulsarParticipante(ParticipanteCola participante, EstadoCola nuevoEstado, String mensaje) {
        Deque<ParticipanteCola> cola = colasPorEspectaculo.get(participante.getEspectaculoId());
        if (cola != null) cola.remove(participante);

        participante.setEstado(nuevoEstado);
        participante.setTurnoExpiraEn(null);
        participante.setMensaje(mensaje);
    }

    private Deque<ParticipanteCola> obtenerCola(Long espectaculoId) {
        return colasPorEspectaculo.computeIfAbsent(espectaculoId, k -> new ArrayDeque<>());
    }

    private void validarEspectaculo(Long espectaculoId) {
        if (espectaculoId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "espectaculoId es requerido");
    }

    private void validarParticipante(String participanteClave) {
        if (participanteClave == null || participanteClave.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "participante es requerido");
    }

    private void validarToken(String queueToken) {
        if (queueToken == null || queueToken.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "queueToken es requerido");
    }

    private String claveParticipante(Long espectaculoId, String participanteClave) {
        return espectaculoId + "::" + participanteClave;
    }

    private boolean usaCola(Long espectaculoId) {
        return TARGET_ESPECTACULO_ID.equals(espectaculoId);
    }

    private boolean isQueueOpenNow() {
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        return !now.isBefore(QUEUE_OPEN_TIME);
    }

    private Long fechaAperturaActualEnMillis() {
        LocalDate hoy = LocalDate.now(ZoneId.systemDefault());
        LocalDateTime apertura = hoy.atTime(QUEUE_OPEN_TIME);
        return apertura.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private ColaStatusDto construirEstadoAccesoLibre(Long espectaculoId) {
        ColaStatusDto estado = new ColaStatusDto();
        estado.setQueueToken("");
        estado.setEspectaculoId(espectaculoId);
        estado.setStatus("BYPASS");
        estado.setPosition(null);
        estado.setAheadCount(0);
        estado.setCanProceed(true);
        estado.setTurnExpiresAt(null);
        estado.setMessage("Este espectáculo no usa cola virtual.");
        return estado;
    }

    private ColaStatusDto construirEstadoTokenNoEncontrado(String queueToken) {
        ColaStatusDto estado = new ColaStatusDto();
        estado.setQueueToken(queueToken);
        estado.setEspectaculoId(-1L);
        estado.setStatus("TOKEN_NOT_FOUND");
        estado.setPosition(null);
        estado.setAheadCount(0);
        estado.setCanProceed(false);
        estado.setTurnExpiresAt(null);
        estado.setMessage("El turno no existe o ha expirado. Se requiere un nuevo acceso a la cola.");
        return estado;
    }

    private enum EstadoCola {
        PREOPEN,
        ESPERANDO,
        TURNO_ACTIVO,
        EXPIRADO,
        SALIDO;

        boolean esActivo() { return this == ESPERANDO || this == TURNO_ACTIVO; }
    }

    private static final class ParticipanteCola {
        private final String queueToken;
        private final Long espectaculoId;
        private final String participanteClave;
        private final long creadoEn;
        private EstadoCola estado;
        private Long turnoExpiraEn;
        private String mensaje;

        private ParticipanteCola(String queueToken, Long espectaculoId, String participanteClave, EstadoCola estadoInicial) {
            this.queueToken = queueToken;
            this.espectaculoId = espectaculoId;
            this.participanteClave = participanteClave;
            this.creadoEn = System.currentTimeMillis();
            this.estado = estadoInicial;
        }

        private String getQueueToken() { return queueToken; }
        private Long getEspectaculoId() { return espectaculoId; }
        private String getParticipanteClave() { return participanteClave; }
        private long getCreadoEn() { return creadoEn; }
        private EstadoCola getEstado() { return estado; }
        private void setEstado(EstadoCola estado) { this.estado = estado; }
        private Long getTurnoExpiraEn() { return turnoExpiraEn; }
        private void setTurnoExpiraEn(Long turnoExpiraEn) { this.turnoExpiraEn = turnoExpiraEn; }
        private void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }
}
