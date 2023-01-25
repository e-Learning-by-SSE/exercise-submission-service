package net.ssehub.teaching.exercise_submission.service.storage;

/**
 * A generic exception caused by the storage.
 * 
 * @author Adam
 */
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
