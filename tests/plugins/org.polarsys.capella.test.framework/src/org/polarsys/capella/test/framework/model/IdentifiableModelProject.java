/*******************************************************************************
 * Copyright (c) 2019 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.test.framework.model;

import java.lang.reflect.Field;

import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.shared.id.handler.IdManager;
import org.polarsys.capella.shared.id.handler.ResourceSetScope;
import org.polarsys.capella.test.framework.api.BasicTestCase;

public abstract class IdentifiableModelProject extends BasicTestCase {
  
  protected abstract Class<?> getModelClass();
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    for (String s : getRequiredTestModels()) {
      ResourceSetScope scope = new ResourceSetScope(getSession(s).getTransactionalEditingDomain().getResourceSet());
      for (Field field : getModelClass().getDeclaredFields()) {
        if (field.getAnnotation(Identifier.class) != null) {
          EObject obj = IdManager.getInstance().getEObject(((Identifier) field.getAnnotation(Identifier.class)).id(), scope);
          field.set(this, obj);
        }
      }
    }
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    for (Field field : getModelClass().getDeclaredFields()) {
      if (field.getAnnotation(Identifier.class) != null) {
        field.set(this, null);
      }
    }
  }

}