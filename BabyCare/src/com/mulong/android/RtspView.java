package com.mulong.android;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import com.lnpdit.file.FileUtil;
import com.lnpdit.photo.ZoomState;
import com.lnpdit.util.ByteInfo;
import com.lnpdit.util.ComUtil;

import android.R.integer;
import android.R.string;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.Toast;


public class RtspView extends View implements Runnable, Observer {

	Bitmap mBitQQ = null;

	Bitmap mSCBitmap = null;

	int width; // 此处设定初始分辨率
	int height;

	int workmode = -1;

	// dst address parameter
	String dstip;
	int dstPort;
	Context pcontext;

	// 临时测试设备名
	String tempString;
	String recordName;
	int byteFlag = 1;
	// 通道号
	int chNo;
	// 适配ID
	int adapterID;

	public int screenHeight;
	public int screenWidth;
	// **********屏幕绘制相关****************
	public int resolutionW = 352;// 宽
	public int resolutionH = 288;// 高
	Bitmap videoBit = Bitmap.createBitmap(resolutionW, resolutionH,
			Config.RGB_565);
	Bitmap resizedBitmap;
	// 创建操作图片用的matrix对象
	Matrix matrix = new Matrix();
	// 计算缩放率，新尺寸除原始尺寸
	private float leftDis;
	private float topOffset;

	// 视频缩放参数

	private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	private final Rect mRectSrc = new Rect();
	private final Rect mRectDst = new Rect();
	private float mAspectQuotient;

	private ZoomState mState;
	// ************************************

	byte[] mPixel = new byte[width * height * 2];

	ByteBuffer buffer = ByteBuffer.wrap(mPixel);

	int mTrans = 0x0F0F0F0F;

	String PathFileName;

	public native int InitDecoder(int width, int height);

	public native int UninitDecoder();

	public native int DecoderNal(byte[] in, int insize, byte[] out);

	// new func by wu
	public native int InputData(byte[] in, int len);

	public native int Decode(byte[] out);

	public native int ChangeBuffer();

	public native int InitAudioDecoder();

	public native int CloseAudioDecoder();

	public native int AudioDecode(byte[] in, int insize, int outsize);

	public native int AudioEncode(byte[] in, int insize, byte[] out, int outsize);

	static Socket socket;

	Boolean viewStatus = false;

	Boolean stopStatusBoolean = true;

	Boolean isHeart = true;

	public ComUtil util;

	byte[] wavAudio;
	int wavLength;

	public void InitVideoParam(String file, int w, int h, int flag) {
		PathFileName = file;
		width = w;
		height = h;

		byteFlag = flag;
		util = new ComUtil();

	}

	public void PlayVideo(int sWidth, int sHeight) {

		screenWidth = sWidth;
		screenHeight = sHeight;

		mPixel = new byte[width * height * 2];

		buffer = ByteBuffer.wrap(mPixel);

		int i = mPixel.length;

		for (i = 0; i < mPixel.length; i++) {
			mPixel[i] = (byte) 0x00;
		}
		new Thread(this).start();
	}

	public void setContext(Context context) {
		pcontext = context;

	}

	public RtspView(Context context) {
		super(context);
		pcontext = context;
		setFocusable(true);

	}

	// 获得查看状态
	public Boolean getViewStatus() {
		return viewStatus;
	}

	// 退出时设置状态
	public void setStopStatus(Boolean status) {
		stopStatusBoolean = status;
		ifThisVideo = !status;
	}

	public void setViewStatusFalse() {
		viewStatus = false;
	}

	// 是否发送心跳
	public void setHeart(Boolean status) {
		isHeart = status;
	}

	public Bitmap snapBitmap() {
		final int viewWidth = getWidth();
		final int viewHeight = getHeight();
		return zoomImage(resizedBitmap, viewWidth, viewHeight);
	}

