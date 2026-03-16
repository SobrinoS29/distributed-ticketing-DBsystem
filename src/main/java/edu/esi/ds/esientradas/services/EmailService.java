// Como solo nos comunicaremos desde el backend con el servicio de Brevo, solo necesitaríamos la clave privada

package edu.esi.ds.esientradas.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import edu.esi.ds.esientradas.dao.ConfiguracionDao;

@Service
public class EmailService {

    @Autowired
    private ConfiguracionDao configuracionDao;  // Coger la API Key de la BD para comunicarnos con el servicio de Brevo


    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


    public void enviarEmailCompra(Object[] userInfoEmail, String entradasJson) {
        String apiKey = this.configuracionDao.findByClave("BREVO_SECRET_KEY");
        String apiUrl = this.configuracionDao.findByClave("BREVO_API_URL");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Falta configuración: BREVO_SECRET_KEY");
        }
        if (apiUrl == null || apiUrl.isBlank()) {
            throw new IllegalStateException("Falta configuración: BREVO_API_URL");
        }

        String userName = (userInfoEmail != null && userInfoEmail.length > 0 && userInfoEmail[0] != null)
                ? String.valueOf(userInfoEmail[0]).trim()
                : "Cliente";

        String userEmail = (userInfoEmail != null && userInfoEmail.length > 1 && userInfoEmail[1] != null)
                ? String.valueOf(userInfoEmail[1]).trim()
                : "";

        if (userEmail.isBlank()) {
            throw new IllegalArgumentException("El email del usuario es obligatorio para enviar la confirmación.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        String htmlContent = construirHtmlCompra(userName, entradasJson);

        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("name", "ESI Entradas");
        sender.put("email", "esiEntradas@localhost.com");

        Map<String, String> to = new HashMap<>();
        to.put("email", userEmail);
        to.put("name", userName);

        body.put("sender", sender);
        body.put("to", List.of(to));
        body.put("subject", "Confirmación de compra - ESI Entradas");
        body.put("htmlContent", htmlContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("null")
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error sending email: " + response.getBody());
            }
        } catch (RestClientException e) {
            throw new RuntimeException("No se pudo enviar el email a Brevo", e);
        }
    }

    private String construirHtmlCompra(String userName, String entradasJson) {
        StringBuilder filas = new StringBuilder();
        int totalCentimos = 0;
        int totalEntradas = 0;

        try {
            JsonNode root = objectMapper.readTree(
                    entradasJson == null || entradasJson.isBlank() ? "[]" : entradasJson
            );

            JsonNode array = root.isArray() ? root : objectMapper.createArrayNode().add(root);

            for (JsonNode entrada : array) {
                int entradaId = entrada.path("entradaId").asInt(0);
                int zona = entrada.path("zona").asInt(0);
                int precioCentimos = entrada.path("precioCentimos").asInt(0);

                totalCentimos += precioCentimos;
                totalEntradas++;

                filas.append("<tr>")
                        .append("<td style='padding:8px;border:1px solid #ddd;'>").append(entradaId).append("</td>")
                        .append("<td style='padding:8px;border:1px solid #ddd;'>").append(escapeHtml(getZonaNombrePorId(zona))).append("</td>")
                        .append("<td style='padding:8px;border:1px solid #ddd;text-align:right;'>")
                        .append(formatearEuros(precioCentimos))
                        .append("</td>")
                        .append("</tr>");
            }
        } catch (Exception e) {
            String escapedJson = escapeHtml(entradasJson == null || entradasJson.isBlank() ? "[]" : entradasJson);
            filas.append("<tr><td colspan='3' style='padding:8px;border:1px solid #ddd;'><pre style='margin:0;'>")
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
                + "</tr>"
                + "</thead>"
                + "<tbody>" + filas + "</tbody>"
                + "</table>"
                + "<p style='margin-top:14px;font-size:16px;'><strong>Total pagado: " + totalEuros + "</strong></p>"
                + "<p style='margin-top:24px;color:#6b7280;'>Este es un mensaje automático, por favor no respondas a este correo.</p>"
                + "</div>";
    }

    private String formatearEuros(int centimos) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.of("es", "ES"));
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

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}