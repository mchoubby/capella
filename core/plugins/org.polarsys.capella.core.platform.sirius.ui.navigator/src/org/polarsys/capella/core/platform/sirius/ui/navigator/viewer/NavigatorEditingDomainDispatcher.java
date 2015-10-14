/*******************************************************************************
 * Copyright (c) 2006, 2015 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.core.platform.sirius.ui.navigator.viewer;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.INotifyChangedListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PlatformUI;
import org.polarsys.capella.common.ef.domain.IEditingDomainListener;
import org.polarsys.capella.common.platform.sirius.ted.SemanticEditingDomainFactory.SemanticEditingDomain;

/**
 * This class is a IEditingDomainListener which redirect required events on registered ICommandStackSelectionProvider and INotifyChangedListener On created
 * editing domain, it create a NavigatorCommandStackListener on it and a NavigatorModelDataListener on its DataNotifier. On command stack events or on
 * DataNotifier events, it dispatch it to registered ICommandStackSelectionProvider and INotifyChangedListener
 */
public class NavigatorEditingDomainDispatcher implements IEditingDomainListener, INotifyChangedListener, ICommandStackSelectionProvider {

  private static Collection<INotifyChangedListener> _notifyListeners = new HashSet<INotifyChangedListener>();

  private static Collection<ICommandStackSelectionProvider> _commandStackListeners = new HashSet<ICommandStackSelectionProvider>();

  /**
   * Add a listener on all editing domains to handle model element changes even if not displayed in the viewer.
   */
  private NavigatorModelDataListener _dataListener;

  /**
   * Add a command stack listener on all editing domains
   */
  private NavigatorCommandStackListener _csListener;

  public NavigatorEditingDomainDispatcher() {
    _dataListener = new NavigatorModelDataListener(this);
    _csListener = new NavigatorCommandStackListener(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createdEditingDomain(EditingDomain editingDomain) {
    _dataListener.registerToDataNotifier((SemanticEditingDomain) editingDomain);
    _csListener.registerCommandStackListener((SemanticEditingDomain) editingDomain);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void disposedEditingDomain(EditingDomain editingDomain) {
    _dataListener.unregisterFromDataNotifier((SemanticEditingDomain) editingDomain);
    _csListener.unregisterCommandStackListener((SemanticEditingDomain) editingDomain);
    ActiveSessionManager.getInstance().remove((SemanticEditingDomain) editingDomain);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void notifyChanged(Notification notification) {
    for (INotifyChangedListener provider : _notifyListeners) {
      provider.notifyChanged(notification);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void commandStackSelectionChanged(final ISelection selection) {
    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        for (final ICommandStackSelectionProvider provider : _commandStackListeners) {
          provider.commandStackSelectionChanged(selection);
        }
      }
    });
  }

  public static void registerNotifyChangedListener(INotifyChangedListener listener) {
    _notifyListeners.add(listener);
  }

  public static void unregisterNotifyChangedListener(INotifyChangedListener listener) {
    _notifyListeners.remove(listener);
  }

  public static void registerCommandStackSelectionProvider(ICommandStackSelectionProvider listener) {
    _commandStackListeners.add(listener);
  }

  public static void unregisterCommandStackSelectionProvider(ICommandStackSelectionProvider listener) {
    _commandStackListeners.remove(listener);
  }

}
