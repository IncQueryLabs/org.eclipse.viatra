/*******************************************************************************
 * Copyright (c) 2010-2012, Mark Czotter, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mark Czotter, Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.patternlanguage.validation;

import static org.eclipse.xtext.util.Strings.equal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.incquery.patternlanguage.annotations.IPatternAnnotationValidator;
import org.eclipse.incquery.patternlanguage.annotations.PatternAnnotationProvider;
import org.eclipse.incquery.patternlanguage.helper.CorePatternLanguageHelper;
import org.eclipse.incquery.patternlanguage.patternLanguage.AggregatedValue;
import org.eclipse.incquery.patternlanguage.patternLanguage.Annotation;
import org.eclipse.incquery.patternlanguage.patternLanguage.AnnotationParameter;
import org.eclipse.incquery.patternlanguage.patternLanguage.BoolValue;
import org.eclipse.incquery.patternlanguage.patternLanguage.CheckConstraint;
import org.eclipse.incquery.patternlanguage.patternLanguage.CompareConstraint;
import org.eclipse.incquery.patternlanguage.patternLanguage.CompareFeature;
import org.eclipse.incquery.patternlanguage.patternLanguage.Constraint;
import org.eclipse.incquery.patternlanguage.patternLanguage.DoubleValue;
import org.eclipse.incquery.patternlanguage.patternLanguage.FunctionEvaluationValue;
import org.eclipse.incquery.patternlanguage.patternLanguage.IntValue;
import org.eclipse.incquery.patternlanguage.patternLanguage.ListValue;
import org.eclipse.incquery.patternlanguage.patternLanguage.Modifiers;
import org.eclipse.incquery.patternlanguage.patternLanguage.ParameterRef;
import org.eclipse.incquery.patternlanguage.patternLanguage.Pattern;
import org.eclipse.incquery.patternlanguage.patternLanguage.PatternBody;
import org.eclipse.incquery.patternlanguage.patternLanguage.PatternCall;
import org.eclipse.incquery.patternlanguage.patternLanguage.PatternCompositionConstraint;
import org.eclipse.incquery.patternlanguage.patternLanguage.PatternLanguagePackage;
import org.eclipse.incquery.patternlanguage.patternLanguage.PatternModel;
import org.eclipse.incquery.patternlanguage.patternLanguage.StringValue;
import org.eclipse.incquery.patternlanguage.patternLanguage.ValueReference;
import org.eclipse.incquery.patternlanguage.patternLanguage.Variable;
import org.eclipse.incquery.patternlanguage.patternLanguage.VariableReference;
import org.eclipse.incquery.patternlanguage.patternLanguage.VariableValue;
import org.eclipse.incquery.patternlanguage.typing.ITypeInferrer;
import org.eclipse.incquery.patternlanguage.typing.ITypeSystem;
import org.eclipse.incquery.patternlanguage.validation.VariableReferenceCount.ReferenceType;
import org.eclipse.incquery.patternlanguage.validation.whitelist.PureClassChecker;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.util.Primitives.Primitive;
import org.eclipse.xtext.util.IResourceScopeCache;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.CheckType;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations;
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver;
import org.eclipse.xtext.xbase.typesystem.IResolvedTypes;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Validators for Core Pattern Language.
 * <p>
 * Validators implemented:
 * </p>
 * <ul>
 * <li>Duplicate parameter in pattern declaration</li>
 * <li>Duplicate pattern definition (name duplication only, better calculation is needed)</li>
 * <li>Pattern call parameter checking (only the number of the parameters, types not supported yet)</li>
 * <li>Empty PatternBody check</li>
 * <li>Check for recursive pattern calls</li>
 * </ul>
 *
 * @author Mark Czotter
 * @author Tamas Szabo (itemis AG)
 */
@SuppressWarnings("restriction")
public class PatternLanguageJavaValidator extends AbstractPatternLanguageJavaValidator implements IIssueCallback {

    public static final String DUPLICATE_VARIABLE_MESSAGE = "Duplicate parameter ";
    public static final String DUPLICATE_PATTERN_DEFINITION_MESSAGE = "Duplicate pattern ";
    public static final String UNKNOWN_ANNOTATION_ATTRIBUTE = "Undefined annotation attribute ";
    public static final String MISSING_ANNOTATION_ATTRIBUTE = "Required attribute missing ";
    public static final String ANNOTATION_PARAMETER_TYPE_ERROR = "Invalid parameter type %s. Expected %s";
    public static final String TRANSITIVE_CLOSURE_ARITY_IN_PATTERNCALL = "The pattern %s is not of binary arity (it has %d parameters), therefore transitive closure is not supported.";
    public static final String TRANSITIVE_CLOSURE_ONLY_IN_POSITIVE_COMPOSITION = "Transitive closure of %s is currently only allowed in simple positive pattern calls (no negation or aggregation).";
    public static final String UNUSED_PRIVATE_PATTERN_MESSAGE = "The pattern '%s' is never used locally.";
    public static final String RECURSIVE_PATTERN_CALL = "Recursive pattern call: %s";

