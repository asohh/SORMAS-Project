package de.symeda.sormas.api.externaljournal;

import java.io.Serializable;

public class ExternalPersonValidation implements Serializable {

    private boolean valid;
    private String message;

    public ExternalPersonValidation(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
