package net.evmodder.EvLib.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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

	public record ReadResponse(int statusCode, String body){}
	private static final String readResponse(final InputStream stream) throws IOException{
		if(stream == null) return null;
		try(final BufferedReader reader = new BufferedReader(new InputStreamReader(stream))){
			final StringBuilder response = new StringBuilder();
			String line;
			while((line=reader.readLine()) != null) response.append(line.replace(" ", ""));
			return response.isEmpty() ? null : response.toString();
		}
	}
	public static final ReadResponse getReadResponse(final String url, final int timeoutMillis) throws IOException{
		final URLConnection connection = URI.create(url).toURL().openConnection();
		connection.setConnectTimeout(timeoutMillis);
		connection.setReadTimeout(timeoutMillis);
		connection.setUseCaches(false);
		connection.setDoInput(true);
		if(!(connection instanceof HttpURLConnection)) return new ReadResponse(200, readResponse(connection.getInputStream()));

		final HttpURLConnection http = (HttpURLConnection)connection;
		http.setRequestMethod("GET");
		try{
			final int statusCode = http.getResponseCode();
			final InputStream stream = statusCode >= 400 ? http.getErrorStream() : http.getInputStream();
			return new ReadResponse(statusCode, readResponse(stream));
		}
		finally{http.disconnect();}
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