    @Inject
    private PatternAnnotationProvider annotationProvider;

    @Inject
    private PureClassChecker purityChecker;

    @Inject
    private IJvmModelAssociations associations;

    @Inject
    private ITypeSystem typeSystem;

    @Inject
    private ITypeInferrer typeInferrer;

    @Inject
    private IBatchTypeResolver typeResolver;

    @Inject
    private IResourceScopeCache cache;

    @Check
    public void checkPatternParameters(Pattern pattern) {
        if (pattern.getParameters().size() == 0) {
            warning("Parameterless patterns can only be used to check for existence of a condition.",
                    PatternLanguagePackage.Literals.PATTERN__NAME, IssueCodes.MISSING_PATTERN_PARAMETERS);
            // As no duplicate parameters are available, returning now
            return;
        }
        for (int i = 0; i < pattern.getParameters().size(); ++i) {
            String leftParameterName = pattern.getParameters().get(i).getName();
            for (int j = i + 1; j < pattern.getParameters().size(); ++j) {
                if (equal(leftParameterName, pattern.getParameters().get(j).getName())) {
                    error(DUPLICATE_VARIABLE_MESSAGE + leftParameterName,
                            PatternLanguagePackage.Literals.PATTERN__PARAMETERS, i,
                            IssueCodes.DUPLICATE_PATTERN_PARAMETER_NAME);
                    error(DUPLICATE_VARIABLE_MESSAGE + leftParameterName,
                            PatternLanguagePackage.Literals.PATTERN__PARAMETERS, j,
                            IssueCodes.DUPLICATE_PATTERN_PARAMETER_NAME);
                }
            }
        }
    }

    @Check
    public void checkPrivatePatternUsage(Pattern pattern) {
        if (CorePatternLanguageHelper.isPrivate(pattern) && !isLocallyUsed(pattern, pattern.eContainer())) {
            String message = String.format(UNUSED_PRIVATE_PATTERN_MESSAGE, pattern.getName());
            warning(message, PatternLanguagePackage.Literals.PATTERN__NAME, IssueCodes.UNUSED_PRIVATE_PATTERN);
        }
    }

    @Check
    public void checkPrivatePatternCall(PatternCall call) {
        final Pattern calledPattern = call.getPatternRef();
        if (calledPattern != null) {
            if (Iterables.any(calledPattern.getModifiers(), new Predicate<Modifiers>() {

                @Override
                public boolean apply(Modifiers input) {
                    return input.isPrivate();
                }
            }) && calledPattern.eResource() != call.eResource()) {
                error(String.format("The pattern %s is not visible.", getFormattedPattern(calledPattern)),
                        PatternLanguagePackage.Literals.PATTERN_CALL__PATTERN_REF, IssueCodes.PRIVATE_PATTERN_CALLED);
            }
        }
    }

    @Check
    public void checkPatternCallParameters(PatternCall call) {
        if (call.getPatternRef() != null && call.getPatternRef().getName() != null && call.getParameters() != null) {
            final int definitionParameterSize = call.getPatternRef().getParameters().size();
            final int callParameterSize = call.getParameters().size();
            if (definitionParameterSize != callParameterSize) {
                error("The pattern " + getFormattedPattern(call.getPatternRef())
                        + " is not applicable for the arguments(" + getFormattedArgumentsList(call) + ")",
                        PatternLanguagePackage.Literals.PATTERN_CALL__PATTERN_REF,
                        IssueCodes.WRONG_NUMBER_PATTERNCALL_PARAMETER);
            }
        }
    }

    @Check
    public void checkApplicabilityOfTransitiveClosureInPatternCall(PatternCall call) {
        final Pattern patternRef = call.getPatternRef();
        final EObject eContainer = call.eContainer();
        if (patternRef != null && call.isTransitive()) {
            if (patternRef.getParameters() != null) {
                final int arity = patternRef.getParameters().size();
                if (2 != arity) {
                    error(String
                            .format(TRANSITIVE_CLOSURE_ARITY_IN_PATTERNCALL, getFormattedPattern(patternRef), arity),
                            PatternLanguagePackage.Literals.PATTERN_CALL__TRANSITIVE,
                            IssueCodes.TRANSITIVE_PATTERNCALL_ARITY);
                } else {

                    Object type1 = typeInferrer.getVariableType(patternRef.getParameters().get(0));
                    Object type2 = typeInferrer.getVariableType(patternRef.getParameters().get(1));
                    if (!typeSystem.isConformant(type1, type2) && !typeSystem.isConformant(type2, type1)) {
                        error(String.format(
                                "The parameter types %s and %s are not compatible, so no transitive references can exist in instance models.",
                                typeSystem.typeString(type1), typeSystem.typeString(type2)),
                                PatternLanguagePackage.Literals.PATTERN_CALL__PARAMETERS,
                                IssueCodes.TRANSITIVE_PATTERNCALL_TYPE);
                    }
                }
            }
            if (eContainer != null
                    && (!(eContainer instanceof PatternCompositionConstraint) || ((PatternCompositionConstraint) eContainer)
                            .isNegative())) {
                error(String.format(TRANSITIVE_CLOSURE_ONLY_IN_POSITIVE_COMPOSITION, getFormattedPattern(patternRef)),
                        PatternLanguagePackage.Literals.PATTERN_CALL__TRANSITIVE,
                        IssueCodes.TRANSITIVE_PATTERNCALL_NOT_APPLICABLE);
            }

        }

    }