	/***
	 * 图片的缩放方法
	 * 
	 * @param bgimage
	 *            ：源图片资源
	 * @param newWidth
	 *            ：缩放后宽度
	 * @param newHeight
	 *            ：缩放后高度
	 * @return
	 */
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
			double newHeight) {
		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
				(int) height, matrix, true);
		return bitmap;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		try {
			buffer.rewind();
			// buffer.position(0);
			videoBit.copyPixelsFromBuffer(buffer);
			// System.out.print("resolutionW is ");
			// System.out.println(resolutionW);
			// System.out.print("resolutionH is ");
			// System.out.println(resolutionH);
			// System.out.print("videoBit is ");
			// System.out.println(videoBit);
			// System.out.print("matrix is ");
			// System.out.println(matrix);
			resizedBitmap = Bitmap.createBitmap(videoBit, 0, 0, resolutionW,
					resolutionH, matrix, true);

			if (resizedBitmap != null && mState != null) {
				final int viewWidth = getWidth();
				final int viewHeight = getHeight();
				final int bitmapWidth = resizedBitmap.getWidth();
				final int bitmapHeight = resizedBitmap.getHeight();

				final float panX = mState.getPanX();
				final float panY = mState.getPanY();
				final float zoomX = mState.getZoomX(mAspectQuotient)
						* viewWidth / bitmapWidth;
				final float zoomY = mState.getZoomY(mAspectQuotient)
						* viewHeight / bitmapHeight;

				// Setup source and destination rectangles
				// 这里假定图片的高和宽都大于显示区域的高和宽，如果不是在下面做调整
				mRectSrc.left = (int) (panX * bitmapWidth - viewWidth
						/ (zoomX * 2));
				mRectSrc.top = (int) (panY * bitmapHeight - viewHeight
						/ (zoomY * 2));
				mRectSrc.right = (int) (mRectSrc.left + viewWidth / zoomX);
				mRectSrc.bottom = (int) (mRectSrc.top + viewHeight / zoomY);
				mRectDst.left = getLeft();
				mRectDst.top = getTop();
				mRectDst.right = getRight();
				mRectDst.bottom = getBottom();

				// Adjust source rectangle so that it fits within the source
				// image.
				// 如果图片宽或高小于显示区域宽或高（组件大小）或者由于移动或缩放引起的下面条件成立则调整矩形区域边界
				if (mRectSrc.left < 0) {
					mRectDst.left += -mRectSrc.left * zoomX;
					mRectSrc.left = 0;
				}
				if (mRectSrc.right > bitmapWidth) {
					mRectDst.right -= (mRectSrc.right - bitmapWidth) * zoomX;
					mRectSrc.right = bitmapWidth;
				}
				if (mRectSrc.top < 0) {
					mRectDst.top += -mRectSrc.top * zoomY;
					mRectSrc.top = 0;
				}
				if (mRectSrc.bottom > bitmapHeight) {
					mRectDst.bottom -= (mRectSrc.bottom - bitmapHeight) * zoomY;
					mRectSrc.bottom = bitmapHeight;
				}
				canvas.drawBitmap(resizedBitmap, mRectSrc, mRectDst, mPaint);
			}
		} catch (Exception e) {
			// TODO: handle exception
			// System.out.print(" drawerror is ");
			// System.out.println(e.toString());
			e.printStackTrace();
		}
	}

	public void setZoomState(ZoomState state) {
		if (mState != null) {
			mState.deleteObserver(this);
		}
		mState = state;
		mState.addObserver(this);
		invalidate();
	}

	public void update(Observable observable, Object data) {
		invalidate();
	}

	private void calculateAspectQuotient() {
		if (resizedBitmap != null) {
			mAspectQuotient = (((float) resizedBitmap.getWidth()) / resizedBitmap
					.getHeight()) / (((float) getWidth()) / getHeight());
		}
	}

	public void setImage(Bitmap bitmap) {
		resizedBitmap = bitmap;
		calculateAspectQuotient();
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		calculateAspectQuotient();
	}

	public void run() {
		if (workmode == 1)
			runnet();
		else if (workmode == 2) {
			runrecord();
		} else
			runfile();
	}

	// 找到包头
	public int findHeader(byte[] buff) {
		int i;
		byte[] tempByte;
		int ulPDUID; // PDU ID
		ComUtil util = new ComUtil();

		tempByte = new byte[4];
		for (i = 0; i < 4; i++) {
			tempByte[i] = buff[i + 12];
		}
		ulPDUID = util.bytesToInt(tempByte);
		return ulPDUID;
	}

	// 获取视频数据
	public byte[] receiveStFrameData(byte[] buff, int length) {
		int i;
		byte[] returnBuffer;

		returnBuffer = new byte[length];
		for (i = 0; i < length; i++) {
			returnBuffer[i] = buff[i + 4];
		}
		return returnBuffer;
	}

	// 获取应答长度
	public int receiveAnswerLength(byte[] buff) {
		int i;
		byte[] tempByte;
		int ulPDUDataLen; // PDU数据长度(不含协议头)
		ComUtil util = new ComUtil();

		tempByte = new byte[4];
		for (i = 0; i < 4; i++) {
			tempByte[i] = buff[i + 16];
		}
		ulPDUDataLen = util.bytesToInt(tempByte);

		return ulPDUDataLen;
	}

	// 获取视频数据长度
	public int receiveStFrameDataLength(byte[] buff) {
		int i;
		byte[] tempByte;
		int ulPDUDataLen; // PDU数据长度(不含协议头)
		ComUtil util = new ComUtil();

		tempByte = new byte[4];
		for (i = 0; i < 4; i++) {
			tempByte[i] = buff[i + 16];
		}
		ulPDUDataLen = util.bytesToInt(tempByte);

		int buffLength = ulPDUDataLen - 12;
		return buffLength;
	}

	// 获取音频数据长度
	public int receiveRadioDataLength(byte[] buff) {
		int i;
		byte[] tempByte;
		int ulPDUDataLen; // PDU数据长度(不含协议头)
		ComUtil util = new ComUtil();

		tempByte = new byte[4];
		for (i = 0; i < 4; i++) {
			tempByte[i] = buff[i + 16];
		}
		ulPDUDataLen = util.bytesToInt(tempByte);
		return ulPDUDataLen;
	}

	// 完整解析，目前未使用
	public int receiveBuffer(byte[] buff) {
		int i;
		int j;
		byte[] tempByte;
		byte[] code;
		String codeNoString;
		int ulPDUFlag; // PDU标志位
		int ulReservered1; // 保留字段，默认为0
		int ulReservered2; // 保留字段，默认为0
		int ulPDUID; // PDU ID
		int ulPDUDataLen; // PDU数据长度(不含协议头)
		int ulResult;// 结果
		int ulAdapterID;
		int ulDeviceSNLen;
		int clDeviceSN;
		int ulChannelID;
		ComUtil util = new ComUtil();

		tempByte = new byte[4];
		for (i = 0; i < 4; i++) {
			tempByte[i] = buff[i];
		}
		ulPDUFlag = util.bytesToInt(tempByte);

		for (i = 0; i < 4; i++) {
			tempByte[i] = buff[i + 4];
		}
		ulReservered1 = util.bytesToInt(tempByte);

		for (i = 0; i < 4; i++) {
			tempByte[i] = buff[i + 8];
		}
		ulReservered2 = util.bytesToInt(tempByte);

		for (i = 0; i < 4; i++) {
			tempByte[i] = buff[i + 12];
		}
		ulPDUID = util.bytesToInt(tempByte);

		for (i = 0; i < 4; i++) {
			tempByte[i] = buff[i + 16];
		}
		ulPDUDataLen = util.bytesToInt(tempByte);

		for (i = 0; i < 4; i++) {
			tempByte[i] = buff[i + 20];
		}
		ulResult = util.bytesToInt(tempByte);

		for (i = 0; i < 4; i++) {
			tempByte[i] = buff[i + 24];
		}
		ulAdapterID = util.bytesToInt(tempByte);

		for (i = 0; i < 4; i++) {
			tempByte[i] = buff[i + 28];
		}
		ulDeviceSNLen = util.bytesToInt(tempByte);

		code = new byte[ulDeviceSNLen];
		for (i = 0; i < ulDeviceSNLen; i++) {
			code[i] = buff[i + 32];
		}
		codeNoString = new String(code);

		for (i = 0; i < 4; i++) {
			tempByte[i] = buff[i + 32 + ulDeviceSNLen];
		}
		ulChannelID = util.bytesToInt(tempByte);

		return ulResult;
	}

	// 是否在后台运行
	public Boolean isBackRunning = false;

	public void setBackRunning(Boolean bool) {
		isBackRunning = bool;
	}

	byte[] recordBuffer;

	// 缓冲区
	Queue<ByteInfo> byteQueue = new LinkedList<ByteInfo>();
	ByteInfo info;
	int byteCount;
	boolean ifThisVideo; // 说明本缓冲区是否还继续播放；


	public void runnet() {
		BufferedReader in;
		// PrintWriter out;
		int totallen = 0, count = 0;

		try {
			socket = new Socket(dstip, dstPort);
		} catch (IOException e) {
			// System.out.print("we get exception receive len is ");
			// System.out.println(totallen);
			// System.out.print("we get totoal frame is  ");
			// System.out.println(count);
			if (e != null) {
				System.out.println(e.getMessage());
			}
			// e.printStackTrace();
		}
	}

	ProgressDialog dialog;

	int audioFlag = 1;

	// 播放缓冲区
	private class PlayQueue implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				while (ifThisVideo) {
					int queueSize = byteQueue.size();
					// System.out.println("queueSize:" + queueSize);
					ByteInfo queueGetInfo;
					byte[] queueGetBuff;
					int queueGetLength;
					if (byteCount > byteFlag) {
						if (queueSize > 0) {
							// 根据buff（包括了帧类型）和视频数据的长度获取到视频数据，也就是把帧类型去掉
							queueGetInfo = byteQueue.poll();
							queueGetBuff = queueGetInfo.getStream();
							queueGetLength = queueGetInfo.getLength();
							try {
								InputData(queueGetBuff, queueGetLength);
							} catch (Exception e) {
								// TODO: handle exception
							}

							while (true) {
								try {
									int type = Decode(mPixel);
									if (type < 0) {
										break;
									}
									if (type == 5 || type == 1) {
										postInvalidate();
										// 如果显示图像，则viewStatus=true
										viewStatus = true;
										if (type == 1) {
											recordBuffer = queueGetBuff;
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							try {
								ChangeBuffer();
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {

							// new Thread() {
							// public void run() {
							// handler.post(netWaiting);
							// }
							// }.start();
							// System.out.println("queueSize:" + queueSize);

						}

					}
				}
				UninitDecoder();
				byteQueue.clear();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
		}
	};

	public void runfile() {
		try {
			InputStream is = null;
			FileInputStream fileIS = null;

			int totallen = 0;
			int bytesRead = 0;

			byte[] SockBuf = new byte[2048];

			try {
				fileIS = new FileInputStream(PathFileName);
			} catch (IOException e) {
				// System.out.print("can't open ");
				// System.out.println(PathFileName);
				return;
			}

			InitDecoder(width, height);
			int count = 0;
			long startTime = System.nanoTime();

			while (!Thread.currentThread().isInterrupted()) {
				try {
					bytesRead = fileIS.read(SockBuf, 0, 1024);
				} catch (IOException e) {
				}

				if (bytesRead <= 0)
					break;

				InputData(SockBuf, bytesRead);
				totallen += bytesRead;

				while (true) {
					try {
						int type = Decode(mPixel);
						if (type < 0)
							break;
						if (type == 5 || type == 1) {
							count++;
							postInvalidate();
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				ChangeBuffer();

			}
			// try {
			// if (fileIS != null)
			// fileIS.close();
			// if (is != null)
			// is.close();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// Log.d("", "end program");
			long endTime = System.nanoTime();
			// System.out.print("Done! Total frames is ");
			// System.out.println(count);
			float totalTime = (endTime - startTime) / 1000000000.0f;
			// System.out.print(" Total used time is ");
			// System.out.println(totalTime);
			float fps = count / totalTime;
			// System.out.print(" fps is ");
			// System.out.println(fps);
			//
			WriteToFile("Total frames is ");
			WriteToFile(String.valueOf(count));
			WriteToFile("Total used time is ");
			WriteToFile(String.valueOf(totalTime));
			WriteToFile("fps is ");
			WriteToFile(String.valueOf(fps));

			UninitDecoder();
		} catch (Exception e) {
			// TODO: handle exception
			// System.out.print(" runfileerror is ");
			// System.out.println(e.toString());
			e.printStackTrace();
		}
	}

	// 播控算法
	public void VideoControl(long time) {
		String formatStrEnd = "";
		long DateTemp1 = new Date().getTime();
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		formatStrEnd = bartDateFormat.format(time);

	}

	// 检查socket
	public Socket checkSocket() {
		return socket;
	}

	// 关闭socket
	public void closeSocket() {
		try {
			if (socket != null) {
				if (resizedBitmap != null && !resizedBitmap.isRecycled())
					resizedBitmap.recycle();
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void NetTest(String dip, int dPort, String vString, int no, int aid) {
		dstip = dip;
		dstPort = dPort;

		workmode = 1;

		tempString = vString;
		chNo = no;
		adapterID = aid;
	}

	public void NetTestRecord(String dip, int dPort, String vString, int no,
			int aid, String name) {
		dstip = dip;
		dstPort = dPort;

		workmode = 2;

		tempString = vString;
		chNo = no;
		adapterID = aid;
		recordName = name;
	}

	// write result to config file
	public int WriteToFile(String content) {
		FileOutputStream fileIS = null;

		try {
			fileIS = new FileOutputStream("/sdcard/result.conf", true);

			fileIS.write(content.getBytes());
			// StringBuffer text = new StringBuffer();
			// while ((tempStr = br.readLine())!= null )
			// System.out.print(tempStr);
			// text.append(tempStr);

			fileIS.close();
		} catch (IOException e) {
			return 0;
		}

		return 0;
	}

	public void holderCtrol(String direction, int channelNum) {
		try {
			if (socket != null) {
				DataOutputStream out = new DataOutputStream(
						socket.getOutputStream());
				PrintWriter pw = new PrintWriter(out);
				pw.write(direction);
				pw.flush();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

	public void sendByte(byte[] bit) {
		try {
			if (socket != null) {
				DataOutputStream out = new DataOutputStream(
						socket.getOutputStream());
				out.write(bit);
				out.flush();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

	// 发送协议
	// 视频开始协议
	public void videoBegin() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[32 + code.length];
			// for (i = 0; i < head.length; i++) {
			// bit[i] = head[i];
			// }
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			// PDU数据长度
			tempByte = util.longToBytes(12 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 通道ID
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			sendByte(bit);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 视频停止协议
	public void videoStop() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[32 + code.length];
			// for (i = 0; i < head.length; i++) {
			// bit[i] = head[i];
			// }
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(2);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			// PDU数据长度
			tempByte = util.longToBytes(12 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 通道ID
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			sendByte(bit);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 发送协议
	// 对讲开始协议
	public void radioBegin() {

		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[32 + code.length];
			// for (i = 0; i < head.length; i++) {
			// bit[i] = head[i];
			// }
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			// PDU数据长度
			tempByte = util.longToBytes(12 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 通道ID
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			sendByte(bit);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 心跳
	public void heartBeat() {
		int i;
		byte[] bit;
		byte[] tempByte;
		try {
			bit = new byte[24];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(3000);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			tempByte = util.longToBytes(1);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			sendByte(bit);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 云台向上
	public void videoUP() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(1);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台开始
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 云台向上停止
	public void videoUPStop() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(1);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台停止
			tempByte = util.longToBytes(1);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 云台向下
	public void videoDOWN() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(2);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台开始
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 云台向下停止
	public void videoDOWNStop() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(2);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台停止
			tempByte = util.longToBytes(1);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 云台向左
	public void videoLEFT() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(3);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台开始
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 云台向左停止
	public void videoLEFTStop() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(3);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台停止
			tempByte = util.longToBytes(1);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 云台向右
	public void videoRIGHT() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台开始
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 云台向右停止
	public void videoRIGHTStop() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台停止
			tempByte = util.longToBytes(1);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 变倍加
	public void videoZoomIn() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(11);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台开始
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 变倍加停止
	public void videoZoomInStop() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(11);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台停止
			tempByte = util.longToBytes(1);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 变倍减
	public void videoZoomOut() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(12);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台开始
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 变倍减停止
	public void videoZoomOutStop() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(12);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台停止
			tempByte = util.longToBytes(1);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 变焦加
	public void videoFocusIn() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(13);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台开始
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 变焦加停止
	public void videoFocusInStop() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(13);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台停止
			tempByte = util.longToBytes(1);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 变焦减
	public void videoFocusOut() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(14);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台开始
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 变焦减停止
	public void videoFocusOutStop() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			bit = new byte[44 + code.length];
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(4);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			tempByte = util.longToBytes(24 + code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 云台通道
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + j] = tempByte[i];
			}
			// 云台命令
			tempByte = util.longToBytes(14);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + j] = tempByte[i];
			}
			// 云台速度
			tempByte = util.longToBytes(5);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 36 + j] = tempByte[i];
			}
			// 云台停止
			tempByte = util.longToBytes(1);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 40 + j] = tempByte[i];
			}
			sendByte(bit);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 录像开始协议
	public void recordBegin() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] name;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			name = recordName.getBytes("UTF8");
			bit = new byte[36 + code.length + name.length];
			// for (i = 0; i < head.length; i++) {
			// bit[i] = head[i];
			// }
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(103);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			// PDU数据长度
			tempByte = util.longToBytes(16 + code.length + name.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 通道ID
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + code.length] = tempByte[i];
			}
			// 录像名称长度
			tempByte = util.longToBytes(name.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + code.length] = tempByte[i];
			}
			// 录像名称
			for (i = 0; i < name.length; i++) {
				bit[i + 36 + code.length] = name[i];
			}
			sendByte(bit);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 录像停止协议
	public void recordStop() {
		int i;
		int j;
		// String codeno = "ML2";
		String codeno = tempString;
		byte[] bit;
		byte[] code;
		byte[] name;
		byte[] tempByte;
		try {
			code = codeno.getBytes("UTF8");
			name = recordName.getBytes("UTF8");
			bit = new byte[36 + code.length + name.length];
			// for (i = 0; i < head.length; i++) {
			// bit[i] = head[i];
			// }
			tempByte = util.longToBytes(0);
			for (i = 0; i < tempByte.length; i++) {
				bit[i] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 4] = tempByte[i];
			}
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 8] = tempByte[i];
			}
			tempByte = util.longToBytes(106);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 12] = tempByte[i];
			}
			// PDU数据长度
			tempByte = util.longToBytes(16 + code.length + name.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 16] = tempByte[i];
			}
			// 适配ID
			tempByte = util.longToBytes(adapterID);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 20] = tempByte[i];
			}
			tempByte = util.longToBytes(code.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 24] = tempByte[i];
			}
			for (j = 0; j < code.length; j++) {
				bit[i + 24 + j] = code[j];
			}
			// 通道ID
			tempByte = util.longToBytes(chNo);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 28 + code.length] = tempByte[i];
			}
			// 录像名称长度
			tempByte = util.longToBytes(name.length);
			for (i = 0; i < tempByte.length; i++) {
				bit[i + 32 + code.length] = tempByte[i];
			}
			// 录像名称
			for (i = 0; i < name.length; i++) {
				bit[i + 36 + code.length] = name[i];
			}
			sendByte(bit);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void init(int widthFrame, int heightFrame) {
		try {
			InitDecoder(widthFrame, heightFrame);

			resolutionW = widthFrame;
			resolutionH = heightFrame;
			videoBit = Bitmap.createBitmap(resolutionW, resolutionH,
					Config.RGB_565);

			mPixel = new byte[widthFrame * heightFrame * 2];

			buffer = ByteBuffer.wrap(mPixel);

			int i = mPixel.length;

			for (i = 0; i < mPixel.length; i++) {
				mPixel[i] = (byte) 0x00;
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void runrecord() {
		BufferedReader in;
		// PrintWriter out;
		int totallen = 0, count = 0;
		int flag = 0;// 第一次获取到宽度高度进行初始化。

		try {
			socket = new Socket(dstip, dstPort);

			recordBegin();

			// 发送心跳
			heartTimer = new Timer();

			Thread hThread = new Thread(heartThread);
			hThread.start();

			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			// out = new PrintWriter(socket.getOutputStream(), true);
			// // out.println("1jkl");

			byte[] buff;// 定义二进制缓存
			byte[] headBuffer = new byte[20];// 定义协议头
			byte[] buffer; // 缓冲区
			InputStream is = null; // socket连接需要的
			BufferedInputStream bis = null;// 接收二进制数据需要的，获取接收到的二进制流用BufferedInputStream；
			// FileOutputStream fos = null; //
			// 文件处理需要的，FileOutputStream，二进制流由内存写入硬盘
			is = socket.getInputStream();// 获取流向socket的流
			bis = new BufferedInputStream(is);// 这里开始接收二进制数据流
			// 记录编码返回-1
			int falseCount = 0;
			int changeCount = 0;
			int inputCount = 0;
			int decodeCount = 0;

			while (true) {

				try {

					// if (!isBackRunning) {
					int len = bis.read(headBuffer);// 读出接收到的二进制流，放入缓存buff，返回本次接收到的二进制流的长度,若长度为零，返回-1
					System.out.print("length of bis");
					System.out.println(len);

					if (socket.isClosed()) {
						if (!stopStatusBoolean)// 判断是否退出的属性，如果未退出，则重连
						{
							socket.close();
							is = null;
							socket = new Socket(dstip, dstPort);
							recordBegin();
							is = socket.getInputStream();// 获取流向socket的流
							bis = new BufferedInputStream(is);// 这里开始接收二进制数据流
							len = bis.read(headBuffer);//
						} else {
							break;
						}
					}

					if (-1 == len)// len==-12h,没流再来了，结束
					{
						break;
					}

					// 判断头长度是否为20，如果不是，继续读取
					while (len != 20 && len != -1) {
						byte[] tempBuffer = new byte[20 - len];
						int tempLen = bis.read(tempBuffer);
						for (int i = 0; i < tempLen; i++) {
							headBuffer[len + i] = tempBuffer[i];
						}
						len = len + tempLen;
					}

					// 通过协议头找到PDUID
					int pduID = findHeader(headBuffer);
					System.out.print("pduID is ");
					System.out.println(pduID);

					switch (pduID) {
					case 104: {
						// 如果是应答，应该将这部分数据读出。
						byte[] answerBuffer = new byte[4];// 返回值
						int answerLength = bis.read(answerBuffer);
						while (answerLength != 4) {
							byte[] tempBuffer = new byte[4 - answerLength];
							int tempLen = bis.read(tempBuffer);
							for (int i = 0; i < tempLen; i++) {
								answerBuffer[answerLength + i] = tempBuffer[i];
							}
							answerLength = answerLength + tempLen;
						}
						int status = util.bytesToInt(answerBuffer);
						// System.out.print("answer status is : ");
						// System.out.println(status);

						// if (status == 0) {
						// 读取适配 ID
						byte[] temp = new byte[4];
						answerLength = bis.read(temp);
						while (answerLength != 4) {
							byte[] tempBuffer = new byte[4 - answerLength];
							int tempLen = bis.read(tempBuffer);
							for (int i = 0; i < tempLen; i++) {
								answerBuffer[answerLength + i] = tempBuffer[i];
							}
							answerLength = answerLength + tempLen;
						}
						int shipeiID = util.bytesToInt(temp);
						// System.out.print("shipeiID is : ");
						// System.out.println(shipeiID);

						// 读取 序列号长度
						temp = new byte[4];
						answerLength = bis.read(temp);
						while (answerLength != 4) {
							byte[] tempBuffer = new byte[4 - answerLength];
							int tempLen = bis.read(tempBuffer);
							for (int i = 0; i < tempLen; i++) {
								answerBuffer[answerLength + i] = tempBuffer[i];
							}
							answerLength = answerLength + tempLen;
						}
						int codeLength = util.bytesToInt(temp);
						// System.out.print("codeLength is : ");
						// System.out.println(codeLength);

						temp = new byte[codeLength];
						answerLength = bis.read(temp);
						while (answerLength != codeLength) {
							byte[] tempBuffer = new byte[codeLength
									- answerLength];
							int tempLen = bis.read(tempBuffer);
							for (int i = 0; i < tempLen; i++) {
								answerBuffer[answerLength + i] = tempBuffer[i];
							}
							answerLength = answerLength + tempLen;
						}
						String code = new String(temp);
						// System.out.print("code is : ");
						// System.out.println(code);

						// 读取设备通道ID
						temp = new byte[4];
						answerLength = bis.read(temp);
						while (answerLength != 4) {
							byte[] tempBuffer = new byte[4 - answerLength];
							int tempLen = bis.read(tempBuffer);
							for (int i = 0; i < tempLen; i++) {
								answerBuffer[answerLength + i] = tempBuffer[i];
							}
							answerLength = answerLength + tempLen;
						}
						int tongdaoId = util.bytesToInt(temp);
						// System.out.print("tongdaoId is : ");
						// System.out.println(tongdaoId);

						// 读取录像名称长度
						temp = new byte[4];
						answerLength = bis.read(temp);
						while (answerLength != 4) {
							byte[] tempBuffer = new byte[4 - answerLength];
							int tempLen = bis.read(tempBuffer);
							for (int i = 0; i < tempLen; i++) {
								answerBuffer[answerLength + i] = tempBuffer[i];
							}
							answerLength = answerLength + tempLen;
						}
						int recordLength = util.bytesToInt(temp);
						// System.out.print("recordLength is : ");
						// System.out.println(recordLength);

						temp = new byte[recordLength];
						answerLength = bis.read(temp);
						while (answerLength != recordLength) {
							byte[] tempBuffer = new byte[recordLength
									- answerLength];
							int tempLen = bis.read(tempBuffer);
							for (int i = 0; i < tempLen; i++) {
								answerBuffer[answerLength + i] = tempBuffer[i];
							}
							answerLength = answerLength + tempLen;
						}
						String rName = new String(temp);
						// System.out.print("recordName is : ");
						// System.out.println(rName);

						// }

					}
						break;
					// case 3: {
					// // 通过协议头获取到视频数据的长度
					// InitDecoder(width, height);
					// int dataLength = receiveStFrameDataLength(headBuffer);
					//
					// // 获取帧类型
					// buff = new byte[4];
					//
					// int typeLength = bis.read(buff);
					// while (typeLength != 4) {
					// byte[] tempBuffer = new byte[4 - typeLength];
					// int tempLen = bis.read(tempBuffer);
					// for (int i = 0; i < tempLen; i++) {
					// buff[typeLength + i] = tempBuffer[i];
					// }
					// typeLength = typeLength + tempLen;
					// }
					// int typeInt = util.bytesToInt(buff);
					// System.out.print("typeInt is : ");
					// System.out.println(typeInt);
					//
					// // 获取frameRate
					// byte[] temp = new byte[4];
					//
					// int rateLength = bis.read(temp);
					// while (rateLength != 4) {
					// byte[] tempBuffer = new byte[4 - rateLength];
					// int tempLen = bis.read(tempBuffer);
					// for (int i = 0; i < tempLen; i++) {
					// temp[rateLength + i] = tempBuffer[i];
					// }
					// rateLength = rateLength + tempLen;
					// }
					// int frameRate = util.bytesToInt(temp);
					// System.out.print("frameRate is : ");
					// System.out.println(frameRate);
					//
					// // 获取 frameIndex
					// temp = new byte[4];
					//
					// int indexLength = bis.read(temp);
					// while (indexLength != 4) {
					// byte[] tempBuffer = new byte[4 - indexLength];
					// int tempLen = bis.read(tempBuffer);
					// for (int i = 0; i < tempLen; i++) {
					// temp[indexLength + i] = tempBuffer[i];
					// }
					// indexLength = indexLength + tempLen;
					// }
					// int frameIndex = util.bytesToInt(temp);
					// System.out.print("frameIndex is : ");
					// System.out.println(frameIndex);
					//
					// // 然后获取帧数据
					// byte[] receiveBuff = new byte[dataLength];
					// int tempDataLen = bis.read(receiveBuff);
					//
					// // 判断数据长度是否为dataLength，如果不是，继续读取
					// while (tempDataLen != dataLength) {
					// byte[] tempBuffer = new byte[dataLength
					// - tempDataLen];
					// int tempLen = bis.read(tempBuffer);
					// for (int i = 0; i < tempLen; i++) {
					// receiveBuff[tempDataLen + i] = tempBuffer[i];
					// }
					// tempDataLen = tempDataLen + tempLen;
					// }
					//
					// // 根据buff（包括了帧类型）和视频数据的长度获取到视频数据，也就是把帧类型去掉
					//
					// try {
					// InputData(receiveBuff, dataLength);
					// } catch (Exception e) {
					// // TODO: handle exception
					// inputCount++;
					// System.out.print("inputCount is :");
					// System.out.println(inputCount);
					// }
					// totallen += dataLength;
					//
					// while (true) {
					// try {
					// int type = Decode(mPixel);
					// if (type < 0) {
					// falseCount++;
					// System.out.print("falseCount is :");
					// System.out.println(falseCount);
					// break;
					// }
					// if (type == 5 || type == 1) {
					// count++;
					// postInvalidate();
					// // 如果显示图像，则viewStatus=true
					// viewStatus = true;
					// if (type == 1) {
					// recordBuffer = receiveBuff;
					// }
					// }
					// } catch (Exception e) {
					// // TODO: handle exception
					// decodeCount++;
					// System.out.print("decodeCount is :");
					// System.out.println(decodeCount);
					// }
					// }
					// try {
					// ChangeBuffer();
					// } catch (Exception e) {
					// // TODO: handle exception
					// changeCount++;
					// System.out.print("changeCount is :");
					// System.out.println(changeCount);
					// }
					//
					// }
					// break;
					case 3: {
						// 通过协议头获取到视频数据的长度
						int dataLength = receiveStFrameDataLength(headBuffer);

						// 获取帧类型
						buff = new byte[4];

						int typeLength = bis.read(buff);
						while (typeLength != 4) {
							byte[] tempBuffer = new byte[4 - typeLength];
							int tempLen = bis.read(tempBuffer);
							for (int i = 0; i < tempLen; i++) {
								buff[typeLength + i] = tempBuffer[i];
							}
							typeLength = typeLength + tempLen;
						}
						int typeInt = util.bytesToInt(buff);
						// System.out.print("typeInt is : ");
						// System.out.println(typeInt);

						// 获取frameRate
						byte[] temp = new byte[4];

						int rateLength = bis.read(temp);
						while (rateLength != 4) {
							byte[] tempBuffer = new byte[4 - rateLength];
							int tempLen = bis.read(tempBuffer);
							for (int i = 0; i < tempLen; i++) {
								temp[rateLength + i] = tempBuffer[i];
							}
							rateLength = rateLength + tempLen;
						}
						int frameRate = util.bytesToInt(temp);
						System.out.print("frameRate is : ");
						System.out.println(frameRate);

						// 获取 frameIndex
						temp = new byte[4];

						int indexLength = bis.read(temp);
						while (indexLength != 4) {
							byte[] tempBuffer = new byte[4 - indexLength];
							int tempLen = bis.read(tempBuffer);
							for (int i = 0; i < tempLen; i++) {
								temp[indexLength + i] = tempBuffer[i];
							}
							indexLength = indexLength + tempLen;
						}
						int frameIndex = util.bytesToInt(temp);
						System.out.print("frameIndex is : ");
						System.out.println(frameIndex);

						// 获取 视频宽度
						temp = new byte[4];

						int widthLength = bis.read(temp);
						while (widthLength != 4) {
							byte[] tempBuffer = new byte[4 - widthLength];
							int tempLen = bis.read(tempBuffer);
							for (int i = 0; i < tempLen; i++) {
								temp[widthLength + i] = tempBuffer[i];
							}
							widthLength = widthLength + tempLen;
						}
						int widthFrame = util.bytesToInt(temp);
						// System.out.print("widthFrame is : ");
						// System.out.println(widthFrame);

						// 获取 视频高度
						temp = new byte[4];

						int heightLength = bis.read(temp);
						while (heightLength != 4) {
							byte[] tempBuffer = new byte[4 - heightLength];
							int tempLen = bis.read(tempBuffer);
							for (int i = 0; i < tempLen; i++) {
								temp[heightLength + i] = tempBuffer[i];
							}
							heightLength = heightLength + tempLen;
						}
						int heightFrame = util.bytesToInt(temp);
						// System.out.print("heightFrame is : ");
						// System.out.println(heightFrame);

						if (flag == 0) {
							init(widthFrame, heightFrame);
							flag++;
						}

						// 然后获取帧数据
						byte[] receiveBuff = new byte[dataLength];
						int tempDataLen = bis.read(receiveBuff);

						// 判断数据长度是否为dataLength，如果不是，继续读取
						while (tempDataLen != dataLength) {
							byte[] tempBuffer = new byte[dataLength
									- tempDataLen];
							int tempLen = bis.read(tempBuffer);
							for (int i = 0; i < tempLen; i++) {
								receiveBuff[tempDataLen + i] = tempBuffer[i];
							}
							tempDataLen = tempDataLen + tempLen;
						}

						// 根据buff（包括了帧类型）和视频数据的长度获取到视频数据，也就是把帧类型去掉

						try {
							InputData(receiveBuff, dataLength);
						} catch (Exception e) {
							// TODO: handle exception
							inputCount++;
							// System.out.print("inputCount is :");
							// System.out.println(inputCount);
						}
						totallen += dataLength;

						while (true) {
							try {
								int type = Decode(mPixel);
								if (type < 0) {
									falseCount++;
									// System.out.print("falseCount is :");
									// System.out.println(falseCount);
									break;
								}
								if (type == 5 || type == 1) {
									count++;
									postInvalidate();
									// 如果显示图像，则viewStatus=true
									viewStatus = true;
									if (type == 1) {
										recordBuffer = receiveBuff;
									}
								}
							} catch (Exception e) {
								// TODO: handle exception
								decodeCount++;
								// System.out.print("decodeCount is :");
								// System.out.println(decodeCount);
							}
						}
						try {
							ChangeBuffer();
						} catch (Exception e) {
							// TODO: handle exception
							changeCount++;
							// System.out.print("changeCount is :");
							// System.out.println(changeCount);
						}

					}
						break;
					case 3000: {
						// 将心跳后面的int型数据读出。

						byte[] intBuffer = new byte[4];// 返回值
						int intLength = bis.read(intBuffer);
						while (intLength != 4) {
							byte[] tempBuffer = new byte[4 - intLength];
							int tempLen = bis.read(tempBuffer);
							for (int i = 0; i < tempLen; i++) {
								intBuffer[intLength + i] = tempBuffer[i];
							}
							intLength = intLength + tempLen;
						}
						// System.out.print("this int is :");
						// System.out.println(util.bytesToInt(intBuffer));
					}
						break;
					default:
						int a = 0;
						a++;
						is = null;
						bis = null;
						is = socket.getInputStream();// 获取流向socket的流
						bis = new BufferedInputStream(is);// 这里开始接收二进制数据流

						break;
					}
					// }

				} catch (Exception e) {
					// TODO: handle exception
					if (e != null) {
						System.out.println(e.getMessage());
					}
				}
			}
			// out.close();
			in.close();
			socket.close();
			// System.out.print("End of Receive");
			// System.out.println(totallen);
			// System.out.print("we get totoal frame is  ");
			// System.out.println(count);
			// System.out.print("inputCount is :");
			// System.out.println(inputCount);
			// System.out.print("falseCount is :");
			// System.out.println(falseCount);
			// System.out.print("changeCount is :");
			// System.out.println(changeCount);
		} catch (IOException e) {
			// System.out.print("we get exception receive len is ");
			// System.out.println(totallen);
			System.out.print("we get totoal frame is  ");
			System.out.println(count);
			if (e != null) {
				System.out.println(e.getMessage());
			}
			// e.printStackTrace();
		}

		UninitDecoder();
	}

	// 录像方法
	static Boolean isRecording = false;
	static FileOutputStream fos = null;
	private Timer timer;
	File file;
	private int timerCount = 0;

	public void record() {
		timerCount = 0;
		timer = new Timer();
		String strFolder = checkRecordPath();

		SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd-HHmmss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String fileNameString = "录像" + formatter.format(curDate) + "W" + width
				+ "H" + height;
		try {
			fos = new FileOutputStream(strFolder + fileNameString);
			file = new File(strFolder + fileNameString);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isRecording = true;

		Thread timeThread = new Thread(waitThread);
		timeThread.start();

		Thread record = new Thread(recordThread);
		record.start();
	}

	public String stopRecord() {
		isRecording = false;
		try {
			// fos.flush();
			fos.close();
			if (timerCount < 3) {
				FileUtil.deleteFile(file);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file.toString();
	}

	// 保存到sdcard

	Runnable recordThread = new Runnable() {
		public void run() {
			byte[] bitsame = null;

			try {
				while (isRecording) {
					if (!recordBuffer.equals(bitsame)) {
						if (null != fos) {
							fos.write(recordBuffer);
							fos.flush();
							bitsame = recordBuffer;
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	// 录像计时
	private void TimerCount(Timer t) {
		timerCount++;
	}

	private void TimerOut() {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				TimerCount(timer);
			}
		};
		timer.schedule(timerTask, 0, 1000);
	}

	Runnable waitThread = new Runnable() {
		public void run() {
			if (isRecording) {
				TimerOut();
			}
		}
	};

	private static String checkRecordPath() {
		String strFolder = Environment.getExternalStorageDirectory().toString()
				+ "/MLVideo/";

		File file = new File(strFolder);

		if (!file.exists()) {
			file.mkdir();
		}

		strFolder = Environment.getExternalStorageDirectory().toString()
				+ "/MLVideo/Record/";

		file = new File(strFolder);

		if (!file.exists()) {
			file.mkdir();
		}
		return strFolder;
	}

	// 心跳方法
	private Timer heartTimer;
	private int heartCount = 0;

	// 心跳计时
	private void heartTimerCount(Timer t) {
		if (isHeart) {
			heartBeat();
			System.out.println("this is a HeartBeat!!!!!!!!! ");
		} else {
			t.cancel();
		}
	}

	private void heartTimerOut() {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				heartTimerCount(heartTimer);
			}
		};
		heartTimer.schedule(timerTask, 0, 5000);
	}

	Runnable heartThread = new Runnable() {
		public void run() {
			heartTimerOut();
		}
	};

}
