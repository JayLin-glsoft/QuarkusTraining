package org.jay.user.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jay.user.model.dto.*;
import org.jay.user.service.UserService;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService; // 現在只注入 UserService

    @POST
    @Path("/register")
    @PermitAll
    public Response register(@Valid RegisterRequest request) {
        TokenResponse tokenResponse = userService.register(request);
        // 回傳 201 Created 並附上 token
        return Response.status(Response.Status.CREATED).entity(tokenResponse).build();
    }

    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginRequest request) {
        TokenResponse tokenResponse = userService.login(request);
        return Response.ok(tokenResponse).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({"user", "admin"})
    public Response getMe() {
        UserProfileResponse responseDto = userService.getCurrentUser();
        return Response.ok(responseDto).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"user", "admin"})
    public Response updateUser(@PathParam("id") Long id, @Valid UserUpdateRequest request) {
        UserProfileResponse responseDto = userService.updateUser(id, request);
        return Response.ok(responseDto).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"user", "admin"})
    public Response deleteUser(@PathParam("id") Long id) {
        userService.deleteUser(id);
        return Response.noContent().build(); // 204 No Content
    }
}