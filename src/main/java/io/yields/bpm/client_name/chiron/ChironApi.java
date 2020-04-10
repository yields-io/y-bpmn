package io.yields.bpm.client_name.chiron;

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

import static io.yields.bpm.client_name.chiron.NoSSLCheckRestTemplate.restTemplate;


@UtilityClass
@Slf4j
public class ChironApi {

    //TODO: make it configurable
    private static final String BASE_URL = "https://client_name.uat.yields.io/y-api";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String token = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJnT2p6TkJLR3ljOVFOZHJNQ2FhVDBMLWFYRDBLeDYwc3JnMFFnWWhtSkhVIn0.eyJqdGkiOiI3YTc0NDkwMS01NTg0LTQ5YjMtYjI3ZS1kZThkMWZhYTg2YTEiLCJleHAiOjE1ODE2ODM2MzksIm5iZiI6MCwiaWF0IjoxNTc5MDkxNjM5LCJpc3MiOiJodHRwczovL2JucC51YXQueWllbGRzLmlvL3kta2V5Y2xvYWsvcmVhbG1zL3lpZWxkcyIsImF1ZCI6WyJyZWdpc3RyeSIsImFjY291bnQiXSwic3ViIjoiNTI4YTI5ODYtOWQwMS00NzNhLTkwOTAtMzI4ZDFmZjk5NmFiIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiY2FtdW5kYS1pZGVudGl0eS1zZXJ2aWNlIiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiMTM4YWU0YTYtNDNkZi00YWI4LTlhMWEtZWEyNjY1ZGEyYTliIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsicmVnaXN0cnkiOnsicm9sZXMiOlsiY2VudHJhbC10ZWFtIl19LCJjYW11bmRhLWlkZW50aXR5LXNlcnZpY2UiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6ImVtYWlsIHByb2ZpbGUiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJTZWJhc3RpZW4gVmlndWllIHN2aWd1aWUiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJzdmlndWllIiwiZ2l2ZW5fbmFtZSI6IlNlYmFzdGllbiBWaWd1aWUiLCJmYW1pbHlfbmFtZSI6InN2aWd1aWUifQ.iWT-3GkrujPV7vdMKvfkSn-QCDfQbmss2jnOcDnvSDDd2Tdf_BfODaH_EizCayZHbaZvL0F4kSykLqLn4MoFfaoUkuKKBoJJcX1UHg1me64Dx7_PY9PreS3lPqZD6Y06JG17_W-RmHW6lkZZn4cftAbvPnQopD1l6l65ohzmE4WHtdFefzbe5JGVu0BfxpKNgw6KzoOZAMtPe1TdoGSOmbqMNNCNGgNNd6tQzgLcO79Rw3fsvEjzX3exSAtoJ-37_pdMU32Y-ygRRZsEKCx5a4KyvKHeED3PQ1K9Hvwcgv8-stex3pJPDbAl2ZL41sOe_gEluN64IBG1j3nsgwHH2g";
    private Map<String, String> tokens = new HashMap<>();
    private String lastUserId;

    public void setUserToken(String userId, String accessToken) {
        lastUserId = userId;
        tokens.put(userId, accessToken);
    }

    private String getToken() {
//        return token;
        Authentication currentAuthentication = ProcessEngines.getDefaultProcessEngine()
                .getIdentityService()
                .getCurrentAuthentication();

        String userId = currentAuthentication == null ? lastUserId : currentAuthentication.getUserId();

        return "Bearer " + tokens.get(userId);
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
