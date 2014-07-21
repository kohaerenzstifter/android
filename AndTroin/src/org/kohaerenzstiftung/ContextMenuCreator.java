package org.kohaerenzstiftung;

import android.view.ContextMenu.ContextMenuInfo;

public interface ContextMenuCreator {
	int createContextMenu(ContextMenuInfo menuInfo);
}