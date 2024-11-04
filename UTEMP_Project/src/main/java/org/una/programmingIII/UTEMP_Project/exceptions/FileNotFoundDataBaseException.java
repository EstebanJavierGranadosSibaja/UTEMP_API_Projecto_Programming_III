package org.una.programmingIII.UTEMP_Project.exceptions;

public class FileNotFoundDataBaseException extends RuntimeException {
    public FileNotFoundDataBaseException(Long id) {
        super("No such file ID: " + id);
    }
}