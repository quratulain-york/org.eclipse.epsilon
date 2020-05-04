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
 * $Id$
 */
package org.eclipse.epsilon.hutn.test.models;

import java.io.File;
import java.io.IOException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.epsilon.common.util.FileUtil;

public abstract class BankAccounts {

	private BankAccounts() {}
	
	public static File getMetaModelFile() throws IOException {
		return FileUtil.getFileURL("BankAccounts.ecore", BankAccounts.class);
	}
	
	public static URI getMetaModelUri() throws IOException {
		return URI.createFileURI(getMetaModelFile().getAbsolutePath());
	}
}
