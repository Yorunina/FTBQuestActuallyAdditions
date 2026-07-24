package net.yorunina.maa.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

import java.util.*;
import java.util.stream.Collectors;

public class MobBattleUtil {
    private static final UUID FOLLOW_MODIFIER_UUID = UUID.fromString("c856213f-f50d-46a0-a19e-5672ee2b4ae9");
    private static final String AI_ADDED_TAG = "maa_mobbattle_ai_added";
    private static final String FRIENDLY_TO_PLAYERS_TAG = "maa_friendly_to_players";


    public static void setEntityFriendlyToPlayers(Mob entity) {
        entity.addTag(FRIENDLY_TO_PLAYERS_TAG);
        updateEntityAI(entity);
        clearAttackTarget(entity);
    }

    public static void removeFriendlyToPlayers(Mob entity) {
        entity.removeTag(FRIENDLY_TO_PLAYERS_TAG);
        updateEntityAI(entity);
    }

    public static boolean isFriendlyToPlayers(LivingEntity entity) {
        return entity.getTags().contains(FRIENDLY_TO_PLAYERS_TAG);
    }

    public static void setEntityNoAI(Mob entity) {
        entity.setNoAi(true);
        entity.setTarget(null);
    }

    public static void restoreEntityAI(Mob entity) {
        entity.setNoAi(false);
    }

    public static void addEntitiesToTeam(Level level, String teamName, Mob... entities) {
        if (entities == null || entities.length == 0) return;
        Scoreboard scoreboard = level.getScoreboard();
        PlayerTeam team = getOrCreateTeam(scoreboard, teamName);
        for (Mob entity : entities) {
            scoreboard.addPlayerToTeam(entity.getStringUUID(), team);
            updateEntityAI(entity);
        }
    }

    public static void setEntityAttackTarget(Mob attacker, LivingEntity target) {
        setTargetTo(attacker, target);
        increaseFollowRange(attacker);
    }

    public static void setTeamsHostile(ServerLevel level, String teamName1, String teamName2) {
        List<Mob> team1Mobs = getMobsOnTeam(level, teamName1);
        List<Mob> team2Mobs = getMobsOnTeam(level, teamName2);
        if (team1Mobs.isEmpty() || team2Mobs.isEmpty()) return;

        for (Mob mob1 : team1Mobs) {
            increaseFollowRange(mob1);
            setTargetTo(mob1, team2Mobs.get(level.random.nextInt(team2Mobs.size())));
        }
        for (Mob mob2 : team2Mobs) {
            increaseFollowRange(mob2);
            setTargetTo(mob2, team1Mobs.get(level.random.nextInt(team1Mobs.size())));
        }
    }

    public static boolean isOnSameTeam(Entity entity1, Entity entity2) {
        Team team1 = entity1.getTeam();
        Team team2 = entity2.getTeam();
        return team1 != null && team1 == team2;
    }

    public static String getEntityTeamName(Entity entity) {
        Team team = entity.getTeam();
        return team != null ? team.getName() : null;
    }

    public static boolean hasBattleAI(Mob entity) {
        return entity.getTags().contains(AI_ADDED_TAG);
    }

    public static void removeEntityFromTeam(Entity entity) {
        Team team = entity.getTeam();
        if (team != null) {
            entity.level().getScoreboard().removePlayerFromTeam(entity.getStringUUID(), (PlayerTeam) team);
            if (entity instanceof Mob mob) {
                removeCustomAI(mob);
            }
        }
    }

    public static List<Mob> getMobsOnTeam(ServerLevel level, String teamName) {
        Scoreboard scoreboard = level.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) return Collections.emptyList();

