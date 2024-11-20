package com.greenspace.api.error;

import java.time.DateTimeException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;

import com.greenspace.api.dto.responses.Response;
import com.greenspace.api.dto.responses.ResponseError;
import com.greenspace.api.error.http.BadRequest400Exception;
import com.greenspace.api.error.http.Conflict409Exception;
import com.greenspace.api.error.http.NotFound404Exception;
import com.greenspace.api.error.http.Unauthorized401Exception;

@ControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(DateTimeException.class)
        public ResponseEntity<Response<Object>> handleDateTimeException(DateTimeException ex) {
                ResponseError error = new ResponseError(400, ex.getMessage());
                Response<Object> responseBody = Response.builder()
                                .message("Invalid date time format!")
                                .error(error).status(400).build();

                return ResponseEntity.status(400).body(responseBody);
        }

        /////////////////////////
        /// HTTP STATUSES///
        ////////////////////////

        @ExceptionHandler(BadRequest400Exception.class)
        public ResponseEntity<Response<Object>> handleBadRequestException(BadRequest400Exception ex) {
                ResponseError error = new ResponseError(ex.getCode(), ex.getMessage());

                Response<Object> responseBody = Response.builder()
                                .message("Bad Request!").error(error).status(ex.getCode()).build();

                return ResponseEntity.status(ex.getCode()).body(responseBody);
        }

        @ExceptionHandler(Conflict409Exception.class)
        public ResponseEntity<Response<Object>> handleConflictRequests(Conflict409Exception ex) {

                ResponseError error = new ResponseError(ex.getCode(), ex.getMessage());
                Response<Object> responseBody = Response.builder()
                                .message("Conflict").error(error).status(ex.getCode()).build();

                return ResponseEntity.status(ex.getCode()).body(responseBody);
        }

        @ExceptionHandler(Unauthorized401Exception.class)
        public ResponseEntity<Response<Object>> handleUnauthorizedRequests(
                        Unauthorized401Exception ex) {

                ResponseError error = new ResponseError(ex.getCode(), ex.getMessage());
                Response<Object> responseBody = Response.builder()
                                .message("Unauthorized").error(error).status(ex.getCode())
                                .build();

                return ResponseEntity.status(ex.getCode()).body(responseBody);
        }

        @ExceptionHandler(NotFound404Exception.class)
        public ResponseEntity<Response<Object>> handleNotFoundRequests(NotFound404Exception ex) {

                ResponseError error = new ResponseError(ex.getCode(), ex.getMessage());
                Response<Object> responseBody = Response.builder()
                                .message("Not Found").error(error).status(ex.getCode()).build();

                return ResponseEntity.status(ex.getCode()).body(responseBody);
        }

        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<Response<Object>> handleUsernameNotFoundException(
                        UsernameNotFoundException ex) {

                ResponseError error = new ResponseError(404, ex.getMessage());
                Response<Object> responseBody = Response.builder()
                                .message("User not found").error(error).status(404).build();

                return ResponseEntity.status(404).body(responseBody);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Response<Object>> handleHttpMessageNotReadableException(
                        HttpMessageNotReadableException ex) {
                String message = ex.getMessage().split(":")[0];

                ResponseError error = new ResponseError(400, message);
                Response<Object> responseBody = Response.builder()
                                .message("empty payload, check the docs!").error(error).status(400).build();

                return ResponseEntity.status(400).body(responseBody);
        }

        @ExceptionHandler(MultipartException.class)
        public ResponseEntity<Response<Object>> handleMultipartException(MultipartException ex) {

                ResponseError error = new ResponseError(400, ex.getMessage());
                Response<Object> responseBody = Response.builder()
                                .message("invalid multipart request, check the docs!").error(error)
                                .status(400).build();

                return ResponseEntity.status(400).body(responseBody);
        }

        @ExceptionHandler(MissingPathVariableException.class)
        public ResponseEntity<Response<Object>> handleMissingPathVarException(
                        MissingPathVariableException ex) {

                ResponseError error = new ResponseError(400, ex.getMessage());
                Response<Object> responseBody = Response.builder()
                                .message("Missing path variable, perhaps you forgot something on the url")
                                .error(error)
                                .status(400).build();

                return ResponseEntity.status(400).body(responseBody);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Response<Object>> handleIllegalArgumentException(
                        IllegalArgumentException ex) {

                ResponseError error = new ResponseError(400, ex.getMessage());
                Response<Object> responseBody = Response.builder()
                                .message("Invalid argument").error(error).status(400).build();

                return ResponseEntity.status(400).body(responseBody);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<Response<Object>> handleAuthenticationException(
                        AuthenticationException ex) {

                ResponseError error = new ResponseError(401, ex.getMessage());

                Response<Object> responseBody = Response.builder()
                                .message("Authentication error").error(error).status(401).build();

                return ResponseEntity.status(401).body(responseBody);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<Response<Object>> handleMethodArgumentTypeMismatchException(
                        MethodArgumentTypeMismatchException ex) {

                if (ex.getRequiredType() == null) {
                        ResponseError error = new ResponseError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
                        Response<Object> responseBody = Response.builder()
                                        .message("Invalid argument type, check the docs or contact the admin!")
                                        .error(error)
                                        .status(HttpStatus.BAD_REQUEST.value())
                                        .build();

                        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(responseBody);
                }

                @SuppressWarnings("null")
                ResponseError error = new ResponseError(HttpStatus.BAD_REQUEST.value(),
                                ex.getName() + " should be of type " + ex.getRequiredType().getSimpleName());

                Response<Object> responseBody = Response.builder()
                                .message("Invalid argument type, check the docs or contact the admin!")
                                .error(error)
                                .status(HttpStatus.BAD_REQUEST.value())
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(responseBody);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<Response<Object>> handleHttpRequestMethodNotSupportedException(
                        HttpRequestMethodNotSupportedException ex) {

                ResponseError error = new ResponseError(HttpStatus.METHOD_NOT_ALLOWED.value(), "Method not allowed!",
                                ex.getMethod()
                                                + " method is not supported for this endpoint, expected methods are: "
                                                + ex.getSupportedHttpMethods());

                Response<Object> responseBody = Response.builder()
                                .message("Invalid request method, check the docs or contact the admin!")
                                .error(error)
                                .status(HttpStatus.METHOD_NOT_ALLOWED.value()).build();

                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED.value()).body(responseBody);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<Response<Object>> handleDataIntegrityViolationException(
                        DataIntegrityViolationException ex) {

                ResponseError error = new ResponseError(HttpStatus.BAD_REQUEST.value(),
                                ex.getMostSpecificCause().getMessage().split("Detail: ")[0]);

                Response<Object> responseBody = Response.builder()
                                .message("Data integrity violation, check the docs or contact the admin!")
                                .error(error)
                                .status(HttpStatus.BAD_REQUEST.value())
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(responseBody);
        }

        @ExceptionHandler(ClassNotFoundException.class)
        public ResponseEntity<Response<Object>> handleClassNotFoundException(
                        ClassNotFoundException ex) {

                ResponseError error = new ResponseError(HttpStatus.NOT_FOUND.value(), ex.getMessage());
                Response<Object> responseBody = Response.builder()
                                .message("Class not found, check the docs or contact the admin!")
                                .error(error)
                                .status(HttpStatus.NOT_FOUND.value()).build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).body(responseBody);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Response<Object>> handleException(Exception ex) {

                ResponseError error = new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
                Response<Object> responseBody = Response.builder()
                                .message("Internal server error, contact the admin!")
                                .error(error)
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .build();

                System.out.println("\n\nA unknown exception was thrown : " + ex + "\n\n");

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(responseBody);
        }
}
