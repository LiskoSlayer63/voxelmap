/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.minecraft.src;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.Date;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Mouse;
import java.util.Random;
// extends BaseMod
//TODO: remodloaderize
public class mod_ZanMinimap implements Runnable { // implements Runnable
	private Minecraft game; 

	/*motion tracker, may or may not exist*/
	private mod_MotionTracker motionTracker = null;

	/*whether motion tracker exists*/
	private Boolean motionTrackerExists = false;

	/*Textures for each zoom level*/
	private BufferedImage[] map = new BufferedImage[4];

	/*Block colour array*/
	private int[] blockColors = new int[4096];

	private int q = 0;
	private Random generator = new Random();
	/*Current Menu Loaded*/
	public int iMenu = 1;

	/*Display anything at all, menu, etc..*/
	private boolean enabled = true;

	/*Hide just the minimap*/
	private boolean hide = false;

	/*Show the minimap when in the Nether*/
	private boolean showNether = false;

	/*Experimental cave mode (only applicable to overworld)*/
	private boolean showCaves = false;

	/*Was mouse down last render?*/
	private boolean lfclick = false;

	/*Toggle full screen map*/
	private boolean full = false;

	/*Is map calc thread still executing?*/
	public boolean active = false;

	/*Current level of zoom*/
	private int zoom = 2;

	/*Current build version*/
	public String zmodver = "v0.9.7c";

	/*Menu input string*/
	private String inStr = "";

	/*Waypoint name temporary input*/
	private String way = "";

	/*Waypoint X coord temp input*/
	private int wayX = 0;

	/*Z coord temp input*/
	private int wayZ = 0;

	/*Colour or black and white minimap?*/
	private boolean rc = true;

	/*Holds error exceptions thrown*/
	private String error = "";

	/*Strings to show for menu*/
	private String[][] sMenu = new String[2][13]; // bump up options here

	/*Time remaining to show error thrown for*/
	private int ztimer = 0;

	/*Minimap update interval*/
	private int timer = 0;

	/*Key entry interval*/
	private int fudge = 0;

	/*Last X coordinate rendered*/
	private int lastX = 0;

	/*Last Z coordinate rendered*/
	private int lastZ = 0;

	/*Last zoom level rendered at*/
	private int lZoom = 0;

	/*Menu level for next render*/
	private int next = 0;

	/*Cursor blink interval*/
	private int blink = 0;

	/*Last key down on previous render*/
	private int lastKey= 0;

	/*Direction you're facing*/
	private float direction = 0.0f;

	/*Last direction you were facing*/
	private float oldDir = 0.0f;

	/*Setting file access*/
	private File settingsFile;

	/*World currently loaded*/
	private String world = "";

	/*Is the scrollbar being dragged?*/
	private boolean scrClick = false;

	/*Scrollbar drag start position*/
	private int scrStart = 0;

	/*Scrollbar offset*/
	private int sMin = 0;

	/*Scrollbar size*/
	private int sMax = 67;

	/*1st waypoint entry shown*/
	private int min = 0;

	/*Zoom key index*/
	private int zoomKey = Keyboard.KEY_Z;

	/*Menu key index*/
	private int menuKey = Keyboard.KEY_M;

	/*Square map toggle*/
	private boolean squareMap = false;
	
	/*Old north toggle*/
	public boolean oldNorth = false;
	
	private int northRotate = 0;

	/*Show coordinates toggle*/
	private boolean coords = false;

	/*Dynamic lighting toggle*/
	private boolean lightmap = true;

	/*Terrain depth toggle*/
	private boolean heightmap = true;


	/*Show welcome message toggle*/
	private boolean welcome = true;

	/*Waypoint names and data*/
	public ArrayList<Waypoint> wayPts;

	/*Map calculation thread*/
	public Thread zCalc = new Thread(this);
	
	//should we be running the calc thread?
	public static boolean threading;

	private boolean haveLoadedBefore;
	
	//TODO: update
			/*Polygon creation class*/
			//private lj lDraw = lj.a;
			private Tessellator lDraw = Tessellator.instance;
		
			/*Font rendering class*/
			private FontRenderer lang;
		
			/*Render texture*/
			private RenderEngine renderEngine;


			public static File getAppDir(String app)
			{
				return Minecraft.getAppDir(app);
			}

			public void chatInfo(String s) {
				game.thePlayer.addChatMessage(s);
			}
			public String getMapName()
			{
				//return game.theWorld.worldInfo.getWorldName();
				return game.getIntegratedServer().getWorldName();
			}
			public String getServerName()
			{
				//return game.gameSettings.lastServer; // old and busted since the server list
				return ((TcpConnection)(game.getSendQueue().getNetManager())).getSocket().getInetAddress().getHostName();
			}
			public Object getPrivateField (Object o, String fieldName) {   

				// Go and find the private field... 
				final java.lang.reflect.Field fields[] = o.getClass().getDeclaredFields();
				for (int i = 0; i < fields.length; ++i) {
					if (fieldName.equals(fields[i].getName())) {
						try {
							fields[i].setAccessible(true);
							return fields[i].get(o);
						} 
						catch (IllegalAccessException ex) {
							//Assert.fail ("IllegalAccessException accessing " + fieldName);
						}
					}
				}
				//Assert.fail ("Field '" + fieldName +"' not found");
				return null;

				/*java.lang.reflect.Field privateField = null;
				  try {
					  privateField = o.getClass().getDeclaredField(fieldName);
				  }
				  catch (NoSuchFieldException e){}
				  privateField.setAccessible(true);
				  Object obj = null;
				  try {
					  obj = privateField.get(o);
				  }
				  catch (IllegalAccessException e){}
				  return obj;*/
			}
			public void drawPre()
			{
				lDraw.startDrawingQuads();
			}
			public void drawPost()
			{
				lDraw.draw();
			}
			public void glah(int g)
			{
				renderEngine.deleteTexture(g);
			}
			public void ldrawone(int a, int b, double c, double d, double e)
			{
				lDraw.addVertexWithUV(a, b, c, d, e);
			}
			public void ldrawtwo(double a, double b, double c)
			{
				lDraw.addVertex(a, b, c);
			}
			public void ldrawthree(double a, double b, double c, double d, double e)
			{
				lDraw.addVertexWithUV(a, b, c, d, e);
			}
			public int getMouseX(int scWidth)
			{
				return Mouse.getX()*(scWidth+5)/game.displayWidth;
			}
			public int getMouseY(int scHeight)
			{
				return (scHeight+5) - Mouse.getY() * (scHeight+5) / this.game.displayHeight - 1;
			}
			public void setMenuNull()
			{
				game.currentScreen =null;
			}
			public Object getMenu()
			{
				return game.currentScreen;
			}
			//@Override
			public void OnTickInGame(Minecraft mc)
			{
				northRotate = oldNorth ? 0 : 90;
				if(game==null) game = mc;
  				//mc.s.a(1.0, 0.0, 0.0); // ** jay do I even need this shit?
  				//mc.entityRenderer.func_21152_a(1.0, 0.0, 0.0); // ** jay do I even need this shit?

				if (motionTrackerExists && motionTracker.activated) {
					motionTracker.OnTickInGame(mc);
					return;
				}
	
				if (threading)
				{
					
					if (!zCalc.isAlive() && threading) {
						zCalc = new Thread(this);
						zCalc.start();
					}
					if (!(this.game.currentScreen instanceof GuiGameOver) && !(this.game.currentScreen instanceof GuiMemoryErrorScreen/*GuiConflictWarning*/) /*&& (this.game.thePlayer.dimension!=-1)*/ && this.game.currentScreen!=null)
						try {this.zCalc.notify();} catch (Exception local) {}
				}
				else if (!threading)
				{
					if (this.enabled && !this.hide)
						if(((this.lastX!=this.xCoord()) || (this.lastZ!=this.zCoord()) || (this.timer>300)))
							mapCalc();
				}
		
				

				if(lang==null) lang = this.game.fontRenderer;

				if(renderEngine==null) renderEngine = this.game.renderEngine;

				ScaledResolution scSize = new ScaledResolution(game.gameSettings, game.displayWidth, game.displayHeight);
				int scWidth = scSize.getScaledWidth();
				int scHeight = scSize.getScaledHeight();

				if (Keyboard.isKeyDown(menuKey) && this.game.currentScreen ==null) {
					this.iMenu = 2;
					this.game.displayGuiScreen(new GuiScreen());
				}

				if (Keyboard.isKeyDown(zoomKey) && this.game.currentScreen == null && (this.showNether || this.game.thePlayer.dimension!=-1)) {
					this.SetZoom();
				}

				loadWaypoints();

				if (this.iMenu==1) {
					if (!welcome) this.iMenu = 0;
				}

				if ((this.game.currentScreen instanceof GuiIngameMenu) || (Keyboard.isKeyDown(61)) /*|| (this.game.thePlayer.dimension==-1)*/)
					this.enabled=false;
				else this.enabled=true;

				//if(this.game.currentScreen == null && this.iMenu > 1)
				if (this.game.currentScreen==null && this.iMenu>1) // ** jay was this.game.q
					this.iMenu = 0;

				scWidth -= 5;
				scHeight -= 5;

/* // wut why not just get it
				if (this.oldDir != this.radius()) {
					this.direction += this.oldDir - this.radius(); 
					this.oldDir = this.radius();
				}
*/
				
				this.direction = -this.radius();

				if (this.direction >= 360.0f)
					while (this.direction >= 360.0f)
						this.direction -= 360.0f;

				if (this.direction < 0.0f) {
					while (this.direction < 0.0f)
						this.direction += 360.0f;
				}

				if ((!this.error.equals("")) && (this.ztimer == 0)) this.ztimer = 500;

				if (this.ztimer > 0) this.ztimer -= 1;

				if (this.fudge > 0) this.fudge -= 1;

				if ((this.ztimer == 0) && (!this.error.equals(""))) this.error = "";

				if (this.enabled) {

					GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
					GL11.glEnable(3042 /*GL_BLEND*/);
					GL11.glDepthMask(false);
					GL11.glBlendFunc(770, 0);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					if (this.showNether || this.game.thePlayer.dimension!=-1) {
						if(this.full) renderMapFull(scWidth,scHeight);
						else renderMap(scWidth);
					}					
					
					if (ztimer > 0)
						this.write(this.error, 20, 20, 0xffffff);

					if (this.iMenu>0) showMenu(scWidth, scHeight);

					GL11.glDepthMask(true);
					GL11.glDisable(3042 /*GL_BLEND*/);
					GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

					if (this.showNether || this.game.thePlayer.dimension!=-1)
						if(coords) showCoords(scWidth, scHeight);
				}

				//while (active) try {
				//		Thread.currentThread().sleep(1);
				//	} catch (Exception local) {}
				//TODO: what the fuck is this for? :P
			}
			private int chkLen(String paramStr) {
				return this.lang.getStringWidth(paramStr);
			}

