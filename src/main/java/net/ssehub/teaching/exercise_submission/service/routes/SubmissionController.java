package net.ssehub.teaching.exercise_submission.service.routes;

import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.ssehub.teaching.exercise_submission.service.dto.FileDto;
import net.ssehub.teaching.exercise_submission.service.dto.SubmissionResultDto;
import net.ssehub.teaching.exercise_submission.service.dto.VersionDto;
import net.ssehub.teaching.exercise_submission.service.storage.ISubmissionStorage;
import net.ssehub.teaching.exercise_submission.service.storage.StorageException;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionBuilder;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionManager;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionTarget;
import net.ssehub.teaching.exercise_submission.service.submission.Version;

/**
 * The controller for the /submission route. Used to post new submissions and get version history and retrieve previous
 * submissions. 
 * 
 * @author Adam
 */
@RestController
@RequestMapping("/submission")
public class SubmissionController {
    
    private SubmissionManager manager;
    
    private ISubmissionStorage storage;
    
    /**
     * Creates this controller.
     * 
     * @param manager The manager that orchestrates the submissions (checks, storage, etc.).
     * @param storage The storage where submissions are placed (should be the same as used by the manager).
     */
    public SubmissionController(SubmissionManager manager, ISubmissionStorage storage) {
        this.manager = manager;
        this.storage = storage;
    }

    /**
     * Route for adding a new submission.
     * 
     * @param course The course to add the submission for.
     * @param assignment The assignment to add the submission for.
     * @param group The group to add the submission for.
     * @param files The files of the submission.
     * @param auth The authentication.
     * 
     * @return The result of the submission.
     * 
     * @throws StorageException If a storage exception occurs.
     */
    @PostMapping("/{course}/{assignment}/{group}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SubmissionResultDto> submit(
            @PathVariable String course,
            @PathVariable String assignment,
            @PathVariable String group,
            @RequestBody List<FileDto> files,
            Authentication auth)
            throws StorageException {
        
        SubmissionTarget target = new SubmissionTarget(course, assignment, group);
        SubmissionBuilder submissionBuilder = new SubmissionBuilder(auth.getName());
        
        for (FileDto file : files) {
            submissionBuilder.addFile(Path.of(file.getPath()), Base64.getDecoder().decode(file.getContent()));
        }
     
        SubmissionResultDto resultDto = manager.submit(target, submissionBuilder.build());
        HttpStatus status;
        if (resultDto.getAccepted()) {
            status = HttpStatus.CREATED;
        } else {
            status = HttpStatus.OK;
        }
        
        return new ResponseEntity<>(resultDto, status);
    }
    
    /**
     * Route for retrieving the list of submitted versions.
     * 
     * @param course The course where the submission is located.
     * @param assignment The assignment where the submission is located.
     * @param group The group where the submission is located.
     * @param auth The authentication.
     * 
     * @return The list of versions for that submission.
     * 
     * @throws StorageException If a storage exception occurs.
     */
    @GetMapping("/{course}/{assignment}/{group}/versions")
    @PreAuthorize("permitAll()")
    public List<VersionDto> listVersions(
            @PathVariable String course,
            @PathVariable String assignment,
            @PathVariable String group,
            Authentication auth)
            throws StorageException {
        
        List<Version> version = storage.getVersions(new SubmissionTarget(course, assignment, group));
        
        return version.stream()
                .map(v -> {
                    VersionDto dto = new VersionDto();
                    dto.setAuthor(v.author());
                    dto.setTimestamp(v.creationTime().getEpochSecond());
                    return dto;
                })
                .toList();
    }
    
}
