package aagapp_backend.components;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeAdapter extends TypeAdapter<ZonedDateTime> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Override
    public void write(JsonWriter out, ZonedDateTime value) throws IOException {
        if (value != null) {
            out.value(value.format(formatter));
        } else {
            out.nullValue();
        }
    }

    @Override
    public ZonedDateTime read(JsonReader in) throws IOException {
        String str = in.nextString();
        return ZonedDateTime.parse(str, formatter);
    }
}
