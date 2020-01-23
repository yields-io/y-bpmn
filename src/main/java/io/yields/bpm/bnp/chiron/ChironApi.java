package io.yields.bpm.bnp.chiron;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.yields.bpm.bnp.chiron.NoSSLCheckRestTemplate.restTemplate;


@UtilityClass
@Slf4j
public class ChironApi {

    //TODO: make it configurable
    private static final String BASE_URL = "https://demo.qa.yields.io/y-api";
    private static final ObjectMapper objectMapper = new ObjectMapper();

        private static final String token = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJiemxxOGJHUkpiUEhYREdHelZFbWZBZDRJRkUwMnVCc1YtazlaaUJOWXVNIn0.eyJqdGkiOiIyYjE0NTljYy02ZTIyLTQ4NDctYjI1Yi04NWNjNzNkNWY1YTUiLCJleHAiOjE1Nzk4MTY1OTQsIm5iZiI6MCwiaWF0IjoxNTc5NzgwNjAwLCJpc3MiOiJodHRwOi8va2V5Y2xvYWs6ODA5MC95LWtleWNsb2FrL3JlYWxtcy95aWVsZHMiLCJhdWQiOlsicmVnaXN0cnkiLCJhY2NvdW50Il0sInN1YiI6ImYyMTU5YjMwLWJiNjEtNDViMy05NTA3LTg0NWRmYTI5MzFjZCIsInR5cCI6IkJlYXJlciIsImF6cCI6InktcG9ydGFsIiwibm9uY2UiOiI5YWY2MGE1Mi0xOWYwLTQzM2QtYTdiZi0yZGUwMzNjNWVmYmYiLCJhdXRoX3RpbWUiOjE1Nzk3ODA1OTQsInNlc3Npb25fc3RhdGUiOiIyNGYyYTdlMy0yNmIxLTRmNjUtODNkNS0yYzM4YmU2Njg3MDMiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIi8iXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJyZWdpc3RyeSI6eyJyb2xlcyI6WyJyZWdpc3RyeS1hZG1pbiJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJZaWVsZHMgVXNlciBZaWVsZHMiLCJtb2RpZnlfdGltZXN0YW1wIjoiMjAyMDAxMjExNjQ4NDdaIiwiZ3JvdXBzIjpbImNhbXVuZGEtYWRtaW4iXSwicHJlZmVycmVkX3VzZXJuYW1lIjoieWllbGRzIiwiZ2l2ZW5fbmFtZSI6IllpZWxkcyBVc2VyIiwiZmFtaWx5X25hbWUiOiJZaWVsZHMifQ.tc7jcSFbEM7FGr3mYb9mVmY2McBmqsj5CxyrghSQIW3E1axMTkiG04xHnGZQY5K2QOz4ao8jVdB-l02nrhnqPY_9C-qVgivR_MZCplfkJswXAVjpg9vBatscYskfqid2_aUMZzA6yazypKwOU6yBAQJqb5NN7jzmtnnfsyjINdFgGAYgnw3UDt-W-4mvc0X5XHb63UwuL4GU1ADVy_9cUwMB-xSoLy0KkZUbJLuKI0agS6719VaCWkk_tQKT7M9r5yj6N5nHVg-dKUNcnA_Lr53P6lMdKc7Wp8Yo0Vk02ht_0iCW9AAoNXdThAg-5KyRYfNb4HEh8CigFWpabxhagA";
    private Map<String, String> tokens = new HashMap<>();
    private String lastUserId;

    public void setUserToken(String userId, String accessToken) {
        lastUserId = userId;
        tokens.put(userId, accessToken);
    }

    private String getToken() {
        return token;
//        Authentication currentAuthentication = ProcessEngines.getDefaultProcessEngine()
//                .getIdentityService()
//                .getCurrentAuthentication();
//
//        String userId = currentAuthentication == null ? lastUserId : currentAuthentication.getUserId();
//
//        return "Bearer " + tokens.get(userId);
    }

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

    public void uploadFile(byte[] fileData, String fileName) {
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
        ResponseEntity<String> strResponse = restTemplate.postForEntity(BASE_URL + String.format("/sessions/?stageId=%s", stageId),
                requestEntity, String.class);
        log.debug("startSession response: {}", strResponse);
//        if (strResponse.getStatusCode() != HttpStatus.OK) {
//            throw new RuntimeException("Error calling startSession endpoint: " + strResponse);
//        }

        try {
            return objectMapper.readValue(strResponse.getBody(), StartSessionResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing start start session response", e);
            throw new RuntimeException("Error staring session: " + strResponse.getBody());
        }
    }

    public SessionDetailsDTO getSessionDetails(String sessionId) {
        RestTemplate restTemplate = restTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headersWithToken());
        ResponseEntity<SessionDetailsDTO> response = null;
        try {
            response = restTemplate.exchange(BASE_URL + String.format("/sessions/%s/detail", sessionId),
                    HttpMethod.GET, entity, SessionDetailsDTO.class);
        } catch (Exception e) {
            log.error("getSessionDetails error", e);
        }
        log.debug("getSessionDetails: {}", response);
        SessionDetailsDTO sessionDetails = response.getBody();
        log.info("Session {} status {}", sessionId, sessionDetails.getStatus());
        return sessionDetails;
    }


    private HttpHeaders headersWithToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", getToken());
        return headers;
    }

    public static String getSessionReport(String sessionId) {
        RestTemplate restTemplate = restTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headersWithToken());
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(BASE_URL + String.format("/sessions/%s/output?outputType=html", sessionId),
                    HttpMethod.GET, entity, String.class);
        } catch (Exception e) {
            log.error("getSessionReport error", e);
        }
        log.debug("getSessionReport: {}", response);
        return response.getBody();
//        return Lists.newArrayList(Splitter.fixedLength(3999).split(response.getBody()));

    }
}
