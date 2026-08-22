package com.blazelow.mobsaregone.command;

import com.blazelow.mobsaregone.MobsAreGone;
import com.blazelow.mobsaregone.config.BlockedEntitiesConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;

import java.util.HashSet;

public class ReloadCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("mobsaregone")
                .requires(Commands.hasPermission(PermissionCheck.Require.of(2)))
                .then(Commands.literal("reload")
                    .executes(ctx -> {
                        MobsAreGone.setBlockedEntities(new HashSet<>(BlockedEntitiesConfig.load()));
                        int count = MobsAreGone.getBlockedCount();
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("[MobsAreGone] Blacklist reloaded — blocking " + count + " entity type(s)."),
                            true
                        );
                        MobsAreGone.LOGGER.info("MobsAreGone >> Blacklist reloaded via command — blocking {} entity type(s).", count);
                        return 1;
                    })
                )
        );
    }
}
