package io.yields.bpm.bnp.chiron;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.yields.bpm.bnp.chiron.NoSSLCheckRestTemplate.restTemplate;


@UtilityClass
@Slf4j
public class ChironApi {

    //TODO: make it configurable
    private static final String BASE_URL = "https://bnp.qa.yields.io/y-api";
    private static final String token = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJnT2p6TkJLR3ljOVFOZHJNQ2FhVDBMLWFYRDBLeDYwc3JnMFFnWWhtSkhVIn0.eyJqdGkiOiIyMWFlNjk3MS02ZDE0LTRhMTQtYjhlMC0zNTgyY2I5MWM3ZGYiLCJleHAiOjE1NzgwNjAwOTEsIm5iZiI6MCwiaWF0IjoxNTc4MDQyMDkxLCJpc3MiOiJodHRwOi8va2V5Y2xvYWs6ODA5MC95LWtleWNsb2FrL3JlYWxtcy95aWVsZHMiLCJhdWQiOlsicmVnaXN0cnkiLCJhY2NvdW50Il0sInN1YiI6IjUyOGEyOTg2LTlkMDEtNDczYS05MDkwLTMyOGQxZmY5OTZhYiIsInR5cCI6IkJlYXJlciIsImF6cCI6InktcG9ydGFsIiwibm9uY2UiOiIxNDYxY2I2Ny01OWI3LTQ4ZWItYjFhMi1jNGY3ZTRhMTA1ZjEiLCJhdXRoX3RpbWUiOjE1NzgwNDIwODcsInNlc3Npb25fc3RhdGUiOiJkMjY2Y2VjZS1jMTEyLTQ5MmMtOWUxNC0zN2NjNWY1ZDI2MTUiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIi8iXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJyZWdpc3RyeSI6eyJyb2xlcyI6WyJjZW50cmFsLXRlYW0iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiU2ViYXN0aWVuIFZpZ3VpZSBzdmlndWllIiwibW9kaWZ5X3RpbWVzdGFtcCI6IjIwMTkxMDA0MTU1ODEwWiIsImdyb3VwcyI6WyJjYW11bmRhLWFkbWluIiwiYm5wIl0sInByZWZlcnJlZF91c2VybmFtZSI6InN2aWd1aWUiLCJnaXZlbl9uYW1lIjoiU2ViYXN0aWVuIFZpZ3VpZSIsImZhbWlseV9uYW1lIjoic3ZpZ3VpZSJ9.SUS_BYhB7ef-F95ogg2FjFKX7LEhpFX9KGuOkhMnEXnzC_E0tpwO1j0er0PoHyLNQrpbVZ0uo8XrN3jLZ4jkFbtN5MlGKXm2BXgFJOu3wJjzfPaM9lxUyCCPNzXWDkqN8kV7Lq8TlCIG9F4VSiioctX0N-4FuNY0jk741S9YnQrgXDwswtmWmPk4bck17qMj-Ndokqs0mEYdu_uniJfZjWgnC7kHJ-PW7SHbMUG0xbzVDp9DP2gRzjSlkb-ap-2UoFu73wmi1Nsp0PdwOmwe5VLy7zEzm70ChCSPsVAVrpm5F4A7Gm_NaxHpT_UG9_W2E6DjC82tajG3FNx80NkeFg";


