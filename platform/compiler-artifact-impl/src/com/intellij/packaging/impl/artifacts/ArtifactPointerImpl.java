/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package com.intellij.packaging.impl.artifacts;

import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactModel;
import com.intellij.packaging.artifacts.ArtifactPointer;
import org.consulo.util.pointers.NamedPointerImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @author nik
 */
public class ArtifactPointerImpl extends NamedPointerImpl<Artifact> implements ArtifactPointer {
  public ArtifactPointerImpl(Artifact value) {
    super(value);
  }

  public ArtifactPointerImpl(@NotNull String name) {
    super(name);
  }

  @Override
  @NotNull
  public String getArtifactName() {
    return getName();
  }

  @Override
  public Artifact getArtifact() {
    return get();
  }

  @Override
  @NotNull
  public String getArtifactName(@NotNull ArtifactModel artifactModel) {
    final Artifact artifact = get();
    if (artifact != null) {
      return artifactModel.getArtifactByOriginal(artifact).getName();
    }
    return getName();
  }

  @Override
  public Artifact findArtifact(@NotNull ArtifactModel artifactModel) {
    final Artifact artifact = get();
    if (artifact != null) {
      return artifactModel.getArtifactByOriginal(artifact);
    }
    return artifactModel.findArtifact(getName());
  }
}
