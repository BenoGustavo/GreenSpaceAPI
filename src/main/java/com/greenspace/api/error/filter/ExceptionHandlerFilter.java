package com.greenspace.api.error.filter;

import java.io.IOException;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenspace.api.dto.responses.Response;
import com.greenspace.api.dto.responses.ResponseError;
import com.greenspace.api.error.http.Forbidden403Exception;
import com.greenspace.api.error.http.NotFound404Exception;
import com.greenspace.api.error.http.Unauthorized401Exception;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (UsernameNotFoundException | NotFound404Exception ex) {
            setErrorResponse(HttpServletResponse.SC_NOT_FOUND, response, "User not found!", ex);
        } catch (io.jsonwebtoken.MalformedJwtException ex) {
            setErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, response, "Invalid JWT token", ex);
        } catch (ServletException ex) {
            setErrorResponse(HttpServletResponse.SC_BAD_REQUEST, response, ex.getMessage(), ex);
        } catch (Unauthorized401Exception ex) {
            setErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, response, "You're not allowed to acess this route!",
                    ex);
        } catch (Forbidden403Exception ex) {
            setErrorResponse(HttpServletResponse.SC_FORBIDDEN, response, "Forbidden", ex);
        } catch (IOException ex) {
            setErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response, "An unexpected error occurred",
                    ex);
        } catch (Exception ex) {
            setErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response, "An unexpected error occurred",
                    ex);
        }
    }

    private void setErrorResponse(int status, HttpServletResponse response, String message, Exception ex)
            throws IOException {
        Response<Object> errorResponse = Response.builder()
                .status(status)
                .message("Request Failed!")
                .error(new ResponseError(status, message, ex.getMessage()))
                .build();

        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(convertObjectToJson(errorResponse));
        response.getWriter().flush();
        response.getWriter().close();
    }

    private String convertObjectToJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (IOException e) {
            return "{\"error\": \"Failed to convert object to JSON\"}";
        }
    }
}