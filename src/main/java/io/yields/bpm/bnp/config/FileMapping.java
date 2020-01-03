package io.yields.bpm.bnp.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FileMapping {

    private String processVariable;
    private String dataSet;
    private String fileNameTemplate;
    private String ifModel;

}
