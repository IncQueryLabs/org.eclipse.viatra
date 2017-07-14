/*******************************************************************************
 * Copyright (c) 2010-2016, Abel Hegedus, IncQuery Labs Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.tooling.ui.queryresult.properties

import java.util.ArrayList
import java.util.Arrays
import org.eclipse.emf.common.ui.celleditor.ExtendedComboBoxCellEditor
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EDataType
import org.eclipse.emf.ecore.EEnum
import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry
import org.eclipse.emf.edit.ui.provider.PropertyDescriptor.EDataTypeCellEditor
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.views.properties.IPropertyDescriptor
import org.eclipse.viatra.query.runtime.base.api.IndexingLevel
import org.eclipse.viatra.query.runtime.emf.EMFBaseIndexWrapper
import org.eclipse.viatra.query.runtime.emf.types.EClassTransitiveInstancesKey
import org.eclipse.viatra.query.runtime.emf.types.EDataTypeInSlotsKey
import org.eclipse.viatra.query.runtime.matchers.context.IInputKey
import org.eclipse.viatra.query.runtime.matchers.context.common.JavaTransitiveInstancesKey
import org.eclipse.viatra.query.runtime.matchers.psystem.queries.PParameter
import org.eclipse.viatra.query.tooling.ui.queryresult.QueryResultTreeMatcher
import org.eclipse.viatra.query.tooling.ui.queryresult.util.QueryResultViewUtil
import org.eclipse.xtend.lib.annotations.Accessors

/**
 * @author Abel Hegedus
 *
 */
@Accessors
class MatchParameterPropertyDescriptor implements IPropertyDescriptor {
    
    String category = "Filters"
    val PParameter parameter
    val QueryResultTreeMatcher matcher
    final val adapterFactory = QueryResultViewUtil.getGenericAdapterFactory();
    final val adapterFactoryItemDelegator = new AdapterFactoryItemDelegator(adapterFactory)
    final val adapterFactoryLabelProvider = new AdapterFactoryLabelProvider(adapterFactory)
    /**
     *  delegator handles EList as well
     */
    final val labelProvider = new LabelProvider() {
        
        override getText(Object element) {
            adapterFactoryItemDelegator.getText(element)
        }
        
        override getImage(Object element) {
            ExtendedImageRegistry.INSTANCE.getImage(adapterFactoryItemDelegator.getImage(element))
        }
    }
                
    override createPropertyEditor(Composite parent) {
        val typeKey = parameter.declaredUnaryType
        
        if(typeKey !== null){
            val result = parent.prepareEditorForDeclaredType(typeKey)
            return result
        } else {
            return parent.prepareEditorForUnknownType
        }
        
    }
    
    protected def prepareEditorForDeclaredType(Composite parent, IInputKey typeKey) {
        val result = switch typeKey {
            JavaTransitiveInstancesKey: {
                return parent.prepareEditorForUnknownType
            }
            EClassTransitiveInstancesKey: {
                val eClass = typeKey.emfKey
                val choiceOfValues = eClass.choiceOfValues
                
                new ExtendedComboBoxCellEditor(
                    parent,
                    new ArrayList<Object>(choiceOfValues),
                    labelProvider,
                    true
                )
            }
            EDataTypeInSlotsKey: {
                val dataType = typeKey.emfKey
                if(dataType instanceof EEnum){
                    val choiceOfValues = dataType.ELiterals.map[instance]
                    new ExtendedComboBoxCellEditor(
                        parent,
                        new ArrayList<Object>(choiceOfValues),
                        labelProvider,
                        false
                    )
                } else {
                    if(dataType.instanceClass == Boolean || dataType.instanceClass == Boolean.TYPE){
                        new ExtendedComboBoxCellEditor(
                          parent,
                          Arrays.asList(#[ Boolean.FALSE, Boolean.TRUE ]),
                          labelProvider,
                          true)
                    } else {
                        new EDataTypeCellEditor(dataType, parent)
                    }
                }
            }
            default: {
                if(parameter.typeName == Boolean.name){
                    new ExtendedComboBoxCellEditor(
                          parent,
                          Arrays.asList(#[ Boolean.FALSE, Boolean.TRUE ]),
                          labelProvider,
                          true);
                } else {
                    null
                }
            }
        }
        return result
    }
    
    /**
     *  we don't know the type, so we just provide a list of possible values
     */
    protected def prepareEditorForUnknownType(Composite parent) {
        val choiceOfValues = matcher.matcher.getAllValues(parameter.name)
        val editor =new ExtendedComboBoxCellEditor(
            parent,
            new ArrayList<Object>(choiceOfValues),
            labelProvider,
            false
        )
        return editor
    }
    
    protected def getChoiceOfValues(EClass eClass) {
        val choiceOfValues = newArrayList()
        val emfBaseIndex = matcher.matcher.engine.baseIndex as EMFBaseIndexWrapper
        val baseIndex = emfBaseIndex.wrapped
        if(baseIndex.isInWildcardMode(IndexingLevel.FULL) || baseIndex.getIndexingLevel(eClass) == IndexingLevel.FULL) {
            val allInstances = baseIndex.getAllSurrogateInstances(eClass)
            choiceOfValues += allInstances
        } else {
            choiceOfValues += matcher.matcher.getAllValues(parameter.name)
        }
        return choiceOfValues
    }
    
    protected def getChoiceOfValues(EDataType eDataType) {
        val choiceOfValues = newArrayList()
        val emfBaseIndex = matcher.matcher.engine.baseIndex as EMFBaseIndexWrapper
        val baseIndex = emfBaseIndex.wrapped
        if(baseIndex.isInWildcardMode(IndexingLevel.FULL) || baseIndex.getIndexingLevel(eDataType) == IndexingLevel.FULL) {
            val allInstances = baseIndex.getDataTypeInstances(eDataType)
            choiceOfValues += allInstances
        } else {
            choiceOfValues += matcher.matcher.getAllValues(parameter.name)
        }
        return choiceOfValues
    }
    
    override getCategory() {
        return category
    }
    
    override getDescription() {
        return '''Filter for parameter «parameter.name» with type «parameter.declaredUnaryType»'''
    }
    
    override getDisplayName() {
        return parameter.name
    }
    
    override getFilterFlags() {
        return null
    }
    
    override getHelpContextIds() {
        return null
    }
    
    override getId() {
        return parameter
    }
    
    override getLabelProvider() {
        return labelProvider
    }
    
    override isCompatibleWith(IPropertyDescriptor anotherProperty) {
        return false
    }
    
}