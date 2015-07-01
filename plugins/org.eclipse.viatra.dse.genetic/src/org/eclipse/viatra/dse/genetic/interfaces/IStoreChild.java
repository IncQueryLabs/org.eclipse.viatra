/*******************************************************************************
 * Copyright (c) 2010-2014, Miklos Foldenyi, Andras Szabolcs Nagy, Abel Hegedus, Akos Horvath, Zoltan Ujhelyi and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *   Miklos Foldenyi - initial API and implementation
 *   Andras Szabolcs Nagy - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.dse.genetic.interfaces;

import java.io.IOException;

import org.eclipse.viatra.dse.base.ThreadContext;

public interface IStoreChild {

    void addChild(ThreadContext context);
    
    void setTrajectoriesFileName(String fileName);

    String getTrajectoriesFileName();
    
    void saveTrajectoriesToFile() throws IOException;
}
