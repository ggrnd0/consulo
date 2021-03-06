/*
 * Copyright 2013-2014 must-be.org
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
package com.intellij.openapi.roots.ui.configuration.classpath.dependencyTab;

import com.intellij.application.options.ModuleListCellRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableModuleRootLayer;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ui.configuration.classpath.ClasspathPanel;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBList;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 27.09.14
 */
public class ModuleDependencyTabContext extends AddModuleDependencyTabContext {
  private final JBList myModuleList;
  private final List<Module> myNotAddedModules;

  public ModuleDependencyTabContext(ClasspathPanel panel, StructureConfigurableContext context) {
    super(panel, context);
    myNotAddedModules = getNotAddedModules();
    myModuleList = new JBList(myNotAddedModules);
    myModuleList.setCellRenderer(new ModuleListCellRenderer());
  }

  private List<Module> getNotAddedModules() {
    final ModifiableRootModel rootModel = myClasspathPanel.getRootModel();
    Set<Module> addedModules = new HashSet<Module>(Arrays.asList(rootModel.getModuleDependencies(true)));
    addedModules.add(rootModel.getModule());

    final Module[] modules = myClasspathPanel.getModuleConfigurationState().getModulesProvider().getModules();
    final List<Module> elements = new ArrayList<Module>();
    for (final Module module : modules) {
      if (!addedModules.contains(module)) {
        elements.add(module);
      }
    }
    return elements;
  }

  @NotNull
  @Override
  public String getTabName() {
    return "Module";
  }

  @Override
  public boolean isEmpty() {
    return myNotAddedModules.isEmpty();
  }

  @Override
  public List<OrderEntry> createOrderEntries(@NotNull ModifiableModuleRootLayer layer, DialogWrapper dialogWrapper) {
    Object[] selectedValues = myModuleList.getSelectedValues();
    List<OrderEntry> orderEntries = new ArrayList<OrderEntry>(selectedValues.length);
    for (Object selectedValue : selectedValues) {
      orderEntries.add(layer.addModuleOrderEntry((Module)selectedValue));
    }
    return orderEntries;
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myModuleList;
  }
}