    @Check
    public void checkPatterns(PatternModel model) {
        if (model.getPatterns() != null && !model.getPatterns().isEmpty()) {
            // TODO: more precise calculation is needed for duplicate patterns
            // (number and type of pattern parameters)
            for (int i = 0; i < model.getPatterns().size(); ++i) {
                Pattern leftPattern = model.getPatterns().get(i);
                String leftPatternName = leftPattern.getName();
                for (int j = i + 1; j < model.getPatterns().size(); ++j) {
                    Pattern rightPattern = model.getPatterns().get(j);
                    String rightPatternName = rightPattern.getName();
                    if (leftPatternName.equalsIgnoreCase(rightPatternName)) {
                        error(DUPLICATE_PATTERN_DEFINITION_MESSAGE + leftPatternName, leftPattern,
                                PatternLanguagePackage.Literals.PATTERN__NAME, IssueCodes.DUPLICATE_PATTERN_DEFINITION);
                        error(DUPLICATE_PATTERN_DEFINITION_MESSAGE + rightPatternName, rightPattern,
                                PatternLanguagePackage.Literals.PATTERN__NAME, IssueCodes.DUPLICATE_PATTERN_DEFINITION);
                    }
                }
            }
        }
    }

    @Check
    public void checkPatternBody(PatternBody body) {
        if (body.getConstraints().isEmpty()) {
            String bodyName = getName(body);
            if (bodyName == null) {
                Pattern pattern = ((Pattern) body.eContainer());
                String patternName = pattern.getName();
                error("A patternbody of " + patternName + " is empty", body,
                        PatternLanguagePackage.Literals.PATTERN_BODY__CONSTRAINTS, IssueCodes.PATTERN_BODY_EMPTY);
            } else {
                error("The patternbody " + bodyName + " cannot be empty", body,
                        PatternLanguagePackage.Literals.PATTERN_BODY__NAME, IssueCodes.PATTERN_BODY_EMPTY);
            }
        }
    }

    @Check(CheckType.NORMAL)
    public void checkAnnotation(Annotation annotation) {
        if (annotationProvider.hasValidator(annotation.getName())) {
            IPatternAnnotationValidator validator = annotationProvider.getValidator(annotation.getName());
            // Check for unknown annotation attributes
            for (AnnotationParameter unknownParameter : validator.getUnknownAttributes(annotation)) {
                error(UNKNOWN_ANNOTATION_ATTRIBUTE + unknownParameter.getName(), unknownParameter,
                        PatternLanguagePackage.Literals.ANNOTATION_PARAMETER__NAME,
                        annotation.getParameters().indexOf(unknownParameter), IssueCodes.UNKNOWN_ANNOTATION_PARAMETER);
            }
            // Check for missing mandatory attributes
            for (String missingAttribute : validator.getMissingMandatoryAttributes(annotation)) {
                error(MISSING_ANNOTATION_ATTRIBUTE + missingAttribute, annotation,
                        PatternLanguagePackage.Literals.ANNOTATION__PARAMETERS,
                        IssueCodes.MISSING_REQUIRED_ANNOTATION_PARAMETER);
            }
            // Check for annotation parameter types
            for (AnnotationParameter parameter : annotation.getParameters()) {
                Class<? extends ValueReference> expectedParameterType = validator.getExpectedParameterType(parameter);
                if (expectedParameterType != null && parameter.getValue() != null
                        && !expectedParameterType.isAssignableFrom(parameter.getValue().getClass())) {
                    error(String.format(ANNOTATION_PARAMETER_TYPE_ERROR, getTypeName(parameter.getValue().getClass()),
                            getTypeName(expectedParameterType)), parameter,
                            PatternLanguagePackage.Literals.ANNOTATION_PARAMETER__NAME, annotation.getParameters()
                                    .indexOf(parameter), IssueCodes.MISTYPED_ANNOTATION_PARAMETER);
                } else if (parameter.getValue() instanceof VariableValue) {
                    VariableValue value = (VariableValue) parameter.getValue();
                    if (value.getValue().getVariable() == null) {
                        error(String.format("Unknown variable %s", value.getValue().getVar()), parameter,
                                PatternLanguagePackage.Literals.ANNOTATION_PARAMETER__VALUE, annotation.getParameters()
                                        .indexOf(parameter), IssueCodes.MISTYPED_ANNOTATION_PARAMETER);
                    }
                }
            }
            // Execute extra validation
            if (validator.getAdditionalValidator() != null) {
                validator.getAdditionalValidator().executeAdditionalValidation(annotation, this);
            }
        } else {
            warning("Unknown annotation " + annotation.getName(), PatternLanguagePackage.Literals.ANNOTATION__NAME,
                    IssueCodes.UNKNOWN_ANNOTATION);
        }
    }

