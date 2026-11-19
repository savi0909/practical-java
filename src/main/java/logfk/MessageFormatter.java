package logfk;

import java.io.PrintWriter;
import java.io.StringWriter;

/** Literal {} substitution only: no lookups, evaluation, escaping or reflection. */
final class MessageFormatter {
    private MessageFormatter() { }

    static String format(String template, Object[] arguments) {
        StringBuilder result = new StringBuilder(template.length());
        int start = 0, argument = 0, placeholder;
        while (argument < arguments.length && (placeholder = template.indexOf("{}", start)) >= 0) {
            result.append(template, start, placeholder).append(render(arguments[argument++]));
            start = placeholder + 2;
        }
        result.append(template, start, template.length());
        if (argument < arguments.length) {
            result.append(" [");
            while (argument < arguments.length) {
                result.append(render(arguments[argument++]));
                if (argument < arguments.length) result.append(", ");
            }
            result.append(']');
        }
        return result.toString();
    }

    private static String render(Object value) {
        try { return String.valueOf(value); }
        catch (RuntimeException failure) { return "[toString failed: " + failure.getClass().getSimpleName() + "]"; }
    }

    static String exceptionText(Throwable cause) {
        if (cause == null) return null;
        StringWriter text = new StringWriter();
        try { cause.printStackTrace(new PrintWriter(text)); }
        catch (RuntimeException failure) { return "[exception rendering failed: " + failure.getClass().getSimpleName() + "]"; }
        return text.toString().stripTrailing();
    }
}
