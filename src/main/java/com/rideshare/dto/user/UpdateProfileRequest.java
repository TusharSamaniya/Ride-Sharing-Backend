package com.rideshare.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
	
	@Size(min = 2, max = 50, message = "First name must be 2-50 charachters")
	private String firstName;
	
	@Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
	private String lastName;
	
	@Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
	private String phoneNumber;

}
