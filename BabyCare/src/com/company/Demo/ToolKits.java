package com.company.Demo;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;
import com.company.NetSDK.INetSDK;

public class ToolKits {
    public static void showMessage(Context context , String strLog)
    {
    	Toast.makeText(context, strLog, Toast.LENGTH_SHORT).show();
    }

    public static void showErrorMessage(Context context , String strLog)
    {
    	Toast.makeText(context, strLog + 
    		String.format(" Last Error Code [%x]" , INetSDK.GetLastError()),
    		Toast.LENGTH_SHORT).show();
    }

    public static void writeLog(String strLog)
    {
    	Log.d("NetSDK Log", strLog);
    }
    
    public static void writeErrorLog(String strLog)
    {
    	Log.d("NetSDK Log", strLog +
    		String.format(" Last Error Code [%x]" , INetSDK.GetLastError()) );
    }
    
    public static boolean SetDevConfig(String strCmd ,  Object cmdObject , long hHandle , int nChn , int nBufferLen )
    {
        boolean result = false;
    	Integer error = Integer.valueOf(0);
    	Integer restart = Integer.valueOf(0);
        char szBuffer[] = new char[nBufferLen];
        for(int i=0; i<nBufferLen; i++)szBuffer[i]=0;
        
        if(INetSDK.PacketData(strCmd, cmdObject, szBuffer, nBufferLen))
        {
        	if( INetSDK.SetNewDevConfig(hHandle,strCmd , nChn , szBuffer, nBufferLen, error, restart, 1000))
        	{
        		result = true;
        	}
        	else
        	{
        		writeErrorLog("Set " + strCmd + " Config Failed!");
             	result = false;
        	}
        }
        else
        {
        	writeErrorLog("Packet " + strCmd + " Config Failed!");
         	result = false;
        }
        
        return result;
    }
    
    public static boolean GetDevConfig(String strCmd ,  Object cmdObject , long hHandle , int nChn , int nBufferLen)
    {
        boolean result = false;
    	Integer error = Integer.valueOf(0);
        char szBuffer[] = new char[nBufferLen];
        for(int i=0; i<nBufferLen; i++)szBuffer[i]=0;
         
        if(INetSDK.GetNewDevConfig( hHandle, strCmd , nChn, szBuffer,nBufferLen,error,5000) )
        {  
         	if( INetSDK.ParseData(strCmd ,szBuffer , cmdObject , null ) )
         	{
         		result = true;
         	}
         	else
         	{
         		writeErrorLog("Parse " + strCmd + " Config Failed!");
         		result = false;
         	}
         }
         else
         {
        	 writeErrorLog("Get" + strCmd + " Config Failed!");
        	 result = false;
         }
        return result;
    }
}
