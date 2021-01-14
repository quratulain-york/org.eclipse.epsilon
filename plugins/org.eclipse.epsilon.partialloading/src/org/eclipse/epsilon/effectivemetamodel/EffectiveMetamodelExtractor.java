package org.eclipse.epsilon.effectivemetamodel;

import java.util.ArrayList;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.compile.m3.Attribute;
import org.eclipse.epsilon.eol.compile.m3.MetaClass;
import org.eclipse.epsilon.eol.compile.m3.Reference;
import org.eclipse.epsilon.eol.compile.m3.StructuralFeature;
import org.eclipse.epsilon.eol.dom.ExpressionStatement;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.StringLiteral;
import org.eclipse.epsilon.eol.exceptions.models.EolModelNotFoundException;
import org.eclipse.epsilon.eol.types.EolCollectionType;
import org.eclipse.epsilon.eol.types.EolModelElementType;
import org.eclipse.epsilon.evl.dom.ConstraintContext;

public class EffectiveMetamodelExtractor {

	public EffectiveMetamodelExtractor() {}
	
	public SmartEMF geteffectiveMetamodel(IEolModule module) {
		
		SmartEMF smartEMFModel = null;
		ArrayList<ModuleElement> children = new ArrayList<ModuleElement>();
		ArrayList<StructuralFeature> features = new ArrayList<StructuralFeature>();
		EffectiveType effectiveType;
		EolModelElementType target;
	
		try {
			smartEMFModel = (SmartEMF) module.getContext().getModelRepository().getModelByName(module.getCompilationContext().getModelDeclarations().get(0).getNameExpression().getName());
		} catch (EolModelNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		children.addAll(module.getChildren());
		
		int i = 0;
		while (!(children.isEmpty())) {

			ModuleElement MD = children.get(0);
			children.remove(MD);
			
			
			if ((!(MD.getChildren().isEmpty())))
				children.addAll(MD.getChildren());
			
			if (MD instanceof ConstraintContext && ((ConstraintContext)MD).getTypeExpression()!=null) {
				smartEMFModel.addToAllOfKind(((ConstraintContext)MD).getTypeExpression().getName());
			}
			
			if (MD instanceof OperationCallExpression) {

				OperationCallExpression operationCall = (OperationCallExpression) MD;
				
				if (!operationCall.isContextless()) {

					if (operationCall.getTargetExpression().getResolvedType() instanceof EolModelElementType) {

						target = (EolModelElementType) operationCall.getTargetExpression().getResolvedType();
					
						if (operationCall.getNameExpression().getName().equals("all")
								|| operationCall.getNameExpression().getName().equals("allOfKind")
								|| operationCall.getNameExpression().getName().equals("allInstances")) {

							smartEMFModel.addToAllOfKind(target.getTypeName());
							
							ExpressionStatement statement = new ExpressionStatement();
							statement.setExpression(new OperationCallExpression(new NameExpression(smartEMFModel.getName()), new NameExpression("addToAllOfKind"), new StringLiteral(target.getTypeName())));
							module.getMain().getStatements().add(i, statement);
							i++;
							
							if (smartEMFModel.allOfTypeContains(target.getTypeName())) {
								smartEMFModel.getAllOfType().remove(target.getTypeName());
							
								statement.setExpression(new OperationCallExpression(new NameExpression(smartEMFModel.getName()), new NameExpression("getAllOfType"),new NameExpression("remove"), new StringLiteral(target.getTypeName())));
								module.getMain().getStatements().add(i, statement);
								i++;
							}
							
							else if (smartEMFModel.typesContains(target.getTypeName())) {
								smartEMFModel.removeFromTypes(target.getTypeName());
								// Inject this statement ?
								statement.setExpression(new OperationCallExpression(new NameExpression(smartEMFModel.getName()), new NameExpression("removeFromTypes"), new StringLiteral(target.getTypeName())));
								module.getMain().getStatements().add(i, statement);
								i++;
							}

						} else if (operationCall.getNameExpression().getName().equals("allOfType")) {
							if (!smartEMFModel.allOfKindContains(target.getTypeName())
									&& !smartEMFModel.allOfTypeContains(target.getTypeName()))
							{
								smartEMFModel.addToAllOfType(target.getTypeName());
							
								ExpressionStatement statement = new ExpressionStatement();
								statement.setExpression(new OperationCallExpression(new NameExpression(smartEMFModel.getName()), new NameExpression("addToAllOfType"), new StringLiteral(target.getTypeName())));
								module.getMain().getStatements().add(i, statement);
								i++;
							}
						}
					}
				}
			}
			if (MD instanceof PropertyCallExpression) {

				PropertyCallExpression pro = (PropertyCallExpression) MD;
				if (pro.getTargetExpression().getResolvedType() instanceof EolModelElementType) {

					target = (EolModelElementType) pro.getTargetExpression().getResolvedType();
					
					if (pro.getNameExpression().getName().equals("all") ||pro.getNameExpression().getName().equals("allInstances")) {
						// Like AllofKind Algorithm
						
						smartEMFModel.addToAllOfKind(target.getTypeName());
						
						ExpressionStatement statement = new ExpressionStatement();
						statement.setExpression(new OperationCallExpression(new NameExpression(smartEMFModel.getName()), new NameExpression("addToAllOfKind"),new StringLiteral(target.getTypeName())));
						module.getMain().getStatements().add(i, statement);
						i++;
						
						if (smartEMFModel.allOfTypeContains(target.getTypeName())) {
							smartEMFModel.getAllOfType().remove(target.getTypeName());
							// Inject this statement ?
							statement.setExpression(new OperationCallExpression(new NameExpression(smartEMFModel.getName()), new NameExpression("getAllOfType"),new NameExpression("remove"), new StringLiteral(target.getTypeName())));
							module.getMain().getStatements().add(i, statement);
							i++;
						}
						if (smartEMFModel.typesContains(target.getTypeName())){
							smartEMFModel.removeFromTypes(target.getTypeName());
							// Inject this statement ?
							statement.setExpression(new OperationCallExpression(new NameExpression(smartEMFModel.getName()), new NameExpression("removeFromTypes"), new StringLiteral(target.getTypeName())));
							module.getMain().getStatements().add(i, statement);
							i++;
						}
					}
					// not already under the EM's types, allOfKind or allOfType references
					else {
						effectiveType = new EffectiveType(target.getTypeName());
						effectiveType.setEffectiveMetamodel(smartEMFModel);
						
						if (smartEMFModel.allOfKindContains(effectiveType.getName()))
							effectiveType = smartEMFModel.getFromAllOfKind(effectiveType.getName());
						else if (smartEMFModel.allOfTypeContains(effectiveType.getName()))
							effectiveType = smartEMFModel.getFromAllOfType(effectiveType.getName());
						else if (smartEMFModel.typesContains(effectiveType.getName()))
							effectiveType = smartEMFModel.getFromTypes(effectiveType.getName());
						else
						{
							// add target.getTypeName() under EM's types reference;
							effectiveType = smartEMFModel.addToTypes(effectiveType.getName());
							
							ExpressionStatement statement = new ExpressionStatement();
							statement.setExpression(new OperationCallExpression(new NameExpression(smartEMFModel.getName()), new NameExpression("addToTypes"), new StringLiteral(effectiveType.getName())));
							module.getMain().getStatements().add(i, statement);
							i++;
						}
						if (!target.getMetaClass().getAllStructuralFeatures().isEmpty()) {
							features.addAll(target.getMetaClass().getAllStructuralFeatures());
						}

						for (MetaClass metaclass : target.getMetaClass().getSuperTypes()) {
							features.addAll(metaclass.getAllStructuralFeatures());
						}

						for (StructuralFeature sf : features) {
							if (sf instanceof Attribute && sf.getName().equals(pro.getNameExpression().getName())) {
								effectiveType.addToAttributes(sf.getName());
								
								ExpressionStatement statement = new ExpressionStatement();
								statement.setExpression(new OperationCallExpression(new NameExpression(smartEMFModel.getName()), new NameExpression("addAttributeToEffectiveType"), new StringLiteral(effectiveType.getName()) ,new StringLiteral(sf.getName())));
								module.getMain().getStatements().add(i, statement);
								i++;
								
								break;
							} else if (sf instanceof Reference
									&& sf.getName().equals(pro.getNameExpression().getName())) {
								effectiveType.addToReferences(sf.getName());
								
								ExpressionStatement statement = new ExpressionStatement();
								statement.setExpression(new OperationCallExpression(new NameExpression(smartEMFModel.getName()), new NameExpression("addReferenceToEffectiveType"), new StringLiteral(effectiveType.getName()) ,new StringLiteral(sf.getName())));
								module.getMain().getStatements().add(i, statement);
								i++;
								break;
							}
						}
					}
				} else if (pro.getTargetExpression().getResolvedType() instanceof EolCollectionType) {

					if (((EolCollectionType) pro.getTargetExpression().getResolvedType())
							.getContentType() instanceof EolModelElementType) {

						target = (EolModelElementType) ((EolCollectionType) pro.getTargetExpression().getResolvedType())
								.getContentType();
						
						effectiveType = new EffectiveType(target.getTypeName());

						if (smartEMFModel.allOfKindContains(effectiveType.getName()))
							effectiveType = smartEMFModel.getFromAllOfKind(effectiveType.getName());
						else if (smartEMFModel.allOfTypeContains(effectiveType.getName()))
							effectiveType = smartEMFModel.getFromAllOfType(effectiveType.getName());
						
						else {
							// add elementName under EM's types reference;
							effectiveType = smartEMFModel.addToTypes(effectiveType.getName());
						
							ExpressionStatement statement = new ExpressionStatement();
							statement.setExpression(new OperationCallExpression(new NameExpression(smartEMFModel.getName()), new NameExpression("addToTypes"), new StringLiteral(effectiveType.getName())));
							module.getMain().getStatements().add(i, statement);
							i++;
						}
						if (!target.getMetaClass().getAllStructuralFeatures().isEmpty()) {
							features.addAll(target.getMetaClass().getAllStructuralFeatures());
						}
						for (MetaClass metaclass : target.getMetaClass().getSuperTypes()) {
							features.addAll(metaclass.getAllStructuralFeatures());

						}

						for (StructuralFeature sf : features) {
							if (sf instanceof Attribute
									&& sf.getName().equals(pro.getNameExpression().getName())) {
								effectiveType.addToAttributes(sf.getName());
								
								ExpressionStatement statement = new ExpressionStatement();
								statement.setExpression(new OperationCallExpression(new NameExpression(smartEMFModel.getName()), new NameExpression("addAttributeToEffectiveType"),new StringLiteral(effectiveType.getName()), new StringLiteral(sf.getName())));
								module.getMain().getStatements().add(i, statement);
								i++;
								
								break;
							} else if (sf instanceof Reference
									&& sf.getName().equals(pro.getNameExpression().getName())) {
								effectiveType.addToReferences(sf.getName());
								
								ExpressionStatement statement = new ExpressionStatement();
								statement.setExpression(new OperationCallExpression(new NameExpression(smartEMFModel.getName()), new NameExpression("addReferenceToEffectiveType"), new StringLiteral(effectiveType.getName()) ,new StringLiteral(sf.getName())));
								module.getMain().getStatements().add(i, statement);
								i++;
								break;
							}
						}
					}
				}
			}
		}
		return smartEMFModel;
	}
}
