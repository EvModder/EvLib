package net.evmodder.EvLib.extras;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.EntityType;
import net.evmodder.EvLib.FileIO;

public class WebUtils {
	private final static String authserver = "https://authserver.mojang.com";

	public static String getReadURL(String post){
		try{
			URLConnection connection = new URL(post).openConnection();
			//conn.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);

			BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder resp = new StringBuilder();
			String line = null;
			while ((line=rd.readLine()) != null) resp.append('\n').append(line);
			rd.close();
			return resp.length() > 0 ? resp.substring(1) : null;
		}
		catch(IOException e){
			System.out.println(e.getStackTrace());
			return null;
		}
	}

	public static String putReadURL(String payload, String url){
		try{
			HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("PUT");
			OutputStream out = conn.getOutputStream();
			out.write(payload.getBytes("UTF-8")); out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder resp = new StringBuilder(in.readLine());
			String line = null;
			while ((line=in.readLine()) != null) resp.append('\n').append(line);
			in.close();
	
			return resp.toString();
		}
		catch(IOException e){e.printStackTrace(); return null;}
	}

	public static String postReadURL(String payload, String url){
		try{
			HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoInput(true);
			conn.setDoOutput(true);
	
			OutputStream out = conn.getOutputStream();
			out.write(payload.getBytes("UTF-8")); out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder resp = new StringBuilder(in.readLine());
			String line = null;
			while ((line=in.readLine()) != null) resp.append('\n').append(line);
			in.close();

			return resp.toString();
		}
		catch(IOException e){e.printStackTrace(); return null;}
	}

	private static String getStringBetween(String base, String begin, String end) {
		int resbeg = 0, resend = base.length()-1;

		Pattern patbeg = Pattern.compile(Pattern.quote(begin));
		Matcher matbeg = patbeg.matcher(base);
		if(matbeg.find()) resbeg = matbeg.end();

		Pattern patend = Pattern.compile(Pattern.quote(end));
		Matcher matend = patend.matcher(base.substring(resbeg));
		if(matend.find()) resend = matend.start();

		return base.substring(resbeg, resbeg+resend);
	}

	static String authenticateMojang(String username, String password){
		String genClientToken = UUID.randomUUID().toString();

		// Setting up json POST request
		String payload = "{\"agent\": {\"name\": \"Minecraft\",\"version\": 1},\"username\": \"" + username
				+ "\",\"password\": \"" + password + "\",\"clientToken\": \"" + genClientToken + "\"}";

		String output = postReadURL(payload, authserver + "/authenticate");

		// Setting up patterns
		String authBeg = "\"accessToken\":\"";
		String authEnd = "\",\"";

		return getStringBetween(output, authBeg, authEnd);
	}

	//Names are [3,16] characters from [abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_]
	static HashMap<String, Boolean> exists = new HashMap<String, Boolean>();
	public static boolean checkExists(String player){
		Boolean b = exists.get(player);
		if(b == null){
			//Sample data: {"id":"34471e8dd0c547b9b8e1b5b9472affa4","name":"EvDoc"}
			String data = getReadURL("https://api.mojang.com/users/profiles/minecraft/"+player);
			exists.put(player, b = (data != null));
		}
		return b;
	}

	static BufferedImage upsideDownHead(BufferedImage img){
		int w = img.getWidth(), h = img.getHeight();
		AffineTransform at = new AffineTransform();
		at.concatenate(AffineTransform.getScaleInstance(1, -1));
		at.concatenate(AffineTransform.getTranslateInstance(0, -h));
		BufferedImage newImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImg.createGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
		g.drawImage(img, 0, 0, null);
		g.drawImage(img.getSubimage(w/8, 0, w/8, w/8), w/4, 0, null); // 8, 0, 8, 8
		g.drawImage(img.getSubimage(w/4, 0, w/8, w/8), w/8, 0, null); //16, 0, 8, 8
		g.drawImage(img.getSubimage((5*w)/8, 0, w/8, w/8), (3*w)/4, 0, null);//40, 0, 8, 8
		g.drawImage(img.getSubimage((3*w)/4, 0, w/8, w/8), (5*w)/8, 0, null);//48, 0, 8, 8
		g.transform(at);
		g.drawImage(img.getSubimage(0, w/8, w, w/8), 0, h-w/4, null);//0,8,64,8 | 0, height-16
		g.dispose();
		return newImg;
	}

