package io.yields.bpm.bnp.util;

import io.yields.bpm.bnp.chiron.DatasetDTO;
import lombok.experimental.UtilityClass;

import java.util.List;


@UtilityClass
public class DataSetUtil {

    public String findDatasetId(List<DatasetDTO> datasets, String datasetName) {
        return datasets.stream()
                .filter(dataset -> dataset.getName().equals(datasetName))
                .map(dataset -> dataset.getId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find dataset: " + datasetName));
    }

}
