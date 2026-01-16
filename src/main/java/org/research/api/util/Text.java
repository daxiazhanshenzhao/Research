package org.research.api.util;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.research.Research;

import java.util.UUID;

@Mod.EventBusSubscriber
public class Text {
    @SubscribeEvent
    public static void rightClick(PlayerInteractEvent.RightClickItem event) {
        Item item = event.getItemStack().getItem();
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ResearchApi.getTechTreeData(serverPlayer).ifPresent(techTree -> {
                if (item.equals(Items.STICK) ) {
                    Research.LOGGER.info(techTree.serializeNBT().toString());
                }
                if (item.equals(Items.IRON_INGOT)) {
                    // 在玩家前方2格位置召唤铁傀儡
                    Vec3 spawnPos = serverPlayer.position().add(serverPlayer.getLookAngle().scale(2.0));
                    summonIronGolem(serverPlayer, spawnPos, true);
                }

                if (item.equals(Items.GOLD_INGOT)) {
                    // 在玩家前方3格位置召唤僵尸
                    Vec3 spawnPos = serverPlayer.position().add(serverPlayer.getLookAngle().scale(3.0));
                    summonHostileMob(serverPlayer, spawnPos, EntityType.ZOMBIE);
                }

            });

        }
    }


    /**
     * 玩家受到攻击事件
     * 召唤仆从生物保护玩家，并标记攻击者作为目标
     */
    @SubscribeEvent
    public static void onPlayerHurt(LivingAttackEvent event) {
        // 确保是玩家受伤且在服务器端
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        DamageSource damageSource = event.getSource();
        Entity attacker = damageSource.getEntity();

        // 确保攻击者是生物实体
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }

        // 不处理玩家攻击玩家的情况（可选）
        if (livingAttacker instanceof ServerPlayer) {
            return;
        }

        // 在玩家附近召唤仆从（玩家和攻击者之间）
        Vec3 playerPos = player.position();
        Vec3 attackerPos = livingAttacker.position();
        Vec3 spawnPos = playerPos.add(attackerPos.subtract(playerPos).normalize().scale(2.0));

        // 召唤僵尸守卫
        Entity servant = summonServant(player, spawnPos, livingAttacker, EntityType.ZOMBIE);

        if (servant != null) {
            player.sendSystemMessage(Component.literal("§6召唤了守卫仆从协助战斗！"));
            Research.LOGGER.info("玩家 {} 受到 {} 攻击，召唤守卫仆从 {}",
                player.getName().getString(),
                livingAttacker.getName().getString(),
                servant.getName().getString());
        }
    }

    /**
     * 监听实体更新事件，检查召唤的仆从是否应该消失
     */
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        CompoundTag data = mob.getPersistentData();

        // 检查是否是仆从生物
        if (data.getBoolean("IsServant")) {
            // 每20 tick（1秒）检查一次
            if (mob.tickCount % 20 != 0) {
                return;
            }

            // 检查是否是永久仆从（右键召唤的不会消失）
            boolean isPermanent = data.getBoolean("IsPermanent");

            // 如果不是永久仆从，检查目标是否还存在
            if (!isPermanent && data.contains("ServantTarget")) {
                UUID targetUUID = data.getUUID("ServantTarget");
                ServerLevel serverLevel = (ServerLevel) mob.level();
                Entity target = serverLevel.getEntity(targetUUID);

                // 如果目标死亡或不存在，移除仆从
                if (target == null || !target.isAlive()) {
                    mob.remove(Entity.RemovalReason.DISCARDED);
                    Research.LOGGER.info("仆从 {} 的目标已消失，移除仆从", mob.getName().getString());
                    return;
                }

                // 如果仆从没有目标，设置攻击目标
                if (mob.getTarget() == null || !mob.getTarget().isAlive()) {
                    if (target instanceof LivingEntity livingTarget) {
                        mob.setTarget(livingTarget);
                    }
                }
            }

            // 检查主人是否还存在
            UUID ownerUUID = data.getUUID("ServantOwner");
            ServerLevel serverLevel = (ServerLevel) mob.level();
            Entity ownerEntity = serverLevel.getEntity(ownerUUID);

            // 如果主人存在且距离合理，自动寻找攻击主人的敌人
            if (ownerEntity instanceof ServerPlayer owner && mob.distanceTo(owner) <= 50) {
                // 如果当前目标死亡，寻找攻击主人的新敌人
                if (mob.getTarget() == null || !mob.getTarget().isAlive()) {
                    LivingEntity lastHurtBy = owner.getLastHurtByMob();
                    if (lastHurtBy != null && lastHurtBy.isAlive() && mob.distanceTo(lastHurtBy) < 16) {
                        mob.setTarget(lastHurtBy);
                        // 更新目标UUID（临时仆从）

                        if (!isPermanent) {
                            data.putUUID("ServantTarget", lastHurtBy.getUUID());
                        }
                        Research.LOGGER.info("仆从 {} 切换目标到：{}",
                            mob.getName().getString(),
                            lastHurtBy.getName().getString());
                    }
                }
            }
        }
    }

    /**
     * 防止仆从攻击主人
     */
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        // 检查是否是玩家的仆从
        CompoundTag data = mob.getPersistentData();
        if (!data.getBoolean("IsServant")) {
            return;
        }

        // 获取目标
        LivingEntity newTarget = event.getNewTarget();
        if (newTarget == null) {
            return;
        }

        // 获取主人UUID
        UUID ownerUUID = data.getUUID("ServantOwner");

        // 如果目标是主人，取消攻击
        if (newTarget.getUUID().equals(ownerUUID)) {
            event.setCanceled(true);
            mob.setTarget(null);
            Research.LOGGER.info("仆从 {} 尝试攻击主人，已取消", mob.getName().getString());
        }
    }

    /**
     * 召唤一个仆从生物，专门攻击指定目标
     * @param player 召唤者（玩家）
     * @param position 召唤位置
     * @param target 要攻击的目标
     * @param entityType 要召唤的生物类型（如 EntityType.ZOMBIE, EntityType.SKELETON 等）
     * @return 召唤的仆从，失败返回null
     */
    private static Entity summonServant(ServerPlayer player, Vec3 position, LivingEntity target, EntityType<? extends Entity> entityType) {
        try {
            BlockPos blockPos = BlockPos.containing(position);

            // 检查位置是否有效
            if (!Level.isInSpawnableBounds(blockPos)) {
                return null;
            }

            ServerLevel serverLevel = player.serverLevel();

            // 创建实体
            Entity entity = entityType.create(serverLevel);

            if (entity == null) {
                return null;
            }

            // 设置位置和朝向（面向攻击者）
            Vec3 lookDir = target.position().subtract(position).normalize();
            float yaw = (float) (Math.atan2(lookDir.z, lookDir.x) * 180.0 / Math.PI) - 90.0F;
            entity.moveTo(position.x, position.y, position.z, yaw, 0.0F);

            // 如果是生物，进行特殊设置
            if (entity instanceof Mob mob) {
                // 随机化属性
                ForgeEventFactory.onFinalizeSpawn(
                    mob,
                    serverLevel,
                    serverLevel.getCurrentDifficultyAt(blockPos),
                    MobSpawnType.MOB_SUMMONED,
                    null,
                    null
                );

                // 设置为持久化（不会自然消失）
                mob.setPersistenceRequired();

                // 标记为仆从生物
                CompoundTag data = mob.getPersistentData();
                data.putBoolean("IsServant", true);
                data.putUUID("ServantTarget", target.getUUID());
                data.putUUID("ServantOwner", player.getUUID());
                data.putString("OwnerName", player.getName().getString());

                // 设置攻击目标
                mob.setTarget(target);

                // 如果是驯服类生物，设置为驯服状态
                if (mob instanceof TamableAnimal tamable) {
                    tamable.tame(player);
                    tamable.setOwnerUUID(player.getUUID());
                }
            }

            // 尝试添加实体到世界
            if (!serverLevel.tryAddFreshEntityWithPassengers(entity)) {
                return null;
            }

            return entity;

        } catch (Exception e) {
            Research.LOGGER.error("召唤仆从失败", e);
            return null;
        }
    }

    /**
     * 召唤一个守卫铁傀儡，专门攻击指定目标
     * @param player 被保护的玩家
     * @param position 召唤位置
     * @param target 要攻击的目标
     * @return 召唤的铁傀儡，失败返回null
     */
    private static IronGolem summonGuardianGolem(ServerPlayer player, Vec3 position, LivingEntity target) {
        try {
            BlockPos blockPos = BlockPos.containing(position);

            // 检查位置是否有效
            if (!Level.isInSpawnableBounds(blockPos)) {
                return null;
            }

            ServerLevel serverLevel = player.serverLevel();

            // 创建铁傀儡实体
            IronGolem ironGolem = EntityType.IRON_GOLEM.create(serverLevel);

            if (ironGolem == null) {
                return null;
            }

            // 设置位置和朝向（面向攻击者）
            Vec3 lookDir = target.position().subtract(position).normalize();
            float yaw = (float) (Math.atan2(lookDir.z, lookDir.x) * 180.0 / Math.PI) - 90.0F;
            ironGolem.moveTo(position.x, position.y, position.z, yaw, 0.0F);

            // 设置为玩家创建（不会攻击玩家）
            ironGolem.setPlayerCreated(true);

            // 随机化属性
            ForgeEventFactory.onFinalizeSpawn(
                ironGolem,
                serverLevel,
                serverLevel.getCurrentDifficultyAt(blockPos),
                MobSpawnType.MOB_SUMMONED,
                null,
                null
            );

            // 标记为守卫生物
            CompoundTag data = ironGolem.getPersistentData();
            data.putBoolean("IsGuardian", true);
            data.putUUID("GuardianTarget", target.getUUID());
            data.putUUID("GuardianOwner", player.getUUID());

            // 设置攻击目标
            ironGolem.setTarget(target);

            // 尝试添加实体到世界
            if (!serverLevel.tryAddFreshEntityWithPassengers(ironGolem)) {
                return null;
            }

            return ironGolem;

        } catch (Exception e) {
            Research.LOGGER.error("召唤守卫铁傀儡失败", e);
            return null;
        }
    }


    /**
     * 召唤一个铁傀儡
     * @param player 召唤者（玩家）
     * @param position 召唤位置
     * @param randomizeProperties 是否随机化属性
     * @return 是否成功召唤
     */
    public static boolean summonIronGolem(ServerPlayer player, Vec3 position, boolean randomizeProperties) {
        try {
            BlockPos blockPos = BlockPos.containing(position);

            // 检查位置是否有效
            if (!Level.isInSpawnableBounds(blockPos)) {
                player.sendSystemMessage(Component.literal("无效的召唤位置！"));
                return false;
            }

            ServerLevel serverLevel = player.serverLevel();

            // 创建铁傀儡实体
            IronGolem ironGolem = EntityType.IRON_GOLEM.create(serverLevel);

            if (ironGolem == null) {
                player.sendSystemMessage(Component.literal("创建铁傀儡失败！"));
                return false;
            }

            // 设置位置和朝向
            ironGolem.moveTo(position.x, position.y, position.z, player.getYRot(), 0.0F);

            // 设置为玩家创建（不会攻击玩家）
            ironGolem.setPlayerCreated(true);

            // 如果需要随机化属性（难度相关）
            if (randomizeProperties) {
                ForgeEventFactory.onFinalizeSpawn(
                    ironGolem,
                    serverLevel,
                    serverLevel.getCurrentDifficultyAt(blockPos),
                    MobSpawnType.COMMAND,
                    null,
                    null
                );
            }

            // 尝试添加实体到世界
            if (!serverLevel.tryAddFreshEntityWithPassengers(ironGolem)) {
                player.sendSystemMessage(Component.literal("添加铁傀儡到世界失败（可能UUID重复）！"));
                return false;
            }

            // 成功消息
            player.sendSystemMessage(Component.literal("成功召唤铁傀儡！"));
            Research.LOGGER.info("玩家 {} 在 {} 召唤了铁傀儡", player.getName().getString(), blockPos);

            return true;

        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("召唤铁傀儡时发生错误：" + e.getMessage()));
            Research.LOGGER.error("召唤铁傀儡失败", e);
            return false;
        }
    }

    /**
     * 召唤一个守卫生物（右键召唤，不会消失）
     * @param player 召唤者（玩家）
     * @param position 召唤位置
     * @param entityType 要召唤的生物类型（如 EntityType.ZOMBIE, EntityType.SKELETON 等）
     * @return 召唤的生物实体，失败返回null
     */
    public static Entity summonHostileMob(ServerPlayer player, Vec3 position, EntityType<? extends Entity> entityType) {
        try {
            BlockPos blockPos = BlockPos.containing(position);

            // 检查位置是否有效
            if (!Level.isInSpawnableBounds(blockPos)) {
                player.sendSystemMessage(Component.literal("§c无效的召唤位置！"));
                return null;
            }

            ServerLevel serverLevel = player.serverLevel();

            // 创建实体
            Entity entity = entityType.create(serverLevel);

            if (entity == null) {
                player.sendSystemMessage(Component.literal("§c创建生物失败！"));
                return null;
            }

            // 设置位置和朝向（面向玩家朝向的方向）
            entity.moveTo(position.x, position.y, position.z, player.getYRot(), 0.0F);

            // 如果是生物，进行特殊设置
            if (entity instanceof Mob mob) {
                // 随机化属性
                ForgeEventFactory.onFinalizeSpawn(
                    mob,
                    serverLevel,
                    serverLevel.getCurrentDifficultyAt(blockPos),
                    MobSpawnType.COMMAND,
                    null,
                    null
                );

                // 设置为持久化（不会自然消失）
                mob.setPersistenceRequired();

                // 标记为玩家的仆从（永久仆从，不会因目标死亡消失）
                CompoundTag data = mob.getPersistentData();
                data.putBoolean("IsServant", true);
                data.putBoolean("IsPermanent", true); // 标记为永久仆从
                data.putUUID("ServantOwner", player.getUUID());
                data.putString("OwnerName", player.getName().getString());

                // 如果是驯服类生物，设置为驯服状态
                if (mob instanceof TamableAnimal tamable) {
                    tamable.tame(player);
                    tamable.setOwnerUUID(player.getUUID());
                }
            }

            // 尝试添加实体到世界
            if (!serverLevel.tryAddFreshEntityWithPassengers(entity)) {
                player.sendSystemMessage(Component.literal("§c添加生物到世界失败（可能UUID重复）！"));
                return null;
            }

            // 成功消息
            String entityName = entity.getType().getDescription().getString();
            player.sendSystemMessage(Component.literal("§6成功召唤守卫 " + entityName + "！"));
            Research.LOGGER.info("玩家 {} 在 {} 召唤了守卫 {}",
                player.getName().getString(),
                blockPos,
                entityName);

            return entity;

        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("§c召唤生物时发生错误：" + e.getMessage()));
            Research.LOGGER.error("召唤生物失败", e);
            return null;
        }
    }
}
