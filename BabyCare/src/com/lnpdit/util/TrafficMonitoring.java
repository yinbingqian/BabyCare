package com.lnpdit.util;

import java.math.BigDecimal;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;

public class TrafficMonitoring {
	Context context;
	ConnectivityManager cm ;
	NetworkInfo nwi;
	long lastTraffic = 0;
	long currentTraffic;

	// 构造函数
	public TrafficMonitoring() {
	}

	public TrafficMonitoring(Context context) {
		this.context = context;
		cm =(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);	
		nwi = cm.getActiveNetworkInfo();
	}

	// 获取当前手机的联网类型，返回String
	public int getNetType() {
		if(nwi != null){
			String net = nwi.getTypeName();
			if(net.equals("WIFI")){
				return 0;
			}else {
				return 1;
			}
		}else {
			return -1;
		}
	}	

	// 查询手机总流量
	public static long traffic_Monitoring() {
		long recive_Total = TrafficStats.getTotalRxBytes();
		long send_Total = TrafficStats.getTotalTxBytes();
		long total = recive_Total + send_Total;
		return total;
	}

	//查询手机的Mobile上行流量
	public static long mReceive(){
		return  TrafficStats.getMobileRxBytes();
	}
	
	//查询手机的Mobile下行流量
	public static long mSend(){
		return  TrafficStats.getMobileTxBytes();
	}
	
	//查询手机的WIFI下行流量
	public static long wSend(){
		return  TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
	}
	
	//查询手机Wifi的上行流量
	public static long wReceive(){
		return TrafficStats.getTotalTxBytes() - TrafficStats.getMobileRxBytes();
	}

	// 查询某个Uid的下行值
	public static long monitoringEachApplicationReceive(int uid) {
		return TrafficStats.getUidRxBytes(uid);
	}

	// 查询某个Uid的上行值
	public static long monitoringEachApplicationSend(int uid) {
		return TrafficStats.getUidTxBytes(uid);
	}

	// 流量转化
	public static String convertTraffic(long traffic) {
		BigDecimal trafficKB;
		BigDecimal trafficMB;
		BigDecimal trafficGB;

		BigDecimal temp = new BigDecimal(traffic);
		BigDecimal divide = new BigDecimal(1000);
		trafficKB = temp.divide(divide, 2, 1);
		if (trafficKB.compareTo(divide) > 0) {
			trafficMB = trafficKB.divide(divide, 2, 1);
			if (trafficMB.compareTo(divide) > 0) {
				trafficGB = trafficMB.divide(divide, 2, 1);
				return trafficGB.doubleValue()+"GB";
			} else {
				return trafficMB.doubleValue()+"MB";
			}
		} else {
			return trafficKB.doubleValue()+"KB";
		}
	}
	

	// 流量转化
	public static long convertLongTraffic(long traffic) {
		BigDecimal trafficKB;
		BigDecimal trafficMB;
		BigDecimal trafficGB;

		BigDecimal temp = new BigDecimal(traffic);
		BigDecimal divide = new BigDecimal(1000);
		trafficKB = temp.divide(divide, 2, 1);
		if (trafficKB.compareTo(divide) > 0) {
			trafficMB = trafficKB.divide(divide, 2, 1);
			if (trafficMB.compareTo(divide) > 0) {
				trafficGB = trafficMB.divide(divide, 2, 1);
				return (long) trafficGB.doubleValue();
			} else {
				return (long) trafficMB.doubleValue();
			}
		} else {
			return (long) trafficKB.doubleValue();
		}
	}
}
