package com.spoohapps.jble6lowpand.util;

import java.util.regex.Pattern;

public class BTAddressValidatorImpl implements BTAddressValidator {

	@Override
	public boolean validate(String address) {
		Pattern btAddressPattern = Pattern.compile("^([0-9A-Fa-f]{2}[:-]?){5}([0-9A-Fa-f]{2})$");
		Pattern flatBtAddressPattern = Pattern.compile("^([0-9A-Fa-f]{12})$");
		return btAddressPattern.matcher(address).matches() || flatBtAddressPattern.matcher(address).matches();
	}
	
}
