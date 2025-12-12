package com.high.order.infrastructure.context;

import java.util.UUID;

public class MessageContext {

    private static final ThreadLocal<ContextData> holder = new ThreadLocal<>();

    public static void set(UUID userId, String role) {
        holder.set(new ContextData(userId, role));
    }

    public static UUID getUserId() {
        ContextData ctx = holder.get();
        return ctx != null ? ctx.userId() : null;
    }

    public static String getRole() {
        ContextData ctx = holder.get();
        return ctx != null ? ctx.role() : null;
    }

    public static boolean hasContext() {
        return holder.get() != null;
    }

    public static boolean hasUserId() {
        return getUserId() != null;
    }

    public static void clear() {
        holder.remove();
    }

    public record ContextData(UUID userId, String role) {}
}
