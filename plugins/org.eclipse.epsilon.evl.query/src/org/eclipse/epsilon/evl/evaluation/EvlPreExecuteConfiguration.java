package org.eclipse.epsilon.evl.evaluation;

import java.io.File;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.emc.emf.SubEmfModelFactory;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.launch.EolRunConfiguration;
import org.eclipse.epsilon.erl.ErlModule;
import org.eclipse.epsilon.evl.EvlModule;
import org.eclipse.epsilon.evl.parse.EvlUnparser;
import org.eclipse.epsilon.evl.query.EvlRewritingHandler;
import org.eclipse.epsilon.evl.query.SubJdbcModelFactory;
import org.eclipse.epsilon.evl.staticanalyser.EvlStaticAnalyser;

public class EvlPreExecuteConfiguration extends EolRunConfiguration {
	IEolModule module;
	
	public EvlPreExecuteConfiguration(EolRunConfiguration other) {
		super(other);
		module = super.getModule();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void preExecute() throws Exception {
		super.preExecute();//	Resource resource = resourceSet.createResource(URI.createFileURI(new File(model).getAbsolutePath()));
//		String metamodel = "src/org/eclipse/epsilon/evl/query/psmToJavaTypeMapping.ecore";
//		ResourceSet resourceSet = new ResourceSetImpl();
//		ResourceSet ecoreResourceSet = new ResourceSetImpl();
//		ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
//		Resource ecoreResource = ecoreResourceSet.
//				createResource(URI.createFileURI(new File(metamodel).getAbsolutePath()));
//		try {
//			ecoreResource.load(null);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for (EObject o : ecoreResource.getContents()) {
//			EPackage ePackage = (EPackage) o;
//			resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
//			EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
//		}
		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			if (modelDeclaration.getDriverNameExpression().getName().equals("MySQL")) 
				module.getCompilationContext().setModelFactory(new SubJdbcModelFactory());

			if (modelDeclaration.getDriverNameExpression().getName().equals("EMF")) 
				module.getCompilationContext().setModelFactory(new SubEmfModelFactory());
		}
		
		if(module instanceof EvlModule)  {
			new EvlStaticAnalyser().validate(module);
        
		new EvlRewritingHandler().invokeRewriters(module);
		
		System.err.println(new EvlUnparser().unparse((ErlModule) module));
		}
		
			
		
	}
}