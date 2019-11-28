package com.yields.yieldsbpmn.models;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@RequiredArgsConstructor
public class ModelsFetcher {

    private final String chironApiUrl;

    public List<ModelDTO> fetchModels() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ModelDTO[]> response
                = restTemplate.getForEntity(chironApiUrl + "/models", ModelDTO[].class);
        return List.of(response.getBody());
    }

}
