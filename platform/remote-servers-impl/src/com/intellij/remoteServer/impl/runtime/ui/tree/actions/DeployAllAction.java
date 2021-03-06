package com.intellij.remoteServer.impl.runtime.ui.tree.actions;

import com.intellij.icons.AllIcons;
import com.intellij.remoteServer.impl.runtime.ui.tree.ServerNode;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: michael.golubev
 */
public class DeployAllAction extends ServerActionBase {

  public DeployAllAction() {
    super("Deploy All", "Deploy all the artifacts of the selected server", AllIcons.Nodes.Deploy);
  }

  @Override
  protected void performAction(@NotNull ServerNode serverNode) {
    if (serverNode.isDeployAllEnabled()) {
      serverNode.deployAll();
    }
  }

  @Override
  protected boolean isEnabledForServer(@NotNull ServerNode serverNode) {
    return serverNode.isDeployAllEnabled();
  }
}
