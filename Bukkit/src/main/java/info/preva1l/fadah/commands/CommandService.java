package info.preva1l.fadah.commands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.commands.parsers.ColoringFormatter;
import info.preva1l.fadah.config.Lang;
import info.preva1l.trashcan.extension.annotations.ExtensionReload;
import info.preva1l.trashcan.flavor.annotations.Configure;
import info.preva1l.trashcan.flavor.annotations.Service;
import info.preva1l.trashcan.flavor.annotations.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.exception.*;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.minecraft.extras.caption.ComponentCaptionFormatter;
import org.incendo.cloud.minecraft.extras.caption.RichVariable;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

@Service(priority = 2)
public final class CommandService {
    public static final CommandService instance = new CommandService();

    @Inject private Fadah plugin;

    private LegacyPaperCommandManager<CommandSender> commandManager;
    private AnnotationParser<CommandSender> parser;

    private AuctionHouseCommand mainCommand;

    @Configure
    public void configure() {
        loadCommandManager();
        registerCommands();
    }

    @ExtensionReload
    public void reload() {
        mainCommand.reload();
    }

    private void registerCommands() {
        mainCommand = new AuctionHouseCommand(plugin, commandManager);
        parser.parse(new MigrateCommand());
    }

    private void loadCommandManager() {
        commandManager = LegacyPaperCommandManager.createNative(plugin, ExecutionCoordinator.asyncCoordinator());
        parser = new AnnotationParser<>(commandManager, CommandSender.class);

        commandManager.captionRegistry().registerProvider((caption, recipient) -> switch (caption.key()) {
            case "exception.invalid_syntax" -> Lang.i().getPrefix() + Lang.i().getErrors().getInvalidUsage();
            case "exception.invalid_argument" -> Lang.i().getPrefix() + Lang.i().getErrors().getInvalidArgument();
            case "exception.no_permission" -> Lang.i().getPrefix() + Lang.i().getErrors().getNoPermission();
            case "exception.no_such_command" -> Lang.i().getPrefix() + Lang.i().getErrors().getCommandNotFound();
            case "exception.invalid_sender" -> Lang.i().getPrefix() + Lang.i().getErrors().getMustBePlayer();
            default -> null;
        });

        ColoringFormatter formatter = new ColoringFormatter();
        MinecraftExceptionHandler.<CommandSender>createNative()
                .captionFormatter(formatter)
                .handler(InvalidSyntaxException.class, (sender, ctx) -> ctx.context()
                        .formatCaption(
                                formatter,
                                StandardCaptionKeys.EXCEPTION_INVALID_SYNTAX,
                                CaptionVariable.of("syntax", ctx.exception().correctSyntax())
                        ))
                .handler(ArgumentParseException.class, (sender, ctx) -> ctx.context()
                        .formatCaption(
                                formatter,
                                StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT,
                                RichVariable.of("cause", getMessage(formatter, ctx.exception().getCause()))
                        ))
                .handler(NoPermissionException.class, (sender, ctx) -> ctx.context()
                        .formatCaption(
                                formatter,
                                StandardCaptionKeys.EXCEPTION_NO_PERMISSION,
                                CaptionVariable.of("permission", ctx.exception().permissionResult().permission().permissionString())
                        ))
                .handler(NoSuchCommandException.class, (sender, ctx) ->
                        ctx.context().formatCaption(formatter, StandardCaptionKeys.EXCEPTION_NO_SUCH_COMMAND))
                .handler(InvalidCommandSenderException.class, (sender, ctx) ->
                        ctx.context().formatCaption(formatter, StandardCaptionKeys.EXCEPTION_INVALID_SENDER))
                .registerTo(commandManager);
    }

    private <C> Component getMessage(final ComponentCaptionFormatter<C> formatter, final Throwable throwable) {
        if (throwable instanceof ParserException) {
            return ((ParserException) throwable).formatCaption(formatter);
        }
        final Component msg = ComponentMessageThrowable.getOrConvertMessage(throwable);
        return msg == null ? Component.text("null") : msg;
    }
}
