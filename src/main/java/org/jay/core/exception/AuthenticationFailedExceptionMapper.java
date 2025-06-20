package org.jay.core.exception;

import io.quarkus.security.AuthenticationFailedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider // 必須要有 @Provider
public class AuthenticationFailedExceptionMapper implements ExceptionMapper<AuthenticationFailedException> {

    @Override
    public Response toResponse(AuthenticationFailedException exception) {
        Map<String, String> errorResponse = Map.of(
                "error", "Authentication Failed",
                "message", "Token is invalid or expired. Please log in again."
        );

        return Response.status(Response.Status.UNAUTHORIZED) // 回傳 401
                .entity(errorResponse)
                .build();
    }
}
