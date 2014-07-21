package org.kohaerenzstiftung.andtroin;

import org.kohaerenzstiftung.Activity;
import org.kohaerenzstiftung.Dialog;


import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TextViewDialog extends Dialog {

	private TextView mTextTextView;
	private Button mOkButton;

	public TextViewDialog(Activity activity, String message) {
		super(activity, R.layout.dialog_textview, true);
		setCancelable(false);
		mTextTextView.setText(message);
	}

	@Override
	protected void updateViews() {
	}

	@Override
	protected void recoverResources() {
	}

	@Override
	protected void releaseResources() {
	}

	@Override
	protected void findElements() {
		this.mTextTextView = (TextView) findViewById(R.id.textview_text);
		this.mOkButton = (Button) findViewById(R.id.button_ok);
	}

	@Override
	protected void assignHandlers() {
		mOkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				TextViewDialog.this.dismiss();
			}
		});

	}

}
