package Harmonizer;

import java.io.Closeable;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class PollManager implements Closeable {

    public class PollCloseRunnable implements Runnable {
        private Message oldMessage;

        public PollCloseRunnable(Message message) {
            super();
            oldMessage = message;
        }

        @Override
        public void run() {
            Message message = oldMessage.getChannel().getHistoryAround(oldMessage, 1).complete().getMessageById(oldMessage.getId());
            
            // if embed was deleted, the poll is cancelled
            if (message.getEmbeds().size() == 0) {
                return;
            }

            HashMap<String, Integer> results = new HashMap<String, Integer>();
            for (MessageReaction reaction : message.getReactions()) {
                ImmutableMap.Builder<String, String> letterMapBuilder = ImmutableMap.builder();
                for (int i = 0; i < PollListener.letters.length; i++) {
                    letterMapBuilder.put(PollListener.letters[i], String.valueOf(((char)(64 + i + 1))));
                }
                ImmutableMap<String, String> letterMap = letterMapBuilder.build();

                results.put(letterMap.get(reaction.getReactionEmote().getName()), reaction.getCount() - 1);
            }

            float totalResponses = results.values().stream().mapToInt(i->i).sum();
            int winnerNum = 0;
            float winnerPercent = 0f;

            // edit original message
            MessageEmbed originalEmbed = message.getEmbeds().get(0);
            EmbedBuilder builder = new EmbedBuilder(originalEmbed);
            builder.setAuthor(originalEmbed.getAuthor().getName() + " | POLL CLOSED");
            builder.setFooter("Poll closed at:");
            for (int i = 0; i < builder.getFields().size(); i++) {
                Field oldField = builder.getFields().get(i);
                String fieldLetter = oldField.getName().substring(oldField.getName().length() - 1);
                float percent = (float)(100f * results.get(fieldLetter)) / totalResponses;
                if (percent > winnerPercent) {
                    winnerPercent = percent;
                    winnerNum = i;
                }
                builder.getFields().set(i, new Field(oldField.getName() + " - " + String.format("%.0f%%", percent), oldField.getValue(), true));
            }
            message.editMessage(builder.build()).queue();

            message.getChannel().sendMessage(String.format("Poll \"%s\" has finished. \"%s\" won with %.0f%% of the vote.",
                originalEmbed.getAuthor().getName(),
                originalEmbed.getFields().get(winnerNum).getValue(),
                winnerPercent
            )).queue();
        }
    }

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public void addPoll(Message message, Duration closeTime) {
        executorService.schedule(
            new PollCloseRunnable(message),
            closeTime.toSeconds(),
            TimeUnit.SECONDS
        );
    }

    @Override
    public void close() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            } 
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
