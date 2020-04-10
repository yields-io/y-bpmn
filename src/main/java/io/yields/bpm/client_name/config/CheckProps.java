package io.yields.bpm.client_name.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public
class CheckProps {

    private String stageType;
    private String dataSet;
    private String ifModel;
}
