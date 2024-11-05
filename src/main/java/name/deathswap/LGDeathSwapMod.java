package name.deathswap;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
////////////
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.advancement.criterion.EffectsChangedCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.predicate.entity.EntityEffectPredicate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.EffectCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;


import net.minecraft.world.World;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import org.apache.logging.log4j.core.jmx.Server;


public class LGDeathSwapMod implements ModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger("death-swap-mod");

	int deathSwapTime = 300;

	public static boolean isGameStarting = false;

	long startTime = 0;

	//private ServerTickEvents.StartTick swapTick;

	String winText = "No Winner";

	public static int playerNum = 0;

	//World world;
	String _modAuthor = "LoongLy";

	String _modName = "DeathSwap";

	String _modVersion = "1.4.4";
	//List<BlockPos> safePos = null;
	String _lastEditTime = "2024/11/04";

	String []_modInfo = {_modAuthor,_modName,_modVersion,_lastEditTime};
	@Override
	public void onInitialize()
	{
		//PlayerDeathCallback.EVENT.register(this::onPlayerDeath);

		ServerLifecycleEvents.SERVER_STARTING.register(this::initPlayerHealthDetectionAsync);

		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
		//PlayerDeathCallback.EVENT.register(this::onPlayerDeath);
		ServerTickEvents.START_SERVER_TICK.register(this::onPlayerWin);
		CommandRegistrationCallback.EVENT.register(this::editSwapTime);
		CommandRegistrationCallback.EVENT.register(this::editGameMode);
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(CommandManager.literal("startdeathswap")
					.executes(context -> {
						// 在指令执行时开始操作
						startGame(context.getSource());
						return 1;
					}));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(CommandManager.literal("startdeathswap2")
					.executes(context -> {
						// 在指令执行时开始操作
						StartGame2(context.getSource());
						return 1;
					}));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(CommandManager.literal("aboutdeathswap")
					.executes(context -> {
						// 在指令执行时开始操作
						AboutMod(context.getSource());
						return 1;
					}));
		});

		LOGGER.info("I am LZX(LoongLy),Hello Fabric world!\nInitialize DeathSwap mod completed!");
	}

	private void initPlayerHealthDetectionAsync(MinecraftServer server)
	{
		PlayerHealthDetectionAsync playerHealthDetectionAsync = new PlayerHealthDetectionAsync(server,_modInfo);
		playerHealthDetectionAsync.start();
	}





	private void editSwapTime(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated)
	{
		dispatcher.register(CommandManager.literal("setswaptime")
				.then(CommandManager.argument("value", IntegerArgumentType.integer())
						.executes(context -> {
							if(isGameStarting)
							{
								context.getSource().sendFeedback(new LiteralText("游戏正在进行，不能更改时间"), false);
								return 1;
							}
							// 获取命令参数中的值
							int swaptime = IntegerArgumentType.getInteger(context, "value");
							// 更新变量的值
							deathSwapTime = swaptime;
							if(swaptime<40)
							{
								context.getSource().sendFeedback(new LiteralText("交换时间不得小于40秒"), false);
								return 1;
							}
							// 发送消息给执行命令的玩家
							context.getSource().sendFeedback(new LiteralText("交换时间更新为： " + swaptime), false);
							return 1;
						})));
	}


	private void editGameMode(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated)
	{
		dispatcher.register(CommandManager.literal("gamemode")
				.then(CommandManager.argument("gamemodeValue", StringArgumentType.word())
						.executes(context -> {
							if(isGameStarting)
							{
								context.getSource().sendFeedback(new LiteralText("Game is starting, can't change game mode to "), false);
								return 1;
							}

							String gamemodeValue = StringArgumentType.getString(context, "gamemodeValue");

							if(gamemodeValue.equals("survival"))
							{
								context.getSource().getPlayer().setGameMode(GameMode.SURVIVAL);
							}
							else if(gamemodeValue.equals("creative"))
							{
								context.getSource().getPlayer().setGameMode(GameMode.CREATIVE);
							}
							else if(gamemodeValue.equals("adventure"))
							{
								context.getSource().getPlayer().setGameMode(GameMode.ADVENTURE);
							}
							else if(gamemodeValue.equals("spectator"))
							{
								context.getSource().getPlayer().setGameMode(GameMode.SPECTATOR);
							}
							else
							{
								context.getSource().sendFeedback(new LiteralText("Game mode not found"), false);
								return 1;
							}
							// 发送消息给执行命令的玩家
							context.getSource().sendFeedback(new LiteralText("您的游戏模式已更新"), false);
							return 1;
						})));
	}


	private void AboutMod(ServerCommandSource source)
	{
		Text msg = new LiteralText("Death Swap 版本:"+_modVersion+" 作者:"+_modAuthor+"(Lagging_Warrior)  最后一次更新时间"+_lastEditTime).formatted(Formatting.YELLOW);
		source.sendFeedback(msg, false);
		Text msg2 = new LiteralText("下载链接(更新中): https://mod.3dmgame.com/mod/207510").styled(style->style
			.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://mod.3dmgame.com/mod/207510"))
			.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to jump to: " + "https://mod.3dmgame.com/mod/207510")))
			.withColor(Formatting.UNDERLINE));
		source.sendFeedback(msg2, false);
		Text gitmsg = new LiteralText("github链接: https://github.com/Long-Zixuan/Dealth-Swap").styled(style->style
				.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Long-Zixuan/Dealth-Swap"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to jump to: " + "https://github.com/Long-Zixuan/Dealth-Swap")))
				.withColor(Formatting.UNDERLINE));
		source.sendFeedback(gitmsg, false);
	}

	private void initStartGame(boolean needTransPos,ServerCommandSource source)
	{
		startTime = Instant.now().getEpochSecond();
		winText = "No Winner";
		MinecraftServer server = source.getMinecraftServer();
		//server.getGameRules().get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, server);
		World world = server.getWorld(World.OVERWORLD);
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		playerNum = players.size();
		if (players.size() < 2)
		{
			LOGGER.info("没有足够的玩家参与游戏");
			Text msg = new LiteralText("没有足够的玩家参与游戏").formatted(Formatting.YELLOW);
			players.get(0).sendMessage(msg,true);
			return;
		}
		isGameStarting = true;
		//ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);



		for (ServerPlayerEntity player : players)
		{
			Text msg = new LiteralText("游戏开始！").formatted(Formatting.YELLOW);
			player.sendMessage(msg,true);
			player.setGameMode(GameMode.SURVIVAL);
			player.setHealth(20);
			player.getHungerManager().setFoodLevel(20);
			player.getHungerManager().setSaturationLevel(1.0F);
			//player.clearActiveItem();
			player.inventory.clear();


			Random random = new Random();
			//BlockPos blockPos = findSafePos(random.nextInt(5000),50, random.nextInt(5000));
			//BlockPos blockPos = findSafePos();
			//player.teleport(blockPos.getX(), blockPos.getY(), blockPos.getZ());
			player.setInvulnerable(true);
			if(needTransPos)
			{
				AsyncThread asyncThread = new AsyncThread(player,world);
				asyncThread.start();
			}
		}
	}
	private void startGame(ServerCommandSource source)
	{
		initStartGame(true,source);
		/*startTime = Instant.now().getEpochSecond();
		winText = "No Winner";
		MinecraftServer server = source.getMinecraftServer();
		server.getGameRules().get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, server);
		World world = server.getWorld(World.OVERWORLD);
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		playerNum = players.size();
		if (players.size() < 2)
		{
			LOGGER.info("Not enough players to swap positions.");
			Text msg = new LiteralText("Not enough players to swap positions.").formatted(Formatting.YELLOW);
			players.get(0).sendMessage(msg,true);
			return;
		}
		isGameStarting = true;*/
		//ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);



		/*for (ServerPlayerEntity player : players)
		{
			Text msg = new LiteralText("Death swap game started!").formatted(Formatting.YELLOW);
			player.sendMessage(msg,true);
			player.setGameMode(GameMode.SURVIVAL);
			player.setHealth(20);
			player.getHungerManager().setFoodLevel(20);
			player.getHungerManager().setSaturationLevel(1.0F);

			Random random = new Random();
			//BlockPos blockPos = findSafePos(random.nextInt(5000),50, random.nextInt(5000));
			//BlockPos blockPos = findSafePos();
			//player.teleport(blockPos.getX(), blockPos.getY(), blockPos.getZ());
			player.setInvulnerable(true);
			AsyncThread asyncThread = new AsyncThread(player,world);
			asyncThread.start();

		}*/

	}

	private void StartGame2(ServerCommandSource source)
	{
		initStartGame(false,source);
		/*startTime = Instant.now().getEpochSecond();
		winText = "No Winner";
		MinecraftServer server = source.getMinecraftServer();
		server.getGameRules().get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, server);
		World world = server.getWorld(World.OVERWORLD);
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		playerNum = players.size();
		if (players.size() < 2)
		{
			LOGGER.info("Not enough players to swap positions.");
			Text msg = new LiteralText("Not enough players to swap positions.").formatted(Formatting.YELLOW);
			players.get(0).sendMessage(msg,true);
			return;
		}
		isGameStarting = true;
		//ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);

		for (ServerPlayerEntity player : players)
		{
			//player.kill();
			Text msg = new LiteralText("Death swap game started!").formatted(Formatting.YELLOW);
			player.setHealth(20);
			player.getHungerManager().setFoodLevel(20);
			player.getHungerManager().setSaturationLevel(1.0F);
			player.sendMessage(msg,true);
			player.setGameMode(GameMode.SURVIVAL);
		}*/
	}


	private void onPlayerWin(MinecraftServer server)
	{
		if(!isGameStarting)
		{
			return;
		}
		int SurvalPlayerNum=0;
		ServerPlayerEntity tmpPlayer = null;
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		for(ServerPlayerEntity player : players)
		{
			if (player.interactionManager.getGameMode() == GameMode.SURVIVAL)
			{
				SurvalPlayerNum++;
				tmpPlayer = player;
			}
		}

		if(SurvalPlayerNum==0)
		{
			for(ServerPlayerEntity player : players)
			{
				Text msg2 = new LiteralText(winText).formatted(Formatting.YELLOW);
				player.sendMessage(msg2,true);
				isGameStarting = false;
			}
		}
		if(SurvalPlayerNum==1&&players.size()!=1)
		{

			Text msg = new LiteralText("You Win").formatted(Formatting.YELLOW);
			tmpPlayer.sendMessage(msg,false);
			tmpPlayer.setGameMode(GameMode.SPECTATOR);
			winText = "Winner is:" + tmpPlayer.getGameProfile().getName().toString();
			for(ServerPlayerEntity player : players)
			{
				Text msg2 = new LiteralText("胜利者是:" + tmpPlayer.getGameProfile().getName().toString()).formatted(Formatting.YELLOW);
				player.sendMessage(msg2,true);
				//player.playSound(net.minecraft.sound.SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
				playAnvilFallSound(player,SoundEvents.ENTITY_PLAYER_LEVELUP);
				//player.sendMessage(new LiteralText("Winner is " + tmpPlayer.getGameProfile().getName().toString()), true);
				//ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
				//ServerTickEvents.END_SERVER_TICK.register(this::playerHealthDetection);


			}
			isGameStarting = false;
		}


	}


	public static void playAnvilFallSound(ServerPlayerEntity player,SoundEvent soundEvent)
	{
		World world = player.getEntityWorld();

		// 在指定位置播放音效
		world.playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
	}

	void sendMSGForEveryPlayer(MinecraftServer server,String str)
	{
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		for (ServerPlayerEntity player : players)
		{
			Text msg = new LiteralText(str).formatted(Formatting.YELLOW);
			player.sendMessage(msg, false);
			//player.playSound(SoundEvents.BLOCK_ANVIL_FALL, 5.0F, 5.0F);
			playAnvilFallSound(player,SoundEvents.BLOCK_ANVIL_LAND);

		}
	}

	private void onPlayerDeath(ServerPlayerEntity player, DamageSource source)
	{

		player.setGameMode(GameMode.SPECTATOR);
		Text msg = new LiteralText("You Death").formatted(Formatting.YELLOW);
		player.sendMessage(msg,true);
	}


	boolean shouldSendMSG = true;
	private void onServerTick(MinecraftServer minecraftServer)
	{
		if(!isGameStarting)
		{
			return;
		}
		//LOGGER.info("server tick running");
		long nowUnixTime = Instant.now().getEpochSecond();
		long deltaTime = nowUnixTime - startTime;

		if (deltaTime==deathSwapTime-1&&shouldSendMSG)
		{
			sendMSGForEveryPlayer(minecraftServer,"交换将在1秒后开始");
			shouldSendMSG = false;
		}
		else if (deltaTime==deathSwapTime-2&&!shouldSendMSG)
		{
			sendMSGForEveryPlayer(minecraftServer,"交换将在2秒后开始");
			shouldSendMSG = true;
		}
		else if (deltaTime==deathSwapTime-3&&shouldSendMSG)
		{
			sendMSGForEveryPlayer(minecraftServer,"交换将在3秒后开始");
			shouldSendMSG = false;
		}
		else if (deltaTime==deathSwapTime-4&&!shouldSendMSG)
		{
			sendMSGForEveryPlayer(minecraftServer,"交换将在4秒后开始");
			shouldSendMSG = true;
		}
		else if (deltaTime==deathSwapTime-5&&shouldSendMSG)
		{
			sendMSGForEveryPlayer(minecraftServer,"交换将在5秒后开始");
			shouldSendMSG = false;
		}

		if(deltaTime== deathSwapTime)
		{
			LOGGER.info("Swap");
			startTime = Instant.now().getEpochSecond();
			swapPlayerPos(minecraftServer.getPlayerManager().getPlayerList());
			shouldSendMSG = true;
		}
		/*if (minecraftServer.getTicks() % (20 * (deathSwapTime-1)) == 0)
		{
			sendMSGForEveryPlayer(minecraftServer,"Death swap in 1 second");
		}
		else if (minecraftServer.getTicks() % (20 * (deathSwapTime-2)) == 0)
		{
			sendMSGForEveryPlayer(minecraftServer,"Death swap in 2 second");
		}
		else if (minecraftServer.getTicks() % (20 * (deathSwapTime-3)) == 0)
		{
			sendMSGForEveryPlayer(minecraftServer,"Death swap in 3 second");
		}
		else if (minecraftServer.getTicks() % (20 * (deathSwapTime-4)) == 0)
		{
			sendMSGForEveryPlayer(minecraftServer,"Death swap in 4 second");
		}
		else if (minecraftServer.getTicks() % (20 * (deathSwapTime-5)) == 0)
		{
			sendMSGForEveryPlayer(minecraftServer,"Death swap in 5 second");
		}
		if (minecraftServer.getTicks() % (20 * deathSwapTime) == 0)
		{
			LOGGER.info("Swap");
			swapPlayerPos(minecraftServer.getPlayerManager().getPlayerList());
		}*/
	}
	private void swapPlayerPos(List<ServerPlayerEntity> players)
	{
		SwapPosAsync swapPosAsync = new SwapPosAsync(players);
		swapPosAsync.start();
		/*List<ServerPlayerEntity> alivePlayers = new ArrayList<ServerPlayerEntity>();
		for(ServerPlayerEntity player : players)
		{
			if (player.interactionManager.getGameMode() == GameMode.SURVIVAL && player.getHealth()>0)
			{
				alivePlayers.add(player);
			}
		}

		if (alivePlayers.size() < 2)
		{
			LOGGER.info("Not enough players to swap positions.");
			Text msg = new LiteralText("Not enough players to swap positions.").formatted(Formatting.YELLOW);
			players.get(0).sendMessage(msg,false);
			return;
		}


		ServerPlayerEntity player1 = alivePlayers.get(0);
		double tempX = player1.getX();
		double tempY = player1.getY();
		double tempZ = player1.getZ();
		for(int i = 0; i < alivePlayers.size()-1; i++)
		{
			ServerPlayerEntity tmpPlayer = alivePlayers.get(i);
			tmpPlayer.teleport(alivePlayers.get(i+1).getX(), alivePlayers.get(i+1).getY(), alivePlayers.get(i+1).getZ());
			Text msg = new LiteralText("You are swapped positions with " + alivePlayers.get(i+1).getGameProfile().getName()).formatted(Formatting.YELLOW);
			tmpPlayer.sendMessage(msg,false);
		}
		ServerPlayerEntity lastPlayer = alivePlayers.get(alivePlayers.size()-1);
		lastPlayer.teleport(tempX, tempY, tempZ);
		Text msg = new LiteralText("You are swapped positions with " + player1.getGameProfile().getName()).formatted(Formatting.YELLOW);
		lastPlayer.sendMessage(msg,false);
		alivePlayers.clear();*/

	}
}

//LZX completed this code in 2024/03/21
//LZX-TC-2024-03-21-001