package com.spoohapps.jble6lowpand.model;

import com.spoohapps.jble6lowpand.model.BTAddressValidator;

import java.util.regex.Pattern;

public class BTAddressValidatorImpl implements BTAddressValidator {

	@Override
	public boolean validate(String address) {
		Pattern btAddressPattern = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
		return btAddressPattern.matcher(address).matches();
	}
	
}
