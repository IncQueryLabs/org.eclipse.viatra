/*******************************************************************************
 * Copyright (c) 2004-2014, Istvan David, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Istvan David - initial API and implementation
 *******************************************************************************/

package org.eclipse.viatra.cep.vepl.jvmmodel.expressiontree;

import java.util.List;

import org.eclipse.viatra.cep.core.metamodels.events.EventsFactory;
import org.eclipse.viatra.cep.core.metamodels.events.Timewindow;
import org.eclipse.viatra.cep.vepl.vepl.AbstractMultiplicity;
import org.eclipse.viatra.cep.vepl.vepl.Atom;
import org.eclipse.viatra.cep.vepl.vepl.ChainedExpression;
import org.eclipse.viatra.cep.vepl.vepl.ComplexEventExpression;
import org.eclipse.viatra.cep.vepl.vepl.ComplexEventOperator;
import org.eclipse.viatra.cep.vepl.vepl.FollowsOperator;
import org.eclipse.viatra.cep.vepl.vepl.VeplFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Algorithm for building the {@link ExpressionTree} from the parsed Xtext model.
 * 
 * @author Istvan David
 * 
 */
public class ExpressionTreeBuilder {
    private static ExpressionTreeBuilder instance;

    public static ExpressionTreeBuilder getInstance() {
        if (instance == null) {
            instance = new ExpressionTreeBuilder();
        }
        return instance;
    }

    public ExpressionTree buildExpressionTree(ComplexEventExpression expression) {
        ExpressionTree tree = buildTree(expression);
        while (!done(tree)) {
            decomposeComplexLeaves(tree);
        }
        return tree;
    }

    private ExpressionTree buildTree(ComplexEventExpression expression) {
        if (expression instanceof Atom) {
            return new AtomicExpressionTree((Atom) expression, expression.getMultiplicity(), getTimewindow(expression));
        }

        if (expression.getNegOperator() != null) {
            ComplexEventExpression newExpression = expression.getLeft();
            ExpressionTree subTree = buildTree(newExpression);

            createNode(VeplFactory.eINSTANCE.createNegOperator(), newExpression, subTree);

            return subTree;
        }

        if (expressionInParenthesis(expression)) {
            ComplexEventExpression newExpression = expression.getLeft();

            ExpressionTree subTree = buildTree(newExpression);

            if (!(newExpression instanceof Atom)
            // the condition below prevents unnecessary bracketing and anonymous pattern creation
                    && (newExpression.getMultiplicity() != null || getTimewindow(newExpression) != null)) {
                createNode(VeplFactory.eINSTANCE.createFollowsOperator(), newExpression, subTree);
            }

            return subTree;
        }

        ExpressionTree tree = new ExpressionTree();

        ComplexEventOperator lastOperator = expression.getRight() != null ? expression.getRight().get(0).getOperator()
                : null;
        List<ExpressionGroupElement> currentExpressionGroup = Lists.newArrayList();

        ComplexEventExpression head = expression.getLeft();
        currentExpressionGroup.add(new ExpressionGroupElement(head, head.getMultiplicity(), getTimewindow(head)));

        for (ChainedExpression che : expression.getRight()) {
            if (!sameOperators(che.getOperator(), lastOperator)) {
                packagePatternGroup(lastOperator, currentExpressionGroup, tree);
                currentExpressionGroup.clear();
                lastOperator = che.getOperator();
            }

            ComplexEventExpression tail = che.getExpression();
            if (tail.getMultiplicity() != null) {
                currentExpressionGroup
                        .add(new ExpressionGroupElement(tail, tail.getMultiplicity(), getTimewindow(tail)));
            } else {
                currentExpressionGroup.add(new ExpressionGroupElement(tail, getTimewindow(tail)));
            }

        }

        packagePatternGroup(lastOperator, currentExpressionGroup, tree);

        return tree;
    }

    private void createNode(ComplexEventOperator operator, ComplexEventExpression newExpression, ExpressionTree subTree) {
        Node node = new Node(operator, getMultiplicity(newExpression), getTimewindow(newExpression));
        node.addChild(subTree.getRoot());
        subTree.setRoot(node);
    }

    private void packagePatternGroup(ComplexEventOperator operator, List<ExpressionGroupElement> expressionGroup,
            ExpressionTree tree) {
        Node node = addNode(operator, tree);

        if (operator instanceof FollowsOperator) {
            packageRightCurried(operator, expressionGroup, tree, node);
        } else {
            packageNonCurried(operator, expressionGroup, tree, node);
        }
    }

