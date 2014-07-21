package org.kohaerenzstiftung.wwwidget;

import java.io.File;

import android.content.Context;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {

	/*@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
        final int length = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < length; i++) {
            int appWidgetId = appWidgetIds[i];

			Helper.updateWidgetViaService(context, appWidgetId);
        }
	}*/

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {

		try {
			super.onDeleted(context, appWidgetIds);
	        final int length = appWidgetIds.length;

	        for (int i = 0; i < length; i++) {
	            int appWidgetId = appWidgetIds[i];

	        	String filesDir = context.getFilesDir().getAbsolutePath();
				File file = new File(filesDir + File.separator + appWidgetId);

	            new File(file + File.separator + "lastUpdate").delete();
	            Helper.delete(file);
	        }			
		} catch (Throwable t) {
		} finally {
		}
	}

	
}
