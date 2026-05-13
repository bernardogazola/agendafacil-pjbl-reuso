import { createIsomorphicFn } from "@tanstack/react-start";
import { getCookie } from "@tanstack/react-start/server";
import { parseSessionCookieValue, SESSION_COOKIE, type Session } from "./auth";

export const getSession = createIsomorphicFn()
	.server((): Session | null => {
		try {
			return parseSessionCookieValue(getCookie(SESSION_COOKIE));
		} catch {
			return null;
		}
	})
	.client((): Session | null => {
		const match = new RegExp(
			String.raw`(?:^|;\s*)${SESSION_COOKIE}=([^;]+)`,
		).exec(document.cookie);
		return parseSessionCookieValue(match ? decodeURIComponent(match[1]) : null);
	});
