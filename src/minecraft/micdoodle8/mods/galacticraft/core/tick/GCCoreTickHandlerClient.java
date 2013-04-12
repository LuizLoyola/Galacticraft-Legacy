package micdoodle8.mods.galacticraft.core.tick;

import java.util.EnumSet;

import micdoodle8.mods.galacticraft.API.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.API.ISpaceship;
import micdoodle8.mods.galacticraft.core.GCCoreConfigManager;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.ClientProxyCore;
import micdoodle8.mods.galacticraft.core.client.GCCoreSkyProviderOrbit;
import micdoodle8.mods.galacticraft.core.client.GCCoreSkyProviderOverworld;
import micdoodle8.mods.galacticraft.core.client.gui.GCCoreGuiChoosePlanet;
import micdoodle8.mods.galacticraft.core.client.gui.GCCoreOverlayOxygenTankIndicator;
import micdoodle8.mods.galacticraft.core.client.gui.GCCoreOverlaySensorGlasses;
import micdoodle8.mods.galacticraft.core.client.gui.GCCoreOverlaySpaceship;
import micdoodle8.mods.galacticraft.core.client.sounds.GCCoreSoundUpdaterSpaceship;
import micdoodle8.mods.galacticraft.core.dimension.GCCoreWorldProvider;
import micdoodle8.mods.galacticraft.core.entities.EntitySpaceshipBase;
import micdoodle8.mods.galacticraft.core.entities.GCCoreEntitySpaceship;
import micdoodle8.mods.galacticraft.core.items.GCCoreItemSensorGlasses;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.util.OxygenUtil;
import micdoodle8.mods.galacticraft.core.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldProviderSurface;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;

public class GCCoreTickHandlerClient implements ITickHandler
{
	public static int airRemaining;
	public static int airRemaining2;
    public static boolean checkedVersion = true;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
		ClientProxyCore.getCurrentTime = System.currentTimeMillis();

		final Minecraft minecraft = FMLClientHandler.instance().getClient();

        final WorldClient world = minecraft.theWorld;

        final EntityClientPlayerMP player = minecraft.thePlayer;

