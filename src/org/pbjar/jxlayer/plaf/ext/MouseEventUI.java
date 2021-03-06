/**
 * Copyright (c) 2009, Piet Blok
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of the copyright holder nor the names of the
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.pbjar.jxlayer.plaf.ext;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;
import org.jdesktop.jxlayer.plaf.LayerUI;

/**
 * This class provides for {@link MouseEvent} re-dispatching. It may be used to
 * set a tool tip on {@link JXLayer}'s glass pane and still have the child
 * components receive {@link MouseEvent}s.
 * <p>
 * <b>Note:</b> A {@link MouseEventUI} instance cannot be shared and can be set
 * to a single {@link JXLayer} instance only.
 * <p/>
 */
public class MouseEventUI<V extends JComponent> extends AbstractLayerUI<V> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Component lastEnteredTarget, lastPressedTarget;

    private boolean dispatchingMode = false;

    private JXLayer<? extends V> installedLayer;

    static {
	/*
	 * The instantiating of a JInternalFrame before a MouseEventUI is set to
	 * a JXLayer containing a JDesktopPane that has no JInternalFrames set,
	 * prevents the failing of re dispatched MouseEvents.
	 * 
	 * This is a work around a problem that I don't really understand.
	 * Please see
	 * http://forums.java.net/jive/thread.jspa?threadID=66763&tstart=0 for a
	 * discussion on this problem.
	 */
	new JInternalFrame();
    }

    /**
     * Overridden to override the {@link LayerUI} implementation that only
     * consults the view.
     * 
     * This implementation is a copy of the
     * {@link ComponentUI#contains(JComponent, int, int)} method.
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean contains(JComponent c, int x, int y) {
	return c.inside(x, y);
    }

    /**
     * Overridden to allow for re-dispatching of mouse events to their intended
     * (visual) recipients, rather than to the components according to their
     * bounds.
     */
    @Override
    public void eventDispatched(AWTEvent event, final JXLayer<? extends V> layer) {
	if (event instanceof MouseEvent) {
	    MouseEvent mouseEvent = (MouseEvent) event;
	    if (!dispatchingMode) {
		// Process an original mouse event
		dispatchingMode = true;
		try {
		    redispatch(mouseEvent, layer);
		} finally {
		    dispatchingMode = false;
		}
	    } else {
		// Process a generated mouse event
		/*
		 * Added a check, because on mouse entered or exited, the cursor
		 * may be set to specific dragging cursors.
		 */
		if (MouseEvent.MOUSE_ENTERED == mouseEvent.getID()
			|| MouseEvent.MOUSE_EXITED == mouseEvent.getID()) {
		    layer.getGlassPane().setCursor(null);
		} else {
		    Component component = mouseEvent.getComponent();
		    layer.getGlassPane().setCursor(component.getCursor());
		}
	    }
	}
    }

    /**
     * Overridden to only get the following event types:
     * {@link AWTEvent#MOUSE_EVENT_MASK},
     * {@link AWTEvent#MOUSE_MOTION_EVENT_MASK} and
     * {@link AWTEvent#MOUSE_WHEEL_EVENT_MASK}.
     * 
     */
    @Override
    public long getLayerEventMask() {
	return AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
		| AWTEvent.MOUSE_WHEEL_EVENT_MASK;
    }

    /**
     * Overridden to check if this {@link LayerUI} has not been installed
     * already, and to set the argument {@code component} as the installed
     * {@link JXLayer}.
     * 
     * @throws IllegalStateException
     *             when this {@link LayerUI} has been installed already
     * @see #getInstalledLayer()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void installUI(JComponent component) throws IllegalStateException {
	super.installUI(component);
	if (installedLayer != null) {
	    throw new IllegalStateException(this.getClass().getName()
		    + " cannot be shared between multiple layers");
	}
	installedLayer = (JXLayer<? extends V>) component;
    }

    /**
     * Overridden to remove the installed {@link JXLayer}.
     */
    @Override
    public void uninstallUI(JComponent c) {
	installedLayer = null;
	super.uninstallUI(c);
    }

    private Point calculateTargetPoint(JXLayer<? extends V> layer,
	    MouseEvent mouseEvent) {
	Point point = mouseEvent.getPoint();
	SwingUtilities.convertPointToScreen(point, mouseEvent.getComponent());
	SwingUtilities.convertPointFromScreen(point, layer);
	/*
	 * Removed the contains check because it results in jumping when
	 * dragging internal frames in a desktop pane and dragging outside the
	 * boundaries of the desktop.
	 * 
	 * Introduced this check to solve some scrolling problem, but don't
	 * quite remember the specifics. Maybe that problem is gone by other
	 * changes.
	 */
	// Rectangle layerBounds = layer.getBounds();
	// Container parent = layer.getParent();
	// Rectangle parentRectangle = new Rectangle(-layerBounds.x,
	// -layerBounds.y, parent.getWidth(), parent.getHeight());
	// if (parentRectangle.contains(point)) {
	return transformPoint(layer, point);
	// } else {
	// return new Point(-1, -1);
	// }
    }

    private MouseWheelEvent createMouseWheelEvent(
	    MouseWheelEvent mouseWheelEvent, Point point, Component target) {
	return new MouseWheelEvent(target, //
		mouseWheelEvent.getID(), //
		mouseWheelEvent.getWhen(), //
		mouseWheelEvent.getModifiers(), //
		point.x, //
		point.y, //
		mouseWheelEvent.getClickCount(), //
		mouseWheelEvent.isPopupTrigger(), //
		mouseWheelEvent.getScrollType(), //
		mouseWheelEvent.getScrollAmount(), //
		mouseWheelEvent.getWheelRotation() //
	);
    }

    private void dispatchMouseEvent(MouseEvent mouseEvent) {
	if (mouseEvent != null) {
	    Component target = mouseEvent.getComponent();
	    target.dispatchEvent(mouseEvent);
	    /*
	     * Used to check the re dispatching behavior
	     */
	    // switch (mouseEvent.getID()) {
	    // case (MouseEvent.MOUSE_PRESSED):
	    // System.out.println();
	    // case (MouseEvent.MOUSE_RELEASED):
	    // case (MouseEvent.MOUSE_CLICKED):
	    // System.out.println("Dispatched mouse event " + mouseEvent);
	    // }
	}
    }

    private Component findWheelListenerComponent(Component target) {
	if (target == null) {
	    return null;
	} else if (target.getMouseWheelListeners().length == 0) {
	    return findWheelListenerComponent(target.getParent());
	} else {
	    return target;
	}
    }

    private void generateEnterExitEvents(JXLayer<? extends V> layer,
	    MouseEvent originalEvent, Component newTarget, Point realPoint) {
	if (lastEnteredTarget != newTarget) {
	    dispatchMouseEvent(transformMouseEvent(layer, originalEvent,
		    lastEnteredTarget, realPoint, MouseEvent.MOUSE_EXITED));
	    lastEnteredTarget = newTarget;
	    dispatchMouseEvent(transformMouseEvent(layer, originalEvent,
		    lastEnteredTarget, realPoint, MouseEvent.MOUSE_ENTERED));
	}
    }

    private Component getListeningComponent(MouseEvent event,
	    Component component) {
	switch (event.getID()) {
	case (MouseEvent.MOUSE_CLICKED):
	case (MouseEvent.MOUSE_ENTERED):
	case (MouseEvent.MOUSE_EXITED):
	case (MouseEvent.MOUSE_PRESSED):
	case (MouseEvent.MOUSE_RELEASED):
	    return getMouseListeningComponent(component);
	case (MouseEvent.MOUSE_DRAGGED):
	case (MouseEvent.MOUSE_MOVED):
	    return getMouseMotionListeningComponent(component);
	case (MouseEvent.MOUSE_WHEEL):
	    return getMouseWheelListeningComponent(component);
	}
	return null;
    }

    private Component getMouseListeningComponent(Component component) {
	if (component.getMouseListeners().length > 0) {
	    return component;
	} else {
	    Container parent = component.getParent();
	    if (parent != null) {
		return getMouseListeningComponent(parent);
	    } else {
		return null;
	    }
	}
    }

    private Component getMouseMotionListeningComponent(Component component) {
	/*
	 * Mouse motion events may result in MOUSE_ENTERED and MOUSE_EXITED.
	 * 
	 * Therefore, components with MouseListeners registered should be
	 * returned as well.
	 */
	if (component.getMouseMotionListeners().length > 0
		|| component.getMouseListeners().length > 0) {
	    return component;
	} else {
	    Container parent = component.getParent();
	    if (parent != null) {
		return getMouseMotionListeningComponent(parent);
	    } else {
		return null;
	    }
	}
    }

    private Component getMouseWheelListeningComponent(Component component) {
	if (component.getMouseWheelListeners().length > 0) {
	    return component;
	} else {
	    Container parent = component.getParent();
	    if (parent != null) {
		return getMouseWheelListeningComponent(parent);
	    } else {
		return null;
	    }
	}
    }

    private Component getTarget(JXLayer<? extends V> layer, Point targetPoint) {
	Component view = layer.getView();
	if (view == null) {
	    return null;
	} else {
	    Point viewPoint = SwingUtilities.convertPoint(layer, targetPoint,
		    view);
	    return SwingUtilities.getDeepestComponentAt(view, viewPoint.x,
		    viewPoint.y);
	}
    }

    private void redispatch(MouseEvent originalEvent,
	    final JXLayer<? extends V> layer) {
	if (layer.getView() != null) {
	    if (originalEvent.getComponent() != layer.getGlassPane()) {
		originalEvent.consume();
	    }
	    MouseEvent newEvent = null;

	    Point realPoint = calculateTargetPoint(layer, originalEvent);
	    Component realTarget = getTarget(layer, realPoint);
	    if (realTarget != null) {
		realTarget = getListeningComponent(originalEvent, realTarget);
	    }

	    switch (originalEvent.getID()) {
	    case MouseEvent.MOUSE_PRESSED:
		newEvent = transformMouseEvent(layer, originalEvent,
			realTarget, realPoint);
		if (newEvent != null) {
		    lastPressedTarget = newEvent.getComponent();
		}
		break;
	    case MouseEvent.MOUSE_RELEASED:
		newEvent = transformMouseEvent(layer, originalEvent,
			lastPressedTarget, realPoint);
		lastPressedTarget = null;
		break;
	    case MouseEvent.MOUSE_ENTERED:
		generateEnterExitEvents(layer, originalEvent, realTarget,
			realPoint);
		break;
	    case MouseEvent.MOUSE_EXITED:
		generateEnterExitEvents(layer, originalEvent, realTarget,
			realPoint);
		break;
	    case MouseEvent.MOUSE_MOVED:
		newEvent = transformMouseEvent(layer, originalEvent,
			realTarget, realPoint);
		generateEnterExitEvents(layer, originalEvent, realTarget,
			realPoint);
		break;
	    case MouseEvent.MOUSE_DRAGGED:
		newEvent = transformMouseEvent(layer, originalEvent,
			lastPressedTarget, realPoint);
		generateEnterExitEvents(layer, originalEvent, realTarget,
			realPoint);
		break;
	    case MouseEvent.MOUSE_CLICKED:
		newEvent = transformMouseEvent(layer, originalEvent,
			realTarget, realPoint);
		break;
	    case (MouseEvent.MOUSE_WHEEL):
		redispatchMouseWheelEvent((MouseWheelEvent) originalEvent,
			realTarget, layer);
		break;
	    }
	    dispatchMouseEvent(newEvent);
	}
    }

    private void redispatchMouseWheelEvent(MouseWheelEvent mouseWheelEvent,
	    Component target, JXLayer<? extends V> layer) {
	MouseWheelEvent newEvent = this.transformMouseWheelEvent(
		mouseWheelEvent, target, layer);
	processMouseWheelEvent(newEvent, layer);
    }

    private MouseEvent transformMouseEvent(JXLayer<? extends V> layer,
	    MouseEvent mouseEvent, Component target, Point realPoint) {
	return transformMouseEvent(layer, mouseEvent, target, realPoint,
		mouseEvent.getID());
    }

    private MouseEvent transformMouseEvent(JXLayer<? extends V> layer,
	    MouseEvent mouseEvent, Component target, Point targetPoint, int id) {
	if (target == null) {
	    return null;
	} else {
	    Point newPoint = new Point(targetPoint);
	    SwingUtilities.convertPointToScreen(newPoint, layer);
	    SwingUtilities.convertPointFromScreen(newPoint, target);
	    return new MouseEvent(target, //
		    id, //
		    mouseEvent.getWhen(), //
		    mouseEvent.getModifiers(), //
		    newPoint.x, //
		    newPoint.y, //
		    mouseEvent.getClickCount(), //
		    mouseEvent.isPopupTrigger(), //
		    mouseEvent.getButton());
	}
    }

    private MouseWheelEvent transformMouseWheelEvent(
	    MouseWheelEvent mouseWheelEvent, Component target,
	    JXLayer<? extends V> layer) {
	if (target == null) {
	    target = layer;
	}
	Point point = SwingUtilities.convertPoint(mouseWheelEvent
		.getComponent(), mouseWheelEvent.getPoint(), target);
	MouseWheelEvent newEvent = createMouseWheelEvent(mouseWheelEvent,
		point, target);
	return newEvent;
    }

    private Point transformPoint(JXLayer<? extends V> layer, Point point) {
	AffineTransform transform = this.getTransform(layer);
	if (transform != null) {
	    try {
		transform.inverseTransform(point, point);
	    } catch (NoninvertibleTransformException e) {
		e.printStackTrace();
	    }
	}
	return point;
    }

    protected JXLayer<? extends V> getInstalledLayer() {
	return installedLayer;
    }

    /**
     * Re-dispatches the event to the first component in the hierarchy that has
     * a {@link MouseWheelListener} registered.
     */
    @Override
    protected void processMouseWheelEvent(MouseWheelEvent event,
	    JXLayer<? extends V> jxlayer) {
	/*
	 * Only process an event if it is not already consumed. This may be the
	 * case if this LayerUI is contained in a wrapped hierarchy.
	 */
	if (!event.isConsumed()) {
	    /*
	     * Since we will create a new event, the argument event must be
	     * consumed.
	     */
	    event.consume();
	    /*
	     * Find a target up in the hierarchy that has
	     * MouseWheelEventListeners registered.
	     */
	    Component target = event.getComponent();
	    Component newTarget = findWheelListenerComponent(target);
	    if (newTarget == null) {
		newTarget = jxlayer.getParent();
	    }
	    /*
	     * Convert the location relative to the new target
	     */
	    Point point = SwingUtilities.convertPoint(event.getComponent(),
		    event.getPoint(), newTarget);
	    /*
	     * Create a new event and dispatch it.
	     */
	    newTarget.dispatchEvent(createMouseWheelEvent(event, point,
		    newTarget));
	}
    }

}
