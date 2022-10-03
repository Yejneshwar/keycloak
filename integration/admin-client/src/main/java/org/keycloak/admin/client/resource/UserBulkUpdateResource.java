package org.keycloak.admin.client.resource;

import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * @author yejneshwar@gmail.com
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UserBulkUpdateResource {
    @Path("{id}")
    UserResource get(@PathParam("id") String id);

    @Path("count")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Integer send();

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response bulkUpdate(List<UserRepresentation> bulkUsers);
}