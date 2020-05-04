/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Louis Rose - initial API and implementation
 ******************************************************************************
 *
 * $Id: IncorrectTypeForFloat.java,v 1.2 2008/08/08 17:19:01 louis Exp $
 */
package org.eclipse.epsilon.hutn.validation.model;

import static org.eclipse.epsilon.hutn.test.util.HutnUtil.createAttributeSlot;
import static org.eclipse.epsilon.hutn.test.util.HutnUtil.createClassObject;
import static org.eclipse.epsilon.hutn.test.util.HutnUtil.createPackageObject;
import static org.eclipse.epsilon.hutn.test.util.HutnUtil.createSpec;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class IncorrectTypeForFloat extends HutnModelValidationTest {

	@BeforeClass
	public static void validateModel() throws Exception {
		problems = modelValidationTest(createSpec("families", createPackageObject(createClassObject("The Smiths",
		                                                                                "Family",
		                                                                                createAttributeSlot("averageAge", "nearly 32")))));
	}
	
	@Test
	public void validationShouldReportOneProblem() {
		assertEquals(1, problems.size());
	}
	
	@Test
	public void reasonShouldBeIncorrectType() {
		assertEquals("Expected EFloat for: averageAge", problems.get(0).getReason());
	}
}