    @Check
    public void checkCompareConstraints(CompareConstraint constraint) {
        ValueReference op1 = constraint.getLeftOperand();
        ValueReference op2 = constraint.getRightOperand();
        if (op1 == null || op2 == null) {
            return;
        }

        boolean op1Constant = PatternLanguagePackage.Literals.LITERAL_VALUE_REFERENCE.isSuperTypeOf(op1.eClass());
        boolean op2Constant = PatternLanguagePackage.Literals.LITERAL_VALUE_REFERENCE.isSuperTypeOf(op2.eClass());
        boolean op1Variable = PatternLanguagePackage.Literals.VARIABLE_VALUE.isSuperTypeOf(op1.eClass());
        boolean op2Variable = PatternLanguagePackage.Literals.VARIABLE_VALUE.isSuperTypeOf(op2.eClass());

        // If both operands are constant literals, issue a warning
        if (op1Constant && op2Constant) {
            warning("Both operands are constants - constraint is always true or always false.",
                    PatternLanguagePackage.Literals.COMPARE_CONSTRAINT__LEFT_OPERAND,
                    IssueCodes.CONSTANT_COMPARE_CONSTRAINT);
            warning("Both operands are constants - constraint is always true or always false.",
                    PatternLanguagePackage.Literals.COMPARE_CONSTRAINT__RIGHT_OPERAND,
                    IssueCodes.CONSTANT_COMPARE_CONSTRAINT);
        }
        // If both operands are the same, issues a warning
        if (op1Variable && op2Variable) {
            VariableValue op1v = (VariableValue) op1;
            VariableValue op2v = (VariableValue) op2;
            if (op1v.getValue().getVar().equals(op2v.getValue().getVar())) {
                warning("Comparing a variable with itself.",
                        PatternLanguagePackage.Literals.COMPARE_CONSTRAINT__LEFT_OPERAND,
                        IssueCodes.SELF_COMPARE_CONSTRAINT);
                warning("Comparing a variable with itself.",
                        PatternLanguagePackage.Literals.COMPARE_CONSTRAINT__RIGHT_OPERAND,
                        IssueCodes.SELF_COMPARE_CONSTRAINT);
            }
        }
    }

    @Check
    public void checkRecursivePatternCall(PatternCall call) {
        Map<PatternCall, Set<PatternCall>> graph = cache.get(call.eResource(), call.eResource(), new CallGraphProvider(
                call.eResource()));

        Queue<PatternCall> queue = new LinkedList<PatternCall>();
        queue.add(call);
        Map<PatternCall, PatternCall> parentMap = new HashMap<PatternCall, PatternCall>();
        parentMap.put(call, null);

        // perform a simple BFS, taking care of a possible cycle
        while (!queue.isEmpty()) {
            PatternCall head = queue.poll();

            // the check ensures that we are not seeing the call for the first time and it is part of a cycle
            if (head == call && parentMap.get(call) != null) {
                break;
            }

            for (PatternCall target : graph.get(head)) {
                if (!parentMap.containsKey(target)) {
                    queue.add(target);
                }
                parentMap.put(target, head);
            }
        }

        if (parentMap.get(call) != null) {
            StringBuffer buffer = new StringBuffer();

            PatternCall act = call;
            do {
                buffer.insert(0, " -> " + prettyPrintPatternCall(act));
                act = parentMap.get(act);
            } while (act != call);

            buffer.insert(0, prettyPrintPatternCall(act));

            if (isNegativePatternCall(call)) {
                error(String.format(RECURSIVE_PATTERN_CALL, buffer.toString()), call,
                        PatternLanguagePackage.Literals.PATTERN_CALL__PATTERN_REF, IssueCodes.RECURSIVE_PATTERN_CALL);
            } else {
                warning(String.format(RECURSIVE_PATTERN_CALL, buffer.toString()), call,
                        PatternLanguagePackage.Literals.PATTERN_CALL__PATTERN_REF, IssueCodes.RECURSIVE_PATTERN_CALL);
            }
        }
    }

    private boolean isNegativePatternCall(PatternCall call) {
        return (call.eContainer() instanceof PatternCompositionConstraint && ((PatternCompositionConstraint) call
                .eContainer()).isNegative());
    }

    private String prettyPrintPatternCall(PatternCall call) {
        return (isNegativePatternCall(call) ? "neg " : "") + call.getPatternRef().getName();
    }

    private static class CallGraphProvider implements Provider<Map<PatternCall, Set<PatternCall>>> {

        private Resource resource;

        public CallGraphProvider(Resource resource) {
            this.resource = resource;
        }

