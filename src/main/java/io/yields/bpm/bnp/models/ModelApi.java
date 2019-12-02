package io.yields.bpm.bnp.models;

import com.fasterxml.jackson.core.type.TypeReference;
import http.rest.RestClient;

import java.util.HashMap;
import java.util.List;


public class ModelApi {

    //TODO: make it configurable
    private static final String URL = "https://acaisoft.qa.yields.io/y-api/models";

    public List<ModelDTO> getModels() {
        RestClient client = RestClient.builder().build();
        try {
            List<ModelDTO> models = client.get(URL, new HashMap<>(), new TypeReference<List<ModelDTO>>() {});
            return models;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching models");
        }
    }

}
