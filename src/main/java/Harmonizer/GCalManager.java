package Harmonizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GCalManager {
    private static final String APPLICATION_NAME = "Harmonizer";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private Calendar service = null;

    public GCalManager() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (GeneralSecurityException ex) {
            ex.printStackTrace();
        }
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GCalManager.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        return GoogleCredential.fromStream(in).createScoped(SCOPES);
    }

    public void addEvent(String name, LocalDateTime start, LocalDateTime end, String description) {
        Event event = new Event()
            .setSummary(name)
            .setDescription(description);
        
        boolean allDay = start.getLong(ChronoField.NANO_OF_DAY) == 0 && end.getLong(ChronoField.NANO_OF_DAY) == 0;
        
        EventDateTime startDT = new EventDateTime().setTimeZone("America/Chicago");
        if (allDay) {
            startDT.setDate(new DateTime(start.atZone(ZoneId.of("America/Chicago")).format(DateTimeFormatter.ISO_LOCAL_DATE)));
        } else {
            startDT.setDateTime(new DateTime(start.atZone(ZoneId.of("America/Chicago")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        }
        event.setStart(startDT);

        EventDateTime endDT = new EventDateTime().setTimeZone("America/Chicago");
        if (allDay) {
            endDT.setDate(new DateTime(end.atZone(ZoneId.of("America/Chicago")).plusDays(1L).format(DateTimeFormatter.ISO_LOCAL_DATE)));
        } else {
            endDT.setDateTime(new DateTime(end.atZone(ZoneId.of("America/Chicago")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        }
        event.setEnd(endDT);

        try {
            service.events().insert(getCalendarID(), event).execute().getHtmlLink();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String getCalendarID() throws IOException {
        InputStream in = GCalManager.class.getResourceAsStream(App.SETTINGS_FILE_PATH);
        try {
            JSONObject object = (JSONObject)(new JSONParser().parse(new InputStreamReader(in)));
            return (String)object.get("calendar_id");
        } catch (ParseException ex) {
            System.out.println("Could not parse settings.json");
            System.exit(1);
            return null;
        }
    }
}
