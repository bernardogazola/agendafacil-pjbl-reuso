export const API_URL: string =
	(typeof process === "undefined" ? undefined : process.env.API_URL) ??
	"http://localhost:8080";
