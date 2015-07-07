package com.lnpdit.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class NetUtil {

	public static Socket socket; // static一旦赋值，其他实例也是一样
	public static Timer timer;
	public ComUtil util;
	public DataOutputStream sendout;
	
	public NetUtil() {
		util = new ComUtil();
	}

	public ComUtil getComUtil() {
		return this.util;
	}

	public boolean connectServer(String ip, String port) {
		Socket so = null;
		try {
			so = new Socket(ip, Integer.parseInt(port));

			if (null != so) {
				timer = new Timer();
				TimerTask timerTask = new TimerTask() {
					@Override
					public void run() {
						replayServer();
					}
				};
				timer.schedule(timerTask, 0, 10000);
			}
		} catch (NumberFormatException e) {
			return false;
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			System.out.println("IO:" + e.getMessage());
			return false;
		}
		socket = so;
		return true;
	}

	public boolean commandGetUser(String userName, String passwd) {

		byte bComman[] = util.intToByte(0xbe558877);// command 0xbe558877 get// online users
		byte bLength[] = util.intToByte(40); // length
		byte[] sendBuffer = new byte[48];
		int i = 0;
		for (i = 0; i < bComman.length; i++) {
			sendBuffer[i] = bComman[i];
		}
		for (i = 0; i < bLength.length; i++) {
			sendBuffer[i + 4] = bLength[i];
		}
		for (i = 0; i < 20; i++) {
			if (i < userName.length())
			{
				sendBuffer[i + 8] = (byte) userName.charAt(i);
			}
			else
				sendBuffer[i + 8] = 0x00;
		}
		for (i = 0; i < 20; i++) {
			if (i < passwd.length())
			{
				sendBuffer[i + 28] = (byte) passwd.charAt(i);
			}
				
			else
				sendBuffer[i + 28] = 0x00;
		}
		try {
			DataOutputStream out = new DataOutputStream(socket
					.getOutputStream());
			out.write(sendBuffer);
			out.flush();
			return true;
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

	public boolean isSafety() {
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());
			byte headBuffer[] = new byte[8];
			while (in.available() > 0) {
				in.read(headBuffer);
				if (5 == util.findHeader(headBuffer)) {
					return true;
				}
			}
		} catch (IOException e1) {
			System.out.println(e1.getMessage());
		}
		return false;
	}

	public String receiveUserList() {
		String userList = "";
		String tempName = "";
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());
			byte nameBuffer[] = new byte[20];
			byte headBuffer[] = new byte[4];
			while (in.available() > 0) {
				in.read(headBuffer);
				if (0 == util.findHeader(headBuffer)) {
					in.read(nameBuffer, 0, 20);
					// tempName = new String(nameBuffer, "GB2312");
					tempName = util.utf8ToUnicode(nameBuffer);
					userList += tempName.trim() + ",";
				}
			}
		} catch (IOException e1) {
			System.out.println(e1.getMessage());
		}

		return userList;
	}

	public void replayServer() {

		byte bComman[] = util.intToByte(0xbf558877);// command
		byte bLength[] = util.intToByte(4); // length
		byte command[] = util.intToByte(0);
		byte[] sendBuffer = new byte[12];
		int i = 0;
		for (i = 0; i < bComman.length; i++) {
			sendBuffer[i] = bComman[i];
		}
		for (i = 0; i < bLength.length; i++) {
			sendBuffer[i + 4] = bLength[i];
		}
		for (i = 0; i < 4; i++) {
			sendBuffer[i + 8] = command[i];
		}
		try {
			DataOutputStream out = new DataOutputStream(socket
					.getOutputStream());
			out.write(sendBuffer);
			out.flush();
		} catch (IOException e) {
			System.out.println("replayServer:"+ e.getMessage());
		}
	}

	public void userRegister(String userName) {
		byte busername[] = null;
		try {
			busername = userName.getBytes("GB2312");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		byte bComman[] = util.intToByte(0xbd558877);// command
		byte bLength[] = util.intToByte(busername.length); // length
		byte[] sendBuffer = new byte[28];
		int i = 0;
		for (i = 0; i < bComman.length; i++) {
			sendBuffer[i] = bComman[i];
		}
		for (i = 0; i < bLength.length; i++) {
			sendBuffer[i + 4] = bLength[i];
		}
		for (i = 0; i < busername.length; i++) {
			sendBuffer[i + 8] = busername[i];
		}
		try {
			DataOutputStream out = new DataOutputStream(socket
					.getOutputStream());
			int count = 8 + busername.length;
			out.write(sendBuffer, 0, count);
			out.flush();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void channelRegister(int channelNum, int com) {
		byte bComman[] = util.intToByte(0xaa558877);// command
		byte bLength[] = util.intToByte(8); // length
		byte command[] = util.intToByte(com); // 0:close view 1:open view
		byte channel[] = util.intToByte(channelNum);
		byte[] sendBuffer = new byte[16];
		int i = 0;
		for (i = 0; i < bComman.length; i++) {
			sendBuffer[i] = bComman[i];
		}
		for (i = 0; i < bLength.length; i++) {
			sendBuffer[i + 4] = bLength[i];
		}
		for (i = 0; i < 4; i++) {
			sendBuffer[i + 8] = command[i];
		}
		for (i = 0; i < 4; i++) {
			sendBuffer[i + 12] = channel[i];
		}
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.write(sendBuffer);
			out.flush();

			
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void CloseChannel()
	{
		try {
			if(sendout!=null)
				sendout.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}
	public void holderCtrol(int direction,int channelNum) {
		byte bComman[] = util.intToByte(0x99558877);// command
		byte bLength[] = util.intToByte(8); // length
		byte command[] = util.intToByte(direction); 
		byte channel[] = util.intToByte(channelNum);
		byte[] sendBuffer = new byte[16];
		int i = 0;
		for (i = 0; i < bComman.length; i++) {
			sendBuffer[i] = bComman[i];
		}
		for (i = 0; i < bLength.length; i++) {
			sendBuffer[i + 4] = bLength[i];
		}
		for (i = 0; i < 4; i++) {
			sendBuffer[i + 8] = command[i];
		}
		for (i = 0; i < 4; i++) {
			sendBuffer[i + 12] = channel[i];
		}
		try {
			DataOutputStream out = new DataOutputStream(socket
					.getOutputStream());
			out.write(sendBuffer);
			out.flush();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

}
