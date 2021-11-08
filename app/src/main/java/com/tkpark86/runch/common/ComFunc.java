package com.tkpark86.runch.common;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

public class ComFunc {
	
	private static final String LOG_TAG = "barcelona";
	private static final String SERVICE_URL = "http://vveb5u.cafe24.com/";
	
	private static final String PREF_LOGIN_INFO = "loginInfo";
	
	
	// Login
	public static void logIn(Context context, String memberId, String email, String password, String nickname, String profileImgUrl) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("memberId", memberId);
		editor.putString("email", email);
		editor.putString("password", password);
		editor.putString("nickname", nickname);
		editor.putString("profileImgUrl", profileImgUrl);
		editor.commit();
	}
	// Logout
	public static void logOut(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		SharedPreferences.Editor editor = pref.edit();
		// Init member
		editor.remove("memberId");
		editor.remove("password");
		editor.remove("nickname");
		editor.remove("profileImgUrl");
		
		// Init filter
		editor.remove("raceTp");
		editor.remove("raceDt");
		editor.remove("searchTp");
		editor.remove("overlay");
		editor.remove("regiYn");
		editor.commit();
	}
	// MEMBER_ID
	public static String getMemberId(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		return pref.getString("memberId", "");
	}
	// EMAIL
	public static String getMemberEmail(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		return pref.getString("email", "");
	}
	// PASSWORD
	public static void setMemberPw(Context context, String password) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("password", password);
		editor.commit();
	}
	public static String getMemberPw(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		return pref.getString("password", "");
	}
	// Nickname
	public static void setMemberNickname(Context context, String nickname) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("nickname", nickname);
		editor.commit();
	}
	public static String getMemberNickname(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		return pref.getString("nickname", "");
	}
	// Profile Img Url
	public static void setMemberProfileImgUrl(Context context, String profileImgUrl) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("profileImgUrl", profileImgUrl);
		editor.commit();
	}
	public static String getMemberProfileImgUrl(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		return pref.getString("profileImgUrl", "");
	}
	
	// Race Tp
	public static void setRaceTp(Context context, String raceTp) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("raceTp", raceTp);
		editor.commit();
	}
	public static String getRaceTp(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		return pref.getString("raceTp", "1");
	}
	
	// Search Tp
	public static void setSearchTp(Context context, String searchTp) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("searchTp", searchTp);
		editor.commit();
	}
	public static String getSearchTp(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		return pref.getString("searchTp", "0");
	}
	
	// Race Dt
	public static void setRaceDt(Context context, String raceDt) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("raceDt", raceDt);
		editor.commit();
	}
	public static String getRaceDt(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM", Locale.getDefault());
		return pref.getString("raceDt", sdf.format(today));
	}
	
	// Regi Yn
	public static void setRegiYn(Context context, String regiYn) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("regiYn", regiYn);
		editor.commit();
	}
	public static String getRegiYn(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		return pref.getString("regiYn", "N");
	}
	
	// City Overlay
	public static void setCityOverlay(Context context, int overlay) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt("overlay", overlay);
		editor.commit();
	}
	public static int getCityOverlay(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_LOGIN_INFO, 0);
		return pref.getInt("overlay", 0);
	}
	
	
	
	// SERVICE_URL + service.do
	public static String getServiceURL(String service) {
		StringBuilder sb = new StringBuilder();
		sb.append(SERVICE_URL)
		  .append(service)
		  .append(".do");
		
		return sb.toString();
	}
	
	
	// Encoding password as sha1 with base64
	public static String encodePassword(String password) {
		MessageDigest sha1 = null;
		try {
			sha1 = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			Log.d(LOG_TAG, "ComFunc.encodePassword: error=" + e.getMessage());
		}
		byte[] src = password.getBytes(Charset.forName("UTF-8"));
		byte[] digest = sha1.digest(src);
		byte[] encodedByte = Base64.encode(digest, Base64.NO_WRAP);
		
		return new String(encodedByte, Charset.forName("UTF-8"));
	}
	
	
	// ex)xxx@xxx.com
	public static boolean isEmailValidator(String text) {
		String pattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
						+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		Pattern p = Pattern.compile(pattern);
		Matcher matcher = p.matcher(text);
		return matcher.matches();
	}
	
	
	public static boolean isNicknameValidator(String text) {
		String pattern = "^[a-zA-Z0-9가-힣]{2,12}$";
		Pattern p = Pattern.compile(pattern);
		Matcher matcher = p.matcher(text);
		return matcher.matches();
	}
	
	
	public static boolean isPasswordValidator(String text) {
		// ^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$
		String pattern = "^(?=\\S+$).{8,}$";
		Pattern p = Pattern.compile(pattern);
		Matcher matcher = p.matcher(text);
		return matcher.matches();
	}
	
	
	public static String readableFileSize(long size) {
		if(size <= 0)
			return "0";
		final String[] units =new String[]{"B", "KB", "MB", "GM", "TB"};
		int digitGroups = (int)(Math.log10(size)/Math.log10(1024));
		
		String pattern = "";
		if(digitGroups == 0) {
			pattern = "#,##0";
		} else if(digitGroups == 1) {
			pattern = "#,##0";
		} else if(digitGroups == 2) {
			pattern = "#,##0.##";
		} else if(digitGroups == 3) {
			pattern = "#,##0.##";
		} else if(digitGroups == 4) {
			pattern = "#,##0.##";
		}
		
		return new DecimalFormat(pattern).format(size/Math.pow(1024, digitGroups)) + units[digitGroups];
	}
	

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int width = options.outWidth;
		final int height = options.outHeight;
		int inSampleSize = 1;
		
		if (height > reqHeight || width > reqWidth) {
			final int halfWidth = width / 2;
			final int halfHeight = height / 2;
			
			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfWidth / inSampleSize) > reqWidth && (halfHeight / inSampleSize) > reqHeight) {
//				Log.d(LOG_TAG, "ComFunc.calculateInSampleSize: halfWidth / inSampleSize="+halfWidth / inSampleSize+", reqWidth="+reqWidth);
//				Log.d(LOG_TAG, "ComFunc.calculateInSampleSize: halfHeight / inSampleSize="+halfHeight / inSampleSize+", reqHeight="+reqHeight);
//				Log.d(LOG_TAG, "ComFunc.calculateInSampleSize: inSampleSize="+inSampleSize);
				inSampleSize *= 2;
			}
		}
		
		return inSampleSize;
	}
	
}