    private void packageRightCurried(ComplexEventOperator operator, List<ExpressionGroupElement> expressionGroup,
            ExpressionTree tree, Node root) {
        Preconditions.checkArgument(operator instanceof FollowsOperator);

        if (expressionGroup.size() <= 1) {
            packageNonCurried(operator, expressionGroup, tree, root);
        } else {
            Node node;
            if (root.getChildren().isEmpty()) {
                node = root;
            } else {
                node = new Node(operator);
                root.addChild(node);
            }

            ExpressionGroupElement groupElement = expressionGroup.remove(0);
            createSubTree(operator, groupElement, tree, node);

            packageRightCurried(operator, expressionGroup, tree, node);
        }
    }

    private void packageNonCurried(ComplexEventOperator operator, List<ExpressionGroupElement> expressionGroup,
            ExpressionTree tree, Node node) {
        for (ExpressionGroupElement groupElement : expressionGroup) {
            createSubTree(operator, groupElement, tree, node);
        }
    }

    private void createSubTree(ComplexEventOperator operator, ExpressionGroupElement groupElement, ExpressionTree tree,
            Node node) {
        TreeElement treeElement = createTreeElement(groupElement);
        node.addChild(treeElement);
        if (!(groupElement.getComplexEventExpression() instanceof Atom)) {
            if (treeElement instanceof Node) {
                tree.getComplexLeaves().add((Leaf) ((Node) treeElement).getChildren().get(0));
            } else if (treeElement instanceof Leaf) {
                tree.getComplexLeaves().add((Leaf) treeElement);
            }
        }
    }

    private Node addNode(ComplexEventOperator operator, ExpressionTree tree) {
        Node node = new Node(operator);
        if (tree.getRoot() == null) {
            tree.setRoot(node);
        } else {
            node.addChild(tree.getRoot());
            tree.setRoot(node);
        }
        return node;
    }

    private TreeElement createTreeElement(ExpressionGroupElement groupElement) {
        Leaf leaf = new Leaf(groupElement.getComplexEventExpression());

        if (groupElement.getMultiplicity() != null || groupElement.getTimewindow() != null) {
            Node node = new Node(VeplFactory.eINSTANCE.createFollowsOperator(), groupElement.getMultiplicity(),
                    groupElement.getTimewindow());
            node.addChild(leaf);
            return node;
        }

        return leaf;
    }

    private void decomposeComplexLeaves(ExpressionTree parentTree) {
        for (Leaf leaf : parentTree.getComplexLeaves()) {
            Node parentNode = leaf.getParentNode();

            int leafPosition = parentNode.getChildren().indexOf(leaf);

            // decompose
            ComplexEventExpression expression = leaf.getExpression();
            ExpressionTree subTree = buildTree(expression);

            // replace
            parentNode.getChildren().set(leafPosition, subTree.getRoot());
            subTree.getRoot().setParentNode(parentNode);

            // update global complex leaf list
            for (Leaf complexLeafOfTheSubTree : subTree.getComplexLeaves()) {
                parentTree.getComplexLeaves().add(complexLeafOfTheSubTree);
            }

            // remove obsolete leaf
            parentTree.getComplexLeaves().remove(leaf);
            parentNode.getChildren().remove(leaf);
        }
    }

    private Timewindow getTimewindow(ComplexEventExpression expression) {
        if (expression.getTimewindow() != null && expression.getTimewindow().getLength() > 0) {
            Timewindow timeWindow = EventsFactory.eINSTANCE.createTimewindow();
            timeWindow.setTime(expression.getTimewindow().getLength());
            return timeWindow;
        }
        return null;
    }

    private AbstractMultiplicity getMultiplicity(ComplexEventExpression expression) {
        if (expression.getMultiplicity() != null) {
            return expression.getMultiplicity();
        }
        org.eclipse.viatra.cep.vepl.vepl.Multiplicity multiplicity = VeplFactory.eINSTANCE.createMultiplicity();
        multiplicity.setValue(1);
        return multiplicity;
    }

    private boolean expressionInParenthesis(ComplexEventExpression expression) {
        return expression.getLeft() != null && (expression.getRight() == null || expression.getRight().isEmpty());
    }

    private boolean sameOperators(ComplexEventOperator operator1, ComplexEventOperator operator2) {
        return operator1.getClass().equals(operator2.getClass());
    }

    private boolean done(ExpressionTree tree) {
        return tree.getComplexLeaves().isEmpty();
    }
}
