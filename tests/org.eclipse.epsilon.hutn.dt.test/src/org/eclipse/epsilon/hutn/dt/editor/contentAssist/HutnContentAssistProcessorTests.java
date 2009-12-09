/*******************************************************************************
 * Copyright (c) 2009 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Louis Rose - initial API and implementation
 ******************************************************************************
 *
 * $Id$
 */
package org.eclipse.epsilon.hutn.dt.editor.contentAssist;

import static org.eclipse.epsilon.hutn.dt.editor.contentAssist.CompletionProposalMatcher.completionProposal;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsArrayContainingInOrder.arrayContaining;
import static org.junit.Assert.assertThat;

import org.eclipse.epsilon.hutn.test.model.HutnTestWithFamiliesMetaModel;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class HutnContentAssistProcessorTests extends HutnTestWithFamiliesMetaModel {

	@Test
	public void shouldSuggestConcreteClassNamesWhenAtPackageLevel() {
		final String text = "@Spec {"                    +
		                    "	Metamodel {"             +
		                    "		nsUri: \"families\"" + 
		                    "	}"                       +
		                    "}"                          +
		                    "families { ";
		
		assertThat(new HutnContentAssistProcessor().computeCompletionProposals(text),
		           is(arrayContaining(completionProposal("Bike"),
		                              completionProposal("District"),
		                              completionProposal("Dog"),
		                              completionProposal("Family"),
		                              completionProposal("Model"),
		                              completionProposal("Person"),
		                              completionProposal("Pet")
		                              )));
	}
}
