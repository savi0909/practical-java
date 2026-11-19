package logfk;

/** One JSON object per line, including escaped control characters and exception text. */
public final class JsonLayout implements Layout {
    @Override public String format(LogEvent event) {
        return "{\"timestamp\":" + quote(event.timestamp().toString()) + ",\"level\":" + quote(event.level().name())
                + ",\"logger\":" + quote(event.loggerName()) + ",\"thread\":" + quote(event.threadName())
                + ",\"message\":" + quote(event.message()) + ",\"exception\":" + quote(event.exceptionText()) + "}\n";
    }

    private static String quote(String value) {
        if (value == null) return "null";
        StringBuilder result = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (c < 0x20 || Character.isSurrogate(c)) {
                        result.append("\\u");
                        for (int shift = 12; shift >= 0; shift -= 4)
                            result.append(Character.forDigit((c >> shift) & 15, 16));
                    } else result.append(c);
                }
            }
        }
        return result.append('"').toString();
    }
}
