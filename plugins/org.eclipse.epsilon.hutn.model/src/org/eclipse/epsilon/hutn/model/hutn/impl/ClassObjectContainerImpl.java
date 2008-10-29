/**
 * *******************************************************************************
 * Copyright (c) 2008 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Louis Rose - initial API and implementation
 * ******************************************************************************
 *
 * $Id$
 */
package org.eclipse.epsilon.hutn.model.hutn.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.epsilon.hutn.model.hutn.ClassObject;
import org.eclipse.epsilon.hutn.model.hutn.ClassObjectContainer;
import org.eclipse.epsilon.hutn.model.hutn.HutnPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Class Object Container</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.epsilon.hutn.model.hutn.impl.ClassObjectContainerImpl#getClassObjects <em>Class Objects</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class ClassObjectContainerImpl extends EObjectImpl implements ClassObjectContainer {
	/**
	 * The cached value of the '{@link #getClassObjects() <em>Class Objects</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getClassObjects()
	 * @generated
	 * @ordered
	 */
	protected EList<ClassObject> classObjects;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ClassObjectContainerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HutnPackage.Literals.CLASS_OBJECT_CONTAINER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<ClassObject> getClassObjects() {
		if (classObjects == null) {
			classObjects = new EObjectContainmentWithInverseEList<ClassObject>(ClassObject.class, this, HutnPackage.CLASS_OBJECT_CONTAINER__CLASS_OBJECTS, HutnPackage.CLASS_OBJECT__CONTAINER);
		}
		return classObjects;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HutnPackage.CLASS_OBJECT_CONTAINER__CLASS_OBJECTS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getClassObjects()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HutnPackage.CLASS_OBJECT_CONTAINER__CLASS_OBJECTS:
				return ((InternalEList<?>)getClassObjects()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case HutnPackage.CLASS_OBJECT_CONTAINER__CLASS_OBJECTS:
				return getClassObjects();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case HutnPackage.CLASS_OBJECT_CONTAINER__CLASS_OBJECTS:
				getClassObjects().clear();
				getClassObjects().addAll((Collection<? extends ClassObject>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case HutnPackage.CLASS_OBJECT_CONTAINER__CLASS_OBJECTS:
				getClassObjects().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case HutnPackage.CLASS_OBJECT_CONTAINER__CLASS_OBJECTS:
				return classObjects != null && !classObjects.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //ClassObjectContainerImpl
