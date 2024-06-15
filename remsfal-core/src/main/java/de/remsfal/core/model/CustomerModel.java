package de.remsfal.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface CustomerModel extends UserModel {
	
	default String getName() {
		return String.format("%s %s", this.getFirstName(), this.getLastName()).trim();
	}

	String getFirstName();
	
	String getLastName();
	
	AddressModel getAddress();
	
	String getMobilePhoneNumber();
	
	String getBusinessPhoneNumber();
	
	String getPrivatePhoneNumber();
	
    LocalDate getRegisteredDate();

    LocalDateTime getLastLoginDate();

}
