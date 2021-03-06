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
package org.consulo.projectImport.model;

import org.consulo.projectImport.model.library.LibraryModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author VISTALL
 * @since 17:43/19.06.13
 */
public class ProjectLibraryTableModel extends ModelContainer {
  public void addLibrary(@NotNull LibraryModel libraryModel) {
    addChild(libraryModel);
  }

  @NotNull
  public List<LibraryModel> getLibraries() {
    return findChildren(LibraryModel.class);
  }
}
