package net.ssehub.teaching.exercise_submission.service.routes;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.ssehub.teaching.exercise_submission.service.StorageInitializer;
import net.ssehub.teaching.exercise_submission.service.dto.FileDto;
import net.ssehub.teaching.exercise_submission.service.dto.SubmissionResultDto;

@SpringBootTest
@AutoConfigureMockMvc
public class SubmissionControllerIT extends StorageInitializer {

    @Autowired
    private MockMvc mvc;
    
    @Autowired
    private ObjectMapper json;
    
    @Test
    public void successfulSubmission() {
        Path groupStorage = testStorage.resolve("java-sose23/Homework05/JP123");
        assertDoesNotThrow(() -> Files.createDirectories(groupStorage));
        
        MvcResult result = post("/submission/{course}/{assignment}/{group}",
                List.of(new FileDto("Main.java", "content...")),
                "java-sose23", "Homework05", "JP123");

        SubmissionResultDto dto = parseResponse(result, SubmissionResultDto.class);
        
        assertAll(
            () -> assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus()),
            () -> assertEquals(1, Files.list(groupStorage).count()), // submission folder
            () -> assertTrue(dto.getAccepted()),
            () -> assertEquals(List.of(), dto.getMessages())
        );
    }
    
    public MvcResult post(String urlTemplate, Object body, Object... urlVariables) {
        return assertDoesNotThrow(() -> mvc.perform(postRequest(urlTemplate, body, urlVariables))).andReturn();
    }
    
    public <T> T parseResponse(MvcResult response, Class<T> responseType) {
        return assertDoesNotThrow(() -> json.readValue(response.getResponse().getContentAsString(), responseType));
    }
    
    private RequestBuilder postRequest(String urlTemplate, Object body, Object... urlVariables)
            throws JsonProcessingException {
        return MockMvcRequestBuilders.post(urlTemplate, urlVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body));
    }
    
}