        @Override
        public Map<PatternCall, Set<PatternCall>> get() {
            Map<PatternCall, Set<PatternCall>> graph = new HashMap<PatternCall, Set<PatternCall>>();
            TreeIterator<EObject> resourceIterator = resource.getAllContents();

            while (resourceIterator.hasNext()) {
                EObject resourceContent = resourceIterator.next();
                if (resourceContent instanceof PatternCall) {
                    PatternCall source = (PatternCall) resourceContent;
                    Set<PatternCall> targets = new HashSet<PatternCall>();
                    graph.put(source, targets);

                    TreeIterator<EObject> headIterator = source.getPatternRef().eAllContents();
                    while (headIterator.hasNext()) {
                        EObject headContent = headIterator.next();
                        if (headContent instanceof PatternCall) {
                            PatternCall target = (PatternCall) headContent;
                            targets.add(target);
                        }
                    }
                }
            }

            return graph;
        }
    }

    private String getName(PatternBody body) {
        if (body.getName() != null && !body.getName().isEmpty()) {
            return "'" + body.getName() + "'";
        }
        return null;
    }

    private String getTypeName(Class<? extends ValueReference> typeClass) {
        if (IntValue.class.isAssignableFrom(typeClass)) {
            return "Integer";
        } else if (DoubleValue.class.isAssignableFrom(typeClass)) {
            return "Double";
        } else if (BoolValue.class.isAssignableFrom(typeClass)) {
            return "Boolean";
        } else if (StringValue.class.isAssignableFrom(typeClass)) {
            return "String";
        } else if (ListValue.class.isAssignableFrom(typeClass)) {
            return "List";
        } else if (VariableValue.class.isAssignableFrom(typeClass)) {
            return "Variable";
        }
        return "UNDEFINED";
    }