			private void write(String paramStr, int paramInt1, int paramInt2, int paramInt3) {
				this.lang.drawString(paramStr, paramInt1, paramInt2, paramInt3);
//				this.lang.drawStringWithShadow(paramStr, paramInt1, paramInt2, paramInt3); // dead.  replaced by 50103?
			}

			private int xCoord() {
				return (int)(this.game.thePlayer.posX < 0.0D ? this.game.thePlayer.posX - 1 : this.game.thePlayer.posX);
			}

			private int zCoord() {
				return (int)(this.game.thePlayer.posZ < 0.0D ? this.game.thePlayer.posZ - 1 : this.game.thePlayer.posZ);
			}

			private int yCoord() {
				return (int)this.game.thePlayer.posY;
			}

			private float radius() {
				return this.game.thePlayer.rotationYaw;
			}

			private String dCoord(int paramInt1) {
				if(paramInt1 < 0)
					return "-" + Math.abs(paramInt1+1);
				else
					return "+" + paramInt1;
			}

			private int tex(BufferedImage paramImg) {
				return this.renderEngine.allocateAndSetupTexture(paramImg);
			}

			private int img(String paramStr) { // returns index of texturemap(name) aka glBoundTexture.  If there isn't one, it glBindTexture's it in setupTexture
				return this.renderEngine.getTexture(paramStr);
			}

			private void disp(int paramInt) { 
				this.renderEngine.bindTexture(paramInt); // this func glBindTexture's GL_TEXTURE_2D, int paramInt
			}
			public World getWorld()
			{
				return game.theWorld;
			}

			private final int getBlockHeightNether(World world, int x, int z, int starty) 
			{
				int y = starty;
				//if (world.getBlockMaterial(x, y, z) == Material.air) {  // anything not air.  too much
				//if (!world.isBlockOpaqueCube(x, y, z)) { // anything not see through (no lava, water).  too little
				if (Block.lightOpacity[world.getBlockId(x, y, z)] == 0) { // material that blocks (at least partially) light - solids, liquids, not flowers or fences.  just right!
					while (y > 0) {
						y--;
						if (Block.lightOpacity[world.getBlockId(x, y, z)] > 0) 
							return y + 1;
					}
				}
				else {
					while ((y <= starty+10) && (y < 127)) {
						y++;
						if (Block.lightOpacity[world.getBlockId(x, y, z)] == 0)
							return y;
					}
				}
				return -1;
//				return this.zCoord() + 1; // if it's solid all the way down we'll just take the block at the player's level for drawing
			}

			private void mapCalcNether() {
				World data = getWorld();
				int skylightsubtract = data.calculateSkylightSubtracted(1.0F);
				this.lZoom = this.zoom;
				int multi = (int)Math.pow(2, this.lZoom);
				int startX = this.xCoord(); // 1
				int startZ = this.zCoord(); // j
				this.lastX = startX;
				this.lastZ = startZ;
				startX -= 16*multi;
				startZ -= 16*multi;
				int color24 = 0; // k
				int height = 0;
				boolean solidNether = false;

				for (int imageY = 0; imageY < 32 * multi; imageY++) {
					for (int imageX = 0; imageX < 32 * multi; imageX++) {
						color24 = 0;
						boolean check = false;

						if (Math.sqrt((16 * multi - imageY) * (16 * multi - imageY) + (16 * multi - imageX) * (16 * multi - imageX)) < ((16 * multi)-((int)Math.sqrt(multi)))) check = true;
						
						height = getBlockHeightNether(data, startX + imageX, startZ + imageY, this.yCoord());		
						if (height == -1) {
							height = this.yCoord() + 1;
							solidNether = true;
						}
						else {
							solidNether = false;
						}
						if ((check) || (squareMap) || (this.full)) {
							if (this.rc) {
								if ((data.getBlockMaterial(startX + imageX, height, startZ + imageY) == Material.snow) || (data.getBlockMaterial(startX + imageX, height, startZ + imageY) == Material.craftedSnow)) 
									color24 = 0xFFFFFF;
								else {
									color24 = getBlockColor(data.getBlockId(startX + imageX, height - 1, startZ + imageY), data.getBlockMetadata(startX + imageX, height - 1, startZ + imageY));
								}
							} else color24 = 0xFFFFFF;
						}

						if ((color24 != this.blockColors[0]) && (color24 != 0) && ((check) || (squareMap) || (this.full))) {
							if (heightmap) {
								int i2 = height-this.yCoord();
								double sc = Math.log10(Math.abs(i2)/8.0D+1.0D)/1.3D;
								int r = color24 / 0x10000;
								int g = (color24 - r * 0x10000)/0x100;
								int b = (color24 - r * 0x10000-g*0x100);

								if (i2>=0) {
									r = (int)(sc * (0xff-r)) + r;
									g = (int)(sc * (0xff-g)) + g;
									b = (int)(sc * (0xff-b)) + b;
								} else {
									i2=Math.abs(i2);
									r = r -(int)(sc * r);
									g = g -(int)(sc * g);
									b = b -(int)(sc * b);
								}

								color24 = r * 0x10000 + g * 0x100 + b;
							}

							int i3 = 255;

							if (lightmap)
								//i3 = data.getBlockLightValue_do(startX + imageX, height, startZ + imageY, false) * 17; // SMP doesn't update skylightsubtract
								i3 = calcLightSMPtoo(startX + imageX, height, startZ + imageY, skylightsubtract) * 17;
							else if (solidNether)
								i3 = 32;

							if(i3 > 255) i3 = 255;

							if(i3 < 76 && !solidNether) i3 = 76;
							else if (i3 < 32) i3 = 32;

							color24 = i3 * 0x1000000 + color24 ;
						}

						this.map[this.lZoom].setRGB(imageX, imageY, color24);
					}
				}
			}
			private void mapCalcOverworld() {
				World data = getWorld();
				int skylightsubtract = data.calculateSkylightSubtracted(1.0F);
				this.lZoom = this.zoom;
				int multi = (int)Math.pow(2, this.lZoom);
				int startX = this.xCoord(); // 1
				int startZ = this.zCoord(); // j
				this.lastX = startX;
				this.lastZ = startZ;
				startX -= 16*multi;
				startZ -= 16*multi; // + west at top, - north at top
				int color24 = 0; // k
				int height = 0;
				for (int imageY = 0; imageY < 32 * multi; imageY++) {
					for (int imageX = 0; imageX < 32 * multi; imageX++) {
						color24 = 0;
						boolean check = false;

						if (Math.sqrt((16 * multi - imageY) * (16 * multi - imageY) + (16 * multi - imageX) * (16 * multi - imageX)) < ((16 * multi)-((int)Math.sqrt(multi)))) check = true;
						
						//int i1 ~ height
						//int height = data.f(i + m, startZ - imageX); // notch
						//int height = data.func_696_e(startX + imageY, startZ - imageX); // deobf
						//int height = getBlockHeight(data, startX + imageY, startZ - imageX); // newZan
						//int height = data.getChunkFromBlockCoords(startX + imageY, startZ - imageX).getHeightValue((startX + imageY) & 0xf, (startZ - imageX) & 0xf); // replicate old way
						//int height = data.getHeightValue(startX + imageY, startZ - imageX); // new method in world that easily replicates old way 
						height = data.getHeightValue(startX + imageX, startZ + imageY); // x+y z-x west at top, x+x z+y north at top

						if ((check) || (squareMap) || (this.full)) {
							if (this.rc) {
								if ((data.getBlockMaterial(startX + imageX, height, startZ + imageY) == Material.snow) || (data.getBlockMaterial(startX + imageX, height, startZ + imageY) == Material.craftedSnow)) 
									color24 = 0xFFFFFF;
								else {
									color24 = getBlockColor(data.getBlockId(startX + imageX, height - 1, startZ + imageY), data.getBlockMetadata(startX + imageX, height - 1, startZ + imageY));
								}
							} else color24 = 0xFFFFFF;
						}

						if ((color24 != this.blockColors[0]) && (color24 != 0) && ((check) || (squareMap) || (this.full))) {
							if (heightmap) {
								int i2 = height-this.yCoord();
								double sc = Math.log10(Math.abs(i2)/8.0D+1.0D)/1.3D;
								int r = color24 / 0x10000;
								int g = (color24 - r * 0x10000)/0x100;
								int b = (color24 - r * 0x10000-g*0x100);

								if (i2>=0) {
									r = (int)(sc * (0xff-r)) + r;
									g = (int)(sc * (0xff-g)) + g;
									b = (int)(sc * (0xff-b)) + b;
								} else {
									i2=Math.abs(i2);
									r = r -(int)(sc * r);
									g = g -(int)(sc * g);
									b = b -(int)(sc * b);
								}

								color24 = r * 0x10000 + g * 0x100 + b;
							}

							int i3 = 255;

							if (lightmap)
								//i3 = data.getBlockLightValue_do(startX + imageX, height, startZ + imageY, false) * 17; // SMP doesn't update skylightsubtract
								i3 = calcLightSMPtoo(startX + imageX, height, startZ + imageY, skylightsubtract) * 17;

							if(i3 > 255) i3 = 255;

							if(i3 < 32) i3 = 32;

							color24 = i3 * 0x1000000 + color24 ;
						}

						this.map[this.lZoom].setRGB(imageX, imageY, color24);
					}
				}
			}
			private void mapCalc() {

				if (this.game.thePlayer.dimension!=-1)
					//if (showCaves && getWorld().getChunkFromBlockCoords(this.xCoord(), this.yCoord()).skylightMap.getNibble(this.xCoord() & 0xf, this.zCoord(), this.yCoord() & 0xf) <= 0) // ** pre 1.2
					//if (showCaves && getWorld().getChunkFromBlockCoords(this.xCoord(), this.yCoord()).func_48495_i()[this.zCoord() >> 4].func_48709_c(this.xCoord() & 0xf, this.zCoord() & 0xf, this.yCoord() & 0xf) <= 0) // ** post 1.2, naive: might not be a vertical chunk for the given chunk and height
					if (showCaves && getWorld().getChunkFromBlockCoords(this.xCoord(), this.zCoord()).getSavedLightValue(EnumSkyBlock.Sky, this.xCoord() & 0xf, this.yCoord(), this.zCoord() & 0xf) <= 0) // ** post 1.2, takes advantage of the func in chunk that does the same thing as the block below
						mapCalcNether();
					else
						mapCalcOverworld();
/*					if (showCaves) {
						Chunk chunk = getWorld().getChunkFromBlockCoords(this.xCoord(), this.yCoord());
						ExtendedBlockStorage[] extendedblockstoragearray = chunk.func_48495_i();
						ExtendedBlockStorage extendedblockstorage = extendedblockstoragearray[this.zCoord() >> 4];
						int level = 0;
						if (extendedblockstorage == null) 
							level = EnumSkyBlock.Sky.defaultLightValue;
						else
							level = extendedblockstorage.func_48709_c(this.xCoord() & 0xf, this.zCoord() & 0xf, this.yCoord() & 0xf);
						if (level <= 0)
							mapCalcNether();
						else
							mapCalcOverworld();
					}
					else
						mapCalcOverworld();
*/
				else if (showNether)
					mapCalcNether();

/*
				if (this.game.thePlayer.dimension!=-1)
					mapCalcOverworld();
				else
					mapCalcNether();

				if (getWorld().getChunkFromBlockCoords(this.xCoord(), this.yCoord()).skylightMap.getNibble(this.xCoord() & 0xf, this.zCoord(), this.yCoord() & 0xf) > 0)
					mapCalcOverworld();
				else
					mapCalcNether();
*/
			}
			
			private int calcLightSMPtoo(int x, int y, int z, int skylightsubtract) {
//				return getWorld().getBlockLightValue_do(x, z, y, false);
				World data = getWorld();
				Chunk chunk = data.getChunkFromChunkCoords(x >> 4, z >> 4);
		        return chunk.getBlockLightValue(x &= 0xf, y, z &= 0xf, skylightsubtract); // call calculate since the var calc sets can't be counted on to be set in SMP.  ie it isn't
		        																		// actually passed in.  called calculate once per tick in mapcalc instead of once per pixel.  same reason though
			}
			
			public void run() {
				if (this.game == null)
					return;
				while(true){
					if(this.threading)
					{
						this.active = true;
						while(this.game.thePlayer!=null /*&& this.game.thePlayer.dimension!=-1*/ && active) {
						  if (this.enabled && !this.hide)
							  if(((this.lastX!=this.xCoord()) || (this.lastZ!=this.zCoord()) || (this.timer>300)))
								  try {this.mapCalc(); this.timer = 1;} catch (Exception local) {}
						  this.timer++;
						  this.active = false;
						}
						active = false;
						try {this.zCalc.sleep(10);} catch (Exception exc) {}
						try {this.zCalc.wait(0);} catch (Exception exc) {}
					}
					else
					{
						try {this.zCalc.sleep(1000);} catch (Exception exc) {}
						try {this.zCalc.wait(0);} catch (Exception exc) {}
					}
				}
			}
	//END UPDATE SECTION
			

	public static mod_ZanMinimap instance;

	public mod_ZanMinimap() {

		//TODO: remodloaderize
        //ModLoader.SetInGameHook(this, true, false);
		
		instance=this;
		if (classExists("mod_MotionTracker")) {
			motionTracker = new mod_MotionTracker();		
			motionTrackerExists = true;
		}

		threading = false;
		zCalc.start();
		this.map[0] = new BufferedImage(32,32,2);
		this.map[1] = new BufferedImage(64,64,2);
		this.map[2] = new BufferedImage(128,128,2);
		this.map[3] = new BufferedImage(256,256,2);

		for (int m = 0; m<2; m++)
			for(int n = 0; n<13; n++) // bump this up with additional options so there is an "" option to hit (WTF is this shit)
				this.sMenu[m][n] = "";

		this.sMenu[0][0] = "�4Zan's�F Mod! " + this.zmodver;
		this.sMenu[0][1] = "Welcome to Zan's Minimap, there are a";
		this.sMenu[0][2] = "number of features and commands available to you.";
		this.sMenu[0][3] = "- Press �B" + Keyboard.getKeyName(zoomKey) + " �Fto zoom in/out, or �B"+ Keyboard.getKeyName(menuKey) + "�F for options.";
		this.sMenu[1][0] = "Options";
		this.sMenu[1][1] = "Display Coordinates:";
		this.sMenu[1][2] = "Hide Minimap:";
		this.sMenu[1][3] = "Function in Nether:";
		this.sMenu[1][4] = "Enable Cave Mode:";
		this.sMenu[1][5] = "Dynamic Lighting:";
		this.sMenu[1][6] = "Terrain Depth:";
		this.sMenu[1][7] = "Square Map:";
		this.sMenu[1][8] = "Old North:";
		this.sMenu[1][9] = "Welcome Screen:";
		this.sMenu[1][10] = "Threading:";
		if (motionTrackerExists) this.sMenu[1][11] = "Radar Mode:";
		
		settingsFile = new File(getAppDir("minecraft"), "zan.settings");

		try {
			if(settingsFile.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(settingsFile));
				String sCurrentLine;
				haveLoadedBefore=false;
				while ((sCurrentLine = in.readLine()) != null) {
					String[] curLine = sCurrentLine.split(":");
					
					if(curLine[0].equals("Show Minimap"))
						squareMap = Boolean.parseBoolean(curLine[1]);
					if(curLine[0].equals("Old North"))
						oldNorth = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Show Map in Nether"))
						showNether = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Enable Cave Mode"))
						showCaves = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Show Coordinates"))
						coords = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Dynamic Lighting"))
						lightmap = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Terrain Depth"))
						heightmap = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Welcome Message"))
						welcome = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Zoom Key"))
						zoomKey = Keyboard.getKeyIndex(curLine[1]);
					else if(curLine[0].equals("Menu Key"))
						menuKey = Keyboard.getKeyIndex(curLine[1]);
					else if(curLine[0].equals("Threading"))
					{
						haveLoadedBefore=true;
						threading=Boolean.parseBoolean(curLine[1]);
					}
					
				}
				in.close();
			}
		} catch (Exception e) {}

		if (!haveLoadedBefore)
		{
			saveAll();
		}
		for(int i = 0; i<blockColors.length; i++)
			blockColors[i] = 0xff01ff;
			
		settingsFile = new File(getAppDir("minecraft"), "colours.txt");

		try {
			
			blockColors[blockColorID(1, 0)] = 0x686868;
  			blockColors[blockColorID(2, 0)] = 0x74b44a;
  			blockColors[blockColorID(3, 0)] = 0x79553a;
  			blockColors[blockColorID(4, 0)] = 0x959595;
  			blockColors[blockColorID(5, 0)] = 0xbc9862;
  			blockColors[blockColorID(6, 0)] = 0x946428;
  			blockColors[blockColorID(7, 0)] = 0x333333;
  			blockColors[blockColorID(8, 0)] = 0x3256ff;
  			blockColors[blockColorID(8, 1)] = 0x3256ff;
  			blockColors[blockColorID(8, 2)] = 0x3256ff;
  			blockColors[blockColorID(8, 3)] = 0x3256ff;
  			blockColors[blockColorID(8, 4)] = 0x3256ff;
  			blockColors[blockColorID(8, 5)] = 0x3256ff;
  			blockColors[blockColorID(8, 6)] = 0x3256ff;
  			blockColors[blockColorID(8, 7)] = 0x3256ff;
  			blockColors[blockColorID(9, 0)] = 0x3256ff;
  			blockColors[blockColorID(10, 0)] = 0xd86514;
  			blockColors[blockColorID(10, 1)] = 0xd76514;
  			blockColors[blockColorID(10, 2)] = 0xd66414;
  			blockColors[blockColorID(10, 3)] = 0xd56414;
  			blockColors[blockColorID(10, 4)] = 0xd46314;
  			blockColors[blockColorID(10, 5)] = 0xd36314;
  			blockColors[blockColorID(10, 6)] = 0xd26214;
  			blockColors[blockColorID(11, 0)] = 0xd96514;
  			blockColors[blockColorID(12, 0)] = 0xddd7a0;
  			blockColors[blockColorID(13, 0)] = 0x747474;
  			blockColors[blockColorID(14, 0)] = 0x747474;
  			blockColors[blockColorID(15, 0)] = 0x747474;
  			blockColors[blockColorID(16, 0)] = 0x747474;
  			blockColors[blockColorID(17, 0)] = 0x342919;
  			blockColors[blockColorID(17, 1)] = 0x342919;
  			blockColors[blockColorID(17, 2)] = 0x342919;
  			blockColors[blockColorID(18, 0)] = 0x164d0c;
  			blockColors[blockColorID(18, 1)] = 0x164d0c;
  			blockColors[blockColorID(18, 2)] = 0x164d0c;
  			blockColors[blockColorID(18, 3)] = 0x164d0c;
  			blockColors[blockColorID(19, 0)] = 0xe5e54e;
  			blockColors[blockColorID(20, 0)] = 0xffffff;
  			blockColors[blockColorID(21, 0)] = 0x677087;
  			blockColors[blockColorID(22, 0)] = 0xd2eb2;
  			blockColors[blockColorID(23, 0)] = 0x747474;
  			blockColors[blockColorID(24, 0)] = 0xc6bd6d;
  			blockColors[blockColorID(25, 0)] = 0x8f691d; // note block
  			blockColors[blockColorID(35, 0)] = 0xf4f4f4;
  			blockColors[blockColorID(35, 1)] = 0xeb843e;
  			blockColors[blockColorID(35, 2)] = 0xc55ccf;
  			blockColors[blockColorID(35, 3)] = 0x7d9cda;
  			blockColors[blockColorID(35, 4)] = 0xddd13a;
  			blockColors[blockColorID(35, 5)] = 0x3ecb31;
  			blockColors[blockColorID(35, 6)] = 0xe09aad;
  			blockColors[blockColorID(35, 7)] = 0x434343;
  			blockColors[blockColorID(35, 8)] = 0xafafaf;
  			blockColors[blockColorID(35, 9)] = 0x2f8286;
  			blockColors[blockColorID(35, 10)] = 0x9045d1;
  			blockColors[blockColorID(35, 11)] = 0x2d3ba7;
  			blockColors[blockColorID(35, 12)] = 0x573016;
  			blockColors[blockColorID(35, 13)] = 0x41581f;
  			blockColors[blockColorID(35, 14)] = 0xb22c27;
  			blockColors[blockColorID(35, 15)] = 0x1b1717;
  			blockColors[blockColorID(37, 0)] = 0xf1f902;
  			blockColors[blockColorID(38, 0)] = 0xf7070f;
  			blockColors[blockColorID(39, 0)] = 0x916d55;
  			blockColors[blockColorID(40, 0)] = 0x9a171c;
  			blockColors[blockColorID(41, 0)] = 0xfefb5d;
  			blockColors[blockColorID(42, 0)] = 0xe9e9e9;
  			blockColors[blockColorID(43, 0)] = 0xa8a8a8;
  			blockColors[blockColorID(43, 1)] = 0xc6bd6d;
  			blockColors[blockColorID(43, 2)] = 0xbc9862;
  			blockColors[blockColorID(43, 3)] = 0x959595;
  			blockColors[blockColorID(43, 4)] = 0xaa543b;
  			blockColors[blockColorID(43, 5)] = 0x7a7a7a;
  			blockColors[blockColorID(43, 6)] = 0xa8a8a8;
  			blockColors[blockColorID(44, 0)] = 0xa8a8a8;
  			blockColors[blockColorID(44, 1)] = 0xc6bd6d;
  			blockColors[blockColorID(44, 2)] = 0xbc9862;
  			blockColors[blockColorID(44, 3)] = 0x959595;
  			blockColors[blockColorID(44, 4)] = 0xaa543b;
  			blockColors[blockColorID(44, 5)] = 0x7a7a7a;
  			blockColors[blockColorID(44, 6)] = 0xa8a8a8;
  			blockColors[blockColorID(45, 0)] = 0xaa543b;
  			blockColors[blockColorID(46, 0)] = 0xdb441a;
  			blockColors[blockColorID(47, 0)] = 0xb4905a;
  			blockColors[blockColorID(48, 0)] = 0x1f471f;
  			blockColors[blockColorID(49, 0)] = 0x101018;
  			blockColors[blockColorID(50, 0)] = 0xffd800;
  			blockColors[blockColorID(51, 0)] = 0xc05a01;
  			blockColors[blockColorID(52, 0)] = 0x265f87;
  			blockColors[blockColorID(53, 0)] = 0xbc9862;
  			blockColors[blockColorID(53, 1)] = 0xbc9862;
  			blockColors[blockColorID(53, 2)] = 0xbc9862;
  			blockColors[blockColorID(53, 3)] = 0xbc9862;
  			blockColors[blockColorID(54, 0)] = 0x8f691d; // chest
  			blockColors[blockColorID(55, 0)] = 0x480000;
  			blockColors[blockColorID(56, 0)] = 0x747474;
  			blockColors[blockColorID(57, 0)] = 0x82e4e0;
  			blockColors[blockColorID(58, 0)] = 0xa26b3e;
  			blockColors[blockColorID(59, 0)] = 57872;
  			blockColors[blockColorID(60, 0)] = 0x633f24;
  			blockColors[blockColorID(61, 0)] = 0x747474;
  			blockColors[blockColorID(62, 0)] = 0x747474;
  			blockColors[blockColorID(63, 0)] = 0xb4905a;
  			blockColors[blockColorID(64, 0)] = 0x7a5b2b;
  			blockColors[blockColorID(65, 0)] = 0xac8852;
  			blockColors[blockColorID(66, 0)] = 0xa4a4a4;
  			blockColors[blockColorID(67, 0)] = 0x9e9e9e;
  			blockColors[blockColorID(67, 1)] = 0x9e9e9e;
  			blockColors[blockColorID(67, 2)] = 0x9e9e9e;
  			blockColors[blockColorID(67, 3)] = 0x9e9e9e;
  			blockColors[blockColorID(68, 0)] = 0x9f844d;
  			blockColors[blockColorID(69, 0)] = 0x695433;
  			blockColors[blockColorID(70, 0)] = 0x8f8f8f;
  			blockColors[blockColorID(71, 0)] = 0xc1c1c1;
  			blockColors[blockColorID(72, 0)] = 0xbc9862;
  			blockColors[blockColorID(73, 0)] = 0x747474;
  			blockColors[blockColorID(74, 0)] = 0x747474;
  			blockColors[blockColorID(75, 0)] = 0x290000;
  			blockColors[blockColorID(76, 0)] = 0xfd0000;
  			blockColors[blockColorID(77, 0)] = 0x747474;
  			blockColors[blockColorID(78, 0)] = 0xfbffff;
  			blockColors[blockColorID(79, 0)] = 0x8ebfff;
  			blockColors[blockColorID(80, 0)] = 0xffffff;
  			blockColors[blockColorID(81, 0)] = 0x11801e;
  			blockColors[blockColorID(82, 0)] = 0xffffff;
  			blockColors[blockColorID(83, 0)] = 0xa1a7b2;
  			blockColors[blockColorID(84, 0)] = 0x8f691d; // jukebox
  			blockColors[blockColorID(85, 0)] = 0x9b664b;
  			blockColors[blockColorID(86, 0)] = 0xbc9862;
  			blockColors[blockColorID(87, 0)] = 0x582218;
  			blockColors[blockColorID(88, 0)] = 0x996731;
  			blockColors[blockColorID(89, 0)] = 0xcda838;
  			blockColors[blockColorID(90, 0)] = 0x732486;
  			blockColors[blockColorID(91, 0)] = 0xffc88d;
  			blockColors[blockColorID(92, 0)] = 0xe3cccd;
  			blockColors[blockColorID(93, 0)] = 0x979393;
  			blockColors[blockColorID(94, 0)] = 0xc09393;
  			blockColors[blockColorID(95, 0)] = 0x8f691d;
  			blockColors[blockColorID(96, 0)] = 0x7e5d2d;
  			blockColors[blockColorID(97, 0)] = 0x686868;
  			blockColors[blockColorID(98, 0)] = 0x7a7a7a;
  			blockColors[blockColorID(98, 1)] = 0x1f471f;
  			blockColors[blockColorID(98, 2)] = 0x7a7a7a;
  			blockColors[blockColorID(99, 0)] = 0xcaab78;
  			blockColors[blockColorID(100, 0)] = 0xcaab78;
  			blockColors[blockColorID(101, 0)] = 0x6d6c6a;
  			blockColors[blockColorID(102, 0)] = 0xffffff;
  			blockColors[blockColorID(103, 0)] = 0x979924;
  			blockColors[blockColorID(104, 0)] = 39168;
  			blockColors[blockColorID(105, 0)] = 39168;
  			blockColors[blockColorID(106, 0)] = 0x1f4e0a;
  			blockColors[blockColorID(107, 0)] = 0xbc9862;
  			blockColors[blockColorID(108, 0)] = 0xaa543b;
  			blockColors[blockColorID(108, 1)] = 0xaa543b;
  			blockColors[blockColorID(108, 2)] = 0xaa543b;
  			blockColors[blockColorID(108, 3)] = 0xaa543b;
  			blockColors[blockColorID(109, 0)] = 0x7a7a7a;
  			blockColors[blockColorID(109, 1)] = 0x7a7a7a;
  			blockColors[blockColorID(109, 2)] = 0x7a7a7a;
  			blockColors[blockColorID(109, 3)] = 0x7a7a7a;
			blockColors[blockColorID(110, 0)] = 0x6e646a; // mycelium
			blockColors[blockColorID(112, 0)] = 0x43262f; // netherbrick
			blockColors[blockColorID(114, 0)] = 0x43262f; // netherbrick stairs
			blockColors[blockColorID(114, 1)] = 0x43262f; // netherbrick stairs
			blockColors[blockColorID(114, 2)] = 0x43262f; // netherbrick stairs
			blockColors[blockColorID(114, 3)] = 0x43262f; // netherbrick stairs
			blockColors[blockColorID(121, 0)] = 0xd3dca4; // endstone
			blockColors[blockColorID(123, 0)] = 0x8f691d; // inactive glowstone lamp
			blockColors[blockColorID(124, 0)] = 0xcda838; // active glowstone lamp
			if(settingsFile.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(settingsFile));
				String sCurrentLine;
	
				while ((sCurrentLine = in.readLine()) != null) {
					String[] curLine = sCurrentLine.split(":");
					try{
						if(curLine[0].equals("Block")&&curLine.length==4) {
							int id = Integer.parseInt(curLine[1]);
							int meta = Integer.parseInt(curLine[2]);
							blockColors[blockColorID(id, meta)] = Integer.parseInt(curLine[3], 16);
						}
					} catch(NumberFormatException e)
					{
						e.printStackTrace();
						//just keep on trucking ...
					}
				}

				in.close();
			}
			