    public List<ModelDTO> getModels() {
        RestTemplate restTemplate = restTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headersWithToken());
        ResponseEntity<ModelDTO[]> response;
        try {
            response = restTemplate.exchange(BASE_URL + "/models", HttpMethod.GET, entity, ModelDTO[].class);
        } catch (Exception e) {
            log.error("Get models error", e);
            throw new RuntimeException(e);
        }
        ArrayList<ModelDTO> result = Lists.newArrayList(response.getBody());
        log.debug("Got models: {}", result);
        return result;
    }

    public void uploadRequiredData(byte[] fileData, String fileName) {
        HttpHeaders headers = headersWithToken();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        ByteArrayResource contentsAsResource = new ByteArrayResource(fileData) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        body.add("file", contentsAsResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = restTemplate();
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.postForEntity(BASE_URL + "/upload", requestEntity, String.class);
        } catch (Exception e) {
            log.error("Upload error", e);
        }
        log.debug("Upload response: {}", response);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Error uploading file: " + fileName + ", " + response);
        }
    }

    public void ingest(Map.Entry<String, String> ingestFileAndId) {
        IngestParams params = new IngestParams();
        params.setFileName(ingestFileAndId.getKey());
        params.setDataSetId(ingestFileAndId.getValue());
        HttpEntity requestEntity = new HttpEntity<>(params, headersWithToken());

        RestTemplate restTemplate = restTemplate();
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.postForEntity(BASE_URL + "/ingest", requestEntity, String.class);
        } catch (Exception e) {
            log.error("Ingest error", e);
        }
        log.debug("ingest response: {}", response);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Error calling ingest endpoint: " + response);
        }
    }

    public List<DatasetDTO> getDatasets() {
        RestTemplate restTemplate = restTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headersWithToken());
        ResponseEntity<DatasetDTO[]> response = null;
        try {
            response = restTemplate.exchange(BASE_URL + "/data_sets", HttpMethod.GET, entity, DatasetDTO[].class);
        } catch (Exception e) {
            log.error("getDatasets error", e);
        }

        List<DatasetDTO> result = Lists.newArrayList(response.getBody());
        log.debug("datasets: {}", response);
        return result;
    }


    public String getIngestionStatus(String fileName, String datasetId) {
        RestTemplate restTemplate = restTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headersWithToken());
        ResponseEntity<IngestionDTO[]> response = null;
        try {
            response = restTemplate
                    //data_sets/0951874c-e133-4da7-86ed-ad1938454053/ingestions
                    .exchange(BASE_URL + String.format("/data_sets/%s/ingestions", datasetId),
                            HttpMethod.GET, entity, IngestionDTO[].class);
        } catch (Exception e) {
            log.error("get dataset ingestions error", e);
        }
        log.debug("get dataset ingestions response: {}", response);

        if (response.getStatusCode() == HttpStatus.OK) {
            List<IngestionDTO> ingestions = Lists.newArrayList(response.getBody());
            String result = ingestions.stream()
                    .filter(ingestionDTO -> ingestionDTO.getFileName().equals(fileName))
                    .map(ingestionDTO -> ingestionDTO.getStatus())
                    .findFirst()
                    .orElse("");  // may not be available immediately
            log.info("Ingestion status for fileName: {}, {}", fileName, result);
            return result;
        }

        throw new RuntimeException("Error getting ingestions, " + response);
    }

    public StageDTO getStage(String stageType, String name) {
        RestTemplate restTemplate = restTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headersWithToken());
        ResponseEntity<StageDTO[]> response = null;
        try {
            response = restTemplate.exchange(BASE_URL + String.format("/stages?stageType=%s&name=%s", stageType, name),
                    HttpMethod.GET, entity, StageDTO[].class);
        } catch (Exception e) {
            log.error("getStage error", e);
        }
        log.debug("getStage: {}", response);
        List<StageDTO> stages = Lists.newArrayList(response.getBody());
        if (stages.size() != 1) {
            throw new RuntimeException("Could not get unique stage for: " + stageType + ", " + name);
        }
        return stages.get(0);
    }

    public StartSessionResponse startSession(String stageId) {
        HttpEntity<String> requestEntity = new HttpEntity<>(headersWithToken());
        RestTemplate restTemplate = restTemplate();
        ResponseEntity<StartSessionResponse> response = null;
        try {
            response = restTemplate.postForEntity(BASE_URL + String.format("/sessions/?stageId=%s", stageId),
                    requestEntity, StartSessionResponse.class);
        } catch (Exception e) {
            log.error("startSession error", e);
        }
        log.debug("startSession response: {}", response);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Error calling startSession endpoint: " + response);
        }

        return response.getBody();
    }

    public SessionDetailsDTO getSessionDetails(String sessionId) {
        RestTemplate restTemplate = restTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headersWithToken());
        ResponseEntity<SessionDetailsDTO> response = null;
        try {
            response = restTemplate.exchange(BASE_URL + String.format("/sessions/%s/detail", sessionId),
                    HttpMethod.GET, entity, SessionDetailsDTO.class);
        } catch (Exception e) {
            log.error("getStage error", e);
        }
        log.debug("getStage: {}", response);
        SessionDetailsDTO sessionDetails = response.getBody();
        log.info("Session {} status {}", sessionId, sessionDetails.getStatus());
        return sessionDetails;
    }


    private HttpHeaders headersWithToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        return headers;
    }
}
