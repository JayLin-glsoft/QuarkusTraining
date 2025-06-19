package org.jay.core;

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

        return Response.status(Response.Status.UNAUTHORIZED) // 回傳 401
                .entity(errorResponse)
                .build();
    }
}
