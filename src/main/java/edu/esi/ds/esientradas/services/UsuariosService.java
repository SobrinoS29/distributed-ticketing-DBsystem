package edu.esi.ds.esientradas.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UsuariosService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Object[] getUserInfoEmail(String userToken) {
        String url = UriComponentsBuilder
            .fromUriString("http://localhost:8081/external/getUserInfoEmail")
            .queryParam("userToken", userToken)
            .toUriString();

        return restTemplate.getForObject(url, Object[].class);
    }

}
