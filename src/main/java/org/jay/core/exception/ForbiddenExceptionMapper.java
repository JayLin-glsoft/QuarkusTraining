package org.jay.core.exception;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {
    @Override
    public Response toResponse(ForbiddenException exception) {
        Map<String, String> errorResponse = Map.of(
                "error", "Forbidden",
                "message", exception.getMessage()
        );
        return Response.status(Response.Status.FORBIDDEN)
                .entity(errorResponse)
                .build();
    }
}
