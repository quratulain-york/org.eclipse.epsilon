package org.eclipse.epsilon.eol.staticanalyser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.epsilon.common.dt.editor.AbstractModuleEditor;
import org.eclipse.epsilon.common.module.IModule;
import org.eclipse.epsilon.common.module.IModuleValidator;
import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.common.module.ModuleMarker;
import org.eclipse.epsilon.common.module.ModuleMarker.Severity;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.common.util.StringUtil;
import org.eclipse.epsilon.eol.BuiltinEolModule;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.compile.context.EolCompilationContext;
import org.eclipse.epsilon.eol.compile.m3.MetaClass;
import org.eclipse.epsilon.eol.compile.m3.StructuralFeature;
import org.eclipse.epsilon.eol.dom.AbortStatement;
import org.eclipse.epsilon.eol.dom.AndOperatorExpression;
import org.eclipse.epsilon.eol.dom.AnnotationBlock;
import org.eclipse.epsilon.eol.dom.AssignmentStatement;
import org.eclipse.epsilon.eol.dom.BooleanLiteral;
import org.eclipse.epsilon.eol.dom.BreakStatement;
import org.eclipse.epsilon.eol.dom.Case;
import org.eclipse.epsilon.eol.dom.CollectionLiteralExpression;
import org.eclipse.epsilon.eol.dom.ComplexOperationCallExpression;
import org.eclipse.epsilon.eol.dom.ContinueStatement;
import org.eclipse.epsilon.eol.dom.DeleteStatement;
import org.eclipse.epsilon.eol.dom.DivOperatorExpression;
import org.eclipse.epsilon.eol.dom.DoubleEqualsOperatorExpression;
import org.eclipse.epsilon.eol.dom.ElvisOperatorExpression;
import org.eclipse.epsilon.eol.dom.EnumerationLiteralExpression;
import org.eclipse.epsilon.eol.dom.EqualsOperatorExpression;
import org.eclipse.epsilon.eol.dom.ExecutableAnnotation;
import org.eclipse.epsilon.eol.dom.ExecutableBlock;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.ExpressionInBrackets;
import org.eclipse.epsilon.eol.dom.ExpressionStatement;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.ForStatement;
import org.eclipse.epsilon.eol.dom.GreaterEqualOperatorExpression;
import org.eclipse.epsilon.eol.dom.GreaterThanOperatorExpression;
import org.eclipse.epsilon.eol.dom.ICompilableModuleElement;
import org.eclipse.epsilon.eol.dom.IEolVisitor;
import org.eclipse.epsilon.eol.dom.IfStatement;
import org.eclipse.epsilon.eol.dom.ImpliesOperatorExpression;
import org.eclipse.epsilon.eol.dom.Import;
import org.eclipse.epsilon.eol.dom.IntegerLiteral;
import org.eclipse.epsilon.eol.dom.ItemSelectorExpression;
import org.eclipse.epsilon.eol.dom.LessEqualOperatorExpression;
import org.eclipse.epsilon.eol.dom.LessThanOperatorExpression;
import org.eclipse.epsilon.eol.dom.MapLiteralExpression;
import org.eclipse.epsilon.eol.dom.MinusOperatorExpression;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.dom.ModelDeclarationParameter;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.NegativeOperatorExpression;
import org.eclipse.epsilon.eol.dom.NewInstanceExpression;
import org.eclipse.epsilon.eol.dom.NotEqualsOperatorExpression;
import org.eclipse.epsilon.eol.dom.NotOperatorExpression;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.OperationList;
import org.eclipse.epsilon.eol.dom.OperatorExpression;
import org.eclipse.epsilon.eol.dom.OrOperatorExpression;
import org.eclipse.epsilon.eol.dom.Parameter;
import org.eclipse.epsilon.eol.dom.PlusOperatorExpression;
import org.eclipse.epsilon.eol.dom.PostfixOperatorExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.RealLiteral;
import org.eclipse.epsilon.eol.dom.ReturnStatement;
import org.eclipse.epsilon.eol.dom.SimpleAnnotation;
import org.eclipse.epsilon.eol.dom.StatementBlock;
import org.eclipse.epsilon.eol.dom.StringLiteral;
import org.eclipse.epsilon.eol.dom.SwitchStatement;
import org.eclipse.epsilon.eol.dom.TernaryExpression;
import org.eclipse.epsilon.eol.dom.ThrowStatement;
import org.eclipse.epsilon.eol.dom.TimesOperatorExpression;
import org.eclipse.epsilon.eol.dom.TransactionStatement;
import org.eclipse.epsilon.eol.dom.TypeExpression;
import org.eclipse.epsilon.eol.dom.VariableDeclaration;
import org.eclipse.epsilon.eol.dom.WhileStatement;
import org.eclipse.epsilon.eol.dom.XorOperatorExpression;
import org.eclipse.epsilon.eol.execute.context.FrameStack;
import org.eclipse.epsilon.eol.execute.context.FrameType;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.types.EolAnyType;
import org.eclipse.epsilon.eol.types.EolCollectionType;
import org.eclipse.epsilon.eol.types.EolMapType;
import org.eclipse.epsilon.eol.types.EolModelElementType;
import org.eclipse.epsilon.eol.types.EolNoType;
import org.eclipse.epsilon.eol.types.EolPrimitiveType;
import org.eclipse.epsilon.eol.types.EolSelf;
import org.eclipse.epsilon.eol.types.EolSelfCollectionType;
import org.eclipse.epsilon.eol.types.EolSelfContentType;
import org.eclipse.epsilon.eol.types.EolSelfExpressionType;
import org.eclipse.epsilon.eol.types.EolType;
import org.eclipse.epsilon.evl.EvlModule;

public class EolStaticAnalyser implements IModuleValidator, IEolVisitor {

	protected List<ModuleMarker> errors;
	protected EolModule module;
	protected BuiltinEolModule builtinModule = new BuiltinEolModule();
	protected EolCompilationContext context;
	HashMap<Operation, Boolean> returnFlags = new HashMap<>();
	//For compiling user and builtin operations
	HashMap<OperationCallExpression, ArrayList<Operation>> operations = new HashMap<>(); //keeping all matched operations with same name
	HashMap<OperationCallExpression, ArrayList<Operation>> matchedOperations = new HashMap<>(); //keeping all matched operations with same contextType and parameters
	HashMap<OperationCallExpression, ArrayList<EolType>> matchedReturnType = new HashMap<>(); //keeping returnTypes of matched operations
	HashMap<OperationCallExpression, Boolean> matched = new HashMap<>(); //finding one perfect match, in doesn't change for every missmatch
	CallGraphGenerator callGraph;
	
	public EolStaticAnalyser() {
	}

	@Override
	public void visit(AbortStatement abortStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AndOperatorExpression andOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) andOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(DeleteStatement deleteStatement) {
		deleteStatement.getExpression().accept(this);
	}

