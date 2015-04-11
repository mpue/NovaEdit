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


// $Id: GraphicsUtil.java,v 1.3 2011-09-07 19:56:09 mpue Exp $

package net.infonode.gui;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.SwingUtilities;

/**
 * @author johan
 */
public class GraphicsUtil {
  public static void drawOptimizedLine(Graphics g, int x1, int y1, int x2, int y2) {
    if (g.getColor().getAlpha() < 255 && (x1 == x2 || y1 == y2))
      g.fillRect(x1 < x2 ? x1 : x2, y1 < y2 ? y1 : y2, Math.abs(x2 - x1) + 1, Math.abs(y2 - y1) + 1);
    else
      g.drawLine(x1, y1, x2, y2);
  }

  public static Rectangle calculateIntersectionClip(int x, int y, int width, int height, Shape originalClip) {
    Rectangle bounds = originalClip.getBounds();
    SwingUtilities.computeIntersection(x, y, width, height, bounds);
    return bounds;
  }
}
