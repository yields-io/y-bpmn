package io.yields.bpm.bnp.chiron;

import java.util.List;
import lombok.Builder;
import lombok.Data;

import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;

@Data
@Builder
public class AuthDTO {

    private Resource resource;
    private String resourceId;
    private List<Permission> permissions;

}
