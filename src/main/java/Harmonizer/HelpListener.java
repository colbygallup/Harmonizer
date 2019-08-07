package Harmonizer;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class HelpListener extends ListenerAdapter
{
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.isFromType(ChannelType.TEXT))
        {
            if (event.getMessage().getContentDisplay().startsWith("!h help")) {
                String message = event.getMessage().getContentDisplay();
                String[] args = message.split(" ");

                String content = event.getAuthor().getAsMention() + "\n";
                if (args.length == 2) {  // general help
                    content += "`!h help` - Prints this help message\n" +
                        "`!h help <command>` - Prints detailed info for that command only\n" +
                        "```!h poll <question>\n<duration>\n<option 1>\n<option 2>\n...``` - Creates a poll\n" +
                        "```!h event <name>\n<start date/time>\n<end date/time>\n<description>``` - Creates an event, which syncs to Google Calendar";
                } else if (args.length == 3) {  // specific help
                    if (args[2].toLowerCase().equals("help")) {
                        content += "No help text for this command yet, it's pretty self-explanatory";
                    } else if (args[2].toLowerCase().equals("poll")) {
                        content += "```!h poll <question>\n<duration>\n<option 1>\n<option 2>\n...```\n";
                        content += "Creates a poll. It will close after `duration` and report the results in the chat. Each argument needs to be on its own line (hold Shift and press Enter to create a new line).\n\n";
                        content += "`duration` needs to specify units of time (m, h, d). For example, `30m` equals 30 minutes and `2d` equals two days.";
                    } else if (args[2].toLowerCase().equals("event")) {
                        content += "```!h event <name>\n<start date/time>\n<end date/time>\n<description>```\n";
                        content += "Creates an event, which syncs to Google Calendar.\n\n";
                        content += "Start and end times need to be in a specific format. Here are some examples:\n";
                        content += "  - `8/11` is August 11\n";
                        content += "  - `8/11/21` is August 11, 2021\n";
                        content += "  - `8/11 9:00 AM` is August 11 at 9:00 AM CST/CDT\n";
                        content += "If you have problems with this command, it's probably because you aren't following the date/time formats closely enough.";
                    }
                } else {  // too many arguments
                    content += "Too many arguments. Try just doing `!h help` instead.";
                }
                event.getChannel().sendMessage(content).queue();
            }
        }
    }
}
