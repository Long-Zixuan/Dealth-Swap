package name.deathswap;

import name.deathswap.LGDeathSwapMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class PlayerHealthDetectionAsync extends Thread
{

    @Override
    public void start()
    {
        ServerTickEvents.START_SERVER_TICK.register(this::playerHealthDetection);
    }
    private MinecraftServer _server;

    private String[] _modInfo;

    public PlayerHealthDetectionAsync(MinecraftServer server,String[] modInfo)
    {
        _server = server;
        _modInfo = modInfo;
    }




    private void playerHealthDetection(MinecraftServer server)
    {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        if(!LGDeathSwapMod.isGameStarting)
        {

            if(players.size()>LGDeathSwapMod.playerNum)
            {
                LGDeathSwapMod.playerNum = players.size();
                players.get(LGDeathSwapMod.playerNum-1).sendMessage(new LiteralText(_modInfo[0]+":欢迎加入死亡交换游戏！").formatted(Formatting.YELLOW),false);
            }
            return;
        }
        for (ServerPlayerEntity player : players)
        {
            if (player.getHealth() <= 0)
            {
                onPlayerDeath(player);
            }

        }




    }
    private void onPlayerDeath(ServerPlayerEntity player)
    {
        player.setGameMode(GameMode.SPECTATOR);
        Text msg = new LiteralText("You Death").formatted(Formatting.YELLOW);
        player.sendMessage(msg,true);
        //player.removeStatusEffect(StatusEffects.);
        double tmpX = player.getX();
        double tmpZ = player.getZ();
        double tmpY = player.getY();
        float tmpYaw = player.getYaw(0);
        float tmpPitch = player.getPitch(0);
        ServerWorld tmpWorld = player.getServerWorld();
        //player.stopUsingItem();
        //DeathScreen tmpScreen = new DeathScreen(new LiteralText("You Death"),false);

        //player.closeScreenHandler();
        player.setHealth(20);
        player.teleport(tmpWorld,tmpX,tmpY,tmpZ,tmpYaw,tmpPitch);
    }

}

//LZX completed this code in 2024/03/21
//LZX-TC-2024-03-21-002