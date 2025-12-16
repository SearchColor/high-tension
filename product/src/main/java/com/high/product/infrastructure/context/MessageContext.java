package com.high.product.infrastructure.context;

import java.util.UUID;

public class MessageContext {

	private static final ThreadLocal<Context> context = new ThreadLocal<>();

	public record Context(UUID userId, String role) {}

	public static void set(UUID userId, String role) {
		context.set(new Context(userId, role));
	}

	public static boolean hasContext() {
		return context.get() != null;
	}

	public static UUID getUserId() {
		Context ctx = context.get();
		return ctx != null ? ctx.userId() : null;
	}

	public static String getRole() {
		Context ctx = context.get();
		return ctx != null ? ctx.role() : null;
	}

	public static void clear() {
		context.remove();
	}
}
