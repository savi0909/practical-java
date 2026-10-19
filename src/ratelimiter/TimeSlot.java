package ratelimiter;

import java.time.LocalDateTime;

public class TimeSlot {

    private LocalDateTime start;
    private LocalDateTime end;

    private String user;
    private int count;

    public TimeSlot(String user) {
        this.user = user;
        start = LocalDateTime.now();
        end = start.plusSeconds(60);
        count = 0;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public int getCount() {
        return count;
    }

    public String getUser() {
        return user;
    }

    public LocalDateTime getStart() {
        return start;
    }
}
