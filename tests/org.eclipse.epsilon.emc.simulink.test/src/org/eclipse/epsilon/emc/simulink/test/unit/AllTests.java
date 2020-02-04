/*********************************************************************
* Copyright (c) 2008 The University of York.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.epsilon.emc.simulink.test.unit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CRUDTests.class, ExampleTests.class, InheritanceTests.class,
		OperationTests.class, SimulateTests.class, PropertyTests.class,
		SimulinkPrimitiveTypeAndStatementTest.class, LoadModelTest.class, MatlabEngineTests.class })
public class AllTests {

}
