/*
 * Copyright 2013 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.roots.ui.configuration;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableModuleRootLayer;
import com.intellij.openapi.roots.ModuleExtensionWithSdkOrderEntry;
import com.intellij.openapi.roots.ui.configuration.extension.ExtensionCheckedTreeNode;
import com.intellij.openapi.roots.ui.configuration.extension.ExtensionTreeCellRenderer;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Comparing;
import com.intellij.ui.CheckboxTreeNoPolicy;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.tree.TreeUtil;
import org.consulo.module.extension.ModuleExtension;
import org.consulo.module.extension.ModuleExtensionWithSdk;
import org.consulo.module.extension.MutableModuleExtension;
import org.consulo.psi.PsiPackageManager;
import org.consulo.psi.PsiPackageSupportProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 10:33/19.05.13
 */
public class ExtensionEditor extends ModuleElementsEditor {
  private final ModuleConfigurationState myState;
  private final OutputEditor myOutputEditor;
  private final ClasspathEditor myClasspathEditor;
  private final ContentEntriesEditor myContentEntriesEditor;
  private JPanel myRootPane;
  private CheckboxTreeNoPolicy myTree;
  private Splitter mySplitter;

  private ModuleExtension<?> myConfigurablePanelExtension;

  public ExtensionEditor(ModuleConfigurationState state, OutputEditor outputEditor, ClasspathEditor classpathEditor, ContentEntriesEditor contentEntriesEditor) {
    super(state);
    myState = state;
    myOutputEditor = outputEditor;
    myClasspathEditor = classpathEditor;
    myContentEntriesEditor = contentEntriesEditor;
  }

  @Override
  public void moduleStateChanged() {
    mySplitter.setSecondComponent(null);
    myTree.setModel(new DefaultTreeModel(new ExtensionCheckedTreeNode(null, myState, this)));
    TreeUtil.expandAll(myTree);
  }

  @NotNull
  @Override
  protected JComponent createComponentImpl() {
    myRootPane = new JPanel(new BorderLayout());

    mySplitter = new OnePixelSplitter();

    myTree = new CheckboxTreeNoPolicy(new ExtensionTreeCellRenderer(), new ExtensionCheckedTreeNode(null, myState, this)) {
      @Override
      protected void adjustParentsAndChildren(CheckedTreeNode node, boolean checked) {
        if (!checked) {
          changeNodeState(node, false);
          checkOrUncheckChildren(node, false);
        }
        else {
          // we need collect parents, and enable it in right order
          // A
          // - B
          // -- C
          // when we enable C, ill be calls like A -> B -> C
          List<CheckedTreeNode> parents = new ArrayList<CheckedTreeNode>();
          TreeNode parent = node.getParent();
          while (parent != null) {
            if (parent instanceof CheckedTreeNode) {
              parents.add((CheckedTreeNode)parent);
            }
            parent = parent.getParent();
          }

          Collections.reverse(parents);
          for (CheckedTreeNode checkedTreeNode : parents) {
            checkNode(checkedTreeNode, true);
          }
          changeNodeState(node, true);
        }
        repaint();
      }
    };

    myTree.setRootVisible(false);
    myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    myTree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(final TreeSelectionEvent e) {
        final List<MutableModuleExtension> selected = TreeUtil.collectSelectedObjectsOfType(myTree, MutableModuleExtension.class);
        mySplitter.setSecondComponent(null);

        if (!selected.isEmpty()) {
          final MutableModuleExtension extension = selected.get(0);
          if (!extension.isEnabled()) {
            return;
          }

          mySplitter.setSecondComponent(createConfigurationPanel(extension));
        }
      }
    });
    TreeUtil.expandAll(myTree);

    mySplitter.setFirstComponent(myTree);

    myRootPane.add(new JBScrollPane(mySplitter), BorderLayout.CENTER);

    return myRootPane;
  }

  @Nullable
  private JComponent createConfigurationPanel(final @NotNull MutableModuleExtension<?> extension) {
    myConfigurablePanelExtension = extension;
    return extension.createConfigurablePanel(new Runnable() {
      @Override
      public void run() {
        extensionChanged(extension);
      }
    });
  }

  public void extensionChanged(MutableModuleExtension<?> extension) {
    final JComponent secondComponent = myConfigurablePanelExtension != extension ? null : mySplitter.getSecondComponent();
    if (secondComponent == null && extension.isEnabled() || secondComponent != null && !extension.isEnabled()) {
      if (!extension.isEnabled()) {
        mySplitter.setSecondComponent(null);
      }
      else {
        mySplitter.setSecondComponent(createConfigurationPanel(extension));
      }
    }

    if (extension instanceof ModuleExtensionWithSdk) {
      // we using module layer, dont use modifiable model - it ill proxy, and methods 'addModuleExtensionSdkEntry' && 'removeOrderEntry'
      // ill call this method again
      ModifiableModuleRootLayer moduleRootLayer = extension.getModuleRootLayer();

      final ModuleExtensionWithSdkOrderEntry sdkOrderEntry = moduleRootLayer.findModuleExtensionSdkEntry(extension);
      if (!extension.isEnabled() && sdkOrderEntry != null) {
        moduleRootLayer.removeOrderEntry(sdkOrderEntry);
      }

      if (extension.isEnabled()) {
        final ModuleExtensionWithSdk sdkExtension = (ModuleExtensionWithSdk)extension;
        if (!sdkExtension.getInheritableSdk().isNull()) {
          if (sdkOrderEntry == null) {
            moduleRootLayer.addModuleExtensionSdkEntry(sdkExtension);
          }
          else {
            final ModuleExtensionWithSdk<?> moduleExtension = sdkOrderEntry.getModuleExtension();
            if (moduleExtension != null && !Comparing.equal(sdkExtension.getInheritableSdk(), moduleExtension.getInheritableSdk())) {
              moduleRootLayer.addModuleExtensionSdkEntry(sdkExtension);
            }
          }
        }
        else if(sdkOrderEntry != null) {
          moduleRootLayer.removeOrderEntry(sdkOrderEntry);
        }
      }
    }

    for (PsiPackageSupportProvider supportProvider : PsiPackageSupportProvider.EP_NAME.getExtensions()) {
      final Module module = extension.getModule();
      if (supportProvider.isSupported(extension)) {
        PsiPackageManager.getInstance(module.getProject()).dropCache(extension.getClass());
      }
    }

    myClasspathEditor.moduleStateChanged();
    myContentEntriesEditor.moduleStateChanged();
    myOutputEditor.moduleStateChanged();
  }

  @Override
  public void saveData() {

  }

  @Nls
  @Override
  public String getDisplayName() {
    return "Extensions";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }
}
