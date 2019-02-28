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
package org.polarsys.capella.test.navigator.ju;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.navigator.CommonViewer;
import org.polarsys.capella.common.ef.command.AbstractReadWriteCommand;
import org.polarsys.capella.common.helpers.TransactionHelper;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.ctx.CapabilityPkg;
import org.polarsys.capella.core.data.ctx.CtxPackage;
import org.polarsys.capella.core.data.ctx.SystemFunction;
import org.polarsys.capella.core.data.ctx.SystemFunctionPkg;
import org.polarsys.capella.core.data.fa.AbstractFunction;
import org.polarsys.capella.core.data.fa.FaPackage;
import org.polarsys.capella.core.data.fa.FunctionPkg;
import org.polarsys.capella.core.data.fa.FunctionalChain;
import org.polarsys.capella.core.data.fa.FunctionalChainInvolvementFunction;
import org.polarsys.capella.core.data.fa.FunctionalChainInvolvementLink;
import org.polarsys.capella.core.data.fa.FunctionalChainReference;
import org.polarsys.capella.core.data.information.DataPkg;
import org.polarsys.capella.core.data.information.datatype.BooleanType;
import org.polarsys.capella.core.data.information.datatype.DatatypePackage;
import org.polarsys.capella.core.data.information.datatype.Enumeration;
import org.polarsys.capella.core.data.information.datavalue.DatavaluePackage;
import org.polarsys.capella.core.data.information.datavalue.EnumerationLiteral;
import org.polarsys.capella.core.data.information.datavalue.LiteralBooleanValue;
import org.polarsys.capella.core.data.la.CapabilityRealizationPkg;
import org.polarsys.capella.core.data.la.LaPackage;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.la.LogicalFunction;
import org.polarsys.capella.core.data.la.LogicalFunctionPkg;
import org.polarsys.capella.core.data.pa.PaPackage;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.model.helpers.move.MoveHelper;
import org.polarsys.capella.core.platform.sirius.ui.navigator.drop.DiagramDropAdapterAssistant;
import org.polarsys.capella.core.platform.sirius.ui.navigator.drop.ExplorerDropAdapterAssistant;
import org.polarsys.capella.core.platform.sirius.ui.navigator.view.CapellaCommonNavigator;
import org.polarsys.capella.test.framework.helpers.TestHelper;
import org.polarsys.capella.test.navigator.ju.model.NavigatorEmptyProject;

public class DragDropTest extends NavigatorEmptyProject {

  /**
   * Check customized semantic rules are allowing the drop of current element in the given newTarget
   * 
   * It doesn't check basic EMF rules
   */
  void checkMoveAllowed(EObject current, EObject newTarget) {
    assertTrue(MoveHelper.getInstance().checkSemanticRules(Arrays.asList(current), newTarget).isOK());
  }

  /**
   * Check customized semantic rules are disabling the drop of current element in the given newTarget
   * 
   * It doesn't check basic EMF rules
   */
  void checkMoveDisabled(EObject current, EObject newTarget) {
    assertTrue(!MoveHelper.getInstance().checkSemanticRules(Arrays.asList(current), newTarget).isOK());
  }

