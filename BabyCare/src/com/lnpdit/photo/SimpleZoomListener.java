package com.lnpdit.photo;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class SimpleZoomListener implements View.OnTouchListener {

	public enum ControlType {
		PAN, ZOOM
	}

	private ControlType mControlType = ControlType.PAN;

	private ZoomState mState;

	private float mX;
	private float mY;
	private float mGap;

	private GestureDetector mGestureDetector;

	public void setZoomState(ZoomState state) {
		mState = state;
	}

	public void setControlType(ControlType controlType) {
		mControlType = controlType;
	}

	public void setmGestureDetector(GestureDetector mGestureDetector) {
		this.mGestureDetector = mGestureDetector;
	}

	public boolean onTouch(View v, MotionEvent event) {
		boolean ev = true;
		try {
			ev = mGestureDetector.onTouchEvent(event);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (mGestureDetector != null && ev) {
			return true;
		}
		final int action = event.getAction();
		int pointCount = event.getPointerCount();
		if (pointCount == 1) {
			final float x = event.getX();
			final float y = event.getY();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mX = x;
				mY = y;
				break;
			case MotionEvent.ACTION_MOVE: {
				final float dx = (x - mX) / v.getWidth();
				final float dy = (y - mY) / v.getHeight();
				// if (x > (0 - v.getWidth()) && x < v.getWidth()
				// && y > (0 - v.getHeight()) && y < v.getHeight()) {
				mState.setPanX(mState.getPanX() - dx);
				mState.setPanY(mState.getPanY() - dy);
				// }
				mState.notifyObservers();
				mX = x;
				mY = y;
				break;
			}
			}
		}
		if (pointCount == 2) {
			try {

				final float x0 = event.getX(event.getPointerId(0));
				final float y0 = event.getY(event.getPointerId(0));

				final float x1 = event.getX(event.getPointerId(1));
				final float y1 = event.getY(event.getPointerId(1));

				final float gap = getGap(x0, x1, y0, y1);
				switch (action) {
				case MotionEvent.ACTION_POINTER_2_DOWN:
				case MotionEvent.ACTION_POINTER_1_DOWN:
					mGap = gap;
					break;
				case MotionEvent.ACTION_POINTER_1_UP:
					mX = x1;
					mY = y1;
					break;
				case MotionEvent.ACTION_POINTER_2_UP:
					mX = x0;
					mY = y0;
					break;
				case MotionEvent.ACTION_MOVE: {
					final float dgap = (gap - mGap) / mGap;
					float zoom = mState.getZoom() * gap / mGap;
					if (zoom < 4 && zoom > 0.5) {
						mState.setZoom(zoom);
					}
					mState.notifyObservers();
					mGap = gap;
					break;
				}
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return true;
	}

	private float getGap(float x0, float x1, float y0, float y1) {
		return (float) Math.pow(
				Math.pow((x0 - x1), 2) + Math.pow((y0 - y1), 2), 0.5);
	}

}
