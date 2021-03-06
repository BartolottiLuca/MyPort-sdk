package tesi.barto.myport.model.consents;

/**
 * Created by Valentina on 24/08/2017.
 */

public enum ConsentStatus {
    ACTIVE ("Active"),
    WITHDRAWN ("Withdrawn"),
    DISABLED ("Disabled");

    private String status;

    ConsentStatus(String status) {
        this.status = status;
    }

    public String toString() {
        return this.status;
    }
}

