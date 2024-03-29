/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.actions;

import java.awt.event.ActionEvent;
import java.net.URL;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.AbstractAction;
import static javax.swing.Action.LARGE_ICON_KEY;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import org.opentcs.guing.components.dialogs.CreateGroupPanel;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to create a group.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class CreateGroupAction
    extends AbstractAction {

  /**
   * This action class's ID.
   */
  public static final String ID = "openTCS.createGroup";
  /**
   * Provides panels for creating groups.
   */
  private final Provider<CreateGroupPanel> panelProvider;

  /**
   * Creates a new instance.
   *
   * @param panelProvider Provides panels for creating groups
   */
  @Inject
  public CreateGroupAction(Provider<CreateGroupPanel> panelProvider) {
    this.panelProvider = requireNonNull(panelProvider, "panelProvider");
    ResourceBundleUtil.getBundle().configureAction(this, ID);
    
    URL url = getClass().getResource(ImageDirectory.DIR + "/toolbar/groups.png");
    putValue(SMALL_ICON, new ImageIcon(url));
    putValue(LARGE_ICON_KEY, new ImageIcon(url));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    CreateGroupPanel panel = panelProvider.get();
    panel.setLocationRelativeTo(null);
    panel.setVisible(true);
  }

}
