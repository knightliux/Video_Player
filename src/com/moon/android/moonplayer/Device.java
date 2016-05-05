package com.moon.android.moonplayer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;

import com.moon.android.moonplayer.util.Logger;


public enum Device {
	
    INSTANCE;
    private static Logger logger = Logger.getLogger();

    private static String TAG = Device.class.getSimpleName();

    private boolean mHasRootBeenChecked = false;
    private boolean mIsDeviceRooted = false;

    private boolean mHasBeenInitialized = false;
    private Context mAppContext = null;

    private boolean mSystembarVisible = true;

    static public void initialize(Context appContext) {
        if (INSTANCE.mHasBeenInitialized == true) {
            logger.e("Initializing already initialized class " + TAG);
        }
        INSTANCE.mHasBeenInitialized = true;
        INSTANCE.mAppContext = appContext;
        AddKillAll(appContext, "killall");
        AddKillAll(appContext, "usleep");
    }

	private static void AddKillAll(Context appContext, String commandFileName) {
		File killAllFile = new File("/system/xbin/"+commandFileName);
		if (!killAllFile.exists()) {
			AssetManager assetManager = appContext.getAssets();
			InputStream inputStream = null;
			String commandFilePath = null;
			try {
				inputStream = assetManager.open(commandFileName);
				commandFilePath = appContext.getApplicationContext().getFilesDir()
						.getAbsolutePath() + File.separator + commandFileName;
				saveToFile(commandFilePath, inputStream);
			} catch (IOException e) {
				logger.e(e.toString());
			}
			try {
				Process p;
				p = Runtime.getRuntime().exec("su");
				DataOutputStream os = new DataOutputStream(p.getOutputStream());
				os.writeBytes("mount -o remount,rw /dev/block/stl6 /system\n");
				os.writeBytes("cd system/xbin\n");
				os.writeBytes("cat " + commandFilePath + " > " + commandFileName + "\n");
				os.writeBytes("chmod 755 " + commandFileName + "\n");
				os.writeBytes("mount -o remount,ro /dev/block/stl6 /system\n");
				os.writeBytes("exit\n");
				os.flush();
				p.waitFor();
			} catch (Exception e) {
				logger.e(e.toString());
			}
		}
	}

    static public Device getInstance() {
        INSTANCE.checkInitialized();
        return INSTANCE;
    }

    private void checkInitialized() {
        if (mHasBeenInitialized == false)
            throw new IllegalStateException("Singleton class " + TAG
                    + " is not yet initialized");
    }

    public boolean isRooted() {

        checkInitialized();


        if (mHasRootBeenChecked) {
            return mIsDeviceRooted;
        }

        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                mHasRootBeenChecked = true;
                mIsDeviceRooted = true;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ArrayList<String> envlist = new ArrayList<String>();
            Map<String, String> env = System.getenv();
            for (String envName : env.keySet()) {
                envlist.add(envName + "=" + env.get(envName));
            }
            String[] envp = (String[]) envlist.toArray(new String[0]);
            Process proc = Runtime.getRuntime()
                    .exec(new String[] { "which", "su" }, envp);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    proc.getInputStream()));
            if (in.readLine() != null) {
                mHasRootBeenChecked = true;
                mIsDeviceRooted = true;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mHasRootBeenChecked = true;
        mIsDeviceRooted = false;
        return false;

    }

    public enum AndroidVersion {
        HC, ICS, JB, UNKNOWN
    };

    public AndroidVersion getAndroidVersion() {
        checkInitialized();
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (11 <= sdk && sdk <= 13) {
            return AndroidVersion.HC;
        } else if (14 <= sdk && sdk <= 15) {
            return AndroidVersion.ICS;
        } else if (16 == sdk) {
            return AndroidVersion.JB;
        } else {
            return AndroidVersion.UNKNOWN;
        }
    }

    public void showSystembar(boolean makeVisible) {
        checkInitialized();
        try {
            ArrayList<String> envlist = new ArrayList<String>();
            Map<String, String> env = System.getenv();
            for (String envName : env.keySet()) {
                envlist.add(envName + "=" + env.get(envName));
            }
            String[] envp = (String[]) envlist.toArray(new String[0]);
            if (makeVisible) {
                String command;
                Device dev = Device.getInstance();
                if (dev.getAndroidVersion() == AndroidVersion.HC) {
                    command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib am startservice -n com.android.systemui/.SystemUIService";
                } else {
                    command = "rm /sdcard/hidebar-lock\n"
                            + "sleep 5\n"
                            + "LD_LIBRARY_PATH=/vendor/lib:/system/lib am startservice -n com.android.systemui/.SystemUIService";
                }
                Runtime.getRuntime().exec(new String[] { "su", "-c", command }, envp);
                mSystembarVisible = true;
            } else {
                String command;
                Device dev = Device.getInstance();
                if (dev.getAndroidVersion() == AndroidVersion.HC) {
                    command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib service call activity 79 s16 com.android.systemui";
                } else {
                    command = "touch /sdcard/hidebar-lock\n"
                            + "while [ -f /sdcard/hidebar-lock ]\n"
                            + "do\n"
                            + "killall com.android.systemui\n"
                            + "usleep 500000\n"
                            + "done\n"
                            + "LD_LIBRARY_PATH=/vendor/lib:/system/lib am startservice -n com.android.systemui/.SystemUIService";
                }
                Runtime.getRuntime().exec(new String[] { "su", "-c", command }, envp);
                mSystembarVisible = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isSystembarVisible() {
        checkInitialized();
        return mSystembarVisible;
    }

    public void sendBackEvent() {
        try {
            ArrayList<String> envlist = new ArrayList<String>();
            Map<String, String> env = System.getenv();
            for (String envName : env.keySet()) {
                envlist.add(envName + "=" + env.get(envName));
            }
            String[] envp = (String[]) envlist.toArray(new String[0]);
            Runtime.getRuntime().exec(
                    new String[] { "su", "-c",
                            "LD_LIBRARY_PATH=/vendor/lib:/system/lib input keyevent 4" },
                    envp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void saveToFile(String filePath, InputStream in){
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		int BUFFER_SIZE = 1024;
		byte[] buf = new byte[BUFFER_SIZE];
		int size = 0;
		bis = new BufferedInputStream(in);
		try {
			fos = new FileOutputStream(filePath);
			while ((size = bis.read(buf)) != -1)
				fos.write(buf, 0, size);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
				if (bis != null) {
					bis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
