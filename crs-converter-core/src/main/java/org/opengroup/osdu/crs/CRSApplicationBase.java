package org.opengroup.osdu.crs;

import jakarta.annotation.PostConstruct;
import java.util.logging.Logger;

public class CRSApplicationBase {

	private static Logger logger = Logger.getLogger(CRSApplicationBase.class.getName());

	@PostConstruct
	public void setup() {
		//
	}
}