	public static BufferedImage rotate(BufferedImage img, double angle){
//		double sin = Math.abs(Math.sin(Math.toRadians(angle))), cos = Math.abs(Math.cos(Math.toRadians(angle)));
		int w = img.getWidth(null), h = img.getHeight(null);
//		int neww = (int) Math.floor(w*cos + h*sin), newh = (int) Math.floor(h*cos + w*sin);
		BufferedImage newImg = new BufferedImage(w, h, img.getType());
		Graphics2D g = newImg.createGraphics();
//		g.translate((neww-w)/2, (newh-h)/2);
		g.rotate(Math.toRadians(angle), w/2, h/2);
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return newImg;
	}
	public static BufferedImage flip(BufferedImage img){
		BufferedImage newImg = new BufferedImage(img.getWidth(null), img.getHeight(null), img.getType());
		Graphics2D g = newImg.createGraphics();
		AffineTransform at = new AffineTransform();
		at.concatenate(AffineTransform.getScaleInstance(1, -1));
		at.concatenate(AffineTransform.getTranslateInstance(0, -img.getHeight(null)));
		g.transform(at);
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return newImg;
	}
	static BufferedImage sidewaysHead(BufferedImage img){
		BufferedImage newImg = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImg.createGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
		g.drawImage(img, 0, 0, null);
		g.drawImage(flip(img.getSubimage(8, 8, 8, 8)), 16, 0, null);//face
		g.drawImage(rotate(img.getSubimage(24, 8, 8, 8), 180), 8, 0, null);//back
		g.drawImage(img.getSubimage(8, 0, 8, 8), 8, 8, null);//top
		g.drawImage(rotate(flip(img.getSubimage(16, 0, 8, 8)), 180), 24, 8, null);//bottom
		g.drawImage(flip(img.getSubimage(40, 8, 8, 8)), 48, 0, null);//face overlay
		g.drawImage(rotate(img.getSubimage(56, 8, 8, 8), 180), 40, 0, null);//back overlay
		g.drawImage(img.getSubimage(40, 0, 8, 8), 40, 8, null);//top overlay
		g.drawImage(rotate(flip(img.getSubimage(48, 0, 8, 8)), 180), 56, 8, null);//bottom overlay
		g.drawImage(rotate(img.getSubimage(0, 8, 8, 8), 90), 0, 8, null);//right cheek
		g.drawImage(rotate(img.getSubimage(16, 8, 8, 8), -90), 16, 8, null);//left cheek
		g.drawImage(rotate(img.getSubimage(32, 8, 8, 8), 90), 32, 8, null);//right cheek overlay
		g.drawImage(rotate(img.getSubimage(48, 8, 8, 8), -90), 48, 8, null);//left cheek overlay
		g.dispose();
		return newImg;
	}

