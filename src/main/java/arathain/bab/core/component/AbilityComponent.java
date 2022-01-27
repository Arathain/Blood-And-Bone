package arathain.bab.core.component;

import arathain.bab.client.anim.player.GeckoLibPlayer;
import arathain.bab.client.anim.util.BABGeoModel;
import arathain.bab.core.ability.Ability;
import arathain.bab.core.ability.AbilityHandler;
import arathain.bab.core.ability.AbilityType;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

import java.util.*;

public class AbilityComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent, IAbilityComponent {
    SortedMap<AbilityType<?>, Ability> abilityInstances = new TreeMap<>();
    Ability activeAbility = null;
    Map<String, NbtElement> nbtMap = new HashMap<>();
    private final PlayerEntity obj;
    public AbilityComponent(PlayerEntity obj) {
        this.obj = obj;
    }
    @Override
    public void clientTick() {
        tick(obj);
    }

    @Override
    public void serverTick() {
        tick(obj);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        Set<String> keys = tag.getKeys();
        for (String abilityName : keys) {
            nbtMap.put(abilityName, tag.get(abilityName));
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        for (Map.Entry<AbilityType<?>, Ability> abilityEntry : getAbilityMap().entrySet()) {
               NbtCompound nbt = abilityEntry.getValue().writeNBT();
               if (!nbt.isEmpty()) {
                   tag.put(abilityEntry.getKey().getName(), nbt);
               }
        }
    }

    @Override
    public void activateAbility(AbilityType<?> ability) {
        Ability instance = abilityInstances.get(ability);
            if (instance != null) {
               boolean tryResult = instance.tryAbility();
               if (tryResult) instance.start();
           }
           else System.out.println("BAB: Ability " + ability.toString() + " does not exist on mob " + obj.getClass().getSimpleName());
    }

    @Override
    public void instanceAbilities() {
        for (AbilityType<?> abilityType : getAbilityTypes()) {
              Ability ability = abilityType.makeInstance(obj);
              abilityInstances.put(abilityType, ability);
               if (nbtMap.containsKey(abilityType.getName())) ability.readNBT(nbtMap.get(abilityType.getName()));
        }
    }

    //do we need this?
    @Override
    public void tick(LivingEntity entity) {
        for (Ability ability : abilityInstances.values()) {
              ability.tick();
        }
    }

    @Override
    public AbilityType<?>[] getAbilityTypes() {
        return new AbilityType[0];
    }

    @Override
    public Map<AbilityType<?>, Ability> getAbilityMap() {
        return abilityInstances;
    }

    @Override
    public AbilityType<?>[] getAbilities() {
        return AbilityHandler.PLAYER_ABILITIES;
    }

    @Override
    public Ability getActiveAbility() {
        return activeAbility;
    }

    @Override
    public void setActiveAbility(Ability activeAbility) {
        if (getActiveAbility() != null && getActiveAbility().isUsing()) getActiveAbility().interrupt();
           this.activeAbility = activeAbility;
    }

    @Override
    public boolean attackingPrevented() {
        return getActiveAbility() != null && getActiveAbility().preventsAttacking();
    }

    @Override
    public boolean blockBreakingBuildingPrevented() {
        return getActiveAbility() != null && getActiveAbility().preventsBlockBreakingBuilding();
    }

    @Override
    public boolean interactingPrevented() {
        return getActiveAbility() != null && getActiveAbility().preventsInteracting();
    }

    @Override
    public <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> e, GeckoLibPlayer.Perspective perspective) {
        return getActiveAbility().animationPredicate(e, perspective);
    }

    @Override
    public void codeAnimations(BABGeoModel<? extends IAnimatable> model, float partialTick) {
        getActiveAbility().codeAnimations(model, partialTick);
    }

}
