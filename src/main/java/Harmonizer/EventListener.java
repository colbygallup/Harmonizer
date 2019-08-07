package Harmonizer;

import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListener extends ListenerAdapter
{
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.isFromType(ChannelType.TEXT))
        {
            if (event.getMessage().getContentDisplay().startsWith("!h event ")) {
                String message = event.getMessage().getContentDisplay();
                String[] lines = message.substring(9).split("\\r?\\n");
                if (lines.length != 4) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Wrong number of arguments").queue();
                    return;
                }

                LocalDate today = LocalDate.now();
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern("M/d[/uu][ h:mm a]")
                    .parseDefaulting(ChronoField.YEAR, today.getYear())
                    .toFormatter();

                LocalDateTime startTime;
                TemporalAccessor startTimeAccessor = formatter.parseBest(lines[1], LocalDateTime::from, LocalDate::from);
                if (startTimeAccessor instanceof LocalDateTime) {
                    startTime = (LocalDateTime)startTimeAccessor;
                } else {
                    startTime = ((LocalDate)startTimeAccessor).atStartOfDay();
                }

                LocalDateTime endTime;
                TemporalAccessor endTimeAccessor = formatter.parseBest(lines[2], LocalDateTime::from, LocalDate::from);
                if (endTimeAccessor instanceof LocalDateTime) {
                    endTime = (LocalDateTime)endTimeAccessor;
                } else {
                    endTime = ((LocalDate)endTimeAccessor).atStartOfDay();
                }

                DateTimeFormatterBuilder outputFormatterBuilder = new DateTimeFormatterBuilder();
                outputFormatterBuilder.appendPattern("EEEE, MMMM d");
                if (startTime.getYear() != today.getYear())
                    outputFormatterBuilder.appendPattern(" uuuu");
                if (startTime.getLong(ChronoField.NANO_OF_DAY) != 0)
                    outputFormatterBuilder.appendPattern(" 'at' h:mm a");
                DateTimeFormatter outputFormatter = outputFormatterBuilder.toFormatter();

                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.CYAN);
                builder.setAuthor(lines[0]);
                builder.addField("Start time", startTime.format(outputFormatter), true);
                builder.addField("End time", endTime.format(outputFormatter), true);
                builder.addField("Description", lines[3], false);
                event.getChannel().sendMessage(builder.build()).queue(outMessage -> {
                    outMessage.addReaction("\uD83D\uDC4D").queue();
                    outMessage.addReaction("\uD83D\uDC4E").queue();
                });

                App.gCalManager.addEvent(lines[0], startTime, endTime, lines[3]);

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
