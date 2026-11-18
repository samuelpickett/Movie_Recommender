// AI-generated code
package movies;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Reads CSV records, including quoted commas, quotes, and line breaks. */
public final class Csv {
    private Csv() {
    }

    /** Reads every row from a UTF-8 CSV file without interpreting its columns. */
    public static List<List<String>> read(Path path) throws IOException {
        String content = Files.readString(path, StandardCharsets.UTF_8);
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < content.length(); index++) {
            char current = content.charAt(index);
            if (current == '"') {
                if (quoted && index + 1 < content.length() && content.charAt(index + 1) == '"') {
                    field.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                row.add(field.toString());
                field.setLength(0);
            } else if ((current == '\n' || current == '\r') && !quoted) {
                row.add(field.toString());
                rows.add(row);
                row = new ArrayList<>();
                field.setLength(0);
                if (current == '\r' && index + 1 < content.length() && content.charAt(index + 1) == '\n') {
                    index++;
                }
            } else {
                field.append(current);
            }
        }
        if (quoted) {
            throw new IOException("Unclosed quoted field in " + path);
        }
        if (!row.isEmpty() || !field.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }
}