        List<Mob> result = new ArrayList<>();
        for (String entry : team.getPlayers()) {
            try {
                UUID uuid = UUID.fromString(entry);
                Entity entity = level.getEntity(uuid);
                if (entity instanceof Mob mob && entity.isAlive()) {
                    result.add(mob);
                }
            } catch (IllegalArgumentException ignored) {
                // 非UUID条目（如玩家名），跳过
            }
        }
        return result;
    }


    public static void clearBattleAI(Mob entity) {
        removeCustomAI(entity);
        entity.setTarget(null);
    }

    public static void removeTeam(ServerLevel level, String teamName) {
        Scoreboard scoreboard = level.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) return;

        for (String entry : new ArrayList<>(team.getPlayers())) {
            try {
                UUID uuid = UUID.fromString(entry);
                Entity entity = level.getEntity(uuid);
                if (entity instanceof Mob mob) {
                    removeCustomAI(mob);
                    mob.setTarget(null);
                }
            } catch (IllegalArgumentException ignored) {
                // 非UUID条目（如玩家名），跳过
            }
        }
        scoreboard.removePlayerTeam(team);
    }

    private static PlayerTeam getOrCreateTeam(Scoreboard scoreboard, String teamName) {
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setAllowFriendlyFire(false);
            team.setCollisionRule(Team.CollisionRule.PUSH_OTHER_TEAMS);
        }
        return team;
    }


    private static void updateEntityAI(Mob entity) {
        entity.setTarget(null);
        entity.addTag(AI_ADDED_TAG);

        GoalSelector targetSelector = entity.targetSelector;

        removeGoalsByClass(targetSelector, TeamHurtByTargetGoal.class);
        removeGoalsByClass(targetSelector, TeamNearestAttackableTargetGoal.class);

        if (entity instanceof PathfinderMob pathfinder) {
            targetSelector.addGoal(1, new TeamHurtByTargetGoal(pathfinder));
        }
        targetSelector.addGoal(2, new TeamNearestAttackableTargetGoal<>(entity, Mob.class, true));
        if (!isFriendlyToPlayers(entity)) {
            targetSelector.addGoal(3, new TeamNearestAttackableTargetGoal<>(entity, Player.class, true));
        }
    }

    private static void removeCustomAI(Mob entity) {
        entity.removeTag(AI_ADDED_TAG);
        entity.removeTag(FRIENDLY_TO_PLAYERS_TAG);
        removeGoalsByClass(entity.targetSelector, TeamHurtByTargetGoal.class);
        removeGoalsByClass(entity.targetSelector, TeamNearestAttackableTargetGoal.class);
        removeFollowRangeModifier(entity);
    }

    private static void removeGoalsByClass(GoalSelector goalSelector, Class<? extends Goal> goalClass) {
        Set<Goal> toRemove = goalSelector.getAvailableGoals().stream()
                .map(WrappedGoal::getGoal)
                .filter(goalClass::isInstance)
                .collect(Collectors.toSet());
        toRemove.forEach(goalSelector::removeGoal);
    }

    private static void increaseFollowRange(Mob mob) {
        AttributeInstance att = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (att != null && att.getModifier(FOLLOW_MODIFIER_UUID) == null) {
            att.addTransientModifier(new AttributeModifier(FOLLOW_MODIFIER_UUID, "maa_mob_battle_follow", 64, AttributeModifier.Operation.ADDITION));
        }
    }

    private static void removeFollowRangeModifier(Mob mob) {
        AttributeInstance att = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (att != null && att.getModifier(FOLLOW_MODIFIER_UUID) != null) {
            att.removeModifier(FOLLOW_MODIFIER_UUID);
        }
    }

    private static void clearAttackTarget(Mob entity) {
        entity.setTarget(null);
        entity.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }

    private static void setTargetTo(Mob entity, LivingEntity target) {
        if (target == null) return;
        entity.setTarget(target);
        entity.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, target, 600);

        if (entity instanceof Warden warden) {
            warden.increaseAngerAt(target, AngerLevel.ANGRY.getMinimumAnger() + 20, false);
            warden.setAttackTarget(target);
        }
    }

    private static boolean isValidBattleTarget(Mob mob, LivingEntity target) {
        if (target == mob) return false;
        if (isOnSameTeam(mob, target)) return false;
        if (target instanceof Player && isFriendlyToPlayers(mob)) return false;
        return true;
    }

    private static class TeamHurtByTargetGoal extends HurtByTargetGoal {
        public TeamHurtByTargetGoal(PathfinderMob mob, Class<?>... toIgnoreDamage) {
            super(mob, toIgnoreDamage);
        }

        @Override
        public boolean canUse() {
            LivingEntity attacker = this.mob.getLastHurtByMob();
            if (attacker == null) return false;
            if (isOnSameTeam(this.mob, attacker)) return false;
            if (attacker instanceof Player && isFriendlyToPlayers(this.mob)) return false;
            return super.canUse();
        }
    }

    private static class TeamNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        public TeamNearestAttackableTargetGoal(Mob mob, Class<T> targetClass, boolean mustSee) {
            super(mob, targetClass, 10, mustSee, false, target -> isValidBattleTarget(mob, target));
        }
    }
}