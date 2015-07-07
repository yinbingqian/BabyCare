package com.lnpdit.util;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.lnpdit.babycare.R;
import com.lnpdit.monitor.MapDialog;
import com.mapbar.android.maps.GeoPoint;
import com.mapbar.android.maps.ItemizedOverlay;
import com.mapbar.android.maps.MapView;
import com.mapbar.android.maps.OverlayItem;
import com.mapbar.android.maps.Projection;

/**
 * @Module com.mapbar.android.maps.demo.TipItemizedOverlay
 * @description 图层标注操作Demo
 * @author
 * @version 1.0
 * @created Dec 13, 2011
 */
public class TipItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	private static final int[][] ITEM_STATE_TO_STATE_SET = {
			{ -16842908, -16842913, -16842919 },
			{ -16842908, -16842913, 16842919 },
			{ -16842908, 16842913, -16842919 },
			{ -16842908, 16842913, 16842919 },
			{ 16842908, -16842913, -16842919 },
			{ 16842908, -16842913, 16842919 },
			{ 16842908, 16842913, -16842919 }, { 16842908, 16842913, 16842919 } };

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Drawable marker = null;
	private Paint mPaint;
	/**
	 * 气泡宽度
	 */
	private static int TIP_MAX_WIDTH = 300;
	/**
	 * 标题字体大小
	 */
	private static int TITLE_FONT_SIZE = 24;
	/**
	 * 地址字体大小
	 */
	private static int ADDRESS_FONT_SIZE = 20;

	private Rect mTipRect;

	private boolean inTheTip = false;

	private ArrayList<String> mTitles = new ArrayList<String>();
	private Drawable mCallImg;
	private Drawable mCallImgIcon;
	private Drawable mRouteImg;
	private Drawable mRouteImgIcon;
	private Paint callPaint;
	private boolean mIsDetailTip = false;
	private boolean mIsAvailPhone = false;

	private Context context;

	public TipItemizedOverlay(Context context, Drawable defaultMarker) {
		super(defaultMarker);
		marker = defaultMarker;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		populate();

		mCallImg = context.getResources().getDrawable(
				R.drawable.tip_pointer_left);
		mCallImgIcon = context.getResources().getDrawable(
				R.drawable.phone_selector);
		int tmpW = mCallImgIcon.getIntrinsicWidth() / 2;
		int tmpH = mCallImgIcon.getIntrinsicHeight() / 2;
		mCallImgIcon.setBounds(-tmpW, -tmpH, tmpW, tmpH);

		mRouteImg = context.getResources().getDrawable(
				R.drawable.tip_pointer_right);
		mRouteImgIcon = context.getResources().getDrawable(
				R.drawable.map_arrow_0);
		tmpW = mRouteImgIcon.getIntrinsicWidth() / 2;
		tmpH = mRouteImgIcon.getIntrinsicHeight() / 2;
		mRouteImgIcon.setBounds(-tmpW, -tmpH, tmpW, tmpH);

		callPaint = new Paint();
		callPaint.setStyle(Style.FILL);
		callPaint.setColor(Color.parseColor("#FEE68D"));

		this.context = context;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	public void addOverlay(OverlayItem overlay) {
		if (overlay == null)
			return;

		inTheTip = false; // 初始设置为未选中状�?
		mIsDetailTip = true;// btn 是否显示

		String name = overlay.getTitle();
		mPaint.setTextSize(TITLE_FONT_SIZE);
		String title = name;
		int len = title.length();
		int h = -2 * TITLE_FONT_SIZE;
		/**
		 * 标题的真实宽�?
		 */
		float wTitle = mPaint.measureText(title, 0, len);

		float wTmp = wTitle;
		if (wTitle <= TIP_MAX_WIDTH) {
			mTitles.add(title);
			h += -1 * TITLE_FONT_SIZE;
		} else {
			wTmp = 0;
			while (wTitle > TIP_MAX_WIDTH) {
				len--;
				title = name.substring(0, len);
				wTitle = mPaint.measureText(title, 0, title.length());
				if (wTitle <= TIP_MAX_WIDTH) {
					wTmp = Math.max(wTmp, wTitle);
					mTitles.add(title);
					h += -1 * TITLE_FONT_SIZE;
					title = name = name.substring(len);
					len = title.length();
					wTitle = mPaint.measureText(title, 0, len);
					if (wTitle <= TIP_MAX_WIDTH) {
						wTmp = Math.max(wTmp, wTitle);
						mTitles.add(title);
						h += -1 * TITLE_FONT_SIZE;
						break;
					}
				}
			}
		}
		wTitle = wTmp;

		String address = overlay.getSnippet();
		float wSnippet = 0;
		String snippet = address;
		if (address != null) {
			mPaint.setTextSize(ADDRESS_FONT_SIZE);
			snippet = snippet.trim();
			len = snippet.length();
			wSnippet = mPaint.measureText(snippet, 0, len);
			while (wSnippet > TIP_MAX_WIDTH) {
				len--;
				snippet = address.substring(0, len) + "...";
				wSnippet = mPaint.measureText(snippet, 0, snippet.length());
			}
		}

		if ((snippet != null) && (snippet.length() > 0)) {
			h += -1 * ADDRESS_FONT_SIZE;
		}
		if (Math.abs(h) < (mRouteImgIcon.getIntrinsicHeight() + 2 * ADDRESS_FONT_SIZE)) {
			h = -1
					* (mRouteImgIcon.getIntrinsicHeight() + 2 * ADDRESS_FONT_SIZE);
		}

		int w = wTitle > wSnippet ? (int) wTitle + 12 : (int) wSnippet + 12;
		w = w < 50 ? 50 : w;

		mTipRect = new Rect(-w / 2 - 14, h - 24 - 8, w / 2 + 2, -24 - 8);
		if (mIsDetailTip) {
			// mCallImg.setBounds(0, 0, mCallImg.getIntrinsicWidth() + 25, -h);
			// mRouteImg.setBounds(0, 0, mRouteImg.getIntrinsicWidth() + 25,
			// -h);
		}
		marker.setBounds(mTipRect);

		OverlayItem newMarker = new OverlayItem(overlay.getPoint(), title,
				snippet);
		newMarker.setMarker(marker);

		mOverlays.add(newMarker);

		populate();
	}

	public void clean() {
		mOverlays.clear();
		mTitles.clear();
		setLastFocusedIndex(-1);
		populate();
	}

	private boolean isClickCallIcon = false;
	private boolean isClickRouteIcon = false;
	private Rect mCallIconRect;
	private Rect mRouteIconRect;

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (size() == 0)
			return;
		Projection projection = mapView.getProjection();
		for (OverlayItem marker : mOverlays) {
			try {
				String snippet = marker.getSnippet();
				Point point = projection.toPixels(marker.getPoint(), null);
				Rect tipBounds = marker.getMarker(0).getBounds();

				int stateIndex = 0;
				if (inTheTip)
					stateIndex = 1;

				drawAt(canvas, marker.getMarker(stateIndex), point.x, point.y,
						false);
				if (mIsDetailTip) {
					mCallImg.setState(ITEM_STATE_TO_STATE_SET[stateIndex]);
					mRouteImg.setState(ITEM_STATE_TO_STATE_SET[stateIndex]);

					Rect tmpRect = mCallImg.getBounds();
					int leftCall = (point.x + tipBounds.left)
							- (tmpRect.right - tmpRect.left) + 5; // 向右错一些，覆盖右边的圆角边
					int rightCall = (point.x + tipBounds.left) + 5; // 向右错一些，覆盖右边的圆角边
					int topCall = point.y + tipBounds.top;
					int bottomCall = topCall + (tmpRect.bottom - tmpRect.top);
					mCallIconRect = new Rect(leftCall + 5, topCall + 5,
							rightCall - 5, bottomCall - 20);
					int iconX = (mCallIconRect.left + mCallIconRect.right) / 2;
					int iconY = (mCallIconRect.top + mCallIconRect.bottom) / 2;
					drawAt(canvas, mCallImg, leftCall, topCall, false);

					tmpRect = mRouteImg.getBounds();
					int leftRoute = (point.x + tipBounds.right) - 5; // 向左错一些，覆盖左边的圆角边
					int rightRoute = leftRoute + (tmpRect.right - tmpRect.left); // 向左错一些，覆盖左边的圆角边
					int topRoute = point.y + tipBounds.top;
					int bottomRoute = topRoute + (tmpRect.bottom - tmpRect.top);
					mRouteIconRect = new Rect(leftRoute + 3, topRoute + 5,
							rightRoute - 3, bottomRoute - 20);
					int iconRouteX = (mRouteIconRect.left + mRouteIconRect.right) / 2;
					int iconRouteY = (mRouteIconRect.top + mRouteIconRect.bottom) / 2;
					drawAt(canvas, mRouteImg, leftRoute, topRoute, false);

					if (mIsAvailPhone && isClickCallIcon)
						canvas.drawRect(mCallIconRect, callPaint);
					if (isClickRouteIcon)
						canvas.drawRect(mRouteIconRect, callPaint);
					if (!mIsAvailPhone)
						mCallImgIcon.setState(ITEM_STATE_TO_STATE_SET[1]);
					else
						mCallImgIcon.setState(ITEM_STATE_TO_STATE_SET[0]);
					// drawAt(canvas, mCallImgIcon, iconX, iconY, false);
					// drawAt(canvas, mRouteImgIcon, iconRouteX, iconRouteY,
					// false);
				}
				point.x += tipBounds.left + 10;
				point.y += tipBounds.top + 8;

				mPaint.setTextSize(TITLE_FONT_SIZE);
				mPaint.setColor(Color.BLACK);

				int size = mTitles.size();
				for (int i = 0; i < size; i++) {
					point.y += TITLE_FONT_SIZE;
					canvas.drawText(mTitles.get(i), point.x, point.y, mPaint);
				}

				if (snippet != null && !"".equals(snippet)) {
					point.y += TITLE_FONT_SIZE;
					mPaint.setTextSize(ADDRESS_FONT_SIZE);
					mPaint.setColor(Color.GRAY);
					canvas.drawText(snippet, point.x, point.y, mPaint);
				}
				break;
			} catch (Exception ex) {
			}
		}
	}

	// @Override
	// public boolean onTap(GeoPoint p, MapView mapView) {
	// clean();
	// return super.onTap(p, mapView);
	// }

	@Override
	public boolean onTap(int index) {
		if (index == 0) {
			OverlayItem item = getCurrentMarker();
			if (item != null) {
				MapDialog dialog = new MapDialog();
				dialog.StartView(context, item);
			}
		}
		clean();
		return super.onTap(index);
	}

	public OverlayItem getCurrentMarker() {
		if (size() > 0) {
			try {
				return mOverlays.get(0);
			} catch (Exception ex) {
			}
		}
		return null;
	}

	public void showToast(String sc) {
		Toast toast = Toast.makeText(context, sc, Toast.LENGTH_SHORT);
		toast.show();
	}
}
