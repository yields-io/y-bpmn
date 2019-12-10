package io.yields.bpm.bnp.chiron;

import lombok.Data;


@Data
class IngestParams {

    private String fileName;
    private String dataSetId;
    private String ingestionType = "Ingestion";
}