    private String getConstantAsString(ValueReference ref) {
        if (ref instanceof IntValue) {
            return Integer.toString(((IntValue) ref).getValue());
        } else if (ref instanceof DoubleValue) {
            return Double.toString(((DoubleValue) ref).getValue());
        } else if (ref instanceof BoolValue) {
            return Boolean.toString(((BoolValue) ref).isValue());
        } else if (ref instanceof StringValue) {
            return "\"" + ((StringValue) ref).getValue() + "\"";
        } else if (ref instanceof ListValue) {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            for (Iterator<ValueReference> iter = ((ListValue) ref).getValues().iterator(); iter.hasNext();) {
                sb.append(getConstantAsString(iter.next()));
                if (iter.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append("}");
            return sb.toString();
        } else if (ref instanceof VariableValue) {
            return ((VariableValue) ref).getValue().getVar();
        }
        return "UNDEFINED";
    }

    private String getFormattedPattern(Pattern pattern) {
        StringBuilder builder = new StringBuilder();
        builder.append(pattern.getName());
        builder.append("(");
        for (Iterator<Variable> iter = pattern.getParameters().iterator(); iter.hasNext();) {
            builder.append(iter.next().getName());
            if (iter.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    protected String getFormattedArgumentsList(PatternCall call) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<ValueReference> iter = call.getParameters().iterator(); iter.hasNext();) {
            ValueReference parameter = iter.next();
            builder.append(getConstantAsString(parameter));
            if (iter.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    @Check
    public void checkPackageDeclaration(PatternModel model) {
        String packageName = model.getPackageName();

        if (packageName == null || packageName.isEmpty()) {
            error("The package declaration must not be empty",
                    PatternLanguagePackage.Literals.PATTERN_MODEL__PACKAGE_NAME, IssueCodes.PACKAGE_NAME_EMPTY);
        }

        if (packageName != null && !packageName.equals(packageName.toLowerCase())) {
            error("Only lowercase package names supported",
                    PatternLanguagePackage.Literals.PATTERN_MODEL__PACKAGE_NAME, IssueCodes.PACKAGE_NAME_MISMATCH);
        }
    }

    @Check
    public void checkReturnTypeOfCheckConstraints(CheckConstraint checkConstraint) {
        XExpression xExpression = checkConstraint.getExpression();
        if (xExpression != null) {
            final IResolvedTypes resolvedType = typeResolver.resolveTypes(xExpression);
            LightweightTypeReference type = resolvedType.getReturnType(xExpression);

            if (type.getPrimitiveIfWrapperType().getPrimitiveKind() != Primitive.Boolean) {
                error("Check expressions must return boolean instead of " + type.getSimpleName(), checkConstraint,
                        PatternLanguagePackage.Literals.CHECK_CONSTRAINT__EXPRESSION, IssueCodes.CHECK_MUST_BE_BOOLEAN);
            }
        }
    }

    @Check(CheckType.NORMAL)
    public void checkVariableNames(PatternBody body) {
        for (Variable var1 : body.getVariables()) {
            Variable otherVar = null;
            for (Variable var2 : body.getVariables()) {
                if (isNamedSingleUse(var1) && var1.getSimpleName().substring(1).equals(var2.getName())) {
                    otherVar = var2;
                }
            }
            if (otherVar != null) {
                if (var1.eContainer() instanceof PatternBody && !var1.getReferences().isEmpty()) {
                    // Local variables do not have source location
                    warning(String.format(
                            "Dubius variable naming: Single use variable %s shares its name with the variable %s",
                            var1.getSimpleName(), otherVar.getSimpleName()), var1.getReferences().get(0),
                            PatternLanguagePackage.Literals.VARIABLE_REFERENCE__VARIABLE,
                            IssueCodes.DUBIUS_VARIABLE_NAME);
                } else {
                    warning(String.format(
                            "Dubius variable naming: Single use variable %s shares its name with the variable %s",
                            var1.getSimpleName(), otherVar.getSimpleName()), var1,
                            PatternLanguagePackage.Literals.VARIABLE__NAME, IssueCodes.DUBIUS_VARIABLE_NAME);
                }
            }
        }
    }

    @Check(CheckType.NORMAL)
    public void checkVariableUsageCounters(PatternBody body) {
        UnionFindForVariables variableUnions = calculateEqualVariables(body);
        Map<Set<Variable>, VariableReferenceCount> unifiedRefCounters = new HashMap<Set<Variable>, VariableReferenceCount>();
        Map<Variable, VariableReferenceCount> individualRefCounters = new HashMap<Variable, VariableReferenceCount>();
        calculateUsageCounts(body, variableUnions, individualRefCounters, unifiedRefCounters);
        for (Variable var : body.getVariables()) {
            if (var instanceof ParameterRef) {
                checkParameterUsageCounter((ParameterRef) var, individualRefCounters, unifiedRefCounters,
                        variableUnions, body);
            } else {
                checkLocalVariableUsageCounter(var, individualRefCounters, unifiedRefCounters, variableUnions);
            }
        }
    }

    private void checkParameterUsageCounter(ParameterRef var, Map<Variable, VariableReferenceCount> individualCounters,
            Map<Set<Variable>, VariableReferenceCount> unifiedRefCounters, UnionFindForVariables variableUnions,
            PatternBody body) {
        Variable parameter = var.getReferredParam();
        VariableReferenceCount individualCounter = individualCounters.get(var);
        VariableReferenceCount unifiedCounter = unifiedRefCounters.get(variableUnions.getPartitionOfVariable(var));
        if (individualCounter.getReferenceCount() == 0) {
            error(String.format("Parameter '%s' is never referenced in body '%s'.", parameter.getName(),
                    getPatternBodyName(body)), parameter, PatternLanguagePackage.Literals.VARIABLE__NAME,
                    IssueCodes.SYMBOLIC_VARIABLE_NEVER_REFERENCED);
        } else if (unifiedCounter.getReferenceCount(ReferenceType.POSITIVE) == 0) {
            error(String.format("Parameter '%s' has no positive reference in body '%s'.", var.getName(),
                    getPatternBodyName(body)), parameter, PatternLanguagePackage.Literals.VARIABLE__NAME,
                    IssueCodes.SYMBOLIC_VARIABLE_NO_POSITIVE_REFERENCE);
        }
    }

    private void checkLocalVariableUsageCounter(Variable var, Map<Variable, VariableReferenceCount> individualCounters,
            Map<Set<Variable>, VariableReferenceCount> unifiedRefCounters, UnionFindForVariables variableUnions) {
        VariableReferenceCount individualCounter = individualCounters.get(var);
        VariableReferenceCount unifiedCounter = unifiedRefCounters.get(variableUnions.getPartitionOfVariable(var));
        if (individualCounter.getReferenceCount(ReferenceType.POSITIVE) == 1
                && individualCounter.getReferenceCount() == 1 && !isNamedSingleUse(var)
                && !isUnnamedSingleUseVariable(var)) {
            warning(String.format(
                    "Local variable '%s' is referenced only once. Is it mistyped? Start its name with '_' if intentional.",
                    var.getName()), var.getReferences().get(0),
                    PatternLanguagePackage.Literals.VARIABLE_REFERENCE__VAR, IssueCodes.LOCAL_VARIABLE_REFERENCED_ONCE);
        } else if (individualCounter.getReferenceCount() > 1 && isNamedSingleUse(var)) {
            for (VariableReference ref : var.getReferences()) {
                error(String.format("Named single-use variable %s used multiple times.", var.getName()), ref,
                        PatternLanguagePackage.Literals.VARIABLE_REFERENCE__VAR,
                        IssueCodes.ANONYM_VARIABLE_MULTIPLE_REFERENCE);

            }
        } else if (unifiedCounter.getReferenceCount(ReferenceType.POSITIVE) == 0) {
            if (unifiedCounter.getReferenceCount(ReferenceType.NEGATIVE) == 0) {
                error(String.format(
                        "Local variable '%s' appears in read-only context(s) only, thus its value cannot be determined.",
                        var.getName()), var, PatternLanguagePackage.Literals.VARIABLE__NAME,
                        IssueCodes.LOCAL_VARIABLE_READONLY);
            } else if (individualCounter.getReferenceCount(ReferenceType.NEGATIVE) == 1
                    && individualCounter.getReferenceCount() == 1 && !isNamedSingleUse(var)
                    && !isUnnamedSingleUseVariable(var)) {
                warning(String.format(
                        "Local variable '%s' will be quantified because it is used only here. Acknowledge this by prefixing its name with '_'.",
                        var.getName()), var.getReferences().get(0),
                        PatternLanguagePackage.Literals.VARIABLE_REFERENCE__VAR,
                        IssueCodes.LOCAL_VARIABLE_QUANTIFIED_REFERENCE);
            } else if (unifiedCounter.getReferenceCount() > 1) {
                error(String.format(
                        "Local variable '%s' has no positive reference, thus its value cannot be determined.",
                        var.getName()), var.getReferences().get(0),
                        PatternLanguagePackage.Literals.VARIABLE_REFERENCE__VAR,
                        IssueCodes.LOCAL_VARIABLE_NO_POSITIVE_REFERENCE);
            }
        }
    }

    // private int getReferenceCount(Variable var, ReferenceType type, Map<Variable, VariableReferenceCount>
    // refCounters,
    // UnionFindForVariables variableUnions) {
    // int sum = 0;
    // for (Variable unionVar : variableUnions.getPartitionOfVariable(var)) {
    // sum += refCounters.get(unionVar).getReferenceCount(type);
    // }
    // return sum;
    // }

    private void calculateUsageCounts(PatternBody body, UnionFindForVariables variableUnions,
            Map<Variable, VariableReferenceCount> individualRefCounters,
            Map<Set<Variable>, VariableReferenceCount> unifiedRefCounters) {
        for (Variable var : body.getVariables()) {
            boolean isParameter = var instanceof ParameterRef;
            individualRefCounters.put(var, new VariableReferenceCount(Collections.singleton(var), isParameter));
        }
        for (Set<Variable> partition : variableUnions.getPartitions()) {
            boolean isParameter = false;
            for (Variable var : partition) {
                if (var instanceof ParameterRef) {
                    isParameter = true;
                    break;
                }
            }
            unifiedRefCounters.put(partition, new VariableReferenceCount(partition, isParameter));
        }

        TreeIterator<EObject> it = body.eAllContents();
        while (it.hasNext()) {
            EObject obj = it.next();
            if (obj instanceof XExpression) {
                XExpression expression = (XExpression) obj;
                for (Variable var : CorePatternLanguageHelper.getReferencedPatternVariablesOfXExpression(expression,
                        associations)) {
                    individualRefCounters.get(var).incrementCounter(ReferenceType.READ_ONLY);
                    unifiedRefCounters.get(variableUnions.getPartitionOfVariable(var)).incrementCounter(
                            ReferenceType.READ_ONLY);
                }
                it.prune();
            }
            if (obj instanceof VariableReference) {
                final VariableReference ref = (VariableReference) obj;
                final Variable var = ref.getVariable();
                final ReferenceType referenceClass = classifyReference(ref);
                individualRefCounters.get(var).incrementCounter(referenceClass);
                unifiedRefCounters.get(variableUnions.getPartitionOfVariable(var)).incrementCounter(referenceClass);
            }
        }
    }

    private UnionFindForVariables calculateEqualVariables(PatternBody body) {
        UnionFindForVariables unions = new UnionFindForVariables(body.getVariables());
        TreeIterator<EObject> it = body.eAllContents();
        while (it.hasNext()) {
            EObject obj = it.next();
            if (obj instanceof CompareConstraint) {
                CompareConstraint constraint = (CompareConstraint) obj;
                if (constraint.getFeature() == CompareFeature.EQUALITY) {
                    ValueReference left = constraint.getLeftOperand();
                    ValueReference right = constraint.getRightOperand();
                    if (left instanceof VariableValue && right instanceof VariableValue) {
                        unions.unite(ImmutableSet.of(((VariableValue) left).getValue().getVariable(),
                                ((VariableValue) right).getValue().getVariable()));
                    }
                }
                it.prune();
            } else if (obj instanceof Constraint) {
                it.prune();
            }
        }
        return unions;
    }

    private String getPatternBodyName(PatternBody patternBody) {
        return (patternBody.getName() != null) ? patternBody.getName() : String.format("#%d",
                ((Pattern) patternBody.eContainer()).getBodies().indexOf(patternBody) + 1);
    }

    private ReferenceType classifyReference(VariableReference ref) {
        EObject parent = ref;
        while (parent != null
                && !(parent instanceof Constraint || parent instanceof AggregatedValue || parent instanceof FunctionEvaluationValue)) {
            parent = parent.eContainer();
        }

        if (parent instanceof CheckConstraint) {
            return ReferenceType.READ_ONLY;
        } else if (parent instanceof FunctionEvaluationValue) { // this should not be a variableReference, so probably
                                                                // this will not happen
            return ReferenceType.READ_ONLY;
        } else if (parent instanceof CompareConstraint) {
            CompareConstraint constraint = (CompareConstraint) parent;
            if (constraint.getFeature() == CompareFeature.EQUALITY) {
                final boolean leftIsVariable = constraint.getLeftOperand() instanceof VariableValue;
                final boolean rightIsVariable = constraint.getRightOperand() instanceof VariableValue;
                if (leftIsVariable && rightIsVariable) {
                    // A==A equivalence between unified variables...
                    // should be ignored in reference counting, except that it spoils quantification
                    return ReferenceType.READ_ONLY;
                } else if (leftIsVariable && !rightIsVariable) {
                    if (ref.equals(((VariableValue) constraint.getLeftOperand()).getValue())) { // this should always be
                                                                                                // true
                        return ReferenceType.POSITIVE;
                    } else
                        reportStrangeVariableRef(ref, constraint);
                } else if (rightIsVariable && !leftIsVariable) {
                    if (ref.equals(((VariableValue) constraint.getRightOperand()).getValue())) { // this should always
                                                                                                 // be true
                        return ReferenceType.POSITIVE;
                    } else
                        reportStrangeVariableRef(ref, constraint);
                } else
                    reportStrangeVariableRef(ref, constraint);
            } else if (constraint.getFeature() == CompareFeature.INEQUALITY) {
                return ReferenceType.READ_ONLY;
            } else
                reportStrangeVariableRef(ref, constraint);
        } else if (parent instanceof PatternCompositionConstraint
                && ((PatternCompositionConstraint) parent).isNegative()) {
            return ReferenceType.NEGATIVE;
        } else if (parent instanceof AggregatedValue) {
            return ReferenceType.NEGATIVE;
        }
        // Other constraints use positive references
        return ReferenceType.POSITIVE;
    }

    private void reportStrangeVariableRef(VariableReference ref, CompareConstraint constraint) {
        throw new IllegalStateException( // this should never come up
                "Strange reference to variable " + ref.getVar() + " in " + constraint.getClass().getName());
    }

    /**
     * @return true if the variable is single-use a named variable
     */
    public boolean isNamedSingleUse(Variable variable) {
        String name = variable.getName();
        return name != null && name.startsWith("_") && !name.contains("<");
    }

    /**
     * @return true if the variable is an unnamed single-use variable
     */
    public boolean isUnnamedSingleUseVariable(Variable variable) {
        String name = variable.getName();
        return name != null && name.startsWith("_") && name.contains("<");
    }

    @Check(CheckType.NORMAL)
    public void checkForImpureJavaCallsInCheckConstraints(CheckConstraint checkConstraint) {
        checkForImpureJavaCallsInternal(checkConstraint.getExpression(),
                PatternLanguagePackage.Literals.CHECK_CONSTRAINT__EXPRESSION);
    }

    @Check(CheckType.NORMAL)
    public void checkForImpureJavaCallsInEvalExpressions(FunctionEvaluationValue eval) {
        checkForImpureJavaCallsInternal(eval.getExpression(),
                PatternLanguagePackage.Literals.FUNCTION_EVALUATION_VALUE__EXPRESSION);
    }

    private void checkForImpureJavaCallsInternal(XExpression xExpression, EStructuralFeature feature) {
        Set<String> elementsWithWarnings = new HashSet<String>();
        if (xExpression != null) {
            TreeIterator<EObject> eAllContents = xExpression.eAllContents();
            while (eAllContents.hasNext()) {
                EObject nextEObject = eAllContents.next();
                if (nextEObject instanceof XMemberFeatureCall) {
                    XMemberFeatureCall xFeatureCall = (XMemberFeatureCall) nextEObject;
                    JvmIdentifiableElement jvmIdentifiableElement = xFeatureCall.getFeature();
                    if (jvmIdentifiableElement instanceof JvmOperation) {
                        JvmOperation jvmOperation = (JvmOperation) jvmIdentifiableElement;
                        if (purityChecker.isImpureElement(jvmOperation)) {
                            elementsWithWarnings.add(jvmOperation.getQualifiedName());
                        }
                    }
                }
            }
        }
        if (!elementsWithWarnings.isEmpty()) {
            if (elementsWithWarnings.size() > 1) {
                warning("There are potentially problematic java calls in the check()/eval() expression. Custom java calls without @Pure annotations "
                        + "considered unsafe in IncQuery. The possible erroneous calls are the following: "
                        + elementsWithWarnings + ".", xExpression.eContainer(), feature,
                        IssueCodes.CHECK_WITH_IMPURE_JAVA_CALLS);
            } else {
                warning("There is a potentially problematic java call in the check()/eval() expression. Custom java calls without @Pure annotations "
                        + "considered unsafe in IncQuery. The possible erroneous call is the following: "
                        + elementsWithWarnings + ".", xExpression.eContainer(), feature,
                        IssueCodes.CHECK_WITH_IMPURE_JAVA_CALLS);
            }
        }
    }

    @Override
    public void warning(String message, EObject source, EStructuralFeature feature, String code, String... issueData) {
        super.warning(message, source, feature, code, issueData);
    }

    @Override
    public void error(String message, EObject source, EStructuralFeature feature, String code, String... issueData) {
        super.error(message, source, feature, code, issueData);
    }

}