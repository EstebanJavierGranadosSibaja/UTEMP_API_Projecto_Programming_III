package org.una.programmingIII.UTEMP_Project.exceptions;

public class RelationConflictException extends RuntimeException {
    public RelationConflictException(String message) {
        super("Relation conflict: " + message);
    }
}