		if (type.equals(EnumSet.of(TickType.CLIENT)))
        {
			if (minecraft.currentScreen != null && minecraft.currentScreen instanceof GuiMainMenu)
			{
				GalacticraftCore.playersServer.clear();
				GalacticraftCore.playersClient.clear();
				ClientProxyCore.playersUsingParachutes.clear();
				ClientProxyCore.playersWithOxygenGear.clear();
				ClientProxyCore.playersWithOxygenMask.clear();
				ClientProxyCore.playersWithOxygenTankLeftGreen.clear();
				ClientProxyCore.playersWithOxygenTankLeftOrange.clear();
				ClientProxyCore.playersWithOxygenTankLeftRed.clear();
				ClientProxyCore.playersWithOxygenTankRightGreen.clear();
				ClientProxyCore.playersWithOxygenTankRightOrange.clear();
				ClientProxyCore.playersWithOxygenTankRightRed.clear();
			}

	    	if (world != null && GCCoreTickHandlerClient.checkedVersion)
	    	{
	    		GCCoreUtil.checkVersion(Side.CLIENT);
	    		GCCoreTickHandlerClient.checkedVersion = false;
	    	}

			if (player != null && player.ridingEntity != null && player.ridingEntity instanceof ISpaceship)
			{
				final Object[] toSend = {player.ridingEntity.rotationPitch};
	            PacketDispatcher.sendPacketToServer(PacketUtil.createPacket(GalacticraftCore.CHANNEL, 8, toSend));
				final Object[] toSend2 = {player.ridingEntity.rotationYaw};
	            PacketDispatcher.sendPacketToServer(PacketUtil.createPacket(GalacticraftCore.CHANNEL, 7, toSend2));
			}

			if (world != null && world.provider instanceof WorldProviderSurface)
			{
				if (world.provider.getSkyRenderer() == null && player.ridingEntity != null && player.ridingEntity.posY >= 200)
                {
					world.provider.setSkyRenderer(new GCCoreSkyProviderOverworld());
                }
				else if (world.provider.getSkyRenderer() != null && world.provider.getSkyRenderer() instanceof GCCoreSkyProviderOverworld && (player.ridingEntity == null || player.ridingEntity.posY < 200))
				{
					world.provider.setSkyRenderer(null);
				}
			}

			if (world != null && world.provider instanceof GCCoreWorldProvider)
			{
				if (world.provider.getSkyRenderer() == null)
                {
					world.provider.setSkyRenderer(new GCCoreSkyProviderOrbit());
                }
			}

//			if (player != null && player.ridingEntity != null && player.ridingEntity instanceof GCCoreEntityControllable)
//			{
//				final GCCoreEntityControllable entityControllable = (GCCoreEntityControllable) player.ridingEntity;
//
//				if (minecraft.gameSettings.keyBindLeft.pressed)
//				{
//					entityControllable.keyPressed(2, player);
//					final Object[] toSend = {2};
//					PacketDispatcher.sendPacketToServer(PacketUtil.createPacket(GalacticraftCore.CHANNEL, 9, toSend));
//				}
//
//				if (minecraft.gameSettings.keyBindRight.pressed)
//				{
//					entityControllable.keyPressed(3, player);
//					final Object[] toSend = {3};
//					PacketDispatcher.sendPacketToServer(PacketUtil.createPacket(GalacticraftCore.CHANNEL, 9, toSend));
//				}
//
//				if (minecraft.gameSettings.keyBindForward.pressed)
//				{
//					entityControllable.keyPressed(0, player);
//					final Object[] toSend = {0};
//					PacketDispatcher.sendPacketToServer(PacketUtil.createPacket(GalacticraftCore.CHANNEL, 9, toSend));
//				}
//
//				if (minecraft.gameSettings.keyBindBack.pressed)
//				{
//					entityControllable.keyPressed(1, player);
//					final Object[] toSend = {1};
//					PacketDispatcher.sendPacketToServer(PacketUtil.createPacket(GalacticraftCore.CHANNEL, 9, toSend));
//				}
//			}

			if (player != null && player.ridingEntity != null && player.ridingEntity instanceof GCCoreEntitySpaceship)
			{
				final GCCoreEntitySpaceship ship = (GCCoreEntitySpaceship) player.ridingEntity;

				if (minecraft.gameSettings.keyBindLeft.pressed)
				{
    	            ship.turnYaw(-1.0F);
    				final Object[] toSend = {ship.rotationYaw};
    	            PacketDispatcher.sendPacketToServer(PacketUtil.createPacket(GalacticraftCore.CHANNEL, 7, toSend));
				}

				if (minecraft.gameSettings.keyBindRight.pressed)
				{
    	            ship.turnYaw(1.0F);
    				final Object[] toSend = {ship.rotationYaw};
    	            PacketDispatcher.sendPacketToServer(PacketUtil.createPacket(GalacticraftCore.CHANNEL, 7, toSend));
				}

				if (minecraft.gameSettings.keyBindForward.pressed)
				{
					if (ship.getLaunched() == 1)
					{
        	            ship.turnPitch(-0.7F);
        				final Object[] toSend = {ship.rotationPitch};
        	            PacketDispatcher.sendPacketToServer(PacketUtil.createPacket("Galacticraft", 8, toSend));
					}
				}

				if (minecraft.gameSettings.keyBindBack.pressed)
				{
					if (ship.getLaunched() == 1)
					{
        	            ship.turnPitch(0.7F);
        				final Object[] toSend = {ship.rotationPitch};
        	            PacketDispatcher.sendPacketToServer(PacketUtil.createPacket(GalacticraftCore.CHANNEL, 8, toSend));
					}
				}
			}

        	if (world != null)
        	{
    	        for (int i = 0; i < world.loadedEntityList.size(); i++)
    	        {
	        		final Entity e = (Entity) world.loadedEntityList.get(i);

	        		if (e != null)
	        		{
	        			if (e instanceof GCCoreEntitySpaceship)
	        			{
	        				final GCCoreEntitySpaceship eship = (GCCoreEntitySpaceship) e;

	        				if (eship.rocketSoundUpdater == null)
	        				{
	        					eship.rocketSoundUpdater = new GCCoreSoundUpdaterSpaceship(FMLClientHandler.instance().getClient().sndManager, eship, FMLClientHandler.instance().getClient().thePlayer);
	        				}
	        			}
	        		}
    	        }
        	}

			if (FMLClientHandler.instance().getClient().currentScreen instanceof GCCoreGuiChoosePlanet)
			{
				player.motionY = 0;
			}

			if (world != null && world.provider instanceof IGalacticraftWorldProvider)
			{
				world.setRainStrength(0.0F);
			}

			if (!minecraft.gameSettings.keyBindJump.pressed)
			{
				ClientProxyCore.lastSpacebarDown = false;
			}

			if (player != null && player.ridingEntity != null && minecraft.gameSettings.keyBindJump.pressed && !ClientProxyCore.lastSpacebarDown)
			{
				final Object[] toSend = {0};
	            PacketDispatcher.sendPacketToServer(PacketUtil.createPacket(GalacticraftCore.CHANNEL, 3, toSend));
	            ClientProxyCore.lastSpacebarDown = true;
			}
        }
    }
	
    private void drawOutlinedBoundingBox(AxisAlignedBB par1AxisAlignedBB)
    {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(3);
        tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
        tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
        tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
        tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
        tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
        tessellator.draw();
        tessellator.startDrawing(3);
        tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
        tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
        tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
        tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
        tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
        tessellator.draw();
        tessellator.startDrawing(1);
        tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
        tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
        tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
        tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
        tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
        tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
        tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
        tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
        tessellator.draw();
    }

	public static void zoom(float value)
	{
		try
		{
	        ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, value, 13);
	        ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, value, 14);
		}
		catch (final Exception ex)
		{
	        ex.printStackTrace();
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		final Minecraft minecraft = FMLClientHandler.instance().getClient();

        final EntityPlayerSP player = minecraft.thePlayer;

        ItemStack helmetSlot = null;

		if (player != null && player.inventory.armorItemInSlot(3) != null)
		{
			helmetSlot = player.inventory.armorItemInSlot(3);
		}

		if (type.equals(EnumSet.of(TickType.RENDER)))
        {
    		final float partialTickTime = (Float) tickData[0];

			if (player != null)
			{
    			ClientProxyCore.playerPosX = player.prevPosX + (player.posX - player.prevPosX) * partialTickTime;
    			ClientProxyCore.playerPosY = player.prevPosY + (player.posY - player.prevPosY) * partialTickTime;
    			ClientProxyCore.playerPosZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTickTime;
    			ClientProxyCore.playerRotationYaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTickTime;
    			ClientProxyCore.playerRotationPitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTickTime;
			}

			if (player != null && player.ridingEntity != null && player.ridingEntity instanceof GCCoreEntitySpaceship)
    		{
				float f = (((GCCoreEntitySpaceship)player.ridingEntity).getTimeSinceLaunch() - 250F) / 175F;

				if (f < 0)
				{
					f = 0F;
				}

				if (f > 1)
				{
					f = 1F;
				}

				final ScaledResolution scaledresolution = new ScaledResolution(minecraft.gameSettings, minecraft.displayWidth, minecraft.displayHeight);
		        scaledresolution.getScaledWidth();
		        scaledresolution.getScaledHeight();
		        minecraft.entityRenderer.setupOverlayRendering();
		        GL11.glEnable(GL11.GL_BLEND);
		        GL11.glDisable(GL11.GL_DEPTH_TEST);
		        GL11.glDepthMask(false);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, f);
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				GL11.glDepthMask(true);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    		}

    		if (helmetSlot != null && helmetSlot.getItem() instanceof GCCoreItemSensorGlasses && minecraft.currentScreen == null)
    		{
    			GCCoreOverlaySensorGlasses.renderSensorGlassesMain();
    			GCCoreOverlaySensorGlasses.renderSensorGlassesValueableBlocks();
    		}

    		if (minecraft.currentScreen == null && player != null && player.ridingEntity != null && player.ridingEntity instanceof EntitySpaceshipBase && minecraft.gameSettings.thirdPersonView != 0 && !minecraft.gameSettings.hideGUI)
    		{
    			GCCoreOverlaySpaceship.renderSpaceshipOverlay();
    		}

    		if (player != null && player.worldObj.provider instanceof IGalacticraftWorldProvider && OxygenUtil.shouldDisplayTankGui(minecraft.currentScreen))
			{
				int var6 = (GCCoreTickHandlerClient.airRemaining - 90) * -1;

				if (GCCoreTickHandlerClient.airRemaining <= 0)
				{
					var6 = 90;
				}

				int var7 = (GCCoreTickHandlerClient.airRemaining2 - 90) * -1;

				if (GCCoreTickHandlerClient.airRemaining2 <= 0)
				{
					var7 = 90;
				}

				if (GCCoreConfigManager.oxygenIndicatorLeftSide)
				{
    				GCCoreOverlayOxygenTankIndicator.renderOxygenTankIndicatorLeft(var6, var7);
				}
				else
				{
    				GCCoreOverlayOxygenTankIndicator.renderOxygenTankIndicatorRight(var6, var7);
				}
			}
        }
	}

    @Override
	public String getLabel()
    {
        return "Galacticraft Client";
    }

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.RENDER, TickType.CLIENT);
	}
}