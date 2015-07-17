package com.lnpdit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.LinearLayout;
import android.widget.ListView;

/***
 * 锟皆讹拷锟藉布锟斤拷锟侥硷拷.
 * 
 * @author wangyuanshi
 * 
 */
public class RightMenuLinearLayout extends LinearLayout {
	private GestureDetector mGestureDetector;
	View.OnTouchListener mGestureListener;

	private boolean isLock = false;// 锟斤拷锟斤拷锟狡讹拷锟斤拷.

	public OnScrollListener onScrollListener;// 锟皆讹拷锟藉滑锟斤拷锟接匡拷

	private boolean b;// 9锟斤拷touch锟斤拷识

	public RightMenuLinearLayout(Context context) {
		super(context);
	}

	public void setOnScrollListener(OnScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}

	public RightMenuLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(new MySimpleGesture());

	}

	/***
	 * 锟铰硷拷锟街凤拷
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		b = mGestureDetector.onTouchEvent(ev);// 锟斤拷取锟斤拷锟狡凤拷锟斤拷值.
		/***
		 * 锟缴匡拷时锟角得达拷锟斤拷锟斤拷锟�..
		 */
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			onScrollListener.doLoosen();
		}
		return super.dispatchTouchEvent(ev);
	}

	/***
	 * 锟铰硷拷9锟截达拷锟斤拷
	 * 
	 * 要锟斤拷谆锟斤拷疲锟斤拷锟斤拷锟絫ure锟侥伙拷锟斤拷锟角撅拷锟角斤拷锟斤拷9锟截ｏ拷锟斤拷锟斤拷锟皆硷拷锟斤拷ontouch. 锟斤拷锟斤拷false锟侥伙拷锟斤拷锟斤拷么锟酵伙拷锟斤拷锟铰达拷锟斤拷...
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		super.onInterceptTouchEvent(ev);
		return b;
	}

	/***
	 * 锟铰硷拷锟斤拷锟斤拷
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		isLock = false;
		return super.onTouchEvent(event);
	}

	/***
	 * 锟皆讹拷锟斤拷锟斤拷锟斤拷执锟斤拷
	 * 
	 * @author zhangjia
	 * 
	 * 
	 */
	class MySimpleGesture extends SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			isLock = true;
			return super.onDown(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (!isLock)
				onScrollListener.doScroll(distanceX);

			// 锟斤拷直锟斤拷锟斤拷水平
			if (Math.abs(distanceY) > Math.abs(distanceX)) {
				return false;
			} else {
				return true;
			}

		}
	}

	/***
	 * 锟皆讹拷锟斤拷涌锟�实锟街伙拷锟斤拷...
	 * 
	 * @author zhangjia
	 * 
	 */
	public interface OnScrollListener {
		void doScroll(float distanceX);// 锟斤拷锟斤拷...

		void doLoosen();// 锟斤拷指锟缴匡拷锟斤拷执锟斤拷...
	}

}
