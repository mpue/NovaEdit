/*
 * Copyright (C) 2004 NNL Technology AB
 * Visit www.infonode.net for information about InfoNode(R) 
 * products and how to contact NNL Technology AB.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA 02111-1307, USA.
 */


// $Id: DropAction.java,v 1.3 2011-09-07 19:56:10 mpue Exp $
package net.infonode.docking.internalutil;

import java.awt.event.MouseEvent;

import net.infonode.docking.DockingWindow;

/**
 * @author $Author: mpue $
 * @version $Revision: 1.3 $
 */
abstract public class DropAction {
  abstract public void execute(DockingWindow window, MouseEvent mouseEvent);

  protected DropAction() {
  }

  public boolean showTitle() {
    return true;
  }

  public void clear(DockingWindow window, DropAction newDropAction) {
  }

}
