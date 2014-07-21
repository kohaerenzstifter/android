package org.kohaerenzstiftung.andtroin;

import org.kohaerenzstiftung.Language;

public interface NewListListener {
	void chosen(String name, Language sourceLanguage, Language targetLanguage);
}
