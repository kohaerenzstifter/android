package org.kohaerenzstiftung.andtroin;

import java.util.ArrayList;


import android.os.Bundle;

public abstract class EditEntryStarterActivity extends AndtroinActivity {

	public abstract class EditEntryReturner {
		public abstract void onReturn(ArrayList<Entry> entries, Entry originalEntry);
	}

	private EditEntryReturner mEditEntryReturner;

	public EditEntryStarterActivity() {
		super();
	}

	protected void editEntry(Entry entry, EditEntryReturner editEntryReturner) {
		this.mEditEntryReturner = editEntryReturner;
		Bundle bundle = new Bundle();
		bundle.putParcelable("entry", entry);		

		startActivityForResult(EntryActivity.class, new ActivityReturner(bundle) {
			@Override
			protected void handleResult(Bundle bundle) {

				Entry originalEntry = this.getmExtras().getParcelable("entry");

				ArrayList<Entry> entries =
					bundle.getParcelableArrayList("entries");
				/*if (entries != null) {
					EditEntryStarterActivity.this.putEntries(entries, originalEntry);
				}*/
				EditEntryStarterActivity.this.mEditEntryReturner.onReturn(entries, originalEntry);
			}
		}, bundle);
	}
	
	protected void editEntry(Entry entry, EditEntryReturner editEntryReturner,
			boolean source) {
		this.mEditEntryReturner = editEntryReturner;
		Bundle bundle = new Bundle();
		bundle.putParcelable("entry", entry);
		bundle.putBoolean("source", source);
		startActivityForResult(EntryActivity.class, new ActivityReturner(null) {
			
			@Override
			protected void handleResult(Bundle bundle) {
				Bundle extras = this.getmExtras();
				if (extras != null) {
					Entry originalEntry = extras.getParcelable("entry");

					ArrayList<Entry> entries = bundle
							.getParcelableArrayList("entries");

					EditEntryStarterActivity.this.mEditEntryReturner.onReturn(
							entries, originalEntry);
				}
			}
		}, bundle);
	}

	protected void putEntries(ArrayList<Entry> entries, Entry originalEntry) {
		if (originalEntry.getmId() != -1) {
			int index = 0;
			for (Entry entry : entries) {
				if (originalEntry.getmId() == entry.getmId()) {
					if (originalEntry.equals(entry)) {
						entries.remove(index);
					}
					break;
				}
				index++;
			}
		}
		
		if (entries.size() > 0) {
			this.mAndtroinService.putEntries(entries, entries.get(0).getmListId());
		}
	}
	
	@Override
	protected void recoverResources() {
	}
	
	@Override
	protected void releaseResources() {
	}

}