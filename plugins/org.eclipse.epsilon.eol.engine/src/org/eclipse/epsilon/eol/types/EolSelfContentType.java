package org.eclipse.epsilon.eol.types;

import java.util.List;

import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;

public class EolSelfContentType extends EolType {

public static EolSelfContentType Instance  = new EolSelfContentType();
	
	public EolSelfContentType()  {
		super();
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "EolSelfContentType";
	}

	@Override
	public boolean isType(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isKind(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EolType getParentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object createInstance() throws EolRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object createInstance(List<Object> parameters) throws EolRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

}