package name.deathswap;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Random;


import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


import net.minecraft.world.World;


public class TransAsyncThread extends Thread
{
    private final int ERROR_POS = 1000;

    @Override
    public void start()
    {
        BlockPos safePos = findSafePos();
        if(safePos.getY() != ERROR_POS)
        {
            _player.teleport(safePos.getX(), safePos.getY() + 1, safePos.getZ());
        }
        _player.setInvulnerable(false);
    }
    private final ServerPlayerEntity _player;
    private final World _world;
    public TransAsyncThread(ServerPlayerEntity player, World world)
    {
        _player = player;
        _world = world;
    }

    private int _findPosCount = 0;
    private BlockPos findSafePos()
    {
        if(_findPosCount > 10)
        {
            String str = "尝试十次寻找安全坐标均失败，请检查世界设置";
            Text msg = new LiteralText(str).formatted(Formatting.YELLOW);
            _player.sendMessage(msg, false);
            return new BlockPos(0,ERROR_POS,0);
        }
        Random random = new Random();
        BlockPos blockPos = new BlockPos(random.nextInt(5000),255,random.nextInt(5000));
        Block block = _world.getBlockState(blockPos).getBlock();


        while (block.is(Blocks.AIR)&&blockPos.getY()>=0)
        {
            blockPos = new BlockPos(blockPos.getX(), blockPos.getY() - 1, blockPos.getZ());
            block = _world.getBlockState(blockPos).getBlock();

        }
        System.out.println(_player.toString()+" SafePos:"+blockPos.toString());
        System.out.println(_player.toString()+" SafePos:"+block.toString());
        if(blockPos.getY()<=0)
        {
            _findPosCount++;
            System.out.println("findPos Failed:"+_findPosCount);
            return findSafePos();
        }
        return blockPos;
    }
}

//LZX completed this code in 2024/03/21
//LZX-TC-2024-03-21-002