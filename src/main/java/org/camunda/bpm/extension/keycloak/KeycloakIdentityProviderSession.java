//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.camunda.bpm.extension.keycloak;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import io.yields.bpm.client_name.chiron.ChironApi;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.NativeUserQuery;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.GroupQueryProperty;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.UserQueryImpl;
import org.camunda.bpm.engine.impl.UserQueryProperty;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.identity.IdentityProviderException;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.camunda.bpm.extension.keycloak.json.JSONArray;
import org.camunda.bpm.extension.keycloak.json.JSONException;
import org.camunda.bpm.extension.keycloak.json.JSONObject;
import org.camunda.bpm.extension.keycloak.util.KeycloakPluginLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class KeycloakIdentityProviderSession implements ReadOnlyIdentityProvider {
    protected KeycloakConfiguration keycloakConfiguration;
    protected RestTemplate restTemplate;
    protected KeycloakContextProvider keycloakContextProvider;

    public KeycloakIdentityProviderSession(KeycloakConfiguration keycloakConfiguration, RestTemplate restTemplate, KeycloakContextProvider keycloakContextProvider) {
        this.keycloakConfiguration = keycloakConfiguration;
        this.restTemplate = restTemplate;
        this.keycloakContextProvider = keycloakContextProvider;
    }

    public void flush() {
    }

    public void close() {
    }

    public User findUserById(String userId) {
        return (User) this.createUserQuery(Context.getCommandContext()).userId(userId).singleResult();
    }

    public UserQuery createUserQuery() {
        return new KeycloakUserQuery(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
    }

    public UserQueryImpl createUserQuery(CommandContext commandContext) {
        return new KeycloakUserQuery();
    }

    public NativeUserQuery createNativeUserQuery() {
        throw new BadUserRequestException("Native user queries are not supported for Keycloak identity service provider.");
    }

    protected long findUserCountByQueryCriteria(KeycloakUserQuery userQuery) {
        return (long) this.findUserByQueryCriteria(userQuery).size();
    }

    protected List<User> findUserByQueryCriteria(KeycloakUserQuery userQuery) {
        return !StringUtils.isEmpty(userQuery.getGroupId()) ? this.requestUsersByGroupId(userQuery) : this.requestUsersWithoutGroupId(userQuery);
    }

    protected List<User> requestUsersByGroupId(KeycloakUserQuery query) {
        String groupId = query.getGroupId();
        List<User> userList = new ArrayList();
        StringBuilder resultLogger = new StringBuilder();
        if (KeycloakPluginLogger.INSTANCE.isDebugEnabled()) {
            resultLogger.append("Keycloak user query results: [");
        }

        try {
            String keyCloakID;
            try {
                keyCloakID = this.getKeycloakGroupID(groupId);
            } catch (KeycloakGroupNotFoundException var11) {
                return (List) userList;
            }

            ResponseEntity<String> response = this.restTemplate.exchange(this.keycloakConfiguration.getKeycloakAdminUrl() + "/groups/" + keyCloakID + "/members", HttpMethod.GET, this.keycloakContextProvider.createApiRequestEntity(), String.class, new Object[0]);
            if (!response.getStatusCode().equals(HttpStatus.OK)) {
                throw new IdentityProviderException("Unable to read group members from " + this.keycloakConfiguration.getKeycloakAdminUrl() + ": HTTP status code " + response.getStatusCodeValue());
            }

            JSONArray searchResult = new JSONArray((String) response.getBody());

            for (int i = 0; i < searchResult.length(); ++i) {
                JSONObject keycloakUser = searchResult.getJSONObject(i);
                if ((!this.keycloakConfiguration.isUseEmailAsCamundaUserId() || !StringUtils.isEmpty(this.getStringValue(keycloakUser, "email"))) && (!this.keycloakConfiguration.isUseUsernameAsCamundaUserId() || !StringUtils.isEmpty(this.getStringValue(keycloakUser, "username")))) {
                    UserEntity user = this.transformUser(keycloakUser);
                    if (this.matches((Object) query.getId(), user.getId()) && this.matches((Object[]) query.getIds(), user.getId()) && this.matches((Object) query.getEmail(), user.getEmail()) && this.matchesLike(query.getEmailLike(), user.getEmail()) && this.matches((Object) query.getFirstName(), user.getFirstName()) && this.matchesLike(query.getFirstNameLike(), user.getFirstName()) && this.matches((Object) query.getLastName(), user.getLastName()) && this.matchesLike(query.getLastNameLike(), user.getLastName()) && (this.isAuthenticatedUser(user) || this.isAuthorized(Permissions.READ, Resources.USER, user.getId()))) {
                        ((List) userList).add(user);
                        if (KeycloakPluginLogger.INSTANCE.isDebugEnabled()) {
                            resultLogger.append(user);
                            resultLogger.append(" based on ");
                            resultLogger.append(keycloakUser.toString());
                            resultLogger.append(", ");
                        }
                    }
                }
            }
        } catch (HttpClientErrorException var12) {
            if (var12.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return (List) userList;
            }

            throw var12;
        } catch (RestClientException var13) {
            throw new IdentityProviderException("Unable to query members of group " + groupId, var13);
        } catch (JSONException var14) {
            throw new IdentityProviderException("Unable to query members of group " + groupId, var14);
        }

        if (KeycloakPluginLogger.INSTANCE.isDebugEnabled()) {
            resultLogger.append("]");
            KeycloakPluginLogger.INSTANCE.userQueryResult(resultLogger.toString());
        }

        if (query.getOrderingProperties().size() > 0) {
            ((List) userList).sort(new KeycloakIdentityProviderSession.UserComparator(query.getOrderingProperties()));
        }

        if (query.getFirstResult() > 0 || query.getMaxResults() < 2147483647) {
            userList = ((List) userList).subList(query.getFirstResult(), Math.min(((List) userList).size(), query.getFirstResult() + query.getMaxResults()));
        }

        return (List) userList;
    }

    protected List<User> requestUsersWithoutGroupId(KeycloakUserQuery query) {
        List<User> userList = new ArrayList();
        StringBuilder resultLogger = new StringBuilder();
        if (KeycloakPluginLogger.INSTANCE.isDebugEnabled()) {
            resultLogger.append("Keycloak user query results: [");
        }

        try {
            ResponseEntity<String> response = null;
            if (!StringUtils.isEmpty(query.getId())) {
                response = this.requestUserById(query.getId());
            } else {
                String userFilter = this.createUserSearchFilter(query);
                response = this.restTemplate.exchange(this.keycloakConfiguration.getKeycloakAdminUrl() + "/users" + userFilter, HttpMethod.GET, this.keycloakContextProvider.createApiRequestEntity(), String.class, new Object[0]);
            }

            if (!response.getStatusCode().equals(HttpStatus.OK)) {
                throw new IdentityProviderException("Unable to read users from " + this.keycloakConfiguration.getKeycloakAdminUrl() + ": HTTP status code " + response.getStatusCodeValue());
            }

            JSONArray searchResult = new JSONArray((String) response.getBody());

            for (int i = 0; i < searchResult.length(); ++i) {
                JSONObject keycloakUser = searchResult.getJSONObject(i);
                if ((!this.keycloakConfiguration.isUseEmailAsCamundaUserId() || !StringUtils.isEmpty(this.getStringValue(keycloakUser, "email"))) && (!this.keycloakConfiguration.isUseUsernameAsCamundaUserId() || !StringUtils.isEmpty(this.getStringValue(keycloakUser, "username")))) {
                    UserEntity user = this.transformUser(keycloakUser);
                    if (this.matches((Object) query.getId(), user.getId()) && this.matches((Object) query.getEmail(), user.getEmail()) && this.matches((Object) query.getFirstName(), user.getFirstName()) && this.matches((Object) query.getLastName(), user.getLastName()) && this.matches((Object[]) query.getIds(), user.getId()) && this.matchesLike(query.getEmailLike(), user.getEmail()) && this.matchesLike(query.getFirstNameLike(), user.getFirstName()) && this.matchesLike(query.getLastNameLike(), user.getLastName()) && (this.isAuthenticatedUser(user) || this.isAuthorized(Permissions.READ, Resources.USER, user.getId()))) {
                        ((List) userList).add(user);
                        if (KeycloakPluginLogger.INSTANCE.isDebugEnabled()) {
                            resultLogger.append(user);
                            resultLogger.append(" based on ");
                            resultLogger.append(keycloakUser.toString());
                            resultLogger.append(", ");
                        }
                    }
                }
            }
        } catch (RestClientException var9) {
            throw new IdentityProviderException("Unable to query users", var9);
        } catch (JSONException var10) {
            throw new IdentityProviderException("Unable to query users", var10);
        }

        if (KeycloakPluginLogger.INSTANCE.isDebugEnabled()) {
            resultLogger.append("]");
            KeycloakPluginLogger.INSTANCE.userQueryResult(resultLogger.toString());
        }

        if (query.getOrderingProperties().size() > 0) {
            ((List) userList).sort(new KeycloakIdentityProviderSession.UserComparator(query.getOrderingProperties()));
        }

        if (query.getFirstResult() > 0 || query.getMaxResults() < 2147483647) {
            userList = ((List) userList).subList(query.getFirstResult(), Math.min(((List) userList).size(), query.getFirstResult() + query.getMaxResults()));
        }

        return (List) userList;
    }

    protected String createUserSearchFilter(KeycloakUserQuery query) {
        StringBuilder filter = new StringBuilder();
        if (!StringUtils.isEmpty(query.getEmail())) {
            this.addArgument(filter, "email", query.getEmail());
        }

        if (!StringUtils.isEmpty(query.getEmailLike())) {
            this.addArgument(filter, "email", query.getEmailLike().replaceAll("[%,\\*]", ""));
        }

        if (!StringUtils.isEmpty(query.getFirstName())) {
            this.addArgument(filter, "firstName", query.getFirstName());
        }

        if (!StringUtils.isEmpty(query.getFirstNameLike())) {
            this.addArgument(filter, "firstName", query.getFirstNameLike().replaceAll("[%,\\*]", ""));
        }

        if (!StringUtils.isEmpty(query.getLastName())) {
            this.addArgument(filter, "lastName", query.getLastName());
        }

        if (!StringUtils.isEmpty(query.getLastNameLike())) {
            this.addArgument(filter, "lastName", query.getLastNameLike().replaceAll("[%,\\*]", ""));
        }

        if (filter.length() > 0) {
            filter.insert(0, "?");
            String result = filter.toString();
            KeycloakPluginLogger.INSTANCE.userQueryFilter(result);
            return result;
        } else {
            return "";
        }
    }

    protected ResponseEntity<String> requestUserById(String userId) throws RestClientException {
        try {
            String userSearch;
            if (this.keycloakConfiguration.isUseEmailAsCamundaUserId()) {
                userSearch = "/users?email=" + userId;
            } else if (this.keycloakConfiguration.isUseUsernameAsCamundaUserId()) {
                userSearch = "/users?username=" + userId;
            } else {
                userSearch = "/users/" + userId;
            }

            ResponseEntity<String> response = this.restTemplate.exchange(this.keycloakConfiguration.getKeycloakAdminUrl() + userSearch, HttpMethod.GET, this.keycloakContextProvider.createApiRequestEntity(), String.class, new Object[0]);
            String result = !this.keycloakConfiguration.isUseEmailAsCamundaUserId() && !this.keycloakConfiguration.isUseUsernameAsCamundaUserId() ? "[" + (String) response.getBody() + "]" : (String) response.getBody();
            return new ResponseEntity(result, response.getHeaders(), response.getStatusCode());
        } catch (HttpClientErrorException var5) {
            if (var5.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                String result = "[]";
                return new ResponseEntity(result, HttpStatus.OK);
            } else {
                throw var5;
            }
        }
    }

    protected String getKeycloakUserID(String userId) throws KeycloakUserNotFoundException, RestClientException {
        String userSearch;
        if (this.keycloakConfiguration.isUseEmailAsCamundaUserId()) {
            userSearch = "/users?email=" + userId;
        } else {
            if (!this.keycloakConfiguration.isUseUsernameAsCamundaUserId()) {
                return userId;
            }

            userSearch = "/users?username=" + userId;
        }

        try {
            ResponseEntity<String> response = this.restTemplate.exchange(this.keycloakConfiguration.getKeycloakAdminUrl() + userSearch, HttpMethod.GET, this.keycloakContextProvider.createApiRequestEntity(), String.class, new Object[0]);
            return (new JSONArray((String) response.getBody())).getJSONObject(0).getString("id");
        } catch (JSONException var4) {
            throw new KeycloakUserNotFoundException(userId + (this.keycloakConfiguration.isUseEmailAsCamundaUserId() ? " not found - email unknown" : " not found - username unknown"), var4);
        }
    }

    public String getKeycloakAdminUserId(String configuredAdminUserId) {
        try {
            ResponseEntity response;
            try {
                response = this.restTemplate.exchange(this.keycloakConfiguration.getKeycloakAdminUrl() + "/users/" + configuredAdminUserId, HttpMethod.GET, this.keycloakContextProvider.createApiRequestEntity(), String.class, new Object[0]);
                if (this.keycloakConfiguration.isUseEmailAsCamundaUserId()) {
                    return (new JSONObject((String) response.getBody())).getString("email");
                } else {
                    return this.keycloakConfiguration.isUseUsernameAsCamundaUserId() ? (new JSONObject((String) response.getBody())).getString("username") : (new JSONObject((String) response.getBody())).getString("id");
                }
            } catch (JSONException | RestClientException var5) {
                if (this.keycloakConfiguration.isUseEmailAsCamundaUserId() && configuredAdminUserId.contains("@")) {
                    try {
                        this.getKeycloakUserID(configuredAdminUserId);
                        return configuredAdminUserId;
                    } catch (KeycloakUserNotFoundException var4) {
                    }
                }

                try {
                    response = this.restTemplate.exchange(this.keycloakConfiguration.getKeycloakAdminUrl() + "/users?username=" + configuredAdminUserId, HttpMethod.GET, this.keycloakContextProvider.createApiRequestEntity(), String.class, new Object[0]);
                    if (this.keycloakConfiguration.isUseEmailAsCamundaUserId()) {
                        return (new JSONArray((String) response.getBody())).getJSONObject(0).getString("email");
                    } else {
                        return this.keycloakConfiguration.isUseUsernameAsCamundaUserId() ? (new JSONArray((String) response.getBody())).getJSONObject(0).getString("username") : (new JSONArray((String) response.getBody())).getJSONObject(0).getString("id");
                    }
                } catch (JSONException var3) {
                    throw new IdentityProviderException("Configured administratorUserId " + configuredAdminUserId + " does not exist.");
                }
            }
        } catch (RestClientException var6) {
            throw new IdentityProviderException("Unable to read data of configured administratorUserId " + configuredAdminUserId, var6);
        }
    }

    protected UserEntity transformUser(JSONObject result) throws JSONException {
        UserEntity user = new UserEntity();
        if (this.keycloakConfiguration.isUseEmailAsCamundaUserId()) {
            user.setId(this.getStringValue(result, "email"));
        } else if (this.keycloakConfiguration.isUseUsernameAsCamundaUserId()) {
            user.setId(this.getStringValue(result, "username"));
        } else {
            user.setId(result.getString("id"));
        }

        user.setFirstName(this.getStringValue(result, "firstName"));
        user.setLastName(this.getStringValue(result, "lastName"));
        if (StringUtils.isEmpty(user.getFirstName()) && StringUtils.isEmpty(user.getLastName())) {
            user.setFirstName(this.getStringValue(result, "username"));
        }

        user.setEmail(this.getStringValue(result, "email"));
        return user;
    }

    public boolean checkPassword(String userId, String password) {
        if (StringUtils.isEmpty(userId)) {
            return false;
        } else if (StringUtils.isEmpty(password)) {
            return false;
        } else {
            String userName;
            try {
                userName = this.getKeycloakUsername(userId);
            } catch (KeycloakUserNotFoundException var6) {
                KeycloakPluginLogger.INSTANCE.userNotFound(userId, var6);
                return false;
            }

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "application/x-www-form-urlencoded;charset=" + this.keycloakConfiguration.getCharset());
                HttpEntity<String> request = new HttpEntity("client_id=" + this.keycloakConfiguration.getClientId() + "&client_secret=" + this.keycloakConfiguration.getClientSecret() + "&username=" + userName + "&password=" + URLEncoder.encode(password, this.keycloakConfiguration.getCharset()) + "&grant_type=password", headers);
                ResponseEntity<String> response = this.restTemplate.postForEntity(this.keycloakConfiguration.getKeycloakIssuerUrl() + "/protocol/openid-connect/token", request, String.class, new Object[0]);

                JSONObject json = new JSONObject(response.getBody());
                String accessToken = json.getString("access_token");
                String tokenType = json.getString("token_type");
                String refreshToken = json.getString("refresh_token");

                ChironApi.setUserToken(userId, accessToken);

                return true;
            } catch (HttpClientErrorException var7) {
                if (var7.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                    return false;
                } else {
                    throw new IdentityProviderException("Unable to authenticate user at " + this.keycloakConfiguration.getKeycloakIssuerUrl(), var7);
                }
            } catch (RestClientException var8) {
                throw new IdentityProviderException("Unable to authenticate user at " + this.keycloakConfiguration.getKeycloakIssuerUrl(), var8);
            } catch (UnsupportedEncodingException var9) {
                throw new IdentityProviderException("Unable to authenticate user at " + this.keycloakConfiguration.getKeycloakIssuerUrl(), var9);
            }
        }
    }

    protected String getKeycloakUsername(String userId) throws KeycloakUserNotFoundException, RestClientException {
        if (this.keycloakConfiguration.isUseUsernameAsCamundaUserId()) {
            return userId;
        } else {
            try {
                ResponseEntity response;
                if (this.keycloakConfiguration.isUseEmailAsCamundaUserId()) {
                    response = this.restTemplate.exchange(this.keycloakConfiguration.getKeycloakAdminUrl() + "/users?email=" + userId, HttpMethod.GET, this.keycloakContextProvider.createApiRequestEntity(), String.class, new Object[0]);
                    return (new JSONArray((String) response.getBody())).getJSONObject(0).getString("username");
                } else {
                    response = this.restTemplate.exchange(this.keycloakConfiguration.getKeycloakAdminUrl() + "/users/" + userId, HttpMethod.GET, this.keycloakContextProvider.createApiRequestEntity(), String.class, new Object[0]);
                    return (new JSONObject((String) response.getBody())).getString("username");
                }
            } catch (JSONException var3) {
                throw new KeycloakUserNotFoundException(userId + (this.keycloakConfiguration.isUseEmailAsCamundaUserId() ? " not found - email unknown" : " not found - ID unknown"), var3);
            } catch (HttpClientErrorException var4) {
                if (var4.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    throw new KeycloakUserNotFoundException(userId + " not found", var4);
                } else {
                    throw var4;
                }
            }
        }
    }

    public Group findGroupById(String groupId) {
        return (Group) this.createGroupQuery(Context.getCommandContext()).groupId(groupId).singleResult();
    }

    public GroupQuery createGroupQuery() {
        return new KeycloakGroupQuery(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
    }

    public GroupQuery createGroupQuery(CommandContext commandContext) {
        return new KeycloakGroupQuery();
    }

    protected long findGroupCountByQueryCriteria(KeycloakGroupQuery groupQuery) {
        return (long) this.findGroupByQueryCriteria(groupQuery).size();
    }

    protected List<Group> findGroupByQueryCriteria(KeycloakGroupQuery groupQuery) {
        return !StringUtils.isEmpty(groupQuery.getUserId()) ? this.requestGroupsByUserId(groupQuery) : this.requestGroupsWithoutUserId(groupQuery);
    }

    protected List<Group> requestGroupsByUserId(KeycloakGroupQuery query) {
        String userId = query.getUserId();
        List<Group> groupList = new ArrayList();
        StringBuilder resultLogger = new StringBuilder();
        if (KeycloakPluginLogger.INSTANCE.isDebugEnabled()) {
            resultLogger.append("Keycloak group query results: [");
        }

        try {
            String keyCloakID;
            try {
                keyCloakID = this.getKeycloakUserID(userId);
            } catch (KeycloakUserNotFoundException var12) {
                return (List) groupList;
            }

            ResponseEntity<String> response = this.restTemplate.exchange(this.keycloakConfiguration.getKeycloakAdminUrl() + "/users/" + keyCloakID + "/groups", HttpMethod.GET, this.keycloakContextProvider.createApiRequestEntity(), String.class, new Object[0]);
            if (!response.getStatusCode().equals(HttpStatus.OK)) {
                throw new IdentityProviderException("Unable to read user groups from " + this.keycloakConfiguration.getKeycloakAdminUrl() + ": HTTP status code " + response.getStatusCodeValue());
            }

            JSONArray searchResult = new JSONArray((String) response.getBody());

            for (int i = 0; i < searchResult.length(); ++i) {
                JSONObject keycloakGroup = searchResult.getJSONObject(i);
                Group group = this.transformGroup(keycloakGroup);
                if (this.matches((Object) query.getId(), group.getId()) && this.matches((Object[]) query.getIds(), group.getId()) && this.matches((Object) query.getName(), group.getName()) && this.matchesLike(query.getNameLike(), group.getName()) && this.matches((Object) query.getType(), group.getType())) {
                    boolean isAuthenticatedUser = this.isAuthenticatedUser(userId);
                    if (isAuthenticatedUser || this.isAuthorized(Permissions.READ, Resources.GROUP, group.getId())) {
                        ((List) groupList).add(group);
                        if (KeycloakPluginLogger.INSTANCE.isDebugEnabled()) {
                            resultLogger.append(group);
                            resultLogger.append(" based on ");
                            resultLogger.append(keycloakGroup.toString());
                            resultLogger.append(", ");
                        }
                    }
                }
            }
        } catch (HttpClientErrorException var13) {
            if (var13.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return (List) groupList;
            }

            throw var13;
        } catch (RestClientException var14) {
            throw new IdentityProviderException("Unable to query groups of user " + userId, var14);
        } catch (JSONException var15) {
            throw new IdentityProviderException("Unable to query groups of user " + userId, var15);
        }

        if (KeycloakPluginLogger.INSTANCE.isDebugEnabled()) {
            resultLogger.append("]");
            KeycloakPluginLogger.INSTANCE.groupQueryResult(resultLogger.toString());
        }

        if (query.getOrderingProperties().size() > 0) {
            ((List) groupList).sort(new KeycloakIdentityProviderSession.GroupComparator(query.getOrderingProperties()));
        }

        if (query.getFirstResult() > 0 || query.getMaxResults() < 2147483647) {
            groupList = ((List) groupList).subList(query.getFirstResult(), Math.min(((List) groupList).size(), query.getFirstResult() + query.getMaxResults()));
        }

        return (List) groupList;
    }

    protected List<Group> requestGroupsWithoutUserId(KeycloakGroupQuery query) {
        List<Group> groupList = new ArrayList();
        StringBuilder resultLogger = new StringBuilder();
        if (KeycloakPluginLogger.INSTANCE.isDebugEnabled()) {
            resultLogger.append("Keycloak group query results: [");
        }

        try {
            ResponseEntity<String> response = null;
            String searchResult;
            if (!StringUtils.isEmpty(query.getId())) {
                response = this.requestGroupById(query.getId());
            } else {
                searchResult = this.createGroupSearchFilter(query);
                response = this.restTemplate.exchange(this.keycloakConfiguration.getKeycloakAdminUrl() + "/groups" + searchResult, HttpMethod.GET, this.keycloakContextProvider.createApiRequestEntity(), String.class, new Object[0]);
            }

            if (!response.getStatusCode().equals(HttpStatus.OK)) {
                throw new IdentityProviderException("Unable to read groups from " + this.keycloakConfiguration.getKeycloakAdminUrl() + ": HTTP status code " + response.getStatusCodeValue());
            }

            JSONArray jsonSearchResult = null;
            if (!StringUtils.isEmpty(query.getId())) {
                jsonSearchResult = new JSONArray((String) response.getBody());
            } else {
                jsonSearchResult = this.flattenSubGroups(new JSONArray((String) response.getBody()), new JSONArray());
            }

            for (int i = 0; i < jsonSearchResult.length(); ++i) {
                JSONObject keycloakGroup = jsonSearchResult.getJSONObject(i);
                Group group = this.transformGroup(keycloakGroup);
                if (this.matches((Object[]) query.getIds(), group.getId()) && this.matches((Object) query.getName(), group.getName()) && this.matchesLike(query.getNameLike(), group.getName()) && this.matches((Object) query.getType(), group.getType()) && this.isAuthorized(Permissions.READ, Resources.GROUP, group.getId())) {
                    ((List) groupList).add(group);
                    if (KeycloakPluginLogger.INSTANCE.isDebugEnabled()) {
                        resultLogger.append(group);
                        resultLogger.append(" based on ");
                        resultLogger.append(keycloakGroup.toString());
                        resultLogger.append(", ");
                    }
                }
            }
        } catch (RestClientException var9) {
            throw new IdentityProviderException("Unable to query groups", var9);
        } catch (JSONException var10) {
            throw new IdentityProviderException("Unable to query groups", var10);
        }

        if (KeycloakPluginLogger.INSTANCE.isDebugEnabled()) {
            resultLogger.append("]");
            KeycloakPluginLogger.INSTANCE.groupQueryResult(resultLogger.toString());
        }

        if (query.getOrderingProperties().size() > 0) {
            ((List) groupList).sort(new KeycloakIdentityProviderSession.GroupComparator(query.getOrderingProperties()));
        }

        if (query.getFirstResult() > 0 || query.getMaxResults() < 2147483647) {
            groupList = ((List) groupList).subList(query.getFirstResult(), Math.min(((List) groupList).size(), query.getFirstResult() + query.getMaxResults()));
        }

        return (List) groupList;
    }

    protected String createGroupSearchFilter(KeycloakGroupQuery query) {
        StringBuilder filter = new StringBuilder();
        if (!StringUtils.isEmpty(query.getName())) {
            this.addArgument(filter, "search", query.getName());
        }

        if (!StringUtils.isEmpty(query.getNameLike())) {
            this.addArgument(filter, "search", query.getNameLike().replaceAll("[%,\\*]", ""));
        }

        if (filter.length() > 0) {
            filter.insert(0, "?");
            String result = filter.toString();
            KeycloakPluginLogger.INSTANCE.groupQueryFilter(result);
            return result;
        } else {
            return "";
        }
    }

    private JSONArray flattenSubGroups(JSONArray groups, JSONArray result) throws JSONException {
        if (groups == null) {
            return result;
        } else {
            for (int i = 0; i < groups.length(); ++i) {
                JSONObject group = groups.getJSONObject(i);

                try {
                    JSONArray subGroups = group.getJSONArray("subGroups");
                    group.remove("subGroups");
                    result.put(group);
                    this.flattenSubGroups(subGroups, result);
                } catch (JSONException var7) {
                    result.put(group);
                }
            }

            return result;
        }
    }

    protected ResponseEntity<String> requestGroupById(String groupId) throws RestClientException {
        try {
            String groupSearch;
            if (this.keycloakConfiguration.isUseGroupPathAsCamundaGroupId()) {
                groupSearch = "/group-by-path/" + groupId;
            } else {
                groupSearch = "/groups/" + groupId;
            }

            ResponseEntity<String> response = this.restTemplate.exchange(this.keycloakConfiguration.getKeycloakAdminUrl() + groupSearch, HttpMethod.GET, this.keycloakContextProvider.createApiRequestEntity(), String.class, new Object[0]);
            String result = "[" + (String) response.getBody() + "]";
            return new ResponseEntity(result, response.getHeaders(), response.getStatusCode());
        } catch (HttpClientErrorException var5) {
            if (var5.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                String result = "[]";
                return new ResponseEntity(result, HttpStatus.OK);
            } else {
                throw var5;
            }
        }
    }

    protected String getKeycloakGroupID(String groupId) throws KeycloakGroupNotFoundException, RestClientException {
        if (this.keycloakConfiguration.isUseGroupPathAsCamundaGroupId()) {
            String groupSearch = "/group-by-path/" + groupId;

            try {
                ResponseEntity<String> response = this.restTemplate.exchange(this.keycloakConfiguration.getKeycloakAdminUrl() + groupSearch, HttpMethod.GET, this.keycloakContextProvider.createApiRequestEntity(), String.class, new Object[0]);
                return (new JSONObject((String) response.getBody())).getString("id");
            } catch (JSONException var4) {
                throw new KeycloakGroupNotFoundException(groupId + " not found - path unknown", var4);
            }
        } else {
            return groupId;
        }
    }

    public String getKeycloakAdminGroupId(String configuredAdminGroupName) {
        try {
            ResponseEntity response;
            try {
                response = this.restTemplate.exchange(this.keycloakConfiguration.getKeycloakAdminUrl() + "/group-by-path/" + configuredAdminGroupName, HttpMethod.GET, this.keycloakContextProvider.createApiRequestEntity(), String.class, new Object[0]);
                return this.keycloakConfiguration.isUseGroupPathAsCamundaGroupId() ? (new JSONObject((String) response.getBody())).getString("path").substring(1) : (new JSONObject((String) response.getBody())).getString("id");
            } catch (JSONException | RestClientException var8) {
                try {
                    response = this.restTemplate.exchange(this.keycloakConfiguration.getKeycloakAdminUrl() + "/groups?search=" + configuredAdminGroupName, HttpMethod.GET, this.keycloakContextProvider.createApiRequestEntity(), String.class, new Object[0]);
                    JSONArray result = this.flattenSubGroups(new JSONArray((String) response.getBody()), new JSONArray());
                    JSONArray groups = new JSONArray();

                    for (int i = 0; i < result.length(); ++i) {
                        JSONObject keycloakGroup = result.getJSONObject(i);
                        if (keycloakGroup.getString("name").equals(configuredAdminGroupName)) {
                            groups.put(keycloakGroup);
                        }
                    }

                    if (groups.length() == 1) {
                        if (this.keycloakConfiguration.isUseGroupPathAsCamundaGroupId()) {
                            return groups.getJSONObject(0).getString("path").substring(1);
                        }

                        return groups.getJSONObject(0).getString("id");
                    }

                    if (groups.length() > 0) {
                        throw new IdentityProviderException("Configured administratorGroupName " + configuredAdminGroupName + " is not unique. Please configure exact group path.");
                    }
                } catch (JSONException var7) {
                }

                throw new IdentityProviderException("Configured administratorGroupName " + configuredAdminGroupName + " does not exist.");
            }
        } catch (RestClientException var9) {
            throw new IdentityProviderException("Unable to read data of configured administratorGroupName " + configuredAdminGroupName, var9);
        }
    }

    protected GroupEntity transformGroup(JSONObject result) throws JSONException {
        GroupEntity group = new GroupEntity();
        if (this.keycloakConfiguration.isUseGroupPathAsCamundaGroupId()) {
            group.setId(result.getString("path").substring(1));
        } else {
            group.setId(result.getString("id"));
        }

        group.setName(result.getString("name"));
        if (this.isSystemGroup(result)) {
            group.setType("SYSTEM");
        } else {
            group.setType("WORKFLOW");
        }

        return group;
    }

    private boolean isSystemGroup(JSONObject result) {
        String name = result.getString("name");
        if (!"camunda-admin".equals(name) && !name.equals(this.keycloakConfiguration.getAdministratorGroupName())) {
            try {
                JSONArray types = result.getJSONObject("attributes").getJSONArray("type");

                for (int i = 0; i < types.length(); ++i) {
                    if ("SYSTEM".equals(types.getString(i).toUpperCase())) {
                        return true;
                    }
                }

                return false;
            } catch (JSONException var5) {
                return false;
            }
        } else {
            return true;
        }
    }

    public TenantQuery createTenantQuery() {
        return new KeycloakTenantQuery(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
    }

    public TenantQuery createTenantQuery(CommandContext commandContext) {
        return new KeycloakTenantQuery();
    }

    public Tenant findTenantById(String id) {
        return null;
    }

    protected String getStringValue(JSONObject result, String name) {
        try {
            return result.getString(name);
        } catch (JSONException var4) {
            return null;
        }
    }

    protected void addArgument(StringBuilder filter, String name, String value) {
        if (filter.length() > 0) {
            filter.append("&");
        }

        filter.append(name).append('=').append(value);
    }

    protected boolean matches(Object queryParameter, Object attribute) {
        return queryParameter == null || queryParameter.equals(attribute);
    }

    protected boolean matches(Object[] queryParameter, Object attribute) {
        return queryParameter == null || queryParameter.length == 0 || Arrays.asList(queryParameter).contains(attribute);
    }

    protected boolean matchesLike(String queryParameter, String attribute) {
        return queryParameter == null || attribute.matches(queryParameter.replaceAll("[%\\*]", ".*"));
    }

    protected boolean isAuthenticatedUser(UserEntity user) {
        return this.isAuthenticatedUser(user.getId());
    }

    protected boolean isAuthenticatedUser(String userId) {
        return userId == null ? false : userId.equalsIgnoreCase(Context.getCommandContext().getAuthenticatedUserId());
    }

    protected boolean isAuthorized(Permission permission, Resource resource, String resourceId) {
        return !this.keycloakConfiguration.isAuthorizationCheckEnabled() || Context.getCommandContext().getAuthorizationManager().isAuthorized(permission, resource, resourceId);
    }

    protected static int compare(String str1, String str2) {
        if (str1 == str2) {
            return 0;
        } else if (str1 == null) {
            return -1;
        } else {
            return str2 == null ? 1 : str1.compareTo(str2);
        }
    }

    private static class GroupComparator implements Comparator<Group> {
        private static final int GROUP_ID = 0;
        private static final int NAME = 1;
        private static final int TYPE = 2;
        private int[] order;
        private boolean[] desc;

        public GroupComparator(List<QueryOrderingProperty> orderList) {
            this.order = new int[orderList.size()];
            this.desc = new boolean[orderList.size()];

            for (int i = 0; i < orderList.size(); ++i) {
                QueryOrderingProperty qop = (QueryOrderingProperty) orderList.get(i);
                if (qop.getQueryProperty().equals(GroupQueryProperty.GROUP_ID)) {
                    this.order[i] = 0;
                } else if (qop.getQueryProperty().equals(GroupQueryProperty.NAME)) {
                    this.order[i] = 1;
                } else if (qop.getQueryProperty().equals(GroupQueryProperty.TYPE)) {
                    this.order[i] = 2;
                } else {
                    this.order[i] = -1;
                }

                this.desc[i] = Direction.DESCENDING.equals(qop.getDirection());
            }

        }

        public int compare(Group g1, Group g2) {
            int c = 0;

            for (int i = 0; i < this.order.length; ++i) {
                switch (this.order[i]) {
                    case 0:
                        c = KeycloakIdentityProviderSession.compare(g1.getId(), g2.getId());
                        break;
                    case 1:
                        c = KeycloakIdentityProviderSession.compare(g1.getName(), g2.getName());
                        break;
                    case 2:
                        c = KeycloakIdentityProviderSession.compare(g1.getType(), g2.getType());
                }

                if (c != 0) {
                    return this.desc[i] ? -c : c;
                }
            }

            return c;
        }
    }

    private static class UserComparator implements Comparator<User> {
        private static final int USER_ID = 0;
        private static final int EMAIL = 1;
        private static final int FIRST_NAME = 2;
        private static final int LAST_NAME = 3;
        private int[] order;
        private boolean[] desc;

        public UserComparator(List<QueryOrderingProperty> orderList) {
            this.order = new int[orderList.size()];
            this.desc = new boolean[orderList.size()];

            for (int i = 0; i < orderList.size(); ++i) {
                QueryOrderingProperty qop = (QueryOrderingProperty) orderList.get(i);
                if (qop.getQueryProperty().equals(UserQueryProperty.USER_ID)) {
                    this.order[i] = 0;
                } else if (qop.getQueryProperty().equals(UserQueryProperty.EMAIL)) {
                    this.order[i] = 1;
                } else if (qop.getQueryProperty().equals(UserQueryProperty.FIRST_NAME)) {
                    this.order[i] = 2;
                } else if (qop.getQueryProperty().equals(UserQueryProperty.LAST_NAME)) {
                    this.order[i] = 3;
                } else {
                    this.order[i] = -1;
                }

                this.desc[i] = Direction.DESCENDING.equals(qop.getDirection());
            }

        }

        public int compare(User u1, User u2) {
            int c = 0;

            for (int i = 0; i < this.order.length; ++i) {
                switch (this.order[i]) {
                    case 0:
                        c = KeycloakIdentityProviderSession.compare(u1.getId(), u2.getId());
                        break;
                    case 1:
                        c = KeycloakIdentityProviderSession.compare(u1.getEmail(), u2.getEmail());
                        break;
                    case 2:
                        c = KeycloakIdentityProviderSession.compare(u1.getFirstName(), u2.getFirstName());
                        break;
                    case 3:
                        c = KeycloakIdentityProviderSession.compare(u1.getLastName(), u2.getLastName());
                }

                if (c != 0) {
                    return this.desc[i] ? -c : c;
                }
            }

            return c;
        }
    }
}
