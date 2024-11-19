package org.una.programmingIII.UTEMP_Project.exceptions;

public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String resource, String identifier) {
        super(resource + " already exists with identifier: " + identifier);
    }
}
