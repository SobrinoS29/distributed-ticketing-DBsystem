package edu.esi.ds.esientradas.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UsuariosService {  // Servicio para interactuar con el microservicio de usuarios (ExternalController)

    private final RestTemplate restTemplate = new RestTemplate();

    public Object[] getUserInfoEmail(String userToken) {
        String url = UriComponentsBuilder
            .fromUriString("http://localhost:8081/external/getUserInfoEmail")
            .queryParam("userToken", userToken)
            .toUriString();

        Object[] response = restTemplate.getForObject(url, Object[].class);
        return (response != null && response[0] instanceof java.util.List<?> list) ? list.toArray(): null;
    }

    public String checkUserToken(String userToken) {
        String url = UriComponentsBuilder
            .fromUriString("http://localhost:8081/external/checkUserToken")
            .queryParam("userToken", userToken)
            .toUriString();

        return restTemplate.getForObject(url, String.class);
    }

}