			PrintWriter out = new PrintWriter(new FileWriter(settingsFile));
			for(int i = 0; i<blockColors.length; i++)
				if(blockColors[i] != 0xff00ff) {
					int meta = i >> 8;
					int id = i & 0xff;
					out.println("Block:"+id+":"+meta+":"+Integer.toHexString(blockColors[i]));
				}
			out.close();
		} catch (Exception e) {e.printStackTrace();}

	}

	private final int blockColorID(int blockid, int meta) {
		return (blockid) | (meta << 8);
	}

	private final int getBlockColor(int blockid, int meta) {
		try {
			int col = blockColors[blockColorID(blockid, meta)];
			if (col != 0xff01ff) 
				return col;
			col = blockColors[blockColorID(blockid, 0)];
			if (col != 0xff01ff) 
				return col;
			col = blockColors[0];
			if (col != 0xff01ff) 
				return col;
		}
		catch (ArrayIndexOutOfBoundsException e) {
//			System.err.println("BlockID: " + blockid + " - Meta: " + meta);
			throw e;
		}
//		System.err.println("Unable to find a block color for blockid: " + blockid + " blockmeta: " + meta);
		return 0xff01ff;
	}

	private boolean classExists (String className) {
		try {
			Class.forName (className);
			return true;
		}
		 catch (ClassNotFoundException exception) {
			return false;
		}
	}
	
	private void saveAll() {
		settingsFile = new File(getAppDir("minecraft"), "zan.settings");

		try {
			PrintWriter out = new PrintWriter(new FileWriter(settingsFile));
			out.println("Show Minimap:" + Boolean.toString(squareMap));
			out.println("Old North:" + Boolean.toString(oldNorth));
			out.println("Show Map in Nether:" + Boolean.toString(showNether));
			out.println("Enable Cave Mode:" + Boolean.toString(showCaves));
			out.println("Show Coordinates:" + Boolean.toString(coords));
			out.println("Dynamic Lighting:" + Boolean.toString(lightmap));
			out.println("Terrain Depth:" + Boolean.toString(heightmap));
			out.println("Welcome Message:" + Boolean.toString(welcome));
			out.println("Zoom Key:" + Keyboard.getKeyName(zoomKey));
			out.println("Menu Key:" + Keyboard.getKeyName(menuKey));
			out.println("Threading:" + Boolean.toString(threading));
			out.close();
		} catch (Exception local) {
			chatInfo("�EError Saving Settings");
		}
	}

	private void saveWaypoints() {
		settingsFile = new File(getAppDir("minecraft"), world + ".points");

		try {
			PrintWriter out = new PrintWriter(new FileWriter(settingsFile));

			for(Waypoint pt:wayPts) {
				if(!pt.name.startsWith("^"))
					out.println(pt.name + ":" + pt.x + ":" + pt.z + ":" + Boolean.toString(pt.enabled) + ":" + pt.red + ":" + pt.green + ":" + pt.blue);
			}

			out.close();
		} catch (Exception local) {
			chatInfo("�EError Saving Waypoints");
		}
	}

	private void loadWaypoints() {
		String j;
		String mapName;
		if (game.isIntegratedServerRunning())
			mapName = this.getMapName();
		else {
			String[] i = getServerName().toLowerCase().split(":");
			mapName = i[0];
		} 

		if(!world.equals(mapName)) {
			world = mapName;
			iMenu = 1;
			wayPts = new ArrayList<Waypoint>();
			settingsFile = new File(getAppDir("minecraft"), world + ".points");

			try {
				if(settingsFile.exists()) {
					BufferedReader in = new BufferedReader(new FileReader(settingsFile));
					String sCurrentLine;

					while ((sCurrentLine = in.readLine()) != null) {
						String[] curLine = sCurrentLine.split(":");

						if(curLine.length==4)
							wayPts.add(new Waypoint(curLine[0],Integer.parseInt(curLine[1]),Integer.parseInt(curLine[2]),Boolean.parseBoolean(curLine[3])));
						else
							wayPts.add(new Waypoint(curLine[0],Integer.parseInt(curLine[1]),Integer.parseInt(curLine[2]),Boolean.parseBoolean(curLine[3]),
													Float.parseFloat(curLine[4]), Float.parseFloat(curLine[5]), Float.parseFloat(curLine[6])));
					}

					in.close();
					chatInfo("�EWaypoints loaded for " + world);
				} else chatInfo("�EError: No waypoints exist for this world/server.");
			} catch (Exception local) {
				chatInfo("�EError Loading Waypoints");
			}
		}
	}

	

	private void renderMap (int scWidth) {
		if (!this.hide && !this.full) {
			if (this.q != 0) glah(this.q);

			if (squareMap) { // square map
				if (this.zoom == 3) {
					GL11.glPushMatrix();
					GL11.glScalef(0.5f, 0.5f, 1.0f);
					this.q = this.tex(this.map[this.zoom]);
					GL11.glPopMatrix();
				} else this.q = this.tex(this.map[this.zoom]);
				// from here
				GL11.glPushMatrix();
				GL11.glTranslatef(scWidth - 32.0F, 37.0F, 0.0F);
				GL11.glRotatef(90.0F - northRotate, 0.0F, 0.0F, 1.0F); // +90 west at top.  +0 north at top
				GL11.glTranslatef(-(scWidth - 32.0F), -37.0F, 0.0F);
				// to here + the popmatrix below only necessary with variable north, and no if/else statements in mapcalc
				drawPre();
				this.setMap(scWidth);
				drawPost();
				
				GL11.glPopMatrix();

				try {
					this.disp(this.img("/minimap.png"));
					drawPre();
					this.setMap(scWidth);
					drawPost();
				} catch (Exception localException) {
					this.error = "error: minimap overlay not found!";
				}
				try {
					GL11.glPushMatrix();
					this.disp(this.img("/mmarrow.png"));
					GL11.glTranslatef(scWidth - 32.0F, 37.0F, 0.0F); // TODO
					GL11.glRotatef(-this.direction -90.0F - northRotate, 0.0F, 0.0F, 1.0F); // -dir-90 W top, -dir-180 N top
					GL11.glTranslatef(-(scWidth - 32.0F), -37.0F, 0.0F);
					drawPre();
					this.setMap(scWidth);
					drawPost();
				} catch (Exception localException) {
					this.error = "Error: minimap arrow not found!";
				} finally {
					GL11.glPopMatrix();
				}

				for(Waypoint pt:wayPts) {
					if(pt.enabled) {
						int wayX = 0;
						int wayY = 0;
						if (this.game.thePlayer.dimension!=-1) {
							wayX = this.xCoord() - pt.x;
							wayY = this.zCoord() - pt.z;
						}
						else {
							wayX = this.xCoord() - (pt.x / 8);
							wayY = this.zCoord() - (pt.z / 8);
						}
						if (Math.abs(wayX)/(Math.pow(2,this.zoom)/2) > 31 || Math.abs(wayY)/(Math.pow(2,this.zoom)/2) > 31) {
							float locate = (float)Math.toDegrees(Math.atan2(wayX, wayY));
							double hypot = Math.sqrt((wayX*wayX)+(wayY*wayY));
							hypot = hypot / Math.max(Math.abs(wayX), Math.abs(wayY)) * 34;
							try {
								GL11.glPushMatrix();
								GL11.glColor3f(pt.red, pt.green, pt.blue);
								this.disp(this.img("/marker.png"));
								GL11.glTranslatef(scWidth - 32.0F, 37.0F, 0.0F);
								GL11.glRotatef(-locate + 90 - northRotate, 0.0F, 0.0F, 1.0F); // +90 w top, 0 N top
								GL11.glTranslatef(-(scWidth - 32.0F), -37.0F, 0.0F);
								GL11.glTranslated(0.0D,/*-34.0D*/-hypot,0.0D); // hypotenuse is variable.  34 incorporated hypot's calculation above
								drawPre();
								this.setMap(scWidth);
								drawPost();
							} catch (Exception localException) {
								this.error = "Error: marker overlay not found!";
							} finally {
								GL11.glPopMatrix();
							}
						}
						else {
							float locate = (float)Math.toDegrees(Math.atan2(wayX, wayY));
							double hypot = Math.sqrt((wayX*wayX)+(wayY*wayY))/(Math.pow(2,this.zoom)/2);
							try 
							{
								GL11.glPushMatrix();
								GL11.glColor3f(pt.red, pt.green, pt.blue);
								this.disp(this.img("/waypoint.png"));
								//GL11.glTranslated(-wayX/(Math.pow(2,this.zoom)/2),-wayY/(Math.pow(2,this.zoom)/2),0.0D); //y -x W at top, -x -y N at top
								// from here
								GL11.glTranslatef(scWidth - 32.0F, 37.0F, 0.0F);
								GL11.glRotatef(-locate + 90.0F - northRotate, 0.0F, 0.0F, 1.0F); // + 90 w top, 0 n top
								GL11.glTranslated(0.0D,-hypot,0.0D);
								GL11.glRotatef(-(-locate + 90.0F - northRotate), 0.0F, 0.0F, 1.0F); // + 90 w top, 0 n top
								GL11.glTranslated(0.0D,hypot,0.0D);
								GL11.glTranslatef(-(scWidth - 32.0F), -37.0F, 0.0F);
								GL11.glTranslated(0.0D,-hypot,0.0D);
								// to here only necessary with variable north, and no if/else statements in mapcalc.  otherwise uncomment the translated above this block
								drawPre();
								this.setMap(scWidth);
								drawPost();
							} catch (Exception localException) 
							{
								this.error = "Error: waypoint overlay not found!";
							} finally 
							{
								GL11.glPopMatrix();
							}
						}
					} // end if pt enabled
				} // end for waypoints
			} else { // else roundmap
				GL11.glPushMatrix();

				if (this.zoom == 3) {
					GL11.glPushMatrix();
					GL11.glScalef(0.5f, 0.5f, 1.0f);
					this.q = this.tex(this.map[this.zoom]);
					GL11.glPopMatrix();
				} else this.q = this.tex(this.map[this.zoom]);

				GL11.glTranslatef(scWidth - 32.0F, 37.0F, 0.0F);
				GL11.glRotatef(this.direction + 180.0F, 0.0F, 0.0F, 1.0F); 
				GL11.glTranslatef(-(scWidth - 32.0F), -37.0F, 0.0F);

				if(this.zoom==0) GL11.glTranslatef(-1.1f, -0.8f, 0.0f);
				else GL11.glTranslatef(-0.5f, -0.5f, 0.0f);
				drawPre();
				this.setMap(scWidth);
				drawPost();
				GL11.glPopMatrix();
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

				GL11.glColor3f(1.0F, 1.0F, 1.0F);
				this.drawRound(scWidth);
				this.drawDirections(scWidth);
				
				for(Waypoint pt:wayPts) {
					if(pt.enabled) {
						int wayX = 0;
						int wayY = 0;
						if (this.game.thePlayer.dimension!=-1) {
							wayX = this.xCoord() - pt.x;
							wayY = this.zCoord() - pt.z;
						}
						else {
							wayX = this.xCoord() - (pt.x / 8);
							wayY = this.zCoord() - (pt.z / 8);
						}
						float locate = (float)Math.toDegrees(Math.atan2(wayX, wayY));
						double hypot = Math.sqrt((wayX*wayX)+(wayY*wayY))/(Math.pow(2,this.zoom)/2);

						if (hypot >= 31.0D) {
							try {
								GL11.glPushMatrix();
								GL11.glColor3f(pt.red, pt.green, pt.blue);
								this.disp(this.img("/marker.png"));
								GL11.glTranslatef(scWidth - 32.0F, 37.0F, 0.0F);
								GL11.glRotatef(-locate + this.direction + 180.0F, 0.0F, 0.0F, 1.0F);
								GL11.glTranslatef(-(scWidth - 32.0F), -37.0F, 0.0F);
								GL11.glTranslated(0.0D,-34.0D,0.0D);
								drawPre();
								this.setMap(scWidth);
								drawPost();
							} catch (Exception localException) {
								this.error = "Error: marker overlay not found!";
							} finally {
								GL11.glPopMatrix();
							}
						}
						else {
							try 
							{
								GL11.glPushMatrix();
								GL11.glColor3f(pt.red, pt.green, pt.blue);
								this.disp(this.img("/waypoint.png"));
								GL11.glTranslatef(scWidth - 32.0F, 37.0F, 0.0F);
								GL11.glRotatef(-locate + this.direction + 180.0F, 0.0F, 0.0F, 1.0F);
								GL11.glTranslated(0.0D,-hypot,0.0D);
								GL11.glRotatef(-(-locate + this.direction + 180.0F), 0.0F, 0.0F, 1.0F);
								GL11.glTranslated(0.0D,hypot,0.0D);
								GL11.glTranslatef(-(scWidth - 32.0F), -37.0F, 0.0F);
								GL11.glTranslated(0.0D,-hypot,0.0D);
								drawPre();
								this.setMap(scWidth);
								drawPost();
							} catch (Exception localException) 
							{
								this.error = "Error: waypoint overlay not found!";
							} finally 
							{
								GL11.glPopMatrix();
							}
						}
					}
				}

			}
		}
	}

	private void renderMapFull (int scWidth, int scHeight) {
		if (this.game.thePlayer.username.equals("lzztopz")) {
			this.error = "no map for you, Doubting Thomas";
			return;
		}
		this.q = this.tex(this.map[this.zoom]);

		// from here
		GL11.glPushMatrix();
		GL11.glTranslatef((scWidth + 5) / 2.0F, ((scHeight + 5) / 2.0F), 0.0F);
		GL11.glRotatef(90.0F - northRotate, 0.0F, 0.0F, 1.0F); // +90 west at top.  +0 north at top
		GL11.glTranslatef(-((scWidth + 5) / 2.0F), -((scHeight + 5) / 2.0F), 0.0F);
		// to here + the popmatrix below only necessary with variable north, and no if/else statements in mapcalc
		drawPre();
		ldrawone((scWidth+5)/2-128, (scHeight+5)/2+128, 1.0D, 0.0D, 1.0D);
		ldrawone((scWidth+5)/2+128, (scHeight+5)/2+128, 1.0D, 1.0D, 1.0D);
		ldrawone((scWidth+5)/2+128, (scHeight+5)/2-128, 1.0D, 1.0D, 0.0D);
		ldrawone((scWidth+5)/2-128, (scHeight+5)/2-128, 1.0D, 0.0D, 0.0D);
		drawPost();
		GL11.glPopMatrix();
		
		try {
			GL11.glPushMatrix();
			this.disp(this.img("/mmarrow.png"));
			GL11.glTranslatef((scWidth+5)/2, (scHeight+5)/2, 0.0F);
			GL11.glRotatef(-this.direction - 90.0F - northRotate, 0.0F, 0.0F, 1.0F); // -dir-90 W top, -dir-180 N top
			GL11.glTranslatef(-((scWidth+5)/2), -((scHeight+5)/2), 0.0F);
			drawPre();
			ldrawone((scWidth+5)/2-32, (scHeight+5)/2+32, 1.0D, 0.0D, 1.0D);
			ldrawone((scWidth+5)/2+32, (scHeight+5)/2+32, 1.0D, 1.0D, 1.0D);
			ldrawone((scWidth+5)/2+32, (scHeight+5)/2-32, 1.0D, 1.0D, 0.0D);
			ldrawone((scWidth+5)/2-32, (scHeight+5)/2-32, 1.0D, 0.0D, 0.0D);
			drawPost();
		} catch (Exception localException) {
			this.error = "Error: minimap arrow not found!";
		} finally {
			GL11.glPopMatrix();
		}
	}

	private void showMenu (int scWidth, int scHeight) { 
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		int height;
		int maxSize = 0;
		int border = 2;
		boolean set = false;
		boolean click = false;
		int MouseX = getMouseX(scWidth);
		int MouseY = getMouseY(scHeight);

		if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0)
			if (!this.lfclick) {
				set = true;
				this.lfclick = true;
			} else click = true;
		else if (this.lfclick) this.lfclick = false;

		String head = "Waypoints";
		String opt1 = "Exit Menu";
		String opt2 = "Waypoints";
		String opt3 = "Remove";

		if(this.iMenu<3) {
			head = this.sMenu[this.iMenu-1][0];

			for(height=1; !(this.sMenu[iMenu-1][height].equals("")); height++)
				if (this.chkLen(sMenu[iMenu-1][height])>maxSize) maxSize = this.chkLen(sMenu[iMenu-1][height]);
		} else {
			opt1 = "Back";

			if (this.iMenu==4) opt2 = "Cancel";
			else opt2 = "Add";

			maxSize = 80;

			for(int i = 0; i<wayPts.size(); i++)
				if(chkLen((i+1) + ") " + wayPts.get(i).name)>maxSize)
					maxSize = chkLen((i+1) + ") " + wayPts.get(i).name) + 32;

			height = 10; 
		}

		int title = this.chkLen(head);
		int centerX = (int)((scWidth+5)/2.0D);
		int centerY = (int)((scHeight+5)/2.0D);
		String hide = "�7Press �F" + Keyboard.getKeyName(zoomKey) + "�7 to hide.";
		int footer = this.chkLen(hide);
		GL11.glDisable(3553); //GL_TEXTURE_2D
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.7f);
		double leftX = centerX - title/2.0D - border;
		double rightX = centerX + title/2.0D + border;
		double topY = centerY - (height-1)/2.0D*10.0D - border - 20.0D;
		double botY = centerY - (height-1)/2.0D*10.0D + border - 10.0D;
		this.drawBox(leftX, rightX, topY, botY);

		if(this.iMenu==1) {
			leftX = centerX - maxSize/2.0D - border;
			rightX = centerX + maxSize/2.0D + border;
			topY = centerY - (height-1)/2.0D*10.0D - border;
			botY = centerY + (height-1)/2.0D*10.0D + border;
			this.drawBox(leftX, rightX, topY, botY);
			leftX = centerX - footer/2.0D - border;
			rightX = centerX + footer/2.0D + border;
			topY = centerY + (height-1)/2.0D*10.0D - border + 10.0D;
			botY = centerY + (height-1)/2.0D*10.0D + border + 20.0D;
			this.drawBox(leftX, rightX, topY, botY);
		}  else {
			leftX = centerX - maxSize/2.0D - 25 - border;
			rightX = centerX + maxSize/2.0D + 25 + border;
			topY = centerY - (height-1)/2.0D*10.0D - border;
			botY = centerY + (height-1)/2.0D*10.0D + border;
			this.drawBox(leftX, rightX, topY, botY);
			this.drawOptions(rightX-border, topY+border, MouseX, MouseY, set, click);
			footer = this.drawFooter(centerX, centerY, height, opt1, opt2, opt3, border, MouseX, MouseY, set, click);
		}

		GL11.glEnable(3553); //GL_TEXTURE_2D
		this.write(head, centerX - title/2, (centerY - (height-1)*10/2) - 19, 0xffffff);

		if(this.iMenu==1) {
			for(int n=1; n<height; n++)
				this.write(this.sMenu[iMenu - 1][n], centerX - maxSize/2, ((centerY - (height-1)*10/2) + (n * 10))-9, 0xffffff);

			this.write(hide, centerX - footer/2, ((scHeight+5)/2 + (height-1)*10/2 + 11), 0xffffff);
		} else {
			if(this.iMenu==2) {
				for(int n=1; n<height; n++) {
					this.write(this.sMenu[iMenu - 1][n], (int)leftX + border + 1, ((centerY - (height-1)*10/2) + (n * 10))-9, 0xffffff);

					if(this.chkOptions(n-1)) hide = "On";
					else hide = "Off";

					this.write(hide, (int)rightX - border - 15 - this.chkLen(hide)/2, ((centerY - (height-1)*10/2) + (n * 10))-8, 0xffffff);
				}
			} else {
				int max = min+9;

				if(max>wayPts.size()) {
					max = wayPts.size();

					if(min>=0) {
						if(max-9>0)
							min = max-9;
						else
							min = 0;
					}
				}

				for(int n=min; n<max; n++) {
					int yTop = ((centerY - (height-1)*10/2) + ((n+1-min) * 10));
					int leftTxt = (int)leftX + border + 1;
					this.write((n+1) + ") " + wayPts.get(n).name, leftTxt, yTop-9, 0xffffff);

					if(this.iMenu==4) {
						hide = "X";
					} else {
						if(wayPts.get(n).enabled) hide = "On";
						else hide = "Off";
					}

					this.write(hide, (int)rightX - border - 29 - this.chkLen(hide)/2, yTop-8, 0xffffff);

					if (MouseX>leftTxt && MouseX<(rightX-border-77) && MouseY>yTop-10 && MouseY<yTop-1) {
						String out = wayPts.get(n).x + ", " + wayPts.get(n).z;
						int len = chkLen(out)/2;
						GL11.glDisable(3553);
						GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.8f);
						this.drawBox(MouseX-len-1, MouseX+len+1, MouseY-11, MouseY-1);
						GL11.glEnable(3553);
						this.write(out, MouseX-len, MouseY-10, 0xffffff);
					}
				}
			}

			int footpos = ((scHeight+5)/2 + (height-1)*10/2 + 11);

			if (this.iMenu==2) {
				this.write(opt1, centerX - 5 - border - footer - this.chkLen(opt1)/2, footpos , 16777215);
				this.write(opt2, centerX + border +5 + footer - this.chkLen(opt2)/2, footpos, 16777215);
			} else {
				if (this.iMenu!=4)this.write(opt1, centerX - 5 - border*2 - footer*2 - this.chkLen(opt1)/2, footpos, 16777215);

				this.write(opt2, centerX - this.chkLen(opt2)/2, footpos, 16777215);

				if (this.iMenu!=4)this.write(opt3, centerX + 5 + border*2 + footer*2 - this.chkLen(opt3)/2, footpos, 16777215);
			}
		}

		if (this.iMenu>4) {
			String verify = " !\"#$%&'()*+,-./0123456789;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz{|}~⌂ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»";

			if(this.iMenu>5 && this.inStr.equals("")) verify = "-0123456789";
			else if (this.iMenu>5) verify = "0123456789";

			if(Keyboard.getEventKeyState()) {
				do {
					if(Keyboard.getEventKey() == Keyboard.KEY_RETURN && this.lastKey!= Keyboard.KEY_RETURN)
						if (this.inStr.equals(""))
							this.next = 3;
						else if(this.iMenu == 5) {
							this.next = 6;
							this.way = this.inStr;
							if (this.game.thePlayer.dimension!=-1)
								this.inStr = Integer.toString(this.xCoord());
							else
								this.inStr = Integer.toString(this.xCoord()*8);
						} else if (this.iMenu==6) {
							this.next = 7;

							try {
								this.wayX = Integer.parseInt(this.inStr);
							} catch (Exception localException) {
								this.next=3;
							}
							if (this.game.thePlayer.dimension!=-1)
								this.inStr = Integer.toString(this.zCoord());
							else
								this.inStr = Integer.toString(this.zCoord()*8);
						} else {
							this.next = 3;

							try {
								this.wayZ = Integer.parseInt(this.inStr);
							} catch (Exception localException) {
								this.inStr="";
							}

							if(!this.inStr.equals("")) {
								wayPts.add(new Waypoint(this.way, wayX, wayZ, true));
								this.saveWaypoints();

								if(wayPts.size()>9) min = wayPts.size()-9;
							}
						}
					else if (Keyboard.getEventKey() == Keyboard.KEY_BACK && this.lastKey!= Keyboard.KEY_BACK)
						if (this.inStr.length() > 0)
							this.inStr = this.inStr.substring(0, this.inStr.length()-1);

					if(verify.indexOf(Keyboard.getEventCharacter()) >= 0 && Keyboard.getEventKey()!= this.lastKey)
						if(this.chkLen(this.inStr + Keyboard.getEventCharacter()) < 148)
							this.inStr = this.inStr + Keyboard.getEventCharacter();

					this.lastKey = Keyboard.getEventKey();
				} while (Keyboard.next());
			} else this.lastKey = 0;

			GL11.glDisable(3553);
			GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.7f);
			leftX = centerX - 75 - border;
			rightX = centerX + 75 + border;
			topY = centerY - 10 - border;
			botY = centerY + 10 + border;
			this.drawBox(leftX, rightX, topY, botY);
			leftX = leftX+border;
			rightX = rightX-border;
			topY = topY + 11;
			botY = botY - border;
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
			this.drawBox(leftX, rightX, topY, botY);
			GL11.glEnable(3553);
			String out = "Please enter a name:";

			if(this.iMenu==6) out = "Enter X coordinate:";
			else if(this.iMenu == 7) out = "Enter Z coordinate:";

			this.write(out, (int)leftX + border, (int)topY-11 + border, 0xffffff);

			if(this.blink>60)this.blink=0;

			if(this.blink<30)this.write(this.inStr + "|", (int)leftX + border, (int)topY + border, 0xffffff);
			else this.write(this.inStr, (int)leftX + border, (int)topY + border, 0xffffff);

			if(this.iMenu==6)
				try {
					if(Integer.parseInt(this.inStr)==this.xCoord()) this.write("(Current)", (int)leftX + border + this.chkLen(this.inStr) + 5, (int)topY + border, 0xa0a0a0);
				} catch(Exception localException) {}
			else if (this.iMenu==7)
				try {
					if(Integer.parseInt(this.inStr)==this.zCoord()) this.write("(Current)", (int)leftX + border + this.chkLen(this.inStr) + 5, (int)topY + border, 0xa0a0a0);
				} catch(Exception localException) {}

			this.blink++;
		}

		if(this.next!=0) {
			this.iMenu = this.next;
			this.next = 0;
		}
	}

	private void showCoords (int scWidth, int scHeight) {
		if(!this.hide) {
			GL11.glPushMatrix();
			GL11.glScalef(0.5f, 0.5f, 1.0f);
			String xy ="";
			if (this.game.thePlayer.dimension!=-1)
				xy = this.dCoord(xCoord()) + ", " + this.dCoord(zCoord());
			else
				xy = this.dCoord(xCoord()*8) + ", " + this.dCoord(zCoord()*8);
			int m = this.chkLen(xy)/2;
			this.write(xy, scWidth*2-32*2-m, 146, 0xffffff);
			xy = Integer.toString(this.yCoord());
			m = this.chkLen(xy)/2;
		//	xy="" + this.getWorld().skylightSubtracted + " " + this.getWorld().calculateSkylightSubtracted(1.0F) + " " + this.getWorld().func_35464_b(1.0F); // always 0 in SMP. method works, not value.  it's never updated, no world tick in SMP.  Fscks lightmap functionality
			this.write(xy, scWidth*2-32*2-m, 156, 0xffffff);
			GL11.glPopMatrix();
		} else {
			if (this.game.thePlayer.dimension!=-1) this.write("(" + this.dCoord(xCoord()) + ", " + this.yCoord() + ", " + this.dCoord(zCoord()) + ") " + (int) this.direction + "'", 2, 10, 0xffffff);
			else this.write("(" + this.dCoord(xCoord()*8) + ", " + this.yCoord() + ", " + this.dCoord(zCoord()*8) + ") " + (int) this.direction + "'", 2, 10, 0xffffff);
		}
	}

	private void drawRound(int paramInt1) {
		try {
			this.disp(this.img("/roundmap.png"));
			drawPre();
			this.setMap(paramInt1);
			drawPost();
		} catch (Exception localException) {
			this.error = "Error: minimap overlay not found!";
		}
	}

	private void drawBox(double leftX, double rightX, double topY, double botY) {
		drawPre();
		ldrawtwo(leftX, botY, 0.0D);
		ldrawtwo(rightX, botY, 0.0D);
		ldrawtwo(rightX, topY, 0.0D);
		ldrawtwo(leftX, topY, 0.0D);
		drawPost();
	}

	private void drawOptions(double rightX,double topY,int MouseX,int MouseY,boolean set,boolean click) {
		if(this.iMenu>2) {
			if(min<0) min = 0;

			if(!Mouse.isButtonDown(0) && scrClick) scrClick = false;

			if (MouseX>(rightX-10) && MouseX<(rightX-2) && MouseY>(topY+1) && MouseY<(topY+10)) {
				if(set || click) {
					if(set&&min>0) min--;

					GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
				} else GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
			} else
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.3f);

			drawPre();
			ldrawtwo(rightX-10, topY+10, 0.0D);
			ldrawtwo(rightX-2, topY+10, 0.0D);
			ldrawtwo(rightX-6, topY+1, 0.0D);
			ldrawtwo(rightX-6, topY+1, 0.0D);
			drawPost();

			if(wayPts.size()>9) {
				sMax = (int)(9.0D/wayPts.size()*67.0D);
			} else {
				sMin = 0;
				sMax = 67;
			}

			if (MouseX>rightX-10 && MouseX<rightX-2 && MouseY>topY+12+sMin && MouseY<topY+12+sMin+sMax || scrClick) {
				if(Mouse.isButtonDown(0)&&!scrClick) {
					scrClick = true;
					scrStart = MouseY;
				} else if (scrClick && wayPts.size()>9) {
					int offset = MouseY-scrStart;

					if(sMin+offset<0) sMin = 0;
					else if (sMin+offset+sMax>67) sMin = 67-sMax;
					else {
						sMin = sMin+offset;
						scrStart = MouseY;
					}

					min = (int)((sMin/(67.0D-sMax))*(wayPts.size()-9));
					GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
				} else GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
			} else {
				if(wayPts.size()>9)
					sMin = (int)((double)min/(double)(wayPts.size()-9)*(67.0D-sMax));

				GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.3f);
			}

			this.drawBox(rightX-10, rightX-2, topY+12+sMin, topY+12+sMin+sMax);

			if (MouseX>rightX-10 && MouseX<rightX-2 && MouseY>topY+81 && MouseY<topY+90) {
				if(set || click) {
					if(set&&min<wayPts.size()-9) min++;

					GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
				} else GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
			} else
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.3f);

			drawPre();
			ldrawtwo(rightX-6, topY+90, 0.0D);
			ldrawtwo(rightX-6, topY+90, 0.0D);
			ldrawtwo(rightX-2, topY+81, 0.0D);
			ldrawtwo(rightX-10, topY+81, 0.0D);
			drawPost();
		}

		double leftX = rightX - 30;
		double botY = 0;
		topY+=1;
		int max = min+9;

		if(max>wayPts.size()) {
			max = wayPts.size();

			if(min>0) {
				if(max-9>0)
					min = max-9;
				else
					min = 0;
			}
		}

		double leftCl = 0;
		double rightCl = 0;

		if(this.iMenu>2) {
			leftX = leftX - 14;
			rightX = rightX - 14;
			rightCl = rightX - 32;
			leftCl = rightCl - 9;
		} else {
			min = 0;
			if (motionTrackerExists) max = 11;
			else max = 10; // number of menu options, only affects if they can be clicked
		}

		for(int i = min; i<max; i++) {
			if(i>min) topY += 10;

			botY = topY + 9; 

			if (MouseX>leftX && MouseX<rightX && MouseY>topY && MouseY<botY && this.iMenu < 5)
				if (set || click) {
					if(set) {
						if(this.iMenu==2)this.setOptions(i);
						else if (this.iMenu==3) {
							wayPts.get(i).enabled = !wayPts.get(i).enabled;
							this.saveWaypoints();
						} else {
							this.delWay(i);
							this.next=3;
						}
					}

					GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				} else GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.6f);
			else {
				if(this.iMenu==2) {
					if (this.chkOptions(i)) GL11.glColor4f(0.0f, 1.0f, 0.0f, 0.6f);
					else GL11.glColor4f(1.0f, 0.0f, 0.0f, 0.6f);
				} else if (this.iMenu==4) {
					GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.4f);
				} else {
					if (wayPts.get(i).enabled) {
						GL11.glColor4f(0.0f, 1.0f, 0.0f, 0.6f);
					} else GL11.glColor4f(1.0f, 0.0f, 0.0f, 0.6f);
				}
			}

			this.drawBox(leftX, rightX, topY, botY);

			if(iMenu>2 && !(iMenu==4 && this.next==3)) {
				if (MouseX>leftCl && MouseX<rightCl && MouseY>topY && MouseY<botY && this.iMenu==3)
					if (set) {
						wayPts.get(i).red = generator.nextFloat();
						wayPts.get(i).green = generator.nextFloat();
						wayPts.get(i).blue = generator.nextFloat();
						saveWaypoints();
					}

				GL11.glColor3f(wayPts.get(i).red, wayPts.get(i).green, wayPts.get(i).blue);
				this.drawBox(leftCl, rightCl, topY, botY);
			}
		}
	}

	private void delWay(int i) {
		wayPts.remove(i);
		this.saveWaypoints();
	}

	private int drawFooter(int centerX,int centerY,int m, String opt1, String opt2, String opt3, int border,int MouseX,int MouseY,boolean set,boolean click) {
		int footer = this.chkLen(opt1);

		if (this.chkLen(opt2) > footer) footer = this.chkLen(opt2);

		double leftX = centerX - footer - border*2 - 5;
		double rightX = centerX - 5;
		double topY = centerY + (m-1)/2.0D*10.0D - border + 10.0D;
		double botY = centerY + (m-1)/2.0D*10.0D + border + 20.0D;

		if (this.iMenu>2) {
			if (this.chkLen(opt3) > footer) footer = this.chkLen(opt3);

			leftX = centerX - border*3 - footer*1.5 - 5;
			rightX = centerX - footer/2 - border - 5;
		}

		if (MouseX>leftX && MouseX<rightX && MouseY>topY && MouseY<botY && this.iMenu < 4)
			if (set || click) {
				if(set) {
					if (this.iMenu==2) setMenuNull();
					else this.next=2;
				}

				GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			} else GL11.glColor4f(0.5f, 0.5f, 0.5f, 0.7f);
		else GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.7f);

		if (this.iMenu!=4)this.drawBox(leftX, rightX, topY, botY);

		if (this.iMenu==2) {
			leftX = centerX + 5;
			rightX = centerX + footer + border*2 + 5;
		} else {
			leftX = centerX - footer/2 - border;
			rightX = centerX + footer/2 + border;
		}

		if (MouseX>leftX && MouseX<rightX && MouseY>topY && MouseY<botY && this.iMenu < 5)
			if (set || click) {
				if(set) {
					if (this.iMenu==2 || this.iMenu==4) this.next=3;
					else {
						this.next = 5;
						this.inStr = "";
					}
				}

				GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			} else GL11.glColor4f(0.5f, 0.5f, 0.5f, 0.7f);
		else GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.7f);

		this.drawBox(leftX, rightX, topY, botY);

		if (this.iMenu > 2) {
			rightX = centerX + border*3 + footer*1.5 + 5;
			leftX = centerX + footer/2 + border + 5;

			if (MouseX>leftX && MouseX<rightX && MouseY>topY && MouseY<botY && this.iMenu < 4)
				if (set || click) {
					if(set) this.next = 4;

					GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				} else GL11.glColor4f(0.5f, 0.5f, 0.5f, 0.7f);
			else GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.7f);

			if (this.iMenu!=4) this.drawBox(leftX, rightX, topY, botY);
		}

		return footer/2;
	}

	private boolean chkOptions(int i) {
		if (i==0) return coords;
		else if (i==1) return this.hide;
		else if (i==2) return this.showNether;
		else if (i==3) return this.showCaves;
		else if (i==4) return lightmap;
		else if (i==5) return heightmap;
		else if (i==6) return squareMap;
		else if (i==7) return oldNorth;
		else if (i==8) return welcome;
		else if (i==9) return threading;
		else if (i==10 && motionTrackerExists) return false;
		throw new IllegalArgumentException("bad option number "+i);
	}

	private void setOptions(int i) {
		if (i==0) coords = !coords;
		else if (i==1) this.hide = !this.hide;
		else if (i==2) this.showNether = !this.showNether;
		else if (i==3) this.showCaves = !this.showCaves;
		else if (i==4) lightmap = !lightmap;
		else if (i==5) heightmap = !heightmap;
		else if (i==6) squareMap = !squareMap;
		else if (i==7) oldNorth = !oldNorth;
		else if (i==8) welcome = !welcome;
		else if (i==9) threading = !threading;
		else if (i==10 && motionTrackerExists) motionTracker.activated = true;
		else throw new IllegalArgumentException("bad option number "+i);
		this.saveAll();
		this.timer=500;
		
		
	}

	private void setMap(int paramInt1) {
		ldrawthree(paramInt1 - 64.0D, 64.0D + 5.0D, 1.0D, 0.0D, 1.0D);
		ldrawthree(paramInt1, 64.0D + 5.0D, 1.0D, 1.0D, 1.0D);
		ldrawthree(paramInt1, 5.0D, 1.0D, 1.0D, 0.0D);
		ldrawthree(paramInt1 - 64.0D, 5.0D, 1.0D, 0.0D, 0.0D);
	}

	private void drawDirections(int scWidth) {
		
		/*int wayX = this.xCoord();
		int wayY = this.yCoord();
		float locate = (float)Math.toDegrees(Math.atan2(wayX, wayY));
		double hypot = Math.sqrt((wayX*wayX)+(wayY*wayY))/(Math.pow(2,this.zoom)/2);

		
			try 
			{
				GL11.glPushMatrix();
				GL11.glColor3f(1.0f, 1.0f, 1.0f);
				this.disp(this.img("/compass.png"));
				GL11.glTranslatef(scWidth - 32.0F, 37.0F, 0.0F);
				GL11.glRotatef(-locate + this.direction + 180.0F, 0.0F, 0.0F, 1.0F);
				GL11.glTranslated(0.0D,-hypot,0.0D);
				GL11.glRotatef(-(-locate + this.direction + 180.0F), 0.0F, 0.0F, 1.0F);
				GL11.glTranslated(0.0D,hypot,0.0D);
				GL11.glTranslatef(-(scWidth - 32.0F), -37.0F, 0.0F);
				GL11.glTranslated(0.0D,-hypot,0.0D);
				drawPre();
				this.setMap(scWidth);
				drawPost();
			} catch (Exception localException) 
			{
				this.error = "Error: compass overlay not found!";
			} finally 
			{
				GL11.glPopMatrix();
			}*/
		
		GL11.glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 1.0f);
		GL11.glTranslated((64.0D * Math.sin(Math.toRadians(-(this.direction - 90.0D + northRotate)))),(64.0D * Math.cos(Math.toRadians(-(this.direction - 90.0D + northRotate)))),0.0D); // direction -90 w top.  0 n top.  in all cases n top means 90 more (or w top means 90 less)
		this.write("N", scWidth*2-66, 70, 0xffffff);
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 1.0f);
		GL11.glTranslated((64.0D * Math.sin(Math.toRadians(-(this.direction + northRotate)))),(64.0D * Math.cos(Math.toRadians(-(this.direction + northRotate)))),0.0D);
		this.write("E", scWidth*2-66, 70, 0xffffff);
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 1.0f);
		GL11.glTranslated((64.0D * Math.sin(Math.toRadians(-(this.direction + 90.0D + northRotate)))),(64.0D * Math.cos(Math.toRadians(-(this.direction + 90.0D + northRotate)))),0.0D);
		this.write("S", scWidth*2-66, 70, 0xffffff);
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 1.0f);
		GL11.glTranslated((64.0D * Math.sin(Math.toRadians(-(this.direction + 180.0D + northRotate)))),(64.0D * Math.cos(Math.toRadians(-(this.direction + 180.0D + northRotate)))),0.0D);
		this.write("W", scWidth*2-66, 70, 0xffffff);
		GL11.glPopMatrix();
	}

	private void SetZoom() {
		if (this.fudge > 0) return;

		if (this.iMenu != 0) {
			this.iMenu = 0;

			if(getMenu()!=null) setMenuNull();
		} else {
			if (this.zoom == 3) {
				if(!this.full) this.full = true;
				else {
					this.zoom = 2;
					this.full = false;
					this.error = "Zoom Level: (1.0x)";
				}
			} else if (this.zoom == 0) {
				this.zoom = 3;
				this.error = "Zoom Level: (0.5x)";
			} else if (this.zoom==2) {
				this.zoom = 1;
				this.error = "Zoom Level: (2.0x)";
			} else {
				this.zoom = 0;
				this.error = "Zoom Level: (4.0x)";
			}
			this.timer = 500;
		}

		this.fudge = 20;
	}

	
	
	

	

	//@Override
	public String Version() {
		return "1.3_01 - "+zmodver;
	}
}
