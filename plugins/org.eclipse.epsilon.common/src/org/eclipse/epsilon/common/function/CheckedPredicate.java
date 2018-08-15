/*********************************************************************
 * Copyright (c) 2018 The University of York.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.epsilon.common.function;

import java.util.function.Predicate;

@FunctionalInterface
public interface CheckedPredicate<E extends Exception, T> extends Predicate<T> {
	
	@Override
	default boolean test(T t) throws RuntimeException {
		try {
			return testThrows(t);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	boolean testThrows(T t) throws E;
	
}
