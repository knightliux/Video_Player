package com.moon.android.moonplayer;

import java.io.DataOutputStream;
import java.io.OutputStream;

import com.moon.android.moonplayer.util.Logger;

import android.content.Context;

public class ScreenZoom {

	public static final int NORMAL = 0;
	public static final int FULL = 1;
	public static final int _4_3 = 2;
	public static final int _16_9 = 3;
	public static final String SCREEN_MODE_PATH = "/sys/class/video/screen_mode";
	public static final Logger log = Logger.getLogger();
	public static void toFull(Context context) {
		changeScreenMode(FULL, context);
	}

	public static void toNormal(Context context) {
		changeScreenMode(NORMAL, context);
	}
	
	public static void to4_3(Context context) {
		changeScreenMode(_4_3, context);
	}
	
	public static void to16_9(Context context) {
		changeScreenMode(_16_9, context);
	}

	private static void changeScreenMode(int mode, Context context) {
		try{
			String command = "echo " + mode + " > /sys/class/video/screen_mode";
			excuteSuCMD(command);
		}catch(Exception e){
			log.e(e.toString());
		}
	}

	protected static int excuteSuCMD(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec("mnote");
            DataOutputStream dos = new DataOutputStream(
                    (OutputStream) process.getOutputStream());
            dos.writeBytes((String) "export LD_LIBRARY_PATH=/vendor/lib:/system/lib\n");
            cmd = String.valueOf(cmd);
            dos.writeBytes((String) (cmd + "\n"));
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            process.waitFor();
            int result = process.exitValue();
            return (Integer) result;
        } catch (Exception localException) {
//            localException.printStackTrace();
        	log.e(localException.toString());
            return -1;
        }
    }
}
