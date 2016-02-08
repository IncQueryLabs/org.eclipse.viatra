/*******************************************************************************
 * Copyright (c) 2010-2012, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.patternlanguage.emf;

import org.apache.log4j.Logger;
import org.eclipse.incquery.patternlanguage.emf.annotations.AnnotationExpressionValidator;
import org.eclipse.incquery.patternlanguage.emf.jvmmodel.EMFPatternJvmModelAssociator;
import org.eclipse.incquery.patternlanguage.emf.jvmmodel.EMFPatternLanguageJvmModelInferrer;
import org.eclipse.incquery.patternlanguage.emf.scoping.CompoundMetamodelProviderService;
import org.eclipse.incquery.patternlanguage.emf.scoping.EMFPatternLanguageDeclarativeScopeProvider;
import org.eclipse.incquery.patternlanguage.emf.scoping.EMFPatternLanguageImportNamespaceProvider;
import org.eclipse.incquery.patternlanguage.emf.scoping.EMFPatternLanguageLinkingService;
import org.eclipse.incquery.patternlanguage.emf.scoping.IMetamodelProvider;
import org.eclipse.incquery.patternlanguage.emf.scoping.IMetamodelProviderInstance;
import org.eclipse.incquery.patternlanguage.emf.scoping.MetamodelProviderService;
import org.eclipse.incquery.patternlanguage.emf.scoping.ResourceSetMetamodelProviderService;
import org.eclipse.incquery.patternlanguage.emf.serializer.EMFPatternLanguageCrossRefSerializer;
import org.eclipse.incquery.patternlanguage.emf.types.EMFPatternTypeProvider;
import org.eclipse.incquery.patternlanguage.emf.types.EMFTypeInferrer;
import org.eclipse.incquery.patternlanguage.emf.types.EMFTypeSystem;
import org.eclipse.incquery.patternlanguage.emf.types.IEMFTypeProvider;
import org.eclipse.incquery.patternlanguage.emf.util.IClassLoaderProvider;
import org.eclipse.incquery.patternlanguage.emf.util.IErrorFeedback;
import org.eclipse.incquery.patternlanguage.emf.util.IErrorFeedback.EmptyErrorFeedback;
import org.eclipse.incquery.patternlanguage.emf.util.SimpleClassLoaderProvider;
import org.eclipse.incquery.patternlanguage.emf.validation.EMFPatternLanguageJavaValidator;
import org.eclipse.incquery.patternlanguage.emf.validation.EMFPatternLanguageSyntaxErrorMessageProvider;
import org.eclipse.incquery.patternlanguage.scoping.MyAbstractDeclarativeScopeProvider;
import org.eclipse.incquery.patternlanguage.scoping.PatternLanguageResourceDescriptionStrategy;
import org.eclipse.incquery.patternlanguage.typing.ITypeInferrer;
import org.eclipse.incquery.patternlanguage.typing.ITypeSystem;
import org.eclipse.incquery.patternlanguage.validation.IIssueCallback;
import org.eclipse.xtext.linking.ILinkingService;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.parser.antlr.ISyntaxErrorMessageProvider;
import org.eclipse.xtext.resource.IDefaultResourceDescriptionStrategy;
import org.eclipse.xtext.resource.IGlobalServiceProvider;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider;
import org.eclipse.xtext.serializer.tokens.ICrossReferenceSerializer;
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelInferrer;
import org.eclipse.xtext.xbase.jvmmodel.ILogicalContainerProvider;
import org.eclipse.xtext.xbase.jvmmodel.JvmModelAssociator;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
public class EMFPatternLanguageRuntimeModule extends AbstractEMFPatternLanguageRuntimeModule {

    @Provides
    Logger provideLoggerImplementation() {
        return Logger.getLogger(EMFPatternLanguageRuntimeModule.class);
    }

    @Override
    public Class<? extends ILinkingService> bindILinkingService() {
        return EMFPatternLanguageLinkingService.class;
    }

    @Override
    public void configureIScopeProviderDelegate(Binder binder) {
        binder.bind(IScopeProvider.class).annotatedWith(Names.named(AbstractDeclarativeScopeProvider.NAMED_DELEGATE))
                .to(EMFPatternLanguageDeclarativeScopeProvider.class);
        binder.bind(IScopeProvider.class).annotatedWith(Names.named(MyAbstractDeclarativeScopeProvider.NAMED_DELEGATE))
                .to(EMFPatternLanguageImportNamespaceProvider.class);
        // .to(XImportSectionNamespaceScopeProvider.class);
        Multibinder<IMetamodelProviderInstance> metamodelProviderBinder = Multibinder.newSetBinder(binder, IMetamodelProviderInstance.class);
        metamodelProviderBinder.addBinding().to(MetamodelProviderService.class);
        metamodelProviderBinder.addBinding().to(ResourceSetMetamodelProviderService.class);
    }

    @Override
    public Class<? extends IDefaultResourceDescriptionStrategy> bindIDefaultResourceDescriptionStrategy() {
        return PatternLanguageResourceDescriptionStrategy.class;
    }

    public Class<? extends IEMFTypeProvider> bindIEMFTypeProvider() {
        return EMFPatternTypeProvider.class;
    }

    public Class<? extends IMetamodelProvider> bindIMetamodelProvider() {
        return CompoundMetamodelProviderService.class;
    }

    public Class<? extends ICrossReferenceSerializer> bindICrossReferenceSerializer() {
        return EMFPatternLanguageCrossRefSerializer.class;
    }

    public Class<? extends ISyntaxErrorMessageProvider> bindISyntaxErrorMessageProvider() {
        return EMFPatternLanguageSyntaxErrorMessageProvider.class;
    }

    @Override
    public Class<? extends IQualifiedNameProvider> bindIQualifiedNameProvider() {
        return EMFPatternLanguageQualifiedNameProvider.class;
    }

    public Class<? extends IGlobalServiceProvider> bindIGlobalServiceProvider() {
        return EMFPatternLanguageServiceProvider.class;
    }

    public Class<? extends AnnotationExpressionValidator> bindAnnotationExpressionValidator() {
        return AnnotationExpressionValidator.class;
    }

    public Class<? extends IIssueCallback> bindIIssueCallback() {
        return EMFPatternLanguageJavaValidator.class;
    }

    public Class<? extends IClassLoaderProvider> bindIClassLoaderProvider() {
        return SimpleClassLoaderProvider.class;
    }

    public Class<? extends IJvmModelInferrer> bindIJvmModelInferrer() {
        return EMFPatternLanguageJvmModelInferrer.class;
    }

    public Class<? extends ILogicalContainerProvider> bindILogicalContainerProvider() {
        return EMFPatternJvmModelAssociator.class;
    }

    public Class<? extends JvmModelAssociator> bindJvmModelAssociator() {
        return EMFPatternJvmModelAssociator.class;
    }

    public Class<? extends IErrorFeedback> bindIErrorFeedback() {
        return EmptyErrorFeedback.class;
    }
    
    public Class<? extends ITypeSystem> bindITypeSystem() {
        return EMFTypeSystem.class;
    }
    
    public Class<? extends ITypeInferrer> bindITypeInferrer() {
        return EMFTypeInferrer.class;
    }
    
}
