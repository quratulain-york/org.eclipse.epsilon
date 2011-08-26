/*******************************************************************************
 * Copyright (c) 2011 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Louis Rose - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.egl.test.acceptance.traceability;

import org.eclipse.epsilon.egl.test.acceptance.traceability.misc.SubtemplateShouldContributeToTraceOfParent;
import org.eclipse.epsilon.egl.test.acceptance.traceability.misc.TraceShouldNotContainMoreThanOneFeatureAccessPerTextLocation;
import org.eclipse.epsilon.egl.test.acceptance.traceability.sections.DynamicOutputSectionsContributeToTrace;
import org.eclipse.epsilon.egl.test.acceptance.traceability.sections.StaticSectionsDoNotContributeToTrace;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({DynamicOutputSectionsContributeToTrace.class,
               StaticSectionsDoNotContributeToTrace.class,
               SubtemplateShouldContributeToTraceOfParent.class,
               TraceShouldNotContainMoreThanOneFeatureAccessPerTextLocation.class })
public class EglFineGrainedTraceabilityAcceptanceTestSuite {

}
