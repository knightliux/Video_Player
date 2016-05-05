package com.moon.android.moonplayer;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moon.android.moonplayer.util.Logger;
import com.moon.android.moonplayer.util.MACUtils;
import com.moon.android.moonplayer.util.Tools;

public class RequestDAO {
	
	public Logger logger = Logger.getLogger();
	private static RequestDAO reuqestDAO;
	private RequestDAO(){}
	
	public static RequestDAO getInstance(){
		new RequestDAOInstance();
		return reuqestDAO;
	}
	
	private static class RequestDAOInstance{
		public RequestDAOInstance(){
			reuqestDAO = new RequestDAO();
		}
	}
	
	public static UpdateData checkUpate(Context context) {
		try {
			String str1 = Configs.UPDATE_URL+"version=" + Tools.getVerName(context) + "&mac="+MACUtils.getMac();
			String str2 = RequestUtil.getInstance().request(str1);
			if (isBlank(str2))
				return null;
			UpdateData localUpdateData = (UpdateData) new Gson().fromJson(str2,
					new TypeToken<UpdateData>() {
					}.getType());
			if ((localUpdateData != null) && (!localUpdateData.equals(""))) {
				if ((localUpdateData.getCode() != null)
						&& (!localUpdateData.getCode().equals(""))
						&& (localUpdateData.getCode().equals("0"))) {
					return localUpdateData;
				}
				return null;
			}
		} catch (Exception localException) {
			return null;
		}
		return null;
	}
	
	private static boolean isBlank(String paramString) {
		return (paramString == null) || (paramString.trim().equals(""));
	}
}
