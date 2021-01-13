package flinderstemi.util;

import java.util.UUID;

/**
 * Custom Listeners should extend this for id
 */
public class TemiListener {

    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
