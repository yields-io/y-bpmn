package io.yields.bpm.bnp.chiron;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // access_token: "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJnT2p6TkJLR3ljOVFOZHJNQ2FhVDBMLWFYRDBLeDYwc3JnMFFnWWhtSkhVIn0.eyJqdGkiOiI5M2Y4ZmU0Yi01Y2RlLTRlNzctOTZiMy0yMDFiNzQ3ZmVlNzAiLCJleHAiOjE1Nzg3NTg5NzgsIm5iZiI6MCwiaWF0IjoxNTc4NzQwOTc4LCJpc3MiOiJodHRwczovL2JucC5xYS55aWVsZHMuaW8veS1rZXljbG9hay9yZWFsbXMveWllbGRzIiwiYXVkIjpbInJlZ2lzdHJ5IiwiY2FtdW5kYS1pZGVudGl0eS1zZXJ2aWNlIiwiYWNjb3VudCJdLCJzdWIiOiI1MjhhMjk4Ni05ZDAxLTQ3M2EtOTA5MC0zMjhkMWZmOTk2YWIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJ5LXBvcnRhbCIsIm5vbmNlIjoiZjkzN2ZjNDctMDAyOC00MTgzLTg5MzYtODQ5Yjk3YjZlMDE0IiwiYXV0aF90aW1lIjoxNTc4NzQwOTc1LCJzZXNzaW9uX3N0YXRlIjoiMjllZWJmMmItMzc1MC00ZTBiLWE4NGEtOTI4ODBlY2I0NjY5IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIvIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsicmVnaXN0cnkiOnsicm9sZXMiOlsiY2VudHJhbC10ZWFtIl19LCJjYW11bmRhLWlkZW50aXR5LXNlcnZpY2UiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiU2ViYXN0aWVuIFZpZ3VpZSBzdmlndWllIiwibW9kaWZ5X3RpbWVzdGFtcCI6IjIwMTkxMDA0MTU1ODEwWiIsImdyb3VwcyI6WyJjYW11bmRhLWFkbWluIiwiYm5wIl0sInByZWZlcnJlZF91c2VybmFtZSI6InN2aWd1aWUiLCJnaXZlbl9uYW1lIjoiU2ViYXN0aWVuIFZpZ3VpZSIsImZhbWlseV9uYW1lIjoic3ZpZ3VpZSJ9.jZUSqvPCqtdPQfl3StzhcPM0-Vqj3DZy6W6A4RAMTqHmYPp3jXq8yCPku1fm5euExXYBgN35ZS5br38Py-DSPVqGcTF90MFtXG8iOY5hG03mmMfZNpTm8twsi__eExUquyKGWDnQGBSyD2lKPJuFEe4dpv6aVcXggGQhrG0vbLvWEKUBGGagq5gOhjoOMxRqRaaNoq479IXI8b8kvbVeKNL4sgHCOTFz2NV2xuEWDZZPE4vEPKqEMwLikdX5uKDNJhmXK_vRUF9KDqaufweUZ-h5QworiNsTYwMOU7Oz0ifjiH4OYdlzq_ssRds4k6TtIw2cWCYt4MJ8GgQKQfW-WA"
    private static final String token = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJnT2p6TkJLR3ljOVFOZHJNQ2FhVDBMLWFYRDBLeDYwc3JnMFFnWWhtSkhVIn0.eyJqdGkiOiI5M2Y4ZmU0Yi01Y2RlLTRlNzctOTZiMy0yMDFiNzQ3ZmVlNzAiLCJleHAiOjE1Nzg3NTg5NzgsIm5iZiI6MCwiaWF0IjoxNTc4NzQwOTc4LCJpc3MiOiJodHRwczovL2JucC5xYS55aWVsZHMuaW8veS1rZXljbG9hay9yZWFsbXMveWllbGRzIiwiYXVkIjpbInJlZ2lzdHJ5IiwiY2FtdW5kYS1pZGVudGl0eS1zZXJ2aWNlIiwiYWNjb3VudCJdLCJzdWIiOiI1MjhhMjk4Ni05ZDAxLTQ3M2EtOTA5MC0zMjhkMWZmOTk2YWIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJ5LXBvcnRhbCIsIm5vbmNlIjoiZjkzN2ZjNDctMDAyOC00MTgzLTg5MzYtODQ5Yjk3YjZlMDE0IiwiYXV0aF90aW1lIjoxNTc4NzQwOTc1LCJzZXNzaW9uX3N0YXRlIjoiMjllZWJmMmItMzc1MC00ZTBiLWE4NGEtOTI4ODBlY2I0NjY5IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIvIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsicmVnaXN0cnkiOnsicm9sZXMiOlsiY2VudHJhbC10ZWFtIl19LCJjYW11bmRhLWlkZW50aXR5LXNlcnZpY2UiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiU2ViYXN0aWVuIFZpZ3VpZSBzdmlndWllIiwibW9kaWZ5X3RpbWVzdGFtcCI6IjIwMTkxMDA0MTU1ODEwWiIsImdyb3VwcyI6WyJjYW11bmRhLWFkbWluIiwiYm5wIl0sInByZWZlcnJlZF91c2VybmFtZSI6InN2aWd1aWUiLCJnaXZlbl9uYW1lIjoiU2ViYXN0aWVuIFZpZ3VpZSIsImZhbWlseV9uYW1lIjoic3ZpZ3VpZSJ9.jZUSqvPCqtdPQfl3StzhcPM0-Vqj3DZy6W6A4RAMTqHmYPp3jXq8yCPku1fm5euExXYBgN35ZS5br38Py-DSPVqGcTF90MFtXG8iOY5hG03mmMfZNpTm8twsi__eExUquyKGWDnQGBSyD2lKPJuFEe4dpv6aVcXggGQhrG0vbLvWEKUBGGagq5gOhjoOMxRqRaaNoq479IXI8b8kvbVeKNL4sgHCOTFz2NV2xuEWDZZPE4vEPKqEMwLikdX5uKDNJhmXK_vRUF9KDqaufweUZ-h5QworiNsTYwMOU7Oz0ifjiH4OYdlzq_ssRds4k6TtIw2cWCYt4MJ8GgQKQfW-WA";


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
        headers.set("Authorization", token);
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
