package Harmonizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.security.auth.login.LoginException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class App {
    protected static PollManager pollManager;
    protected static GCalManager gCalManager;

    public static final String SETTINGS_FILE_PATH = "/settings.json";

    public static void main(String[] args) {
        pollManager = new PollManager();
        gCalManager = new GCalManager();
        try {
            JDA jda = new JDABuilder(getDiscordClientToken()).build();
            jda.addEventListener(new PollListener());
            jda.addEventListener(new EventListener());
            jda.addEventListener(new HelpListener());
            jda.awaitReady();
            jda.getPresence().setActivity(Activity.watching("!h help"));
            System.out.println("Bot started!");
        } catch (LoginException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
        }
    }

    private static String getDiscordClientToken() {
        InputStream in = GCalManager.class.getResourceAsStream(SETTINGS_FILE_PATH);
        try {
            JSONObject object = (JSONObject)(new JSONParser().parse(new InputStreamReader(in)));
            return (String)object.get("discord_token");
        } catch (ParseException ex) {
            System.out.println("Could not parse settings.json");
            System.exit(1);
            return null;
        } catch (IOException ex) {
            System.out.println("Could not read settings.json");
            System.exit(1);
            return null;
        }
    }
}
