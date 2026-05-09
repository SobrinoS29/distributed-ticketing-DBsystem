// Integracion de envio de correos usando Mailgun desde backend

package edu.esi.ds.esientradas.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.text.NumberFormat;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import edu.esi.ds.esientradas.dao.ConfiguracionDao;
import edu.esi.ds.esientradas.dao.EscenarioDao;
import edu.esi.ds.esientradas.dao.EspectaculoDao;
import edu.esi.ds.esientradas.dao.HistoricalDataPayDao;
import edu.esi.ds.esientradas.dao.PagoDao;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.model.HistoricalDataPay;
import edu.esi.ds.esientradas.model.Pago;

@Service
public class EmailService {

    @Autowired
    private ConfiguracionDao configuracionDao;
    @Autowired
    private HistoricalDataPayDao historicalDataPayDao;
    @Autowired
    private EspectaculoDao espectaculoDao;
    @Autowired
    private EscenarioDao escenarioDao;
    @Autowired
    private PagoDao pagoDao;
    
    

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


    public int enviarEmailCompra(Object[] userInfoEmail, String entradasJson) {
        String apiKey = this.configuracionDao.findByClave("MAILGUN_API_KEY");
        String baseUrl = this.configuracionDao.findByClave("MAILGUN_BASE_URL");
        String domain = this.configuracionDao.findByClave("MAILGUN_DOMAIN");
        String fromEmail = this.configuracionDao.findByClave("MAILGUN_FROM_EMAIL");
        String fromName = this.configuracionDao.findByClave("MAILGUN_FROM_NAME");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_API_KEY");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_BASE_URL");
        }
        if (domain == null || domain.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_DOMAIN");
        }
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_FROM_EMAIL");
        }
        String apiUrl = baseUrl.endsWith("/") ? baseUrl + domain + "/messages" : baseUrl + "/" + domain + "/messages";
        String remitenteNombre = (fromName == null || fromName.isBlank()) ? "ESI Entradas" : fromName;

        String userName = (userInfoEmail.length > 0 && userInfoEmail[0] != null) ? userInfoEmail[0].toString() : "Cliente";
        String userEmail = (userInfoEmail.length > 1 && userInfoEmail[1] != null) ? userInfoEmail[1].toString() : "";

        if (userEmail.isBlank()) {
            throw new IllegalArgumentException("El email del usuario es obligatorio para enviar la confirmación.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + auth);

        String htmlContent = construirHtmlCompra(userName, entradasJson);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("from", remitenteNombre + " <" + fromEmail + ">");
        body.add("to", userEmail);
        body.add("subject", "Confirmacion de compra - ESI Entradas");
        body.add("html", htmlContent);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("null")
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error enviando email con Mailgun: " + response.getBody());
            }
        } catch (HttpStatusCodeException e) {
            String detalle = e.getResponseBodyAsString();
            throw new RuntimeException("No se pudo enviar el email con Mailgun. HTTP " + e.getStatusCode().value() + ": " + detalle, e);
        } catch (RestClientException e) {
            throw new RuntimeException("No se pudo enviar el email con Mailgun", e);
        }
        return 0;
    }

    private String construirHtmlCompra(String userName, String entradasJson) {
        StringBuilder filas = new StringBuilder();
        int totalCentimos = 0;
        int totalEntradas = 0;

        try {
            JsonNode root = objectMapper.readTree(entradasJson == null || entradasJson.isBlank() ? "[]" : entradasJson);

            JsonNode array = root.isArray() ? root : objectMapper.createArrayNode().add(root);

            for (JsonNode entrada : array) {
                int entradaId = entrada.path("entradaId").asInt(0);
                int zona = entrada.path("zona").asInt(0);
                int precioCentimos = entrada.hasNonNull("precioCentimos")
                        ? entrada.path("precioCentimos").asInt(0)
                        : entrada.path("precio").asInt(0);
                int espectaculoId = entrada.path("espectaculoId").asInt(0);
                int escenarioId = entrada.path("escenarioId").asInt(0);
                String qrPayload = construirPayloadQr(entradaId, escenarioId, espectaculoId);
                String qrUrl = generarUrlQr(qrPayload);

                totalCentimos += precioCentimos;
                totalEntradas++;

                filas.append("<tr>")
                        .append("<td style='padding:8px;border:1px solid #ddd;'>").append(entradaId).append("</td>")
                        .append("<td style='padding:8px;border:1px solid #ddd;'>").append(escapeHtml(getZonaNombrePorId(zona))).append("</td>")
                        .append("<td style='padding:8px;border:1px solid #ddd;text-align:right;'>")
                        .append(formatearEuros(precioCentimos))
                        .append("</td>")
                    .append("<td style='padding:6px;border:1px solid #ddd;text-align:center;vertical-align:middle;'>")
                    .append("<img src='").append(qrUrl)
                    .append("' alt='QR entrada ").append(entradaId)
                    .append("' width='80' height='80' style='display:block;margin:0 auto;' />")
                    .append("</td>")
                        .append("</tr>");
            }
        } catch (Exception e) {
            String escapedJson = escapeHtml(entradasJson == null || entradasJson.isBlank() ? "[]" : entradasJson);
                filas.append("<tr><td colspan='4' style='padding:8px;border:1px solid #ddd;'><pre style='margin:0;'>")
                    .append(escapedJson)
                    .append("</pre></td></tr>");
        }

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String totalEuros = formatearEuros(totalCentimos);

        return "<div style='font-family:Arial,sans-serif;color:#1f2937;'>"
                + "<h2 style='color:#111827;'>Confirmación de compra</h2>"
                + "<p>Hola <strong>" + escapeHtml(userName) + "</strong>,</p>"
                + "<p>Tu compra se ha registrado correctamente. Gracias por confiar en <strong>ESI Entradas</strong>.</p>"
                + "<p><strong>Fecha:</strong> " + fecha + "</p>"
                + "<p><strong>Número de entradas:</strong> " + totalEntradas + "</p>"
                + "<table style='border-collapse:collapse;width:100%;max-width:650px;margin-top:12px;'>"
                + "<thead>"
                + "<tr style='background:#f3f4f6;'>"
                + "<th style='padding:8px;border:1px solid #ddd;text-align:left;'>Entrada ID</th>"
                + "<th style='padding:8px;border:1px solid #ddd;text-align:left;'>Zona</th>"
                + "<th style='padding:8px;border:1px solid #ddd;text-align:right;'>Precio</th>"
                + "<th style='padding:8px;border:1px solid #ddd;text-align:center;'>QR</th>"
                + "</tr>"
                + "</thead>"
                + "<tbody>" + filas + "</tbody>"
                + "</table>"
                + "<p style='margin-top:14px;font-size:16px;'><strong>Total pagado: " + totalEuros + "</strong></p>"
                + "<p style='margin-top:24px;color:#6b7280;'>Este es un mensaje automático, por favor no respondas a este correo.</p>"
                + "</div>";
    }

    private String formatearEuros(int centimos) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        return formato.format(centimos / 100.0);
    }

    private String getZonaNombrePorId(int zonaId) {
        switch (zonaId) {
            case 1:
                return "Pista";
            case 2:
                return "Grada Norte";
            case 3:
                return "Grada Sur";
            default:
                return "Zona " + zonaId;
        }
    }

    private String construirPayloadQr(int entradaId, int escenarioId, int espectaculoId) {
        return "entradaId=" + entradaId + "|escenarioId=" + escenarioId + "|espectaculoId=" + espectaculoId;
    }

    private String generarUrlQr(String contenido) {
        try {
            String encoded = java.net.URLEncoder.encode(contenido, "UTF-8");
            return "https://api.qrserver.com/v1/create-qr-code/?size=80x80&data=" + encoded;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar la URL del QR", e);
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public void saveEmailPagoData(String paymentIntentId, Object[] userInfoEmail, String entradasJson) {
        String userName = (userInfoEmail.length > 0 && userInfoEmail[0] != null) ? userInfoEmail[0].toString() : "Cliente";
        String userEmail = (userInfoEmail.length > 1 && userInfoEmail[1] != null) ? userInfoEmail[1].toString() : "";
        String userId = (userInfoEmail.length > 2 && userInfoEmail[2] != null) ? userInfoEmail[2].toString() : "";
        int totalCentimos = 0;
        String totalEntradas = "";
        int espectaculoId = 0;
        int escenarioId = 0;

        try {
            JsonNode root = objectMapper.readTree(entradasJson == null || entradasJson.isBlank() ? "[]" : entradasJson);
            JsonNode array = root.isArray() ? root : objectMapper.createArrayNode().add(root);
            for (JsonNode entrada : array) {
                int entradaId = entrada.path("entradaId").asInt(0);
                int zona = entrada.path("zona").asInt(0);
                int precioCentimos = entrada.hasNonNull("precioCentimos")
                        ? entrada.path("precioCentimos").asInt(0)
                        : entrada.path("precio").asInt(0);
                espectaculoId = entrada.path("espectaculoId").asInt(0);
                escenarioId = entrada.path("escenarioId").asInt(0);

                totalEntradas += entradaId + " (Zona:" + getZonaNombrePorId(zona) + "), ";
                totalCentimos += precioCentimos;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al procesar el JSON de entradas", e);
        }

        Pago pago = pagoDao.findByPaymentIntentId(paymentIntentId);
        if (pago == null)
            throw new IllegalStateException("No existe un pago con paymentIntentId=" + paymentIntentId);
        Espectaculo espectaculo = espectaculoDao.findById((long) espectaculoId).orElseThrow(() -> new IllegalStateException());
        Escenario escenario = escenarioDao.findById((long) escenarioId).orElseThrow(() -> new IllegalStateException());

        HistoricalDataPay historicalDataPay = new HistoricalDataPay();
        historicalDataPay.setPago(pago);
        historicalDataPay.setUserId(userId);
        historicalDataPay.setUserName(userName);
        historicalDataPay.setUserEmail(userEmail);
        historicalDataPay.setTotalCentimos(totalCentimos);
        historicalDataPay.setEspectaculo(espectaculo);
        historicalDataPay.setEscenario(escenario);
        historicalDataPay.setTotalEntradas(totalEntradas);

        historicalDataPayDao.save(historicalDataPay);
    }

    public void enviarEmailRecuperacionContrasena(String email, String resetToken, String frontendUrl) {
        String apiKey = this.configuracionDao.findByClave("MAILGUN_API_KEY");
        String baseUrl = this.configuracionDao.findByClave("MAILGUN_BASE_URL");
        String domain = this.configuracionDao.findByClave("MAILGUN_DOMAIN");
        String fromEmail = this.configuracionDao.findByClave("MAILGUN_FROM_EMAIL");
        String fromName = this.configuracionDao.findByClave("MAILGUN_FROM_NAME");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_API_KEY");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_BASE_URL");
        }
        if (domain == null || domain.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_DOMAIN");
        }
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_FROM_EMAIL");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email del usuario es obligatorio para enviar la recuperación de contraseña.");
        }

        String apiUrl = baseUrl.endsWith("/") ? baseUrl + domain + "/messages" : baseUrl + "/" + domain + "/messages";
        String remitenteNombre = (fromName == null || fromName.isBlank()) ? "ESI Entradas" : fromName;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + auth);

        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        String htmlContent = construirHtmlRecuperacionContrasena(resetUrl);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("from", remitenteNombre + " <" + fromEmail + ">");
        body.add("to", email);
        body.add("subject", "Recupera tu contraseña - ESI Entradas");
        body.add("html", htmlContent);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("null")
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error enviando email con Mailgun: " + response.getBody());
            }
        } catch (HttpStatusCodeException e) {
            String detalle = e.getResponseBodyAsString();
            throw new RuntimeException("No se pudo enviar el email con Mailgun. HTTP " + e.getStatusCode().value() + ": " + detalle, e);
        } catch (RestClientException e) {
            throw new RuntimeException("No se pudo enviar el email con Mailgun", e);
        }
    }

    public void enviarEmailCambioContrasena(String email) {
        String apiKey = this.configuracionDao.findByClave("MAILGUN_API_KEY");
        String baseUrl = this.configuracionDao.findByClave("MAILGUN_BASE_URL");
        String domain = this.configuracionDao.findByClave("MAILGUN_DOMAIN");
        String fromEmail = this.configuracionDao.findByClave("MAILGUN_FROM_EMAIL");
        String fromName = this.configuracionDao.findByClave("MAILGUN_FROM_NAME");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_API_KEY");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_BASE_URL");
        }
        if (domain == null || domain.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_DOMAIN");
        }
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_FROM_EMAIL");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email del usuario es obligatorio para enviar la confirmación de cambio.");
        }

        String apiUrl = baseUrl.endsWith("/") ? baseUrl + domain + "/messages" : baseUrl + "/" + domain + "/messages";
        String remitenteNombre = (fromName == null || fromName.isBlank()) ? "ESI Entradas" : fromName;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + auth);

        String htmlContent = construirHtmlCambioContrasena();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("from", remitenteNombre + " <" + fromEmail + ">");
        body.add("to", email);
        body.add("subject", "Tu contraseña ha sido cambiada - ESI Entradas");
        body.add("html", htmlContent);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("null")
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error enviando email con Mailgun: " + response.getBody());
            }
        } catch (HttpStatusCodeException e) {
            String detalle = e.getResponseBodyAsString();
            throw new RuntimeException("No se pudo enviar el email con Mailgun. HTTP " + e.getStatusCode().value() + ": " + detalle, e);
        } catch (RestClientException e) {
            throw new RuntimeException("No se pudo enviar el email con Mailgun", e);
        }
    }

    private String construirHtmlRecuperacionContrasena(String resetUrl) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        return "<div style='font-family:Arial,sans-serif;color:#1f2937;max-width:600px;margin:0 auto;'>"
                + "<h2 style='color:#111827;'>Recupera tu contraseña</h2>"
                + "<p>Hemos recibido una solicitud para recuperar tu contraseña en <strong>ESI Entradas</strong>.</p>"
                + "<p><strong>Fecha de solicitud:</strong> " + fecha + "</p>"
                + "<p>Haz clic en el siguiente botón para establecer una nueva contraseña:</p>"
                + "<div style='text-align:center;margin:24px 0;'>"
                + "<a href='" + escapeHtml(resetUrl) + "' style='display:inline-block;padding:12px 28px;background:#2563eb;color:#fff;text-decoration:none;border-radius:6px;font-weight:bold;font-size:16px;'>Recuperar Contraseña</a>"
                + "</div>"
                + "<p style='color:#6b7280;font-size:14px;'>O copia y pega este enlace en tu navegador:</p>"
                + "<p style='background:#f3f4f6;padding:12px;border-radius:6px;word-break:break-all;color:#334155;font-family:monospace;font-size:12px;'>" + escapeHtml(resetUrl) + "</p>"
                + "<p style='color:#6b7280;font-size:13px;'>Este enlace es válido durante <strong>1 hora</strong>.</p>"
                + "<hr style='border:none;border-top:1px solid #e5e7eb;margin:24px 0;'>"
                + "<p style='color:#6b7280;font-size:13px;'>Si no solicitaste este cambio, ignora este correo. Tu contraseña permanecerá sin cambios.</p>"
                + "<p style='color:#6b7280;font-size:13px;'>Este es un mensaje automático, por favor no respondas a este correo.</p>"
                + "</div>";
    }

    private String construirHtmlCambioContrasena() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        return "<div style='font-family:Arial,sans-serif;color:#1f2937;max-width:600px;margin:0 auto;'>"
                + "<h2 style='color:#111827;'>Tu contraseña ha sido cambiada</h2>"
                + "<p>Tu contraseña en <strong>ESI Entradas</strong> ha sido actualizada correctamente.</p>"
                + "<p><strong>Fecha de cambio:</strong> " + fecha + "</p>"
                + "<div style='background:#f0fdf4;border-left:4px solid #22c55e;padding:12px;margin:16px 0;'>"
                + "<p style='color:#166534;margin:0;'><strong>✓ Cambio realizado exitosamente</strong></p>"
                + "</div>"
                + "<p style='color:#6b7280;font-size:14px;'>Si no realizaste este cambio o sospechas actividad sospechosa en tu cuenta, por favor <strong>contacta con nosotros inmediatamente</strong>.</p>"
                + "<hr style='border:none;border-top:1px solid #e5e7eb;margin:24px 0;'>"
                + "<p style='color:#6b7280;font-size:13px;'>Este es un mensaje automático, por favor no respondas a este correo.</p>"
                + "</div>";
    }

    public void enviarEmailVerificacion(String email, String verificationToken, String frontendUrl) {
        String apiKey = this.configuracionDao.findByClave("MAILGUN_API_KEY");
        String baseUrl = this.configuracionDao.findByClave("MAILGUN_BASE_URL");
        String domain = this.configuracionDao.findByClave("MAILGUN_DOMAIN");
        String fromEmail = this.configuracionDao.findByClave("MAILGUN_FROM_EMAIL");
        String fromName = this.configuracionDao.findByClave("MAILGUN_FROM_NAME");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_API_KEY");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_BASE_URL");
        }
        if (domain == null || domain.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_DOMAIN");
        }
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("Falta configuración: MAILGUN_FROM_EMAIL");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email del usuario es obligatorio para enviar la verificación.");
        }

        String apiUrl = baseUrl.endsWith("/") ? baseUrl + domain + "/messages" : baseUrl + "/" + domain + "/messages";
        String remitenteNombre = (fromName == null || fromName.isBlank()) ? "ESI Entradas" : fromName;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + auth);

        String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;
        String htmlContent = construirHtmlVerificacionEmail(verificationUrl);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("from", remitenteNombre + " <" + fromEmail + ">");
        body.add("to", email);
        body.add("subject", "Verifica tu email - ESI Entradas");
        body.add("html", htmlContent);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("null")
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error enviando email con Mailgun: " + response.getBody());
            }
        } catch (HttpStatusCodeException e) {
            String detalle = e.getResponseBodyAsString();
            throw new RuntimeException("No se pudo enviar el email con Mailgun. HTTP " + e.getStatusCode().value() + ": " + detalle, e);
        } catch (RestClientException e) {
            throw new RuntimeException("No se pudo enviar el email con Mailgun", e);
        }
    }

    private String construirHtmlVerificacionEmail(String verificationUrl) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        return "<div style='font-family:Arial,sans-serif;color:#1f2937;max-width:600px;margin:0 auto;'>"
                + "<h2 style='color:#111827;'>Verifica tu email</h2>"
                + "<p>Gracias por registrarte en <strong>ESI Entradas</strong>.</p>"
                + "<p><strong>Fecha de registro:</strong> " + fecha + "</p>"
                + "<p>Haz clic en el siguiente botón para verificar tu email y activar tu cuenta:</p>"
                + "<div style='text-align:center;margin:24px 0;'>"
                + "<a href='" + escapeHtml(verificationUrl) + "' style='display:inline-block;padding:12px 28px;background:#2563eb;color:#fff;text-decoration:none;border-radius:6px;font-weight:bold;font-size:16px;'>Verificar Email</a>"
                + "</div>"
                + "<p style='color:#6b7280;font-size:14px;'>O copia y pega este enlace en tu navegador:</p>"
                + "<p style='background:#f3f4f6;padding:12px;border-radius:6px;word-break:break-all;color:#334155;font-family:monospace;font-size:12px;'>" + escapeHtml(verificationUrl) + "</p>"
                + "<p style='color:#6b7280;font-size:13px;'>Este enlace es válido durante <strong>24 horas</strong>.</p>"
                + "<hr style='border:none;border-top:1px solid #e5e7eb;margin:24px 0;'>"
                + "<p style='color:#6b7280;font-size:13px;'>Si no te registraste en ESI Entradas, ignora este correo.</p>"
                + "<p style='color:#6b7280;font-size:13px;'>Este es un mensaje automático, por favor no respondas a este correo.</p>"
                + "</div>";
    }
}