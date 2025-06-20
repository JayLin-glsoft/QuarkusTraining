package org.jay.core.exception;

import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {
    @Override
    public Response toResponse(UnauthorizedException exception) {
        Map<String, String> errorResponse = Map.of(
                "error", "Unauthorized",
                "message", "Token is invalid or expired. Please log in again."
        );

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(errorResponse)
                .build();
    }
}
