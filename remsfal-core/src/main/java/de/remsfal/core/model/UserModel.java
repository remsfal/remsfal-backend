package de.remsfal.core.model;

import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface UserModel {

    UUID getId();

    String getEmail();

    String getName();

    Boolean isActive();

}
