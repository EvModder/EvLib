package net.evmodder.EvLib.hooks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

public class WorldEditHook{
	static WorldEditPlugin wep = null;
	static WorldEdit we = null;

	static boolean initWorldEdit(){
		wep = (WorldEditPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		if(wep == null) return false;
		we = wep.getWorldEdit();
		return true;
	}

	public static boolean saveSchematic(Player player, File schematicFile){
		if(wep == null) if(!initWorldEdit()) return false;

		BukkitPlayer bukkitPlayer = wep.wrapPlayer(player);
		LocalSession localSession = we.getSessionManager().get(bukkitPlayer);
		try{
			ClipboardHolder selection = localSession.getClipboard();
			Clipboard clipboard = selection.getClipboard();
			ClipboardWriter writer = BuiltInClipboardFormat.MCEDIT_SCHEMATIC
						.getWriter(new FileOutputStream(schematicFile));
			writer.write(clipboard);
			return true;
		}
		catch(EmptyClipboardException | IOException ex){ex.printStackTrace();}
		return false;
	}

	public static boolean pasteSchematic(File schematicFile, Location pasteLoc){
		if(wep == null) if(!initWorldEdit()) return false;
		double x = pasteLoc.getX(), y = pasteLoc.getY(), z = pasteLoc.getZ();

		ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
		try(ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))){
			Clipboard clipboard = reader.read();

			EditSessionFactory esFactory = we.getEditSessionFactory();
			try(EditSession editSession = esFactory.getEditSession(new BukkitWorld(pasteLoc.getWorld()), -1)){
				Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
						.to(BlockVector3.at(x, y, z)).ignoreAirBlocks(false).build();
				try{
					Operations.complete(operation);
					return true;
				}
				catch(WorldEditException ex){ex.printStackTrace();}
			}
		}
		catch(IOException ex){ex.printStackTrace();}
		return false;
	}

	public static boolean pasteSchematic(File schematicFile, World world){
		if(wep == null) if(!initWorldEdit()) return false;

		ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
		try(ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))){
			Clipboard clipboard = reader.read();

			EditSessionFactory esFactory = we.getEditSessionFactory();
			try(EditSession editSession = esFactory.getEditSession(new BukkitWorld(world), -1)){
				Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
						.to(clipboard.getOrigin()).ignoreAirBlocks(false).build();
				try{
					Operations.complete(operation);
					return true;
				}
				catch(WorldEditException ex){ex.printStackTrace();}
			}
		}
		catch(IOException ex){ex.printStackTrace();}
		return false;
	}
}