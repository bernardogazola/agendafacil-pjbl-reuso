import { createFileRoute, Link } from "@tanstack/react-router";
import { SignupForm } from "@/components/form/signup-form";

export const Route = createFileRoute("/_auth/signup")({
	component: SignupPage,
});

function SignupPage() {
	return (
		<div className="m-auto w-full max-w-lg py-8">
			<div className="text-center">
				<h1 className="font-serif text-4xl font-medium">Crie sua conta</h1>
				<p className="text-muted-foreground mt-2 text-sm">
					Agende com tranquilidade ou atenda com eficiência.
				</p>
			</div>
			<SignupForm />
			<p className="text-muted-foreground mt-6 text-center text-sm">
				Já tem uma conta?{" "}
				<Link to="/login" className="text-primary font-medium hover:underline">
					Entrar
				</Link>
			</p>
		</div>
	);
}
