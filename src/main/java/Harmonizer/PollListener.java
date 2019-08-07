package Harmonizer;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

public class PollListener extends ListenerAdapter
{
    // Found at https://gist.github.com/jamietech/7c0b01be2ff6439c97fbec55e82daad5
    public static final String[] letters = new String[] { "\uD83C\uDDE6", "\uD83C\uDDE7", "\uD83C\uDDE8", "\uD83C\uDDE9", "\uD83C\uDDEA", "\uD83C\uDDEB", "\uD83C\uDDEC", "\uD83C\uDDED", "\uD83C\uDDEE", "\uD83C\uDDEF", "\uD83C\uDDF0", "\uD83C\uDDF1", "\uD83C\uDDF2", "\uD83C\uDDF3", "\uD83C\uDDF4", "\uD83C\uDDF5", "\uD83C\uDDF6", "\uD83C\uDDF7", "\uD83C\uDDF8", "\uD83C\uDDF9", "\uD83C\uDDFA", "\uD83C\uDDFB", "\uD83C\uDDFC", "\uD83C\uDDFD", "\uD83C\uDDFE", "\uD83C\uDDFF" };

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.isFromType(ChannelType.TEXT))
        {
            if (event.getMessage().getContentDisplay().startsWith("!h poll ")) {
                String message = event.getMessage().getContentDisplay();
                String[] lines = message.substring(8).split("\\r?\\n");
                if (lines.length < 4) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Wrong number of arguments").queue();
                    return;
                }

                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.CYAN);
                builder.setAuthor(lines[0]);
                // lines[1] is time
                String[] options = Arrays.copyOfRange(lines, 2, lines.length);
                for (int i = 1; i <= options.length; i++) {
                    builder.addField("Option " + (char)(64 + i), options[i - 1], true);
                }
                builder.setFooter("Poll close time:");
                Duration duration = Duration.of(Long.valueOf(lines[1].substring(0, lines[1].length() - 1)), ChronoUnit.MINUTES);
                Instant closeTime = Instant.now().plus(duration);
                builder.setTimestamp(closeTime);
                event.getChannel().sendMessage(builder.build()).queue(outMessage -> {
                    for (int i = 0; i < options.length; i++) {
                        outMessage.addReaction(letters[(char)i]).queue();
                    }
                    App.pollManager.addPoll(outMessage, duration);
                });

                Member selfMember = event.getGuild().getSelfMember();
                if (!selfMember.hasPermission(Permission.MESSAGE_MANAGE))
                {
                    System.out.println("Missing permission to delete messages");
                    return;
                }
                event.getChannel().deleteMessageById(event.getMessageId()).queue();
            }
        }
    }
}
