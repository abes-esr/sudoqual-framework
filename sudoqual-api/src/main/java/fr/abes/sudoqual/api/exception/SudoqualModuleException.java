package fr.abes.sudoqual.api.exception;

public class SudoqualModuleException extends Exception {

    public SudoqualModuleException(String message) {
        super(message);
    }

    public SudoqualModuleException(String message, Throwable cause) {
        super(message, cause);
    }

}
