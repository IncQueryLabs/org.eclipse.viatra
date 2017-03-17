/*******************************************************************************
 * Copyright (c) 2004-2014, Istvan David, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Istvan David - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.cep.vepl.ui;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.viatra.cep.vepl.ui.builder.VeplBuilderParticipant;
import org.eclipse.viatra.cep.vepl.ui.syntaxhighlight.CepDslAntlrTokenToAttributeIdMapper;
import org.eclipse.viatra.cep.vepl.ui.syntaxhighlight.CepDslHighlightingConfiguration;
import org.eclipse.viatra.cep.vepl.ui.syntaxhighlight.VeplSemanticHighlightingCalculator;
import org.eclipse.xtext.builder.IXtextBuilderParticipant;
import org.eclipse.xtext.ui.editor.contentassist.XtextContentAssistProcessor;
import org.eclipse.xtext.ui.editor.syntaxcoloring.AbstractAntlrTokenToAttributeIdMapper;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 * Use this class to register components to be used within the IDE.
 */
public class VeplUiModule extends org.eclipse.viatra.cep.vepl.ui.AbstractVeplUiModule {
    private static final String LOGGER_ROOT = "org.eclipse.viatra.cep";

    public VeplUiModule(AbstractUIPlugin plugin) {
        super(plugin);
    }

    @Provides
    @Singleton
    Logger provideLoggerImplementation() {
        Logger logger = Logger.getLogger(LOGGER_ROOT);
        logger.setAdditivity(false);
        logger.addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
        logger.addAppender(new EclipseLogAppender());
        return logger;
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(String.class)
                .annotatedWith(Names.named(XtextContentAssistProcessor.COMPLETION_AUTO_ACTIVATION_CHARS))
                .toInstance(".,");
    }

    @Override
    public Class<? extends IXtextBuilderParticipant> bindIXtextBuilderParticipant() {
        return VeplBuilderParticipant.class;
    }

    public Class<? extends IHighlightingConfiguration> bindIHighlightingConfiguration() {
        return CepDslHighlightingConfiguration.class;
    }

    @Override
    public Class<? extends ISemanticHighlightingCalculator> bindISemanticHighlightingCalculator() {
        return VeplSemanticHighlightingCalculator.class;
    }

    public Class<? extends AbstractAntlrTokenToAttributeIdMapper> bindAbstractAntlrTokenToAttributeIdMapper() {
        return CepDslAntlrTokenToAttributeIdMapper.class;
    }
}
