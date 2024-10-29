package name.deathswap;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.injection.Inject;

public interface PlayerDeathCallback
{

    Event<PlayerDeathCallback> EVENT = EventFactory.createArrayBacked(PlayerDeathCallback.class, (listeners) -> (player, source) -> {
        for (PlayerDeathCallback listener : listeners)
        {
            listener.playerDeath(player, source);
        }
    });

    void playerDeath(ServerPlayerEntity player, DamageSource source);
}




