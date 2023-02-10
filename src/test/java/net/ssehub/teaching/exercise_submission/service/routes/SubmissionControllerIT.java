package net.ssehub.teaching.exercise_submission.service.routes;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.ssehub.teaching.exercise_submission.service.StorageInitializer;
import net.ssehub.teaching.exercise_submission.service.dto.FileDto;
import net.ssehub.teaching.exercise_submission.service.dto.SubmissionResultDto;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class SubmissionControllerIT extends StorageInitializer {

    @Autowired
    private MockMvc mvc;
    
    @Autowired
    private ObjectMapper json;
    
    @Nested
    class Submit {
        // TODO: the tests here fail because StuMgmtView is not working
        
        @Test
        public void successful() {
            Path groupStorage = testStorage.resolve("java-sose23/Homework05/JP123");
            assertDoesNotThrow(() -> Files.createDirectories(groupStorage));
            
            Request request = new Request(mvc)
                    .method(HttpMethod.POST)
                    .url("/submission/{course}/{assignment}/{group}")
                    .urlVariables("java-sose23", "Homework05", "JP123")
                    .body(List.of(FileDto.fromStringContent("Main.java", "content...")))
                    .authenticate("author1")
                    .perform();
            
            assertAll(
                () -> assertEquals(HttpStatus.CREATED, request.getResponseStatus()),
                () -> assertEquals(1, Files.list(groupStorage).count()), // submission folder created
                () -> assertEquals(new SubmissionResultDto(true, List.of()),
                        request.parseResponse(SubmissionResultDto.class))
            );
        }
        
        @Test
        public void unauthenticatedForbidden() {
            Path groupStorage = testStorage.resolve("java-sose23/Homework05/JP123");
            assertDoesNotThrow(() -> Files.createDirectories(groupStorage));
            
            Request request = new Request(mvc)
                    .method(HttpMethod.POST)
                    .url("/submission/{course}/{assignment}/{group}")
                    .urlVariables("java-sose23", "Homework05", "JP123")
                    .body(List.of(FileDto.fromStringContent("Main.java", "content...")))
                    .perform();
            
            assertAll(
                () -> assertEquals(HttpStatus.FORBIDDEN, request.getResponseStatus()),
                () -> assertEquals(0, Files.list(groupStorage).count()) // no submission folder created
            );
        }
        
        @Test
        public void notExistingTargetNotFound() {
            Request request = new Request(mvc)
                    .method(HttpMethod.POST)
                    .url("/submission/{course}/{assignment}/{group}")
                    .urlVariables("java-sose23", "Homework05", "JP123")
                    .body(List.of(FileDto.fromStringContent("Main.java", "content...")))
                    .authenticate("author1")
                    .perform();
            
            assertEquals(HttpStatus.NOT_FOUND, request.getResponseStatus());
        }
        
    }
    
    private class Request {
        
        private MockMvc mvc;
        
        private HttpMethod method = HttpMethod.GET;
        
        private String urlTemplate;
        
        private Object[] urlVariables = new Object[0];
        
        private Object body;
        
        private String username;
        
        private HttpStatus resultStatus;
        
        private String resultContent;
        
        public Request(MockMvc mvc) {
            this.mvc = mvc;
        }
        
        public Request method(HttpMethod method) {
            this.method = method;
            return this;
        }
        
        public Request url(String urlTemplate) {
            this.urlTemplate = urlTemplate;
            return this;
        }
        
        public Request urlVariables(Object... urlVariables) {
            this.urlVariables = urlVariables;
            return this;
        }
        
        public Request body(Object body) {
            this.body = body;
            return this;
        }
        
        public Request authenticate(String username) {
            this.username = username;
            return this;
        }
        
        public Request perform() {
            MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.request(method, urlTemplate, urlVariables);
            if (body != null) {
                builder.contentType(MediaType.APPLICATION_JSON);
                builder.content(assertDoesNotThrow(() -> json.writeValueAsString(body)));
            }
            if (username != null) {
                builder.with(SecurityMockMvcRequestPostProcessors.jwt().jwt(b -> b.subject(username)));
            }
            
            MvcResult result = assertDoesNotThrow(() -> mvc.perform(builder)).andReturn();
            resultStatus = HttpStatus.resolve(result.getResponse().getStatus());
            resultContent = assertDoesNotThrow(() -> result.getResponse().getContentAsString());
            
            return this;
        }
        
        public HttpStatus getResponseStatus() {
            return resultStatus;
        }
        
        public <T> T parseResponse(Class<T> type) {
            return assertDoesNotThrow(() -> json.readValue(resultContent, type));
        }
        
    }
    
}
