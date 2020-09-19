//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package com.intellectualsites.commands.bukkit;

import com.google.common.reflect.TypeToken;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.CommandTree;
import com.intellectualsites.commands.bukkit.parsers.MaterialArgument;
import com.intellectualsites.commands.bukkit.parsers.WorldArgument;
import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Command manager for the Bukkit platform, using {@link BukkitCommandSender} as the
 * command sender type
 *
 * @param <C> Command sender type
 */
public class BukkitCommandManager<C> extends CommandManager<C> {

    private final Plugin owningPlugin;

    private final Function<CommandSender, C> commandSenderMapper;
    private final Function<C, CommandSender> backwardsCommandSenderMapper;

    /**
     * Construct a new Bukkit command manager
     *
     * @param owningPlugin                 Plugin that is constructing the manager
     * @param commandExecutionCoordinator  Coordinator provider
     * @param commandSenderMapper          Function that maps {@link CommandSender} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link CommandSender}
     * @throws Exception If the construction of the manager fails
     */
    public BukkitCommandManager(@Nonnull final Plugin owningPlugin,
                                @Nonnull final Function<CommandTree<C>,
                                        CommandExecutionCoordinator<C>> commandExecutionCoordinator,
                                @Nonnull final Function<CommandSender, C> commandSenderMapper,
                                @Nonnull final Function<C, CommandSender> backwardsCommandSenderMapper)
            throws Exception {
        super(commandExecutionCoordinator, new BukkitPluginRegistrationHandler<>());
        ((BukkitPluginRegistrationHandler<C>) this.getCommandRegistrationHandler()).initialize(this);
        this.owningPlugin = owningPlugin;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;

        /* Register Bukkit parsers */
        this.getParserRegistry().registerParserSupplier(TypeToken.of(World.class), params -> new WorldArgument.WorldParser<>());
        this.getParserRegistry().registerParserSupplier(TypeToken.of(Material.class),
                                                        params -> new MaterialArgument.MaterialParser<>());
    }

    /**
     * Get the plugin that owns the manager
     *
     * @return Owning plugin
     */
    @Nonnull
    public Plugin getOwningPlugin() {
        return this.owningPlugin;
    }

    /**
     * Create default command meta data
     *
     * @return Meta data
     */
    @Nonnull
    @Override
    public BukkitCommandMeta createDefaultCommandMeta() {
        return BukkitCommandMetaBuilder.builder().withDescription("").build();
    }

    /**
     * Get the command sender mapper
     *
     * @return Command sender mapper
     */
    @Nonnull
    public final Function<CommandSender, C> getCommandSenderMapper() {
        return this.commandSenderMapper;
    }

    @Override
    public final boolean hasPermission(@Nonnull final C sender, @Nonnull final String permission) {
        return this.backwardsCommandSenderMapper.apply(sender).hasPermission(permission);
    }

}