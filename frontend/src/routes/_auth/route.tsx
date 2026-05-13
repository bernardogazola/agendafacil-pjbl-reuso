import {
	createFileRoute,
	Link,
	Outlet,
	redirect,
} from "@tanstack/react-router";
import { GalleryVerticalEnd } from "lucide-react";
import ToggleTheme from "@/components/theme/theme-toggle";

export const Route = createFileRoute("/_auth")({
	beforeLoad: ({ context }) => {
		if (context.session) {
			throw redirect({ to: "/" });
		}
	},
	component: AuthLayout,
});

function AuthLayout() {
	return (
		<section className="bg-background grid min-h-screen grid-rows-[auto_1fr] px-4">
			<div className="mx-auto w-full max-w-7xl border-b py-3">
				<Link
					to="/"
					aria-label="go home"
					className="inline-block border-t-2 border-transparent py-3"
				>
					<GalleryVerticalEnd className="w-fit" />
				</Link>
			</div>
			<Outlet />
			<div className="flex justify-center">
				<ToggleTheme />
			</div>
		</section>
	);
}
