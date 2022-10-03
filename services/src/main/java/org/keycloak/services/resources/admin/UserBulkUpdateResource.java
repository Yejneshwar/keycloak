/**
 * Base resource for bulk updating users
 *
 * @resource Users
 * @author <a href="mailto:yejneshwar@gmail.com">Yejneshwar Sivamoorthy</a>
 * @version $Revision: 1 $
 */

package org.keycloak.services.resources.admin;

import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.authentication.actiontoken.execactions.ExecuteActionsActionToken;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.Profile;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserConsentModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserLoginFailureModel;
import org.keycloak.models.UserManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserConsentRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.ForbiddenException;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.BruteForceProtector;
import org.keycloak.services.managers.UserSessionManager;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.services.resources.account.AccountFormService;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.UserPermissionEvaluator;
import org.keycloak.services.validation.Validation;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.utils.ProfileHelper;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.keycloak.models.ImpersonationSessionNote.IMPERSONATOR_ID;
import static org.keycloak.models.ImpersonationSessionNote.IMPERSONATOR_USERNAME;

public class UserBulkUpdateResource {
    private static final Logger logger = Logger.getLogger(UserBulkUpdateResource.class);

    protected RealmModel realm;

    private AdminPermissionEvaluator auth;

    private AdminEventBuilder adminEvent;
    private UserModel user;

    @Context
    protected ClientConnection clientConnection;

    @Context
    protected KeycloakSession session;

    @Context
    protected HttpHeaders headers;

    public UserBulkUpdateResource(RealmModel realm,AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.auth = auth;
        this.realm = realm;
        // this.user = user;
        this.adminEvent = adminEvent.resource(ResourceType.USER);
    }


    //USER PERMISSIONS TEST
    // @Path("count")
    // @GET
    // @Produces(MediaType.APPLICATION_JSON)
    // public Integer send(){
    //     UserPermissionEvaluator userPermissionEvaluator = auth.users();
    //     userPermissionEvaluator.requireQuery();
    //     return 30;
    // }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON) 
    public Response bulkUpdate(List<UserRepresentation> bulkUsers) {
        logger.warn("Could not bulk update!");
        return ErrorResponse.error("Could not bulk update!", Status.BAD_REQUEST);
    }
    
    public static Response validateUserProfile(UserModel user, UserRepresentation rep, KeycloakSession session) {
        return null;
    }

    public static void updateUserFromRep(UserModel user, UserRepresentation rep, KeycloakSession session, boolean isUpdateExistingUser) {
    }
}