package logfk;

/** Full async queue: wait for space, or discard the new event and count the loss. */
public enum OverflowPolicy { BLOCK, DROP_NEWEST }
