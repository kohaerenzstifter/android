package org.kohaerenzstiftung;

import java.text.Collator;
import java.util.Comparator;

public class LanguageCollator implements Comparator<Language> {

	public int compare(Language object1, Language object2) {
		Collator collator = Collator.getInstance();
		return collator.compare(object1.getmDisplayLanguage(), object2.getmDisplayLanguage());
	}

}