  @Override
  public void test() throws Exception {

    // Check that drop adapters are properly registered on Project Explorer
    CommonViewer viewer = getViewer();
    CommonDropAdapterAssistant[] z = viewer.getNavigatorContentService().getDnDService()
        .findCommonDropAdapterAssistants(ROOT_SYSTEM_FUNCTION, new StructuredSelection(ROOT_SYSTEM_FUNCTION));
    assertTrue(Arrays.asList(z).stream().anyMatch(ExplorerDropAdapterAssistant.class::isInstance));
    assertTrue(Arrays.asList(z).stream().anyMatch(DiagramDropAdapterAssistant.class::isInstance));

    // Check function
    EObject systemFunction = createFunction(ROOT_SYSTEM_FUNCTION);
    EObject systemFunction2 = createFunction(ROOT_SYSTEM_FUNCTION);
    EObject systemFunctionPkg = createFunctionPkg(ROOT_SYSTEM_FUNCTION);
    EObject systemFunctionPkg2 = createFunctionPkg(ROOT_SYSTEM_FUNCTION);
    EObject logicalFunction = createFunction(ROOT_LOGICAL_FUNCTION);
    EObject logicalFunctionPkg = createFunctionPkg(ROOT_LOGICAL_FUNCTION);

    checkMoveDisabled(systemFunction, SA_SYSTEM_FUNCTIONS);
    checkMoveDisabled(systemFunction, LA_LOGICAL_FUNCTIONS);
    checkMoveAllowed(systemFunction, systemFunctionPkg);
    checkMoveDisabled(systemFunction, logicalFunctionPkg);
    checkMoveDisabled(systemFunction, logicalFunction);
    checkMoveAllowed(systemFunction, systemFunction2);

    // Check function pkg
    checkMoveAllowed(systemFunctionPkg, systemFunctionPkg2);
    checkMoveDisabled(systemFunctionPkg, logicalFunctionPkg);
    checkMoveDisabled(systemFunctionPkg, EPBS_ARCHITECTURE);

    // Check root components
    checkMoveDisabled(OA_OPERATIONAL_CONTEXT, logicalFunctionPkg);
    checkMoveDisabled(SA_SYSTEM, logicalFunctionPkg);
    checkMoveDisabled(LA_LOGICAL_SYSTEM, logicalFunctionPkg);
    checkMoveDisabled(PA_PHYSICAL_SYSTEM, logicalFunctionPkg);

    // Check functional chain
    EObject chain = createFunctionalChain(ROOT_SYSTEM_FUNCTION);
    EObject capability = createCapability(SA_CAPABILITIES);
    EObject logicalCapability = createCapability(LA_CAPABILITIES);
    checkMoveDisabled(chain, LA_LOGICAL_FUNCTIONS);
    checkMoveAllowed(chain, capability);
    checkMoveAllowed(chain, systemFunction2);
    checkMoveDisabled(chain, logicalCapability);

    // Check chain involvements
    EObject chain2 = createFunctionalChain(ROOT_SYSTEM_FUNCTION);
    checkMoveDisabled(createFunctionalChainLink(chain), chain2);
    checkMoveDisabled(createFunctionalChainFunction(chain), chain2);
    checkMoveDisabled(createFunctionalChainReference(chain), chain2);

    // Check interface pkg
    checkMoveDisabled(LA_INTERFACES, EPBS_ARCHITECTURE);

    // Check components and pkgs
    EObject logicalComponent = createComponent(LA_LOGICAL_SYSTEM);
    EObject physicalComponent = createComponent(PA_PHYSICAL_SYSTEM);
    EObject logicalComponent2 = createComponent(LA_LOGICAL_SYSTEM);
    EObject physicalComponent2 = createComponent(PA_PHYSICAL_SYSTEM);
    EObject logicalComponentPkg = createComponentPkg(LA_LOGICAL_SYSTEM);
    EObject physicalComponentPkg = createComponentPkg(PA_PHYSICAL_SYSTEM);

    checkMoveDisabled(logicalComponent, SA_SYSTEM);
    checkMoveDisabled(logicalComponent, PA_PHYSICAL_SYSTEM);
    checkMoveAllowed(logicalComponent, logicalComponent2);
    checkMoveDisabled(logicalComponent, physicalComponent2);
    checkMoveAllowed(physicalComponent, physicalComponent2);
    checkMoveAllowed(logicalComponent, logicalComponentPkg);

    checkMoveDisabled(logicalComponentPkg, LOGICAL_ARCHITECTURE);
    checkMoveDisabled(physicalComponentPkg, PHYSICAL_ARCHITECTURE);

    // Check Capability Pkgs
    checkMoveAllowed(OA_OPERATIONAL_CAPABILITIES, OPERATIONAL_ANALYSIS);
    checkMoveAllowed(OA_OPERATIONAL_CAPABILITIES, OA_OPERATIONAL_CONTEXT);
    checkMoveDisabled(OA_OPERATIONAL_CAPABILITIES, SYSTEM_ANALYSIS);

    checkMoveAllowed(SA_CAPABILITIES, SYSTEM_ANALYSIS);
    checkMoveAllowed(SA_CAPABILITIES, SA_SYSTEM);
    checkMoveDisabled(SA_CAPABILITIES, OPERATIONAL_ANALYSIS);
    checkMoveDisabled(SA_CAPABILITIES, LOGICAL_ARCHITECTURE);

    checkMoveDisabled(LA_CAPABILITIES, SYSTEM_ANALYSIS);
    checkMoveAllowed(LA_CAPABILITIES, LA_LOGICAL_SYSTEM);
    checkMoveAllowed(LA_CAPABILITIES, LOGICAL_ARCHITECTURE);
    checkMoveAllowed(LA_CAPABILITIES, PHYSICAL_ARCHITECTURE);
    checkMoveAllowed(LA_CAPABILITIES, EPBS_ARCHITECTURE);

    // Check capabilities can be moved on Components (for refinement purposes)
    checkMoveDisabled(capability, OA_OPERATIONAL_CAPABILITIES);
    checkMoveDisabled(capability, PA_CAPABILITIES);
    checkMoveAllowed(logicalCapability, PA_CAPABILITIES);
    EObject capabilityPkg = createCapabilityPkg(LA_LOGICAL_SYSTEM);
    checkMoveDisabled(capability, capabilityPkg);
    checkMoveAllowed(logicalCapability, capabilityPkg);

    // Check Enumeration Literals
    Enumeration enum1 = createEnumeration(SA_DATA);
    Enumeration enum2 = createEnumeration(SA_DATA);
    BooleanType bool1 = createBoolean(SA_DATA);
    BooleanType bool2 = createBoolean(SA_DATA);
    EnumerationLiteral enumLiteral = createEnumerationLiteral(enum1);
    LiteralBooleanValue boolLiteral = createBooleanLiteral(bool1);
    checkMoveAllowed(enumLiteral, enum2);
    checkMoveDisabled(enumLiteral, bool1);

    // Check Boolean Literals
    checkMoveAllowed(boolLiteral, bool2);
    checkMoveDisabled(boolLiteral, enum1);

  }