	static TreeMap<String, String> makeUpsideDownCopies(String[] heads, String outfolder, String uuid, String token){
		TreeMap<String, String> newHeads = new TreeMap<String, String>();
		for(String line : heads){
			int idx = line.indexOf(':');
			if(idx > -1 && idx < line.length()-1){
				String name = line.substring(0, idx).trim();
				String val = line.substring(idx+1).trim();
				if(name.endsWith("GRUMM") || val.isEmpty()) continue;
				String json = new String(Base64.getDecoder().decode(val));
				String url = json.substring(json.indexOf("\"url\":")+7, json.lastIndexOf('"')).trim();
				//String textureId = url.substring(url.lastIndexOf('/')+1);
				System.out.println("1. Got texture url from Base64 val");
				String filename = outfolder+"/"+name+"|GRUMM.png";
				File outfile = new File(filename);
				//===========================================================================================
				try{Thread.sleep(2000);}catch(InterruptedException e1){e1.printStackTrace();}//2s
				HttpURLConnection conn;
				try{
					conn = (HttpURLConnection)new URL(url).openConnection();
					conn.setUseCaches(false);
					conn.setDoOutput(true);
					conn.setDoInput(true);

					BufferedInputStream inImg = new BufferedInputStream(conn.getInputStream());
					BufferedImage image = upsideDownHead(ImageIO.read(inImg));

					ImageIO.write(image, "png", outfile);
					System.out.println("2. Saved upside down img: "+url+" ("+name+")");
//					if(null == null) continue;// Uncomment to generate grumm image file only

					//===========================================================================================
					try{Thread.sleep(8000);}catch(InterruptedException e1){e1.printStackTrace();}//8s
					//https://api.mojang.com/user/profile/0e314b6029c74e35bef33c652c8fb467/skin
					conn = (HttpURLConnection)new URL("https://api.minecraftservices.com/minecraft/profile/skins").openConnection();
//					conn = (HttpURLConnection)new URL("https://api.mojang.com/user/profile/" + uuid + "/skin").openConnection();
					conn.setRequestProperty("Authorization", "Bearer "+token);
					conn.setRequestProperty("Content-Length", "15000");
					String boundary = "---------------------------398324416436304970652995196601";//"someArbitraryText";
					conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
					conn.setDoInput(true);
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					OutputStream conn_out = conn.getOutputStream();
					BufferedWriter conn_out_writer = new BufferedWriter(new OutputStreamWriter(conn_out));

					conn_out_writer.write("\r\n--"+boundary+"\r\n");
					conn_out_writer.write("Content-Disposition: form-data; name=\"variant\"\r\n\r\nclassic");
					conn_out_writer.write("\r\n--"+boundary+"\r\n");
					conn_out_writer.write("Content-Disposition: form-data; name=\"file\"; filename=\"yeet_grumm.png"+"\"\r\n");
					conn_out_writer.write("Content-Type: image/png\r\n\r\n");
					conn_out_writer.flush();

					FileInputStream ifstream = new FileInputStream(outfile);
					int newBytes;
					byte[] buffer = new byte[1024];
					while((newBytes=ifstream.read(buffer)) != -1) conn_out.write(buffer, 0, newBytes);
					ifstream.close();
					conn_out.flush();

					conn_out_writer.write("\r\n--"+boundary+"--\r\n");
					conn_out_writer.flush();
					conn_out_writer.close();
					conn_out.close();

					conn.getInputStream();
					System.out.println("3. Skin uploaded");
					try{Thread.sleep(25000);}catch(InterruptedException e1){e1.printStackTrace();}//25s
					//===========================================================================================
					System.out.println("4. Getting new texture url");
					conn = (HttpURLConnection)new URL(
							"https://sessionserver.mojang.com/session/minecraft/profile/"+uuid).openConnection();
					conn.setDoOutput(true);
					BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					StringBuilder builder = new StringBuilder();
					while((line=in.readLine()) != null) builder.append(line);
					String resp = builder.toString().replaceAll(" ", "");
					//System.out.println("response: "+resp);
					String textureBeg = "\"name\":\"textures\",\"value\":\"";
					String textureEnd = "\"}]}";
					String newVal = resp.substring(resp.indexOf(textureBeg)+textureBeg.length());
					newVal = newVal.substring(0, newVal.indexOf(textureEnd));
					//System.out.println("new val: "+newVal);

					String newJson = new String(Base64.getDecoder().decode(newVal));
					//System.out.println("New Json: " + newJson);
					//===========================================================================================

					String textureVal = newJson.substring(newJson.lastIndexOf("texture/")+8);
					textureVal = textureVal.substring(0, textureVal.indexOf('"')).trim();
					String shortJson = "{\"textures\":{\"SKIN\":{\"url\":"
							+ "\"http://textures.minecraft.net/texture/"
							+ textureVal + "\"}}}";
					//System.out.println("Short Json: " + shortJson);
					String newBase64Val = Base64.getEncoder().encodeToString(shortJson.getBytes());
					System.out.println("5. New Base64 val: " + newBase64Val);
					newHeads.put(name+"|GRUMM", newBase64Val);
					//newHeads.put(name, val);
					try{Thread.sleep(5000);}catch(InterruptedException e1){e1.printStackTrace();}//5s
				}
				catch(IOException e){e.printStackTrace();}
			}
		}
		return newHeads;
	}

	static void runGrumm(){
		String[] targetHeads = new String[]{
//				"SHULKER|BLACK|CLOSED", "SHULKER|BLUE|CLOSED", "SHULKER|BROWN|CLOSED", "SHULKER|CYAN|CLOSED", "SHULKER|GRAY|CLOSED",
//				"SHULKER|GREEN|CLOSED", "SHULKER|LIGHT_BLUE|CLOSED", "SHULKER|LIGHT_GRAY|CLOSED", "SHULKER|LIME|CLOSED", "SHULKER|MAGENTA|CLOSED",
//				"SHULKER|ORANGE|CLOSED", "SHULKER|PINK|CLOSED", "SHULKER|PURPLE|CLOSED", "SHULKER|RED|CLOSED", "SHULKER|WHITE|CLOSED",
//				"SHULKER|YELLOW|CLOSED",
//				"BOAT", "LEASH_HITCH",
		};
		String[] headsData = FileIO.loadFile("head-textures.txt", "").split("\n");
		String[] headsToFlip = new String[targetHeads.length];
		for(int i=0; i<targetHeads.length; ++i){
			for(String headData : headsData){
				if(headData.startsWith(targetHeads[i]+":")){
					headsToFlip[i] = headData;
					break;
				}
			}
			if(headsToFlip[i] == null) System.out.println("Could not find target head: "+targetHeads[i]);
		}

		System.out.print("runGrumm() auth for "+targetHeads.length+" heads...\n"); 
		Scanner scanner = new Scanner(System.in); 
		System.out.print("Enter account email: "); String email = scanner.nextLine();//nl@nl.com
		System.out.print("Enter account passw: "); String passw = scanner.nextLine();//y
		System.out.print("Enter account uuid: "); String uuid = scanner.nextLine();//0e314b6029c74e35bef33c652c8fb467
		scanner.close();
		String token = authenticateMojang(email, passw);
		System.out.println("token = "+token);

		System.out.println(StringUtils.join(headsToFlip, "\n"));
		System.out.println("Beginning conversion...");
		System.out.println("Approximate duration in minutes: "+(40F*headsToFlip.length)/60F);// 40s/head
		TreeMap<String, String> newHeads = makeUpsideDownCopies(headsToFlip, "tmp_textures", uuid, token);

		System.out.println("Results: ");
		for(String e : newHeads.keySet()){
			System.out.println(e + ": " + newHeads.get(e));
		}
	}

	static void checkMissingTextures(){
		TreeSet<String> missingTxr = new TreeSet<String>(), extraTxr = new TreeSet<String>();
		TreeSet<String> missingDrpC = new TreeSet<String>(), extraDrpC = new TreeSet<String>();
		for(EntityType type : EntityType.values()){
			if(type.isAlive()){missingTxr.add(type.name()); missingDrpC.add(type.name());}
		}
		for(EntityType type : Arrays.asList(EntityType.ARMOR_STAND, EntityType.LEASH_HITCH, EntityType.MINECART, EntityType.MINECART_CHEST,
				EntityType.MINECART_COMMAND, EntityType.MINECART_FURNACE, EntityType.MINECART_HOPPER, EntityType.MINECART_MOB_SPAWNER,
				EntityType.MINECART_TNT, EntityType.UNKNOWN)){
			missingTxr.add(type.name()); missingDrpC.add(type.name());
		}
		for(String headData : FileIO.loadFile("head-textures.txt", "").split("\n")){
			int i = headData.indexOf(':');
			if(i != -1){
				if(headData.indexOf('|') == -1){
					String headName = headData.substring(0, i);
					if(headData.substring(i+1).replace("xxx", "").trim().isEmpty()) continue;
					if(!missingTxr.remove(headName)) extraTxr.add(headName);
				}
				else extraTxr.add(headData.substring(0, i));
			}
		}
		System.out.println("Missing textures for: "+missingTxr);
		System.out.println("Extra textures for: "+extraTxr);

		for(String headData : FileIO.loadFile("head-drop-rates.txt", "").split("\n")){
			int i = headData.indexOf(':');
			if(i != -1){
				String headKey = headData.substring(0, i);
				if(!missingDrpC.remove(headKey) && !extraTxr.contains(headKey)) extraDrpC.add(headData.substring(0, i));
			}
		}
		System.out.println("Missing drop rates for: "+missingDrpC);
		System.out.println("Extra drop rates for: "+extraDrpC);
	}
	static void checkMissingGrummTextures(){
		TreeSet<String> regularTxtrs = new TreeSet<String>();
		TreeSet<String> grummTxtrs = new TreeSet<String>();
		for(String headData : FileIO.loadFile("head-textures.txt", "").split("\n")){
			int i = headData.indexOf(':'), j = headData.indexOf('|');
			if(i != -1){
				if(headData.substring(i + 1).replace("xxx", "").trim().isEmpty()) continue;
				String headName = headData.substring(0, i);
				if(headName.equals("LEASH_HITCH")) continue;
				if(headName.equals("UNKNOWN")) continue;
				if(headName.equals("PLAYER|ALEX")) continue;
				if(headName.equals("PLAYER|STEVE")) continue;

				if(j != -1 && headName.endsWith("GRUMM")) grummTxtrs.add(headName.substring(0, headName.length()-6));
				else regularTxtrs.add(headName);
			}
		}
		TreeSet<String> missingGrumms = new TreeSet<String>();
		for(String headName : regularTxtrs){
			if(headName.startsWith("SHULKER|") && headName.contains("|SIDE")) continue;
			if(!grummTxtrs.contains(headName)) missingGrumms.add(headName);
		}
		System.out.println("Missing Grumms for: \""+String.join("\", \"", missingGrumms)+"\"");
	}

	static void checkAbnormalHeadTextures(){
		String[] headTextures = FileIO.loadFile("head-textures.txt", "").split("\n");
		TreeSet<String> abnormalSkins = new TreeSet<>();
		int dotEvery = (int)Math.ceil(headTextures.length/80f); // Improv progress bar
		for(int i=dotEvery; i<headTextures.length; i+=dotEvery) System.out.print('-'); System.out.println();
		int num = 0;
		for(String headData : headTextures){
			if(++num == dotEvery){System.out.print('.'); System.out.flush(); num = 0;}
			int i = headData.indexOf(':');
			if(i == -1) continue;
			String name = headData.substring(0, i).trim();
			String base64 = headData.substring(i + 1).replace("xxx", "").trim();
			if(base64.isEmpty()) continue;
			String json;
			try{json = new String(Base64.getDecoder().decode(base64));}
			catch(IllegalArgumentException ex){continue;} // Probably a redirected texture? (TODO: validate all base64s maybe)
			String url = json.substring(json.indexOf("\"url\":")+7, json.lastIndexOf('"')).trim();
			//String textureId = url.substring(url.lastIndexOf('/')+1);
//			try{Thread.sleep(2000);}catch(InterruptedException e1){e1.printStackTrace();}//2s
			try{
				HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
				conn.setUseCaches(false);
				conn.setDoOutput(true);
				conn.setDoInput(true);

				BufferedInputStream inImg = new BufferedInputStream(conn.getInputStream());
				BufferedImage image = ImageIO.read(inImg);
				int w = image.getWidth(), h = image.getHeight();
				if(!((w==64 && h==64) || (w==640 && h==640))) abnormalSkins.add(name+"="+w+"x"+h);
//				System.out.println("2. Image WxH: "+image.getWidth()+"x"+image.getHeight());
//				ImageIO.write(image, "png", new File("tmp_textures/"+name+".png"));
//				System.out.println("3. Saved image: "+url+" ("+name+")");
			}
			catch(IOException e){e.printStackTrace();}
		}
		System.out.println("\nAbnormal skins: "+abnormalSkins);
	}

	public static void main(String... args){
		//com.sun.org.apache.xml.internal.security.Init.init();
		FileIO.DIR = "./";
		checkMissingTextures();
		checkMissingGrummTextures();
		checkAbnormalHeadTextures();
//		runGrumm();
//		System.out.println("Test: "+Vehicle.class.isAssignableFrom(EntityType.PLAYER.getEntityClass()));
	}
}