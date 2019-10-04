/*********************************************************************
 * Copyright (c) 2018 The University of York.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.epsilon.ecl.execute.context.concurrent;

import org.eclipse.epsilon.common.module.IModule;
import org.eclipse.epsilon.ecl.IEclModule;
import org.eclipse.epsilon.ecl.execute.context.IEclContext;
import org.eclipse.epsilon.ecl.trace.MatchTrace;
import org.eclipse.epsilon.eol.exceptions.concurrent.EolNestedParallelismException;
import org.eclipse.epsilon.erl.execute.context.concurrent.ErlContextParallel;

/**
 * 
 * @author Sina Madani
 * @since 1.6
 */
public class EclContextParallel extends ErlContextParallel implements IEclContextParallel {

	protected MatchTrace matchTrace;
	protected MatchTrace tempMatchTrace;
	
	public EclContextParallel() {
		this(0);
	}

	public EclContextParallel(int parallelism) {
		super(parallelism);
	}

	public EclContextParallel(IEclContext other) {
		super(other);
		this.matchTrace = new MatchTrace(other.getMatchTrace());
		this.tempMatchTrace = new MatchTrace(other.getMatchTrace());
	}
	
	public static IEclContextParallel convertToParallel(IEclContext context) throws EolNestedParallelismException {
		if (context instanceof IEclContextParallel) return (IEclContextParallel) context;
		return new EclContextParallel(context);
	}
	
	@Override
	protected void initMainThreadStructures() {
		super.initMainThreadStructures();
		matchTrace = new MatchTrace(true);
		tempMatchTrace = new MatchTrace(true);
	}

	@Override
	public void setMatchTrace(MatchTrace matchTrace) {
		this.matchTrace = matchTrace;
	}
	
	@Override
	public MatchTrace getMatchTrace() {
		return matchTrace;
	}
	
	@Override
	public MatchTrace getTempMatchTrace() {
		return tempMatchTrace;
	}
	
	@Override
	public IEclModule getModule() {
		return (IEclModule) super.getModule();
	}
	
	@Override
	public void setModule(IModule module) {
		if (module instanceof IEclModule) {
			super.setModule(module);
		}
	}
}
