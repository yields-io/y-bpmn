package io.yields.bpm.client_name;

public interface ProcessVariables {

    // ingest ids map
    String DATASET_IDS = "DATASET_IDS";

    String processError = "processError"; // error message, String

    String uploadSuccess = "uploadSuccess"; // true/false

    String dataCheckSuccess = "dataCheckSuccessful"; // true/false

    String dataCheckReport = "dataCheckReport";

    String performanceCheckSuccess = "performanceCheckSuccessful";
    String performanceCheckReport = "performanceCheckReport";
}
