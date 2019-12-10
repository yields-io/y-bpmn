package io.yields.bpm.bnp.chiron;

import com.fasterxml.jackson.core.type.TypeReference;
import http.rest.RestClient;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@UtilityClass
public class ChironApi {

    //TODO: make it configurable
    private static final String BASE_URL = "https://acaisoft.qa.yields.io/y-api";

    public List<ModelDTO> getModels() {
        RestClient client = RestClient.builder().build();
        try {
            List<ModelDTO> models = client.get(BASE_URL + "/models", new HashMap<>(), new TypeReference<List<ModelDTO>>() {
            });
            return models;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching models");
        }
    }

    public String uploadRequiredData(ByteArrayInputStream fileData, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        try {
            ByteArrayResource contentsAsResource = new ByteArrayResource(IOUtils.toByteArray(fileData)) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
            body.add("file", contentsAsResource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate
                .postForEntity(BASE_URL + "/upload", requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return StringUtils.substringBetween(response.getBody(), "IOResult(", ",Success");
        }

        throw new RuntimeException("Error uploading file: " + fileName + ", " + response);
    }

    public void ingest(Map.Entry<String, String> ingestFileAndId) {
        IngestParams params = new IngestParams();
        params.setFileName(ingestFileAndId.getKey());
        params.setDataSetId(ingestFileAndId.getValue());

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate
                .postForEntity(BASE_URL + "/ingest", params, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Error calling ingest endpoint: " + response);
        }
    }

    public String getDatasetStatus(String datasetId) {

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<DatasetDTO> response = restTemplate
                .getForEntity(BASE_URL + "/data_sets/" + datasetId, DatasetDTO.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody().getStatus();
        }

        throw new RuntimeException("Error getting dataset: " + datasetId + ", " + response);
    }
}
