/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.yields.bpm.bnp.app;

import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;

import io.yields.bpm.bnp.chiron.AuthDTO;
import io.yields.bpm.bnp.config.YieldsProperties;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.AuthorizationService;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;




@SpringBootApplication
@EnableProcessApplication
@EnableConfigurationProperties(YieldsProperties.class)
@ComponentScan("io.yields.bpm.bnp")
@Slf4j
public class WebappExampleProcessApplication {


  @Autowired
  private AuthorizationService authorizationService;

  public static void main(String... args) {
    SpringApplication.run(WebappExampleProcessApplication.class, args);
  }

  // DON'T START THE PROCESS. Process start input parameters are needed
  @EventListener
  private void processPostDeploy(PostDeployEvent event) {
    //runtimeService.startProcessInstanceByKey("bnp-campaign");
    addTeamAuthorizations();
  }

  private void addTeamAuthorizations() {
    String[] teams = new String[]{"France", "Belgium"};
    AuthDTO[] authorizations = new AuthDTO[]{
        AuthDTO.builder().resource(Resources.APPLICATION).resourceId("tasklist")
            .permissions(Arrays.asList(Permissions.ALL)).build(),
        AuthDTO.builder().resource(Resources.APPLICATION).resourceId("cockpit")
            .permissions(Arrays.asList(Permissions.ALL)).build(),
        AuthDTO.builder().resource(Resources.FILTER).resourceId("*")
            .permissions(Arrays.asList(Permissions.ALL)).build(),
    };
    for (String team : teams) {
      for (AuthDTO authorization : authorizations) {
        addAuthorization(team, authorization);
      }
    }
  }

  private void addAuthorization(String team, AuthDTO authorization) {
    Authorization auth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);

    auth.setGroupId(team);

    auth.setResource(authorization.getResource());
    auth.setResourceId(authorization.getResourceId());

    for (Permission permission : authorization.getPermissions()) {
      auth.addPermission(permission);
    }

    authorizationService.saveAuthorization(auth);
  }

}
