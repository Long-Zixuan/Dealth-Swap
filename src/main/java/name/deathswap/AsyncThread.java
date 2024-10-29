package name.deathswap;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Random;






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

import java.util.List;
import java.util.Random;


public class AsyncThread extends Thread {

    @Override
    public void start()
    {
        BlockPos safePos = findSafePos();
        _player.teleport(safePos.getX(), safePos.getY() + 1, safePos.getZ());
        _player.setInvulnerable(false);
    }
    private final ServerPlayerEntity _player;
    private final World _world;
    public AsyncThread(ServerPlayerEntity player,World world)
    {
        _player = player;
        _world = world;
    }


    private BlockPos findSafePos()
    {
        Random random = new Random();
        BlockPos blockPos = new BlockPos(random.nextInt(5000),255,random.nextInt(5000));
		Block block = _world.getBlockState(blockPos).getBlock();


        while (block.is(Blocks.AIR)&&blockPos.getY()>=0)
		{
            blockPos = new BlockPos(blockPos.getX(), blockPos.getY() - 1, blockPos.getZ());
			block = _world.getBlockState(blockPos).getBlock();

        }

		if(blockPos.getY()<=0)
		{
			return new BlockPos(0,255,0);
		}

        return blockPos;
		/*if(block == Blocks.AIR)
		{
			return blockPos;
		}
		else
		{
			return findSafePos(x,y+1, z);
		}*/



    }

}

//LZX completed this code in 2024/03/21
//LZX-TC-2024-03-21-002