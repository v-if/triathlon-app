package com.tkpark86.runch.common;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class HttpClient {
	
	private static final String LOG_TAG = "barcelona";
	
	private String url;
	private HttpURLConnection con;
	private OutputStream os;

	private String delimiter = "--";
	private String boundary = "SwA" + Long.toString(System.currentTimeMillis()) + "SwA";

	public HttpClient(String url) {
		Log.d(LOG_TAG, "HttpClient.HttpClient: url="+url);
		this.url = url;
	}

	public byte[] downloadImage(String imgName) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			System.out.println("URL [" + url + "] - Name [" + imgName + "]");

			HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();
			con.setRequestMethod("POST");
			con.setDoInput(true);
			con.setDoOutput(true);
			con.connect();
			con.getOutputStream().write(("name=" + imgName).getBytes());

			InputStream is = con.getInputStream();
			byte[] b = new byte[1024];

			int length = -1;
			while((length = is.read(b)) != -1) {
				baos.write(b, 0, length);
			}
			
			con.disconnect();
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return baos.toByteArray();
	}

	public void connectForMultipart() throws Exception {
		con = (HttpURLConnection) (new URL(url)).openConnection();
		con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		con.connect();
		os = con.getOutputStream();
	}

	public void addFormPart(String paramName, String value) throws Exception {
		writeParamData(paramName, value);
	}

	public void addFilePart(String paramName, String fileName, byte[] data) throws Exception {
		os.write((delimiter + boundary + "\r\n").getBytes());
		os.write(("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
		os.write(("Content-Type: application/octet-stream\r\n").getBytes());
		os.write(("Content-Transfer-Encoding: binary\r\n").getBytes());
		os.write("\r\n".getBytes());

		os.write(data);

		os.write("\r\n".getBytes());
	}

	public void finishMultipart() throws Exception {
		os.write((delimiter + boundary + delimiter + "\r\n").getBytes());
	}

	public String getResponse() throws Exception {
		InputStream is = con.getInputStream();
		byte[] b1 = new byte[1024];
		StringBuffer buffer = new StringBuffer();

		int length = -1;
		while((length = is.read(b1)) != -1) {
			buffer.append(new String(b1, 0, length));
		}

		con.disconnect();

		return buffer.toString();
	}

	private void writeParamData(String paramName, String value) throws Exception {
		os.write((delimiter + boundary + "\r\n").getBytes());
		os.write("Content-Type: text/plain\r\n".getBytes());
		os.write(("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n").getBytes());
		os.write(("\r\n" + value + "\r\n").getBytes());
	}
}
