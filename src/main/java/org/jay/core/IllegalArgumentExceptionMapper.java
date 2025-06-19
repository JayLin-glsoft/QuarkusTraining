package org.jay.core;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
    @Override
    public Response toResponse(IllegalArgumentException exception) {
        Map<String, String> errorResponse = Map.of("message", exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST) // 回傳 400
                .entity(errorResponse)
                .build();
    }
}
