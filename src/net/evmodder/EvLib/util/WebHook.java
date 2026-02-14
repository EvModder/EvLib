package net.evmodder.EvLib.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;

public class WebHook{
	public static final String getReadURL(final String post){
		try{
			final URLConnection connection = URI.create(post).toURL().openConnection();
			//conn.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);

			final BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			final StringBuilder resp = new StringBuilder();
			String line = null;
			while ((line=rd.readLine()) != null) resp.append(line.replace(" ", ""));
			rd.close();
			return resp.isEmpty() ? null : resp.toString();
		}
		catch(IOException e){e.printStackTrace(); return null;}
	}

	/*public static final String putReadURL(final String payload, final String url){
		try{
			final HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("PUT");
			final OutputStream out = conn.getOutputStream();
			out.write(payload.getBytes("UTF-8")); out.close();

			final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			final StringBuilder resp = new StringBuilder(in.readLine());
			String line = null;
			while ((line=in.readLine()) != null) resp.append('\n').append(line);
			in.close();
			return resp.toString();
		}
		catch(IOException e){e.printStackTrace(); return null;}
	}*/
	
	public static final String postReadURL(final String payload, final String url){
		try{
			final HttpURLConnection conn = (HttpURLConnection)URI.create(url).toURL().openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			final OutputStream out = conn.getOutputStream();
			out.write(payload.getBytes("UTF-8")); out.close();

			final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			final StringBuilder resp = new StringBuilder();
			String line = null;
			while ((line=in.readLine()) != null) resp.append(line.replace(" ", ""));
			in.close();
			return resp.isEmpty() ? null : resp.toString();
		}
		catch(IOException e){e.printStackTrace(); return null;}
	}
}