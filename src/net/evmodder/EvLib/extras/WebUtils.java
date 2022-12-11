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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
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

	/*public static String putReadURL(String payload, String url){
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
	}*/

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
	static String authenticateMicrosoft(String email, String password) throws UnsupportedEncodingException{
		final String genClientToken = UUID.randomUUID().toString();
		final String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString());
		final String encodedPassw = URLEncoder.encode(password, StandardCharsets.UTF_8.toString());

		// Setting up json POST request
		final String payload =
				"{\"login\": \""+encodedEmail+"\",\"loginfmt\": \""+encodedEmail
				+"\",\"type\": 11,\"LoginOptions\": 3,\"ps\": 2,\"passwd\": \""+encodedPassw+"\","
				+"\"PPSX\": \"Pass\",\"PPFT\": \""+genClientToken+"\"}";

		String output = postReadURL(payload, "https://login.live.com");
		System.out.println("auth server response:\n"+output);

		// Setting up patterns
		String authBeg = "\"accessToken\":\"";
		String authEnd = "\",\"";

		return getStringBetween(output, authBeg, authEnd);
	}

	static String addDashesForUUID(String uuidStr){
		return uuidStr.substring(0, 8)+"-"+uuidStr.substring(8, 12)+"-"+uuidStr.substring(12, 16)
				+"-"+uuidStr.substring(16, 20)+"-"+uuidStr.substring(20);
	}

	//Names are [3,16] characters from [abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_]
	static HashMap<String, GameProfile> playerExists = new HashMap<>();
	public static GameProfile getGameProfile(String nameOrUUID, boolean fetchSkin){
		if(nameOrUUID.matches("^[a-f0-9]{32}$")) nameOrUUID = addDashesForUUID(nameOrUUID);
		nameOrUUID = nameOrUUID.toLowerCase();
		GameProfile profile = playerExists.get(nameOrUUID);
		if(profile != null){
			if(fetchSkin && !profile.getProperties().containsKey("textures")) nameOrUUID = profile.getId().toString();
			else return profile;
		}
		try{//Lookup by UUID
			final UUID uuid = UUID.fromString(nameOrUUID);
			String data = getReadURL("https://sessionserver.mojang.com/session/minecraft/profile/"+nameOrUUID);
			if(data != null){
				data = data.replaceAll("\\s+", "");
				final int nameStart = data.indexOf("\"name\":\"")+8;
				final int nameEnd = data.indexOf("\"", nameStart+1);
				final String name = data.substring(nameStart, nameEnd);
				profile = new GameProfile(uuid, name);
				if(fetchSkin){
					final int codeStart = data.indexOf("{\"name\":\"textures\",\"value\":\"")+28;
					final int codeEnd = data.indexOf("\"}", codeStart+1);
					final String base64 = data.substring(codeStart, codeEnd);
					profile.getProperties().put("textures", new Property("textures", base64));
				}
				playerExists.put(name.toLowerCase(), profile);
			}
		}
		catch(IllegalArgumentException e){// Lookup by Name
			//Sample data: {"id":"34471e8dd0c547b9b8e1b5b9472affa4","name":"EvDoc"}
			String data = getReadURL("https://api.mojang.com/users/profiles/minecraft/"+nameOrUUID);
			if(data != null){
				data = data.replace(" ", "");
//				if(data.contains("\"id\":\"")){
					final int idStart = data.indexOf("\"id\":\"")+6;
					final int idEnd = data.indexOf("\"", idStart+1);
					String uuidStr = data.substring(idStart, idEnd);
					if(uuidStr.matches("^[a-f0-9]{32}$")) uuidStr = addDashesForUUID(uuidStr);
					final UUID uuid = UUID.fromString(uuidStr);// Important to validate UUID before recursive call
					if(fetchSkin) return getGameProfile(uuidStr, fetchSkin);
					final int nameStart = data.indexOf("\"name\":\"")+8;
					final int nameEnd = data.indexOf("\"", nameStart+1);
					final String name = data.substring(nameStart, nameEnd);
					profile = new GameProfile(uuid, name);
					playerExists.put(uuidStr.toLowerCase(), profile);
//				}
			}
		}
		playerExists.put(nameOrUUID, profile);
		return profile;
	}

	static HashMap<String, String> textureExists = new HashMap<>();
	public static String getTextureURL(String texture, boolean verify){
		if(texture.replace("xxx", "").trim().isEmpty()) return null;
		String url = textureExists.get(texture);
		if(url == null){
			try{
				String json = new String(Base64.getDecoder().decode(texture));
				int startIndex = json.indexOf("\"url\":");
				if(startIndex != -1) url = json.substring(startIndex+7, json.indexOf('"', startIndex+8)).trim();
			}
			catch(IllegalArgumentException | StringIndexOutOfBoundsException e){}
			if(url == null){
				if(texture.chars().allMatch(ch -> (ch >= 'a' && ch <= 'f') || (ch >= '0' && ch <= '9'))){
					url = "http://textures.minecraft.net/texture/"+texture;
				}
				else url = texture;
			}
			if(verify) try{
				HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
				conn.setUseCaches(false);
				conn.setDoOutput(false);
				conn.setDoInput(true);
				if(conn.getResponseCode() != 200) url = null;
				textureExists.put(texture, url);
			}
			catch(IOException e){url = null;}
		}
		return url;
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

	static String getTextureVal(String uuidNoDashes){
		try{
			HttpURLConnection conn = (HttpURLConnection)new URL(
					"https://sessionserver.mojang.com/session/minecraft/profile/"+uuidNoDashes).openConnection();
			conn.setDoOutput(true);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line;
			while((line=in.readLine()) != null) builder.append(line);
			String resp = builder.toString().replaceAll(" ", "");
			//System.out.println("response: "+resp);
			String textureBeg = "\"name\":\"textures\",\"value\":\"", textureEnd = "\"}]}";
			String newVal = resp.substring(resp.indexOf(textureBeg)+textureBeg.length());
			newVal = newVal.substring(0, newVal.indexOf(textureEnd));
			//System.out.println("new val: "+newVal);
			String newJson = new String(Base64.getDecoder().decode(newVal));
			String textureVal = newJson.substring(newJson.lastIndexOf("texture/")+8);
			textureVal = textureVal.substring(0, textureVal.indexOf('"')).trim();
			return textureVal;
		}
		catch(IOException e){e.printStackTrace();}
		return null;
	}
	static String getBase64FromTextureVal(String textureVal){
		final String shortJson = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/"+textureVal+"\"}}}";
		//System.out.println("Short Json: " + shortJson);
		String newBase64Val = Base64.getEncoder().encodeToString(shortJson.getBytes());
		return newBase64Val;
	}
	// returns Map<HEAD|NAME, base64Value>
	static TreeMap<String, String> makeUpsideDownCopies(String[] heads, String outfolder, String uuidNoDashes, String token){
		TreeMap<String, String> newHeadsTexutureVal = new TreeMap<>(); // Map from HEAD|NAME -> mojangTextureVal
		TreeMap<String, String> newHeadsBase64Val = new TreeMap<>(); // Map from HEAD|NAME -> base64Val
		for(String line : heads){
			int idx = line.indexOf(':');
			if(idx > -1 && idx < line.length()-1){
				String name = line.substring(0, idx).trim();
				String val = line.substring(idx+1).trim();
				if(name.endsWith("GRUMM") || val.isEmpty()) continue;
				String url = getTextureURL(val, /*verify=*/false);
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
					if(token == null) continue;// Generate grumm image file only (no URL for texture code)

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
					final String textureVal = getTextureVal(uuidNoDashes);
					final String newBase64Val = getBase64FromTextureVal(textureVal);
					System.out.println("5. New Base64 val: " + newBase64Val);
					newHeadsTexutureVal.put(name+"|GRUMM", textureVal);
					newHeadsBase64Val.put(name+"|GRUMM", newBase64Val);
					try{Thread.sleep(5000);}catch(InterruptedException e1){e1.printStackTrace();}//5s
				}
				catch(IOException e){e.printStackTrace();}
			}
		}
		System.out.println("Results 1: ");
		for(String e : newHeadsTexutureVal.keySet()){
			System.out.println(e + ": " + newHeadsTexutureVal.get(e));
		}
		System.out.println("Results 2: ");
		for(String e : newHeadsBase64Val.keySet()){
			System.out.println(e + ": " + newHeadsBase64Val.get(e));
		}
		return newHeadsBase64Val;
	}

	static void runGrumm(){
		String[] targetHeads = new String[]{
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
			if(headsToFlip[i] == null) System.err.println("Could not find target head: "+targetHeads[i]);
		}

		System.out.print("runGrumm() auth for "+targetHeads.length+" heads...\n"); 
		/*Scanner scanner = new Scanner(System.in); 
		System.out.print("Enter account email: "); String email = scanner.nextLine();//nl@nl.com
		System.out.print("Enter account passw: "); String passw = scanner.nextLine();//y
		System.out.print("Enter account uuid: "); String uuid = scanner.nextLine();//0e314b6029c74e35bef33c652c8fb467
		scanner.close();
		String token = authenticateMojang(email, passw);
		System.out.println("token = "+token);*/
		String uuid = "0e314b6029c74e35bef33c652c8fb467";
		String token = "eyJhbGciOiJIUzI1NiJ9.eyJ4dWlkIjoiMjUzNTQ2NjM1MzY2NjY2NiIsImFnZyI6IkFkdWx0Iiwic3ViIjoiMWUzYTgyNDYtNTNmMC00ODBmLWIwYjMtMTFiNGU5ZDVkNTY0IiwibmJmIjoxNjU1ODYwNjM5LCJhdXRoIjoiWEJPWCIsInJvbGVzIjpbXSwiaXNzIjoiYXV0aGVudGljYXRpb24iLCJleHAiOjE2NTU5NDcwMzksImlhdCI6MTY1NTg2MDYzOSwicGxhdGZvcm0iOiJVTktOT1dOIiwieXVpZCI6IjY1ODY5ZWMwOGMxZGM1ZDM2YzVjMWMzY2M5YzlmODkwIn0.-NoxeCfsTWF2cI6Wc7rVIJjlYrYsIVMOB30WIipyJkU";

		System.out.println(String.join("\n", headsToFlip));
		System.out.println("Beginning conversion...");
		System.out.println("Approximate duration in minutes: "+(40F*headsToFlip.length)/60F);// 40s/head
		makeUpsideDownCopies(headsToFlip, "tmp_textures", uuid, token);
	}

	static void checkMissingTexturesDropratesAndSpawnModifiers(){
		TreeSet<String> expectedTxr = new TreeSet<>();
		TreeSet<String> foundTxr = new TreeSet<>();
		TreeSet<String> extraTxr = new TreeSet<>();
		TreeSet<String> duplicateTxr = new TreeSet<>();
		TreeSet<String> xxxTxr = new TreeSet<>();
		TreeSet<String> missingDrpC = new TreeSet<>(), extraDrpC = new TreeSet<>();
		for(EntityType type : EntityType.values()){
			if(type.isAlive()){expectedTxr.add(type.name()); missingDrpC.add(type.name());}
		}
		expectedTxr.remove(EntityType.PLAYER.name());
		for(EntityType type : Arrays.asList(EntityType.ARMOR_STAND, EntityType.LEASH_HITCH, EntityType.MINECART, EntityType.MINECART_CHEST,
				EntityType.MINECART_COMMAND, EntityType.MINECART_FURNACE, EntityType.MINECART_HOPPER, EntityType.MINECART_MOB_SPAWNER,
				EntityType.MINECART_TNT, EntityType.UNKNOWN)){
			expectedTxr.add(type.name()); missingDrpC.add(type.name());
		}
		for(String headData : FileIO.loadFile("head-textures.txt", "").split("\n")){
			int i = headData.indexOf(':');
			if(i != -1){
				String headName = headData.substring(0, i);
				headData = headData.substring(i+1);
				if(!foundTxr.add(headName)) duplicateTxr.add(headName);
				if(headData.replace("xxx", "").trim().isEmpty()){
					xxxTxr.add(headName);
					continue;
				}
				if(headName.indexOf('|') == -1 && !expectedTxr.remove(headName)) extraTxr.add(headName);
			}
		}
		System.out.println("Textures missing: "+expectedTxr);
		System.out.println("Textures extra: "+extraTxr);
		System.out.println("Textures duplicated: "+duplicateTxr);
		System.out.println("Textures xxx: "+xxxTxr);

		missingDrpC.addAll(extraTxr);
		for(String headData : FileIO.loadFile("head-drop-rates.txt", "").split("\n")){
			int i = headData.indexOf(':');
			if(i != -1){
				String headKey = headData.substring(0, i);
				if(!missingDrpC.remove(headKey) && !extraTxr.contains(headKey)) extraDrpC.add(headData.substring(0, i));
			}
		}
		System.out.println("Missing drop rates for: "+missingDrpC);
		System.out.println("Extra drop rates for: "+extraDrpC);

		TreeSet<String> alrInConf = new TreeSet<>();
		for(String spawnMod : FileIO.loadFile("spawn-cause-modifiers.txt", "").split("\n")){
			final int i = spawnMod.indexOf(':');
			if(i != -1) alrInConf.add(spawnMod.substring(0, i));
		}
		TreeSet<String> missingFromConf = new TreeSet<>();
		for(SpawnReason reason : SpawnReason.values()){
			if(!alrInConf.contains(reason.name())) missingFromConf.add(reason.name());
		}
		System.out.println("Missing SpawnReason modifiers for: ["+String.join(", ", missingFromConf)+"]");
	}
	static void checkMissingGrummTextures(){
		TreeSet<String> regularTxtrs = new TreeSet<>();
		TreeSet<String> grummTxtrs = new TreeSet<>();
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
		TreeSet<String> missingGrumms = new TreeSet<>();
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
			String textureCode = headData.substring(i + 1).replace("xxx", "").trim();
			if(textureCode.isEmpty()) continue;
			String url = getTextureURL(textureCode, /*verify=*/false);
			//String textureId = url.substring(url.lastIndexOf('/')+1);
//			try{Thread.sleep(2000);}catch(InterruptedException e1){e1.printStackTrace();}//2s
			try{
				HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
				conn.setUseCaches(false);
				conn.setDoOutput(true);
				conn.setDoInput(true);

				BufferedInputStream inImg = new BufferedInputStream(conn.getInputStream());
				BufferedImage image = ImageIO.read(inImg);
				if(image == null){
					System.err.println("Invalid image at url: "+url);
					continue;
				}
				int w = image.getWidth(), h = image.getHeight();
				if(!((w==64 && h==64) || (w==640 && h==640))) abnormalSkins.add(name+"="+w+"x"+h);
//				System.out.println("2. Image WxH: "+image.getWidth()+"x"+image.getHeight());
//				ImageIO.write(image, "png", new File("tmp_textures/"+name+".png"));
//				System.out.println("3. Saved image: "+url+" ("+name+")");
			}
			catch(IOException e){/*e.printStackTrace();*/}
		}
		System.out.println("\n64x32 PRE_JAPPA skins: "+abnormalSkins.stream().filter(name -> name.contains("|PRE_JAPPA")).count());
		abnormalSkins.removeIf(name -> name.contains("|PRE_JAPPA"));
		System.out.println("Abnormal skins: "+abnormalSkins);
	}

	public static String convertUUIDToIntArray(UUID uuid){
		String uuidStr = uuid.toString().replace("-", "");
		if(uuidStr.length() != 32){
			System.err.print(uuid+"is not a valid UUID!");
			return null;
		}
		int[] arr = new int[4];
		for(int i=0; i<4; ++i){
			String slice = uuidStr.substring(i*8, i*8+8);
			arr[i] = Integer.parseUnsignedInt(slice, 16);
		}
		return "[I;"+arr[0]+","+arr[1]+","+arr[2]+","+arr[3]+"]";
	}

	static void printUUIDsForTextureKeys(){
		String[] textureKeys = new String[]{
			"#ARMOR_STAND"
		};
		String[] textures = new String[]{
			"eyJ0ZXh0dXJlcyI6eyJTS0"
		};
		System.out.println("textureKeys: "+textureKeys.length+", Base64s: "+textures.length);
		for(int i=0; i<textureKeys.length; ++i){
			System.out.println(convertUUIDToIntArray(UUID.nameUUIDFromBytes(textureKeys[i].getBytes()))+"="+String.join("=", textures[i]));
			//System.out.println(textureKeys[i]+"="+convertUUIDToIntArray(UUID.nameUUIDFromBytes(textureKeys[i].getBytes())));
		}

		for(int i=0; i<textureKeys.length; ++i){
			try{
			String json = new String(Base64.getDecoder().decode(textures[i]));
			System.out.println("json: "+json);
//			String url = json.substring(json.indexOf("\"url\":")+7, json.lastIndexOf('"')).trim();
//			System.out.println("url: "+url);
			System.out.println(textureKeys[i]+": "+json.substring(66, 130));
			System.out.println("original texture: "+ Base64.getEncoder().encodeToString(
					("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/"+json.substring(66, 130)+"\"}}}")
						.getBytes(StandardCharsets.ISO_8859_1)));
			}
			catch(IllegalArgumentException | StringIndexOutOfBoundsException e){
				System.out.println(textureKeys[i]+": "+textures[i]);
			}
		}
	}

	static void printUUIDsForPlayerNames(){
		String[] nameAndStuff = new String[]{
				"EvDoc,312,4,random,crap,here"
		};
		TreeSet<String> badNames = new TreeSet<>();
		for(String line : nameAndStuff){
			String name = line.split(",")[0];
			GameProfile profile = getGameProfile(name, /*fetchSkin=*/false);
			if(profile != null && profile.getId() != null) System.out.println(profile.getId()+","+line);
			else badNames.add(name);
		}
		System.out.println("unknown players: "+badNames);
	}

	static void reformatTexturesFile(){
		StringBuilder builder = new StringBuilder();
		for(String line : FileIO.loadFile("head-textures.txt", "").split("\n")){
			int i = line.indexOf(':');
			if(i == -1){
				builder.append(line).append('\n');
				continue;
			}
			String name = line.substring(0, i);
			String after = line.substring(i + 1);
			builder.append(name).append(':');
			for(int j = 0; j < 47 - name.length(); ++j) builder.append(' ');
			builder.append(after).append('\n');
		}
		System.out.print(builder.toString());
	}

	public static void main(String... args){
//		System.out.println("a");
//		Component c = TellrawUtils.parseComponentFromString(
//				"{\"italic\":false,\"extra\":[{\"italic\":false,\"extra\":[{\"translate\":\"item.minecraft.netherite_sword\"}],\"text\":\"\"}],\"text\":\"\"}");
//		System.out.println("b");
//		System.out.println(c.toString());
//		System.out.println("c");
		//com.sun.org.apache.xml.internal.security.Init.init();
		FileIO.DIR = "./";
//		reformatTexturesFile();
//		printUUIDsForTextureKeys();
//		printUUIDsForPlayerNames();
		checkMissingTexturesDropratesAndSpawnModifiers();
		checkMissingGrummTextures();
//		checkAbnormalHeadTextures();
//		runGrumm();
//		System.out.println("Test: "+Vehicle.class.isAssignableFrom(EntityType.PLAYER.getEntityClass()));
	}
}