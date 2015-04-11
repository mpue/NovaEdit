/**

	BlackBoard breadboard designer
	Written and maintained by Matthias Pueski 
	
	Copyright (c) 2010-2012 Matthias Pueski
	
	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 */
package org.pmedv.blackboard.commands;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.pmedv.blackboard.EditorUtils;
import org.pmedv.blackboard.components.BoardEditor;
import org.pmedv.blackboard.components.Item;
import org.pmedv.blackboard.components.Layer;
import org.pmedv.blackboard.components.Symbol;

/**
 * {@link UndoableEdit} for the {@link ConvertToSymbolCommand}
 * 
 * @author Matthias Pueski (01.03.2012)
 *
 */
public class ConvertToSymbolEdit extends AbstractUndoableEdit {

	private static final long serialVersionUID = 268168440492800806L;
	
	private final List<Item> items;
	private final Symbol symbol;
	
	public ConvertToSymbolEdit(List<Item> preserveItems, Symbol s) {
		this.items = preserveItems;
		this.symbol = s;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		BoardEditor editor = EditorUtils.getCurrentActiveEditor();
		
		editor.getModel().getLayer(symbol.getLayer()).getItems().remove(symbol);
		
		for (Item item : items) {
			editor.getModel().getLayer(item.getLayer()).getItems().add(item);
		}
		
		editor.clearSelectionBorder();
		editor.refresh();
		editor.updateStatusBar();				
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		BoardEditor editor = EditorUtils.getCurrentActiveEditor();
		
		for (Item item : items) {
			for (Layer layer : editor.getModel().getLayers())
				layer.getItems().remove(item);					
		}
		
		editor.getSelectedItems().clear();
				
		editor.getModel().getCurrentLayer().getItems().add(symbol);

		
		editor.updateStatusBar();	
	}
	
	
	@Override
	public String getPresentationName() {
		return "Paste items";
	}
}
