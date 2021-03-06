package pl.asie.computronics.cc;

import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import pl.asie.computronics.reference.Config;
import pl.asie.computronics.reference.Mods;
import pl.asie.computronics.tile.TileRadar;
import pl.asie.computronics.util.RadarUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CCRadarProxy {
	private static AxisAlignedBB getBounds(int xCoord, int yCoord, int zCoord, int d) {
		int distance = Math.min(d, Config.RADAR_RANGE);
		if(distance < 1) {
			distance = 1;
		}
		return AxisAlignedBB.
			getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1).
			expand(distance, distance, distance);
	}

	@Optional.Method(modid = Mods.ComputerCraft)
	public static String[] getMethodNames() {
		return new String[] { "getEntities", "getPlayers", "getMobs", "getItems" };
	}

	@Optional.Method(modid = Mods.ComputerCraft)
	public static Object[] callMethod(World worldObj, int xCoord, int yCoord, int zCoord, IComputerAccess computer, ILuaContext context,
		int method, Object[] arguments, Object powerProvider) throws LuaException,
		InterruptedException {
		int distance = Config.RADAR_RANGE;
		if(arguments.length >= 1 && (arguments[0] instanceof Double)) {
			distance = ((Double) arguments[0]).intValue();
			if(distance <= 0 || distance > Config.RADAR_RANGE) {
				distance = Config.RADAR_RANGE;
			}
		}
		double energyNeeded = (Config.RADAR_ENERGY_COST_OC * distance);
		if(method == 0) {
			energyNeeded *= 1.75;
		} else if(method == 3) {
			energyNeeded *= 2.0;
		}

		if(powerProvider instanceof TileRadar && !((TileRadar) powerProvider).extractFromBattery(energyNeeded)) {
			return null;
		} else if(powerProvider instanceof ITurtleAccess
			&& ((ITurtleAccess) powerProvider).isFuelNeeded()
			&& !((ITurtleAccess) powerProvider).consumeFuel(
			(int) Math.ceil(energyNeeded)
		)) {
			return null;
		}

		AxisAlignedBB bounds = getBounds(xCoord, yCoord, zCoord, distance);
		Set<Map> entities = new HashSet<Map>();
		if(method == 0 || method == 1) {
			entities.addAll(RadarUtils.getEntities(worldObj, xCoord, yCoord, zCoord, bounds, EntityPlayer.class));
		}
		if(method == 0 || method == 2) {
			entities.addAll(RadarUtils.getEntities(worldObj, xCoord, yCoord, zCoord, bounds, EntityLiving.class));
		}
		if(method == 3) {
			entities.addAll(RadarUtils.getItems(worldObj, xCoord, yCoord, zCoord, bounds, EntityItem.class));
		}

		return new Object[] { RadarUtils.convertSetToMap(entities) };
	}
}
