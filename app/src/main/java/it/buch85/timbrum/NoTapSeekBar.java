package it.buch85.timbrum;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatSeekBar;

public class NoTapSeekBar extends AppCompatSeekBar {
	private Drawable mThumb;

	public NoTapSeekBar(Context context) {
		super(context);
	}

	public NoTapSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoTapSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setThumb(Drawable thumb) {
		super.setThumb(thumb);
		mThumb = thumb;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (mThumb.getBounds().contains((int) event.getX(),
					(int) event.getY())) {
				return super.onTouchEvent(event);
			} else {
				return false;
			}
		} else {
			return super.onTouchEvent(event);
		}
	}

}
