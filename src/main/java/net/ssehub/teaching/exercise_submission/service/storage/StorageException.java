package net.ssehub.teaching.exercise_submission.service.storage;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A generic exception caused by the storage.
 * 
 * @author Adam
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class StorageException extends Exception {

    private static final long serialVersionUID = -8029745708734384975L;

    /**
     * Creates this exception.
     * 
     * @param cause The cause.
     */
    public StorageException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Creates this exception.
     * 
     * @param messsage The detail message.
     */
    public StorageException(String messsage) {
        super(messsage);
    }
    
}