  private EObject createCapabilityPkg(EObject container) {
    if (container instanceof Component) {
      return create(container, LaPackage.Literals.CAPABILITY_REALIZATION_PKG);
    }
    return null;
  }

  private EObject createCapability(EObject container) {
    if (container instanceof CapabilityPkg) {
      return create(container, CtxPackage.Literals.CAPABILITY);

    } else if (container instanceof CapabilityRealizationPkg) {
      return create(container, LaPackage.Literals.CAPABILITY_REALIZATION);
    }
    return null;
  }

  private BooleanType createBoolean(DataPkg container) {
    return create(container, DatatypePackage.Literals.BOOLEAN_TYPE);
  }

  private Enumeration createEnumeration(DataPkg container) {
    return create(container, DatatypePackage.Literals.ENUMERATION);
  }

  private EnumerationLiteral createEnumerationLiteral(Enumeration container) {
    return create(container, DatavaluePackage.Literals.ENUMERATION_LITERAL,
        DatatypePackage.Literals.ENUMERATION__OWNED_LITERALS);
  }

  private LiteralBooleanValue createBooleanLiteral(BooleanType container) {
    return create(container, DatavaluePackage.Literals.LITERAL_BOOLEAN_VALUE,
        DatatypePackage.Literals.BOOLEAN_TYPE__OWNED_LITERALS);
  }