	@Override
	public void visit(AnnotationBlock annotationBlock) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(AssignmentStatement assignmentStatement) {

		Expression targetExpression = assignmentStatement.getTargetExpression();
		Expression valueExpression = assignmentStatement.getValueExpression();

		targetExpression.accept(this);
		valueExpression.accept(this);

		EolType targetType = targetExpression.getResolvedType();
		EolType valueType = valueExpression.getResolvedType();

		if (targetType instanceof EolModelElementType && ((EolModelElementType) targetType).getMetaClass() != null)
			targetType = new EolModelElementType(((EolModelElementType) targetType).getMetaClass());
		if (valueType instanceof EolModelElementType && ((EolModelElementType) valueType).getMetaClass() != null)
			valueType = new EolModelElementType(((EolModelElementType) valueType).getMetaClass());

		if (!(isCompatible(targetType, valueType))) {
			if (canBeCompatible(targetType, valueType))
				createTypeCompatibilityWarning(targetExpression, valueExpression);
			else
				createTypeCompatibilityError(targetExpression, valueExpression);
		}
	}

	@Override
	public void visit(BooleanLiteral booleanLiteral) {
		booleanLiteral.setResolvedType(EolPrimitiveType.Boolean);

	}

	@Override
	public void visit(BreakStatement breakStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Case case_) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CollectionLiteralExpression<?> collectionLiteralExpression) {
		if(!collectionLiteralExpression.getParameterExpressions().isEmpty()) {
		collectionLiteralExpression.getParameterExpressions().get(0).accept(this);
		collectionLiteralExpression
				.setResolvedType(new EolCollectionType(collectionLiteralExpression.getCollectionType(),
						collectionLiteralExpression.getParameterExpressions().get(0).getResolvedType()));
		}
	}

	@Override
	public void visit(ComplexOperationCallExpression complexOperationCallExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ContinueStatement continueStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DivOperatorExpression divOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) divOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(DoubleEqualsOperatorExpression doubleEqualsOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) doubleEqualsOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(ElvisOperatorExpression elvisOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) elvisOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(EnumerationLiteralExpression enumerationLiteralExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(EqualsOperatorExpression equalsOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) equalsOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(ExecutableAnnotation executableAnnotation) {
		executableAnnotation.getExpression().accept(this);
		// TODO body.compile(context);
	}

	@Override
	public void visit(ExecutableBlock<?> executableBlock) {
		 ICompilableModuleElement body = (ICompilableModuleElement)
		 executableBlock.getBody();
		 body.accept(this);
		// Should we add add accept method?
	}

	@Override
	public void visit(ExpressionInBrackets expressionInBrackets) {
		expressionInBrackets.getExpression().accept(this);
		expressionInBrackets.setResolvedType(expressionInBrackets.getExpression().getResolvedType());
	}

	@Override
	public void visit(ExpressionStatement expressionStatement) {
		expressionStatement.getExpression().accept(this);

	}

	@Override
	public void visit(FirstOrderOperationCallExpression firstOrderOperationCallExpression) {
		OperationList builtinOperations = new OperationList();
		Expression targetExpression = firstOrderOperationCallExpression.getTargetExpression();
		EolType contextType = null;
		String name = firstOrderOperationCallExpression.getNameExpression().getName();

		for (Operation op : ((EolModule) module).getOperations())
			if (op.getAnnotation("firstorder") != null)
				builtinOperations.add(op);

		targetExpression.accept(this);
		// targetExpression.compile(context);

		if (targetExpression.getResolvedType() instanceof EolCollectionType) {
			contextType = ((EolCollectionType) targetExpression.getResolvedType()).getContentType();
		} else if (targetExpression.getResolvedType() == EolAnyType.Instance) {
			contextType = targetExpression.getResolvedType();
		}

		if (name.startsWith("sequential"))
			name = name.substring(10);
		else if (name.startsWith("parallel"))
			name = name.substring(8);

		if (contextType != null) {
			context.getFrameStack().enterLocal(FrameType.UNPROTECTED, firstOrderOperationCallExpression);
			Parameter parameter = firstOrderOperationCallExpression.getParameters().get(0);
			// parameter.accept(this);
			visit(parameter, false);
			// parameter.compile(context, false);

			if (parameter.isExplicitlyTyped()) {
				// TODO: Check that the type of the parameter is a subtype of the type of the
				// collection
				contextType = parameter.getCompilationType();
				EolType target = ((EolCollectionType) targetExpression.getResolvedType()).getContentType();
				EolType param = contextType;
				while (!(param.equals(target))) {
					param = param.getParentType();
					if (param instanceof EolAnyType) {
						// context.addErrorMarker(parameter, );
						errors.add(new ModuleMarker(parameter, "The parameter must be instance of " + target.getName(),
								Severity.Error));

						break;
					}
				}
			} else {
				// context.getFrameStack().put(parameter.getName(), contextType);
				if (targetExpression.getResolvedType() instanceof EolCollectionType) {

					parameter.setTypeExpression(new TypeExpression(
							((EolCollectionType) targetExpression.getResolvedType()).getContentType().getName()));

					parameter.getTypeExpression()
							.setResolvedType(((EolCollectionType) targetExpression.getResolvedType()).getContentType());
				} else {
					parameter.setTypeExpression(new TypeExpression("Any"));
					parameter.getTypeExpression().setResolvedType(EolAnyType.Instance);
				}
				parameter.setType(parameter.getTypeExpression().getResolvedType());
				parameter.getTypeExpression().setName(parameter.getTypeExpression().getResolvedType().toString());
				contextType = parameter.getType();
			}
			parameter.pushToStack(context);

			Expression expression = firstOrderOperationCallExpression.getExpressions().get(0);
			expression.accept(this);

			context.getFrameStack().leaveLocal(firstOrderOperationCallExpression);

			if (StringUtil.isOneOf(name, "select", "reject", "rejectOne", "closure", "sortBy")) {
				firstOrderOperationCallExpression.setResolvedType(new EolCollectionType("Collection", contextType));
			} else if (name.equals("selectOne")) {
				firstOrderOperationCallExpression.setResolvedType(contextType);
			} else if (name.equals("collect")) {
				Operation firstOrder = builtinOperations.getOperation(name);
				firstOrder.getReturnTypeExpression().accept(this);
				firstOrder.getReturnTypeExpression().setResolvedType(targetExpression.getResolvedType());

				if (!(firstOrder.getReturnTypeExpression().getResolvedType() instanceof EolAnyType))
					((EolCollectionType) firstOrder.getReturnTypeExpression().getResolvedType()).setContentType(
							firstOrderOperationCallExpression.getExpressions().get(0).getResolvedType());

				firstOrderOperationCallExpression
						.setResolvedType(new EolCollectionType(targetExpression.getResolvedType().getName(),
								firstOrderOperationCallExpression.getExpressions().get(0).getResolvedType()));

			} else if (StringUtil.isOneOf(name, "exists", "forAll", "one", "none", "nMatch")) {
				firstOrderOperationCallExpression.setResolvedType(EolPrimitiveType.Boolean);
			} else if (name.equals("aggregate")) {
				if (firstOrderOperationCallExpression.getExpressions().size() == 2) {
					Expression valueExpression = firstOrderOperationCallExpression.getExpressions().get(1);
					valueExpression.accept(this);
					// valueExpression.compile(context);
					firstOrderOperationCallExpression.setResolvedType(
							new EolMapType(expression.getResolvedType(), valueExpression.getResolvedType()));
				} else {
					errors.add(new ModuleMarker(firstOrderOperationCallExpression.getNameExpression(),
							"Aggregate requires a key and a value expression", Severity.Error));
					// context.addErrorMarker(firstOrderOperationCallExpression.getNameExpression(),
					// "Aggregate requires a key and a value expression");
				}
			} else if (name.equals("mapBy")) {
				firstOrderOperationCallExpression.setResolvedType(
						new EolMapType(expression.getResolvedType(), new EolCollectionType("Sequence", contextType)));
			} else if (name.equals("sortBy")) {
				firstOrderOperationCallExpression.setResolvedType(new EolCollectionType("Sequence", contextType));
			}
			if (StringUtil.isOneOf(name, "select", "selectOne", "reject", "rejectOne", "exists", "one", "none",
					"forAll", "closure") && expression.getResolvedType().isNot(EolPrimitiveType.Boolean)) {

				errors.add(new ModuleMarker(expression, "Expression should return a Boolean but returns a "
						+ expression.getResolvedType().getName() + " instead", Severity.Error));
			}
		} else {
			errors.add(new ModuleMarker(firstOrderOperationCallExpression.getNameExpression(),
					"Operation " + name + " only applies to collections", Severity.Error));
		}
	}

	@Override
	public void visit(ForStatement forStatement) {

		forStatement.getIteratedExpression().accept(this);
		context.getFrameStack().enterLocal(FrameType.UNPROTECTED, forStatement.getBodyStatementBlock(),
				new Variable("loopCount", EolPrimitiveType.Integer), new Variable("hasMore", EolPrimitiveType.Boolean));

		forStatement.getIteratorParameter().accept(this);
		forStatement.getBodyStatementBlock().accept(this);
		context.getFrameStack().leaveLocal(forStatement.getBodyStatementBlock());

		if (forStatement.getIteratedExpression().hasResolvedType()
				&& !(forStatement.getIteratedExpression().getResolvedType() instanceof EolCollectionType)) {
			errors.add(new ModuleMarker(forStatement.getIteratedExpression(),
					"Collection expected instead of " + forStatement.getIteratedExpression().getResolvedType(),
					Severity.Error));
		}
	}

	@Override
	public void visit(GreaterEqualOperatorExpression greaterEqualOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) greaterEqualOperatorExpression;
		visitOperatorExpression(operatorExpression);

	}

	@Override
	public void visit(GreaterThanOperatorExpression greaterThanOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) greaterThanOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(IfStatement ifStatement) {

		Expression conditionExpression = ifStatement.getConditionExpression();
		StatementBlock thenStatementBlock = ifStatement.getThenStatementBlock();
		StatementBlock elseStatementBlock = ifStatement.getElseStatementBlock();

		conditionExpression.accept(this);
		FrameStack frameStack = context.getFrameStack();
		frameStack.enterLocal(FrameType.UNPROTECTED, thenStatementBlock);
		thenStatementBlock.accept(this);
		frameStack.leaveLocal(thenStatementBlock);

		if (elseStatementBlock != null) {
			frameStack.enterLocal(FrameType.UNPROTECTED, elseStatementBlock);
			elseStatementBlock.accept(this);
			context.getFrameStack().leaveLocal(elseStatementBlock);
		}

		if (conditionExpression.hasResolvedType()
				&& conditionExpression.getResolvedType() != EolPrimitiveType.Boolean) {
			errors.add(new ModuleMarker(conditionExpression, "Condition must be a Boolean", Severity.Error));
		}

	}

	@Override
	public void visit(ImpliesOperatorExpression impliesOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) impliesOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(Import import_) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IntegerLiteral integerLiteral) {
		integerLiteral.setResolvedType(EolPrimitiveType.Integer);

	}

	@Override
	public void visit(ItemSelectorExpression itemSelectorExpression) {

		itemSelectorExpression.getTargetExpression().accept(this);
		itemSelectorExpression.getIndexExpression().accept(this);

		EolType targetExpressionType = itemSelectorExpression.getTargetExpression().getResolvedType();
		if (targetExpressionType != EolAnyType.Instance) {
			if (targetExpressionType instanceof EolCollectionType) {
				itemSelectorExpression.setResolvedType(((EolCollectionType) targetExpressionType).getContentType());
			} else {
				errors.add(new ModuleMarker(itemSelectorExpression.getIndexExpression(),
						"[...] only applies to collections", Severity.Error));
			}
		}

	}

	@Override
	public void visit(LessEqualOperatorExpression lessEqualOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) lessEqualOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(LessThanOperatorExpression lessThanOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) lessThanOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(MapLiteralExpression<?, ?> mapLiteralExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MinusOperatorExpression minusOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) minusOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(ModelDeclaration modelDeclaration) {

		if (context.getModelFactory() == null)
			return;
		modelDeclaration.setModel(context.getModelFactory().createModel(modelDeclaration.getDriverNameExpression().getName()));
		if (modelDeclaration.getModel() == null) {
			context.addErrorMarker(modelDeclaration.getDriverNameExpression(),
					"Unknown type of model: " + modelDeclaration.getDriverNameExpression().getName());
		} else {
			StringProperties stringProperties = new StringProperties();
			for (ModelDeclarationParameter parameter : modelDeclaration.getModelDeclarationParameters()) {
				stringProperties.put(parameter.getKey(), parameter.getValue());
			}
			modelDeclaration.setMetamodel(
					modelDeclaration.getModel().getMetamodel(stringProperties, context.getRelativePathResolver()));
			if (modelDeclaration.getMetamodel() != null) {
				for (String error : modelDeclaration.getMetamodel().getErrors()) {
					errors.add(new ModuleMarker(modelDeclaration, error, Severity.Error));
				}
				for (String warning : modelDeclaration.getMetamodel().getWarnings()) {
					errors.add(new ModuleMarker(modelDeclaration, warning, Severity.Warning));
				}
			}
		}

	}

	@Override
	public void visit(ModelDeclarationParameter modelDeclarationParameter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NameExpression nameExpression) {

		EolModelElementType modelElementType;
		Variable variable = context.getFrameStack().get(nameExpression.getName());
		if (variable != null) {
			nameExpression.setResolvedType(variable.getType());
		} else {
			modelElementType = context.getModelElementType(nameExpression.getName());
			if (modelElementType != null) {
				nameExpression.setResolvedType(modelElementType);
				nameExpression.setTypeName(true);
				if (modelElementType.getMetaClass() == null && !context.getModelDeclarations().isEmpty()) {

					errors.add(new ModuleMarker(nameExpression, "Unknown type " + nameExpression.getName(),
							Severity.Error));
				}

			} else {

				errors.add(new ModuleMarker(nameExpression, "Undefined variable or type " + nameExpression.getName(),
						Severity.Error));
			}
		}
	}

	@Override
	public void visit(NegativeOperatorExpression negativeOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) negativeOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(NewInstanceExpression newInstanceExpression) {

		newInstanceExpression.getTypeExpression().accept(this);
		for (Expression parameterExpression : newInstanceExpression.getParameterExpressions()) {
			parameterExpression.compile(context);
		}
		newInstanceExpression.setResolvedType(newInstanceExpression.getTypeExpression().getResolvedType());
	}

	@Override
	public void visit(NotEqualsOperatorExpression notEqualsOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) notEqualsOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(NotOperatorExpression notOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) notOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(Operation operation) {
		TypeExpression contextTypeExpression = operation.getContextTypeExpression();
		EolType contextType = EolNoType.Instance;
		TypeExpression returnTypeExpression = operation.getReturnTypeExpression();
		setReturnFlag(operation, false);
		
		if (contextTypeExpression != null) {
			contextTypeExpression.accept(this);
			contextType = contextTypeExpression.getResolvedType();
		}

		context.getFrameStack().enterLocal(FrameType.PROTECTED, operation,
				new Variable("self", contextType));
		for (Parameter parameter : operation.getFormalParameters()) {
			parameter.accept(this);
		}
		operation.getBody().accept(this);

		if (getReturnFlag(operation)==false && returnTypeExpression != null)
			errors.add(new ModuleMarker(returnTypeExpression,
					"This operation should return " + returnTypeExpression.getName(), Severity.Error));
		context.getFrameStack().leaveLocal(operation);

	}

	@Override
	public void visit(OperationCallExpression operationCallExpression) {
		if(operationCallExpression.getName().equals("createFigure"))
			System.err.println();
		OperationList allOperations = ((EolModule) module).getOperations();
		Expression targetExpression = operationCallExpression.getTargetExpression();
		List<Expression> parameterExpressions = operationCallExpression.getParameterExpressions();
		NameExpression nameExpression = operationCallExpression.getNameExpression();
		setOperations(operationCallExpression, new ArrayList<Operation>()); //Assigning an empty array to OperationCallExpression
		setMatchedOperations(operationCallExpression, new ArrayList<Operation>()); //Assigning an empty array to OperationCallExpression
		setMatchedReturnType(operationCallExpression, new ArrayList<EolType>()); //Assigning an empty array to OperationCallExpression
		setMatched(operationCallExpression, false);// for find at least one perfect match/ It doesn't change for every mismatch
		
		// because one match is enough
		int errorCode = 0; // 1 = mismatch Target 2=number of parameters mismatch 3=parameters type
		// mismatch 4 =undefined Operation // 5 = No-type as target // 6 = No-type as
		// parameter
		EolType contextType = EolAnyType.Instance;
		
		if (targetExpression != null) {
			targetExpression.accept(this);
			operationCallExpression.setContextless(false);
		} else
			operationCallExpression.setContextless(true);
		for (Expression parameterExpression : parameterExpressions) {
			parameterExpression.accept(this);
		}
		boolean operations_contextless;
		boolean successMatch = false; // for a perfect match -> we should keep it for every closest matched
										// possibility as true
		boolean goForward = false; // for keep checking forward

		for (int i = 0; i < allOperations.size(); i++) {

			if (allOperations.get(i).getContextTypeExpression() != null) {
				operations_contextless = false;
			} else {
				operations_contextless = true;
			}
			if (nameExpression.getName().equals(allOperations.get(i).getName())
					&& (operationCallExpression.isContextless() == operations_contextless)) {
				getOperations(operationCallExpression).add(allOperations.get(i));
			}

		}
		if (getOperations(operationCallExpression).size() == 0) {
			errorCode = 4;
		}

		List<Parameter> reqParams = null;
		EolType contentType, collectionType, expType;
		
		for (Operation op : getOperations(operationCallExpression)) {

			/*
			 * if (op.getName().equals("getAllSuitableContainmentReferences"))
			 * op.compile(context); else if (op.getName().equals("getNodes"))
			 * op.compile(context); else if (op.getName().equals("getLinks"))
			 * op.compile(context);
			 */
			successMatch = false;

			reqParams = op.getFormalParameters();
			if (op.getReturnTypeExpression() != null) {
				op.getReturnTypeExpression().accept(this);

				if (op.getReturnTypeExpression().getResolvedType().toString().equals("EolSelf")) {
					op.getReturnTypeExpression().setResolvedType(targetExpression.getResolvedType());
				}

				if (op.getReturnTypeExpression().getResolvedType().toString().equals("EolSelfContentType")) {
					contentType = ((EolCollectionType) targetExpression.getResolvedType()).getContentType();

					while (!(contentType instanceof EolPrimitiveType))
						contentType = ((EolCollectionType) contentType).getContentType();
					op.getReturnTypeExpression().setResolvedType(contentType);
				}

				if (op.getReturnTypeExpression().getResolvedType().toString().equals("EolSelfCollectionType")) {
					collectionType = targetExpression.getResolvedType();
					op.getReturnTypeExpression().setResolvedType(collectionType);
				}

				if (op.getReturnTypeExpression().getResolvedType().toString().equals("EolSelfExpressionType")) {
					expType = parameterExpressions.get(0).getResolvedType();
					op.getReturnTypeExpression().setResolvedType(expType);
				}
			}

			if (!operationCallExpression.isContextless() && !getMatched(operationCallExpression)) {

				contextType = targetExpression.getResolvedType();
				op.getContextTypeExpression().accept(this);

				EolType reqContextType = op.getContextTypeExpression().getResolvedType();

				if (reqContextType instanceof EolModelElementType
						&& ((EolModelElementType) reqContextType).getMetaClass() != null)
					reqContextType = new EolModelElementType(((EolModelElementType) reqContextType).getMetaClass());
				if (contextType instanceof EolModelElementType
						&& ((EolModelElementType) contextType).getMetaClass() != null)
					contextType = new EolModelElementType(((EolModelElementType) contextType).getMetaClass());

				if (isCompatible(reqContextType, contextType)) {

					errorCode = 0;
					goForward = true;

				} else if (canBeCompatible(reqContextType, contextType)) {

					errors.add(new ModuleMarker(
							targetExpression, nameExpression.getName() + " may not be invoked on "
									+ targetExpression.getResolvedType() + ", as it requires " + reqContextType,
							Severity.Warning));

				} else if (targetExpression instanceof OperationCallExpression) {
					if (!((OperationCallExpression) targetExpression).matchedReturnType.isEmpty()) {
						for (int i = 0; i < ((OperationCallExpression) targetExpression).matchedReturnType
								.size(); i++) {
							contextType = ((OperationCallExpression) targetExpression).matchedReturnType.get(i);

							if (isCompatible(op.getContextTypeExpression().getResolvedType(), contextType)) {
								errorCode = 0;
								goForward = true;
								break;
							} else {
								errorCode = 1;
								goForward = false;
							}
						}
					}

					else {
						setMatched(operationCallExpression, false);
						errorCode = 5;
						goForward = false;
						break;
					}
				}

				else {

					setMatched(operationCallExpression, false);
					errorCode = 1;
					goForward = false;
				}

			} else
				goForward = true;

			if (goForward) {

				if (goForward && reqParams.size() > 0) {

					if (reqParams.size() == parameterExpressions.size()) {

						int index = 0;
						errorCode = 0;

						for (Parameter parameterExpression : reqParams) {

							parameterExpression.getTypeExpression().accept(this);
							if (parameterExpressions.get(index) instanceof OperationCallExpression
									&& (getMatched((OperationCallExpression) parameterExpressions.get(index)))) {

								ArrayList<EolType> matchTypes = new ArrayList<EolType>();
								matchTypes = getMatchedReturnType((OperationCallExpression) parameterExpressions.get(index));

								if (!(matchTypes.isEmpty()))

									for (EolType matchType : matchTypes) {
										if (parameterExpression.getTypeExpression().getResolvedType()
												.equals(matchType)) {
											parameterExpressions.get(index).setResolvedType(matchType);
											break;
										} else
											parameterExpressions.get(index).setResolvedType(matchType);

									}
								else {
									errorCode = 6;
									goForward = false;
									break;
								}
							}

							EolType reqParameter = parameterExpression.getTypeExpression().getResolvedType();
							EolType provPrameter = parameterExpressions.get(index).getResolvedType();

							if (isCompatible(reqParameter, provPrameter)) {
								setMatched(operationCallExpression, true);
								successMatch = true;
								errorCode = 0;

							} else if (canBeCompatible(reqParameter, provPrameter)) {
								setMatched(operationCallExpression, true);
								successMatch = true;
								errors.add(new ModuleMarker(
										nameExpression, " Parameter " + provPrameter
												+ " might not match, as it requires " + reqParameter,
										Severity.Warning));
							} else if (operationCallExpression.getMatchedReturnTypes().isEmpty()) {
								// Bcz if we found the perfect match before, no need to make success false at
								// the end
								errorCode = 3;
								setMatched(operationCallExpression, false);
								break;
							}

							index++;
						}

						if (getMatched(operationCallExpression)) {
							if (!(getReturnFlag(op)))
								operationCallExpression.setResolvedType(EolNoType.Instance);
							else {
								operationCallExpression.setResolvedType(op.getReturnTypeExpression().getResolvedType());
								getMatchedReturnType(operationCallExpression).add(operationCallExpression.getResolvedType());
							}
						}
					} else {
						errorCode = 2;

					}
				} else if (parameterExpressions.size() == 0 && errorCode == 0) {
					setMatched(operationCallExpression, true);
					successMatch = true;

					if (successMatch) {
						if (!(getReturnFlag(op)))
							operationCallExpression.setResolvedType(EolNoType.Instance);
						else {
							operationCallExpression.setResolvedType(op.getReturnTypeExpression().getResolvedType());
							getMatchedReturnType(operationCallExpression).add(operationCallExpression.getResolvedType());
						}
					}

				} else if (parameterExpressions.size() != 0) {

					errorCode = 2;
				}
			}

			if (successMatch)
				getMatchedOperations(operationCallExpression).add(op);
		}
		
		if (!getMatched(operationCallExpression) || getOperations(operationCallExpression).size() == 0)
			switch (errorCode) {
			case 1:
				errors.add(new ModuleMarker(targetExpression,
						nameExpression.getName() + " can not be invoked on " + targetExpression.getResolvedType(),
						Severity.Error));
				break;
			case 2:
				errors.add(new ModuleMarker(nameExpression, "Number of parameters doesn't match, as "
						+ nameExpression.getName() + " requires " + reqParams.size() + " parameters", Severity.Error));
				break;
			case 3:
				errors.add(new ModuleMarker(nameExpression, "Parameters type mismatch", Severity.Error));
				break;
			case 4:
				errors.add(new ModuleMarker(nameExpression, "Undefined operation", Severity.Error));
				break;
			case 5:
				errors.add(new ModuleMarker(nameExpression, nameExpression.getName() + " can not be invoked on "
						+ ((OperationCallExpression) targetExpression).getNameExpression().getName() + ", as it's void",
						Severity.Error));
				break;
			case 6:
				errors.add(new ModuleMarker(nameExpression, "Parameters type mismatch, as it's void", Severity.Error));
				break;
			}

	}

	@Override
	public void visit(OrOperatorExpression orOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) orOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(Parameter parameter) {
		visit(parameter, true);
	}

	public void visit(Parameter parameter, boolean createVariable) {
		if (parameter.getTypeExpression() != null) {
			parameter.getTypeExpression().accept(this);
		}
		if (createVariable)
			parameter.pushToStack(context);
	}

	@Override
	public void visit(PlusOperatorExpression plusOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) plusOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(PostfixOperatorExpression postfixOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) postfixOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(PropertyCallExpression propertyCallExpression) {
		Expression targetExpression = propertyCallExpression.getTargetExpression();
		NameExpression nameExpression = propertyCallExpression.getNameExpression();
		targetExpression.accept(this);

		// Extended properties
		if (nameExpression.getName().startsWith("~")) {
			propertyCallExpression.setResolvedType(EolAnyType.Instance);
		}
		// e.g. EPackage.all
		else if (targetExpression instanceof NameExpression && ((NameExpression) targetExpression).isTypeName()) {
			if (((NameExpression) targetExpression).getResolvedType() instanceof EolModelElementType) {
				if (nameExpression.getName().equals("all") || nameExpression.getName().equals("allInstances")) {
					propertyCallExpression
							.setResolvedType(new EolCollectionType("Sequence", targetExpression.getResolvedType()));

				} else {
					EolType type = targetExpression.getResolvedType();

					boolean many = false;
					MetaClass metaClass = null;
					if (type instanceof EolModelElementType && ((EolModelElementType) type).getMetaClass() != null) {
						metaClass = (MetaClass) ((EolModelElementType) type).getMetaClass();
					} else if (type instanceof EolCollectionType
							&& ((EolCollectionType) type).getContentType() instanceof EolModelElementType) {
						metaClass = ((EolModelElementType) ((EolCollectionType) type).getContentType()).getMetaClass();
						many = true;
					}

					if (metaClass != null) {
						StructuralFeature structuralFeature = metaClass.getStructuralFeature(nameExpression.getName());
						if (structuralFeature != null) {
							if (structuralFeature.isMany()) {
								EolCollectionType collectionType = null;
								if (structuralFeature.isOrdered()) {
									if (structuralFeature.isUnique())
										collectionType = new EolCollectionType("OrderedSet");
									else
										collectionType = new EolCollectionType("Sequence");
								} else {
									if (structuralFeature.isUnique())
										collectionType = new EolCollectionType("Set");
									else
										collectionType = new EolCollectionType("Bag");
								}
								collectionType.setContentType(structuralFeature.getType());
								propertyCallExpression.setResolvedType(collectionType);
							} else {
								propertyCallExpression.setResolvedType(structuralFeature.getType());
							}
							if (many) {
								propertyCallExpression.setResolvedType(
										new EolCollectionType("Sequence", propertyCallExpression.getResolvedType()));
							}
						} else {
							errors.add(new ModuleMarker(nameExpression, "Structural feature " + nameExpression.getName()
									+ " not found in type " + metaClass.getName(), Severity.Warning));
						}
					}

				}
			}
		}
		// Regular properties
		else {
			EolType type = targetExpression.getResolvedType();

			boolean many = false;
			MetaClass metaClass = null;
			if (type instanceof EolModelElementType && ((EolModelElementType) type).getMetaClass() != null) {
				metaClass = (MetaClass) ((EolModelElementType) type).getMetaClass();
			} else if (type instanceof EolCollectionType
					&& ((EolCollectionType) type).getContentType() instanceof EolModelElementType) {
				metaClass = ((EolModelElementType) ((EolCollectionType) type).getContentType()).getMetaClass();
				many = true;
			}

			if (metaClass != null) {
				StructuralFeature structuralFeature = metaClass.getStructuralFeature(nameExpression.getName());
				if (structuralFeature != null) {
					if (structuralFeature.isMany()) {
						String collectionTypeName;
						if (structuralFeature.isOrdered()) {
							collectionTypeName = structuralFeature.isUnique() ? "OrderedSet" : "Sequence";
						} else {
							collectionTypeName = structuralFeature.isUnique() ? "Set" : "Bag";
							if (structuralFeature.isConcurrent()) {
								collectionTypeName = "Concurrent" + collectionTypeName;
							}
						}
						propertyCallExpression.setResolvedType(new EolCollectionType(collectionTypeName));
						((EolCollectionType) propertyCallExpression.getResolvedType())
								.setContentType(structuralFeature.getType());
					} else {
						propertyCallExpression.setResolvedType(structuralFeature.getType());
					}
					if (many) {
						propertyCallExpression.setResolvedType(
								new EolCollectionType("Sequence", propertyCallExpression.getResolvedType()));
					}

				} else {
					errors.add(new ModuleMarker(nameExpression, "Structural feature " + nameExpression.getName()
							+ " not found in type " + metaClass.getName(), Severity.Warning));
				}
			}

		}

	}

	@Override
	public void visit(RealLiteral realLiteral) {
		realLiteral.setResolvedType(EolPrimitiveType.Real);

	}

	@Override
	public void visit(ReturnStatement returnStatement) {
		Expression returnedExpression = returnStatement.getReturnedExpression();
		if (returnedExpression != null) {

			returnedExpression.accept(this);
			

			EolType providedReturnType = returnedExpression.getResolvedType();

			ModuleElement parent = returnedExpression.getParent();

			while (!(parent instanceof Operation) && parent != null) {

				parent = parent.getParent();

			}

			if (parent instanceof Operation) {
				setReturnFlag(((Operation) parent), true);
				// add for setting resolved type
				if (((Operation) parent).getReturnTypeExpression() == null)
					((Operation) parent).setReturnTypeExpression(new TypeExpression("Any"));
				(((Operation) parent).getReturnTypeExpression()).compile(context);
				EolType requiredReturnType = ((Operation) parent).getReturnTypeExpression().getResolvedType();

				if (!(isCompatible(requiredReturnType, providedReturnType))) {
					if (canBeCompatible(requiredReturnType, providedReturnType))
						errors.add(new ModuleMarker(returnedExpression, "Return type might be " + requiredReturnType
								+ " instead of " + returnedExpression.getResolvedType(), Severity.Warning));
					else
						errors.add(new ModuleMarker(returnedExpression, "Return type should be " + requiredReturnType
								+ " instead of " + returnedExpression.getResolvedType(), Severity.Error));

				}
			}
		}

	}

	@Override
	public void visit(SimpleAnnotation simpleAnnotation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(StatementBlock statementBlock) {

		statementBlock.getStatements().forEach(s -> s.accept(this));

	}

	@Override
	public void visit(StringLiteral stringLiteral) {
		stringLiteral.setResolvedType(EolPrimitiveType.String);
	}

	@Override
	public void visit(SwitchStatement switchStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TernaryExpression ternaryExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) ternaryExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(ThrowStatement throwStatement) {
		throwStatement.getThrown().accept(this);
	}

	@Override
	public void visit(TimesOperatorExpression timesOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) timesOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	@Override
	public void visit(TransactionStatement transactionStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TypeExpression typeExpression) {
		EolType type = typeExpression.getCompilationType();

		for (TypeExpression typeExp : typeExpression.getParameterTypeExpressions()) {
			typeExp.accept(this);
		}

		if (type instanceof EolPrimitiveType || type instanceof EolSelf || type instanceof EolSelfContentType
				|| type instanceof EolSelfExpressionType || type instanceof EolSelfCollectionType) {
			typeExpression.setResolvedType(type);
		}

		if (type instanceof EolCollectionType) {
			typeExpression.setResolvedType(type);
			if (typeExpression.getParameterTypeExpressions().size() == 1) {
				((EolCollectionType) type)
						.setContentType(typeExpression.getParameterTypeExpressions().get(0).getResolvedType());
				typeExpression.setResolvedType(type);
			} else if (typeExpression.getParameterTypeExpressions().size() > 1) {
				errors.add(new ModuleMarker(typeExpression, "Collection types can have at most one content type",
						Severity.Error));
			}
		}

		if (type instanceof EolMapType) {
			if (typeExpression.getParameterTypeExpressions().size() == 2) {
				((EolMapType) type)
						.setKeyType(typeExpression.getParameterTypeExpressions().get(0).getCompilationType());
				((EolMapType) type)
						.setValueType(typeExpression.getParameterTypeExpressions().get(1).getCompilationType());
			} else if (typeExpression.getParameterTypeExpressions().size() > 0) {
				errors.add(new ModuleMarker(typeExpression, "Maps need two types: key-type and value-type",
						Severity.Error));
			}
		}

		if (type == null) {
			// TODO: Remove duplication between this and NameExpression
			EolModelElementType modelElementType = context
					.getModelElementType(typeExpression.getName());
			if (modelElementType != null) {
				type = modelElementType;
				if (modelElementType.getMetaClass() == null
						&& !context.getModelDeclarations().isEmpty()) {
					errors.add(new ModuleMarker(typeExpression, "Unknown type " + typeExpression.getName(),
							Severity.Error));
				}
			} else {
				errors.add(new ModuleMarker(typeExpression, "Undefined variable or type " + typeExpression.getName(),
						Severity.Error));
			}
		}
		if (type instanceof EolModelElementType)
			typeExpression.setResolvedType(type);

	}

	@Override
	public void visit(VariableDeclaration variableDeclaration) {

		EolType type;
		TypeExpression typeExpression = variableDeclaration.getTypeExpression();

		if (typeExpression != null) {
			typeExpression.accept(this);
			type = typeExpression.getResolvedType();
		} else {
			type = EolAnyType.Instance;
		}

		if (context.getFrameStack().getTopFrame().contains(variableDeclaration.getName())) {
			errors.add(new ModuleMarker(variableDeclaration,
					"Variable " + variableDeclaration.getName() + " has already been defined", Severity.Error));
		} else {
			context.getFrameStack().put(new Variable(variableDeclaration.getName(), type));
			variableDeclaration.setResolvedType(type);
		}

	}

	@Override
	public void visit(WhileStatement whileStatement) {

		FrameStack frameStack = context.getFrameStack();
		Expression conditionExpression = whileStatement.getConditionExpression();
		StatementBlock bodyStatementBlock = whileStatement.getBodyStatementBlock();

		conditionExpression.accept(this);

		frameStack.enterLocal(FrameType.UNPROTECTED, bodyStatementBlock);
		bodyStatementBlock.accept(this);
		;
		frameStack.leaveLocal(bodyStatementBlock);

		if (conditionExpression.hasResolvedType()
				&& conditionExpression.getResolvedType() != EolPrimitiveType.Boolean) {
			errors.add(new ModuleMarker(conditionExpression, "Condition must be a Boolean", Severity.Error));
		}
	}

	@Override
	public void visit(XorOperatorExpression xorOperatorExpression) {
		OperatorExpression operatorExpression = (OperatorExpression) xorOperatorExpression;
		visitOperatorExpression(operatorExpression);
	}

	public void preValidate() {

		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			modelDeclaration.accept(this);
		}

		String root = "/Users/quratulainali/git/org.eclipse.epsilon/plugins/org.eclipse.epsilon.eol.engine/src/org/eclipse/epsilon/eol/";

		if (!(module instanceof BuiltinEolModule)) {
			try {
				// builtinModule.parse(new File("./src/org/eclipse/epsilon/eol/builtin.eol"));
				builtinModule.parse(new File(root + "builtin.eol"));
				module.getOperations().addAll(builtinModule.getDeclaredOperations());

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Check the signature of functions
		for (Operation operation : module.getOperations()) {
			if (operation.getReturnTypeExpression() == null) {

				if (operation.hasReturnStatement()) {
					setReturnFlag(operation, true);
					operation.setReturnTypeExpression(new TypeExpression("Any"));
				}
				else
					setReturnFlag(operation, false);

			}
			// when returnType is not null
			else {

				if (operation.hasReturnStatement())
					setReturnFlag(operation, true);
				else {
					if ((operation.getAnnotation("builtin") != null) || (operation.getAnnotation("firstorder") != null))
						setReturnFlag(operation, true);
					else
						setReturnFlag(operation, false);
				}
			}
		}
	}

	@Override
	public List<ModuleMarker> validate(IModule imodule) {
		if (!(imodule instanceof EolModule))
			return Collections.emptyList();

		errors = new ArrayList<>();

		EolModule eolModule = (EolModule) imodule;
		this.module = eolModule;
		
		context = eolModule.getCompilationContext();
		
		preValidate();
		
		if (eolModule.getMain() != null)
			eolModule.getMain().accept(this);
		eolModule.getDeclaredOperations().forEach(o -> o.accept(this));
		if(!(imodule instanceof EvlModule)) {
			callGraph = new CallGraphGenerator();
			callGraph.generateCallGraph(module, this);
			String path = module.getSourceFile().getPath().split("\\.")[0]+".dot";
			exportCallGraph(path);
		}
		if (!(module instanceof BuiltinEolModule) && !(module instanceof EvlModule) )
			module.getOperations().removeAll(builtinModule.getDeclaredOperations());
		
		return errors;
	}

	@Override
	public String getMarkerType() {
		return AbstractModuleEditor.PROBLEM_MARKER;
	}
	
	public CallGraphGenerator getCallGraph() {
		return callGraph;
	}
	
	public void exportCallGraph(String path) {
		callGraph.exportCallGraphToDot(path);
	}

	public void createTypeCompatibilityWarning(Expression requiredExpression, Expression providedExpression) {
		errors.add(new ModuleMarker(providedExpression, providedExpression.getResolvedType()
				+ " may not be assigned to " + requiredExpression.getResolvedType(), Severity.Warning));
	}

	public void createTypeCompatibilityError(Expression requiredExpression, Expression providedExpression) {
		errors.add(new ModuleMarker(providedExpression,
				providedExpression.getResolvedType() + " cannot be assigned to " + requiredExpression.getResolvedType(),
				Severity.Error));
	}
	
	public Operation getExactMatchedOperation(OperationCallExpression oc) {
		if(oc.getName().equals("getDomainMetaElement"))
			System.err.println();
		List<Operation> operations = matchedOperations.get(oc);
		if (operations.size() > 1) {
			// Check contextType

			for (Operation operation : operations) {
				if (operation.getContextTypeExpression() != null) {
					EolType operationContextType = operation.getContextTypeExpression().getResolvedType();
					EolType opCallExpContextType = oc.getTargetExpression().getResolvedType();

					if (isCompatible(operationContextType, opCallExpContextType)) {
						int loopCounter = 0;
						if (oc.getParameterExpressions().size() > 1) {
							for (Expression parameter : oc.getParameterExpressions()) {
								EolType paramContextType = operation.getFormalParameters().get(loopCounter)
										.getTypeExpression().getResolvedType();
								EolType paramTargetType = parameter.getResolvedType();
								if (isCompatible(paramContextType, paramTargetType))
									return operation;
								loopCounter++;
							}
							loopCounter = 0;
							for (Expression parameter : oc.getParameterExpressions()) {
								EolType paramContextType = operation.getFormalParameters().get(loopCounter)
										.getTypeExpression().getResolvedType();
								EolType paramTargetType = parameter.getResolvedType();
								if (canBeCompatible(paramContextType, paramTargetType))
									return operation;
								loopCounter++;
							}

						}
						return operation;

					} else if (canBeCompatible(operationContextType, opCallExpContextType)) {
						int loopCounter = 0;
						if (oc.getParameterExpressions().size() > 1) {
							for (Expression parameter : oc.getParameterExpressions()) {
								EolType paramContextType = operation.getFormalParameters().get(loopCounter)
										.getTypeExpression().getResolvedType();
								EolType paramTargetType = parameter.getResolvedType();
								if (isCompatible(paramContextType, paramTargetType))
									return operation;
								loopCounter++;
							}
							loopCounter = 0;
							for (Expression parameter : oc.getParameterExpressions()) {
								EolType paramContextType = operation.getFormalParameters().get(loopCounter)
										.getTypeExpression().getResolvedType();
								EolType paramTargetType = parameter.getResolvedType();
								if (canBeCompatible(paramContextType, paramTargetType))
									return operation;
								loopCounter++;
							}

						}
						return operation;
					}
				} else {
					if (oc.getParameterExpressions().size() > 1) {
						int loopCounter = 0;
						for (Expression parameter : oc.getParameterExpressions()) {
							EolType paramContextType = operation.getFormalParameters().get(loopCounter)
									.getTypeExpression().getResolvedType();
							EolType paramTargetType = parameter.getResolvedType();
							if (isCompatible(paramContextType, paramTargetType))
								return operation;
							loopCounter++;
						}
						loopCounter = 0;
						for (Expression parameter : oc.getParameterExpressions()) {
							EolType paramContextType = operation.getFormalParameters().get(loopCounter)
									.getTypeExpression().getResolvedType();
							EolType paramTargetType = parameter.getResolvedType();
							if (canBeCompatible(paramContextType, paramTargetType))
								return operation;
							loopCounter++;
						}

					}
					return operation;
				}
			}

		}
		if(operations.isEmpty())
			return null;
		return operations.get(0);
	}
	
	

	public boolean isCompatible(EolType targetType, EolType valueType) {

		boolean ok = false;

		if (targetType.equals(EolNoType.Instance) || valueType.equals(EolNoType.Instance))
			return false;
		else

			while (!ok) {
				if (!(targetType.equals(valueType)) && !(targetType instanceof EolAnyType)) {

					valueType = valueType.getParentType();

					if (valueType instanceof EolAnyType) {
						return false;
					}

				} else if (targetType instanceof EolAnyType) {
					return true;
				} else if (valueType instanceof EolCollectionType
						&& !((((EolCollectionType) targetType).getContentType()) instanceof EolAnyType)) {

					EolType valueContentType = ((EolCollectionType) valueType).getContentType();
					EolType targetContentType = ((EolCollectionType) targetType).getContentType();

					while (targetContentType instanceof EolCollectionType
							&& valueContentType instanceof EolCollectionType) {
						if (targetContentType.equals(valueContentType)) {
							return isCompatible(((EolCollectionType) targetContentType).getContentType(),
									((EolCollectionType) valueContentType).getContentType());
						} else {
							valueContentType = valueContentType.getParentType();
							return isCompatible(targetContentType, valueContentType);

						}
					}
					while (!ok) {
						if (valueContentType instanceof EolAnyType) {
							return false;
						}
						if (!valueContentType.equals(targetContentType)) {
							valueContentType = valueContentType.getParentType();
						} else {
							return true;
						}
					}
				} else
					return true;
			}
		return false;
	}

	public boolean canBeCompatible(EolType targetType, EolType valueType) {

		boolean ok = false;
		if (targetType == null || valueType == null)
			return false;
		else
			while (!ok) {

				if (!(targetType.equals(valueType)) && !(valueType instanceof EolAnyType)) {

					targetType = targetType.getParentType();

					if (targetType instanceof EolAnyType) {
						return false;
					}

				} else if (valueType instanceof EolAnyType) {
					return true;
				} else if (targetType instanceof EolCollectionType
						&& !((((EolCollectionType) valueType).getContentType()) instanceof EolAnyType)) {

					EolType valueContentType = ((EolCollectionType) valueType).getContentType();
					EolType targetContentType = ((EolCollectionType) targetType).getContentType();

					while (targetContentType instanceof EolCollectionType
							&& valueContentType instanceof EolCollectionType) {
						if (targetContentType.equals(valueContentType)) {
							return canBeCompatible(((EolCollectionType) targetContentType).getContentType(),
									((EolCollectionType) valueContentType).getContentType());
						} else {
							valueContentType = valueContentType.getParentType();
							return canBeCompatible(targetContentType, valueContentType);

						}
					}
					while (!ok) {
						if (valueContentType instanceof EolAnyType || targetContentType instanceof EolAnyType) {
							return true;
						}
						if (!valueContentType.equals(targetContentType)) {
							targetContentType = targetContentType.getParentType();
							if (targetContentType instanceof EolAnyType)
								return false;
						} else {
							return true;
						}
					}
				} else
					return true;
			}
		return false;
	}

	public void visitOperatorExpression(OperatorExpression operatorExpression) {
		Expression firstOperand = operatorExpression.getFirstOperand();
		Expression secondOperand = operatorExpression.getSecondOperand();
		String operator = operatorExpression.getOperator();
		List<Expression> operands = operatorExpression.getOperands();

		firstOperand.accept(this);
		if (secondOperand != null)
			secondOperand.accept(this);
		;

		if (StringUtil.isOneOf(operator, "and", "or", "xor", "not", "implies")) {
			for (Expression operand : operatorExpression.getOperands()) {
				if (operand.hasResolvedType() && operand.getResolvedType() != EolPrimitiveType.Boolean) {
					errors.add(new ModuleMarker(operatorExpression,
							"Boolean expected instead of " + operand.getResolvedType(), Severity.Error));
				}
			}
			operatorExpression.setResolvedType(EolPrimitiveType.Boolean);
		}

//		if (StringUtil.isOneOf(operator, "<", ">", ">=", "<=", "*", "/", "-")) {
//			for (Expression operand : getOperands()) {
//				if (operand.hasResolvedType() && 
//						operand.getResolvedType() != EolPrimitiveType.Integer 
//						&& operand.getResolvedType() != EolPrimitiveType.Real) {
//					
//					context.addErrorMarker(operand, "Number expected instead of " + operand.getResolvedType());
//				}
//			}
//		}
//		
//		if (StringUtil.isOneOf(operator, "==", "=", "<>", "<", ">", ">=", "<=")) {
//			resolvedType = EolPrimitiveType.Boolean;
//		}

		if (StringUtil.isOneOf(operator, "<", ">", ">=", "<=", "*", "/", "-")) {
			for (Expression operand : operands) {
				if (operand.hasResolvedType() && operand.getResolvedType() != EolPrimitiveType.Integer
						&& operand.getResolvedType() != EolPrimitiveType.Real) {
					operatorExpression.setResolvedType(EolAnyType.Instance);
					errors.add(new ModuleMarker(operatorExpression,
							"Number expected instead of " + operand.getResolvedType(), Severity.Error));
				} else if (StringUtil.isOneOf(operator, "*", "/", "-")) {
					if (operand.getResolvedType() == EolPrimitiveType.Real)
						operatorExpression.setResolvedType(EolPrimitiveType.Real);
					else
						operatorExpression.setResolvedType(EolPrimitiveType.Integer);
				}
			}
		}

		if (StringUtil.isOneOf(operator, "==", "=", "<>", "<", ">", ">=", "<=")) {
			operatorExpression.setResolvedType(EolPrimitiveType.Boolean);
		}

		if (StringUtil.isOneOf(operator, "+")) {
			for (Expression operand : operands) {
				if (operand.getResolvedType() == EolPrimitiveType.String) {
					operatorExpression.setResolvedType(EolPrimitiveType.String);
					break;
				}

				if (operand.getResolvedType() == EolPrimitiveType.Integer)
					operatorExpression.setResolvedType(EolPrimitiveType.Integer);

				if (operand.getResolvedType() == EolPrimitiveType.Real)
					operatorExpression.setResolvedType(EolPrimitiveType.Real);
			}

		}

	}

	public boolean hasReturnStatement(Operation operation) {
		ArrayList<ModuleElement> statements = new ArrayList<ModuleElement>();
		statements.addAll(operation.getBody().getChildren());

		while (!(statements.isEmpty())) {
			ModuleElement st = statements.get(0);
			statements.remove(st);
			if (!(st.getChildren().isEmpty()))
				statements.addAll(st.getChildren());
			if (st instanceof ReturnStatement)
				return true;
		}
		return false;
	}
	
	public boolean getReturnFlag(Operation op) {
		   return returnFlags.get(op);
		}

	public void setReturnFlag(Operation op, boolean returnFlag) {
		   returnFlags.put(op, returnFlag);
		}
		
	public ArrayList<Operation> getOperations(OperationCallExpression operationCallExpression) {
			   return operations.get(operationCallExpression);
			}

	public void setOperations(OperationCallExpression operationCallExpression, ArrayList<Operation> ops) {
			   operations.put(operationCallExpression, ops);
			}

	public ArrayList<Operation> getMatchedOperations(OperationCallExpression operationCallExpression) {
				return matchedOperations.get(operationCallExpression);
	}
	
	public void setMatchedOperations(OperationCallExpression operationCallExpression, ArrayList<Operation> ops) {
			  matchedOperations.put(operationCallExpression,ops);
	}
	
	public ArrayList<EolType> getMatchedReturnType(OperationCallExpression operationCallExpression) {
			return matchedReturnType.get(operationCallExpression);
	}
	
	public void setMatchedReturnType(OperationCallExpression operationCallExpression, ArrayList<EolType> returnTypes) {
		matchedReturnType.put(operationCallExpression,returnTypes);
	}
	
	public Boolean getMatched(OperationCallExpression operationCallExpression) {
		return matched.get(operationCallExpression);
	}
	
	public void setMatched(OperationCallExpression operationCallExpression, boolean match) {
	matched.put(operationCallExpression, match);
    }		

}
