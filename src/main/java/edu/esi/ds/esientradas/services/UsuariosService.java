package edu.esi.ds.esientradas.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UsuariosService {  // Servicio para interactuar con el microservicio de usuarios (ExternalController)

    private final RestTemplate restTemplate = new RestTemplate();

    public Object[] getUserInfoEmail(String sessionToken) {
        String url = UriComponentsBuilder
            .fromUriString("http://localhost:8081/external/getUserInfoEmail")
            .queryParam("sessionToken", sessionToken)
            .toUriString();

        return restTemplate.getForObject(url, Object[].class);
    }

    public String checkUserToken(String sessionToken) {
        String url = UriComponentsBuilder
            .fromUriString("http://localhost:8081/external/checkUserToken")
            .queryParam("sessionToken", sessionToken)
            .toUriString();

        return restTemplate.getForObject(url, String.class);
    }

}