  private EObject createComponentPkg(Component container) {
    if (container instanceof LogicalComponent) {
      return create(container, LaPackage.Literals.LOGICAL_COMPONENT_PKG);

    } else if (container instanceof PhysicalComponent) {
      return create(container, PaPackage.Literals.PHYSICAL_COMPONENT_PKG);
    }
    return null;
  }

  private EObject createComponent(Component container) {
    if (container instanceof LogicalComponent) {
      return create(container, LaPackage.Literals.LOGICAL_COMPONENT);

    } else if (container instanceof PhysicalComponent) {
      return create(container, PaPackage.Literals.PHYSICAL_COMPONENT);
    }
    return null;
  }

  private AbstractFunction createFunction(EObject container) {
    return create(container, CtxPackage.Literals.SYSTEM_FUNCTION);
  }

  private FunctionPkg createFunctionPkg(EObject container) {
    if (container instanceof SystemFunction) {
      return create(container, CtxPackage.Literals.SYSTEM_FUNCTION_PKG);

    } else if (container instanceof LogicalFunction) {
      return create(container, LaPackage.Literals.LOGICAL_FUNCTION_PKG);

    } else if (container instanceof SystemFunctionPkg) {
      return create(container, CtxPackage.Literals.SYSTEM_FUNCTION_PKG);

    } else if (container instanceof LogicalFunctionPkg) {
      return create(container, LaPackage.Literals.LOGICAL_FUNCTION_PKG);
    }
    return null;
  }

  private FunctionalChain createFunctionalChain(EObject container) {
    return create(container, FaPackage.Literals.FUNCTIONAL_CHAIN);
  }

  private FunctionalChainInvolvementLink createFunctionalChainLink(EObject container) {
    return create(container, FaPackage.Literals.FUNCTIONAL_CHAIN_INVOLVEMENT_LINK);
  }

  private FunctionalChainInvolvementFunction createFunctionalChainFunction(EObject container) {
    return create(container, FaPackage.Literals.FUNCTIONAL_CHAIN_INVOLVEMENT_FUNCTION);
  }

  private FunctionalChainReference createFunctionalChainReference(EObject container) {
    return create(container, FaPackage.Literals.FUNCTIONAL_CHAIN_REFERENCE);
  }

  @SuppressWarnings("restriction")
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // Deadlock because of Workbench Auto-Save Job, so we have to remove it
    Job[] allJobs = Job.getJobManager().find(null);
    for (Job job : allJobs) {
      if (Workbench.WORKBENCH_AUTO_SAVE_JOB.equals(job.getName())) {
        job.cancel();
      }
    }
  }

  /**
   * @return
   */
  protected CommonViewer getViewer() {
    final CommonViewer[] viewer = new CommonViewer[1];
    Display.getDefault().syncExec(new Runnable() {

      @Override
      public void run() {
        IViewPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
            .findView(CapellaCommonNavigator.ID);

        viewer[0] = ((CapellaCommonNavigator) part).getCommonViewer();
      }
    });
    return viewer[0];
  }

  private <T> T create(final EObject container, EClass clazz) {
    return create(container, clazz, null);
  }

  private <T> T create(final EObject container, EClass clazz, EStructuralFeature f) {
    Collection<CommandParameter> commands = (Collection<CommandParameter>) TransactionHelper.getEditingDomain(container)
        .getNewChildDescriptors(container, null);
    Optional<CommandParameter> cp = commands.stream()
        .filter(c -> clazz.isInstance(c.getValue()) && (f == null || c.getEStructuralFeature() == f)).findFirst();
    if (cp.isPresent()) {
      Command cmd = CreateChildCommand.create(TransactionHelper.getEditingDomain(container), container, cp.get(),
          Arrays.asList(container));
      if (cmd.canExecute()) {
        AbstractReadWriteCommand create = new AbstractReadWriteCommand() {
          @Override
          public void run() {
            cmd.execute();
          }
        };
        TestHelper.getExecutionManager(container).execute(create);
      }
      return (T) cmd.getAffectedObjects().iterator().next();
    }
    assertTrue(false);
    return null;
  }

}
