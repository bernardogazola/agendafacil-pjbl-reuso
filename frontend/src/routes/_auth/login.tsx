import { createFileRoute, Link } from "@tanstack/react-router";
import { LoginForm } from "@/components/form/login-form";

export const Route = createFileRoute("/_auth/login")({
	component: LoginPage,
});

function LoginPage() {
	return (
		<div className="m-auto w-full max-w-sm">
			<div className="text-center">
				<h1 className="font-serif text-4xl font-medium">Bem-vindo de volta</h1>
				<p className="text-muted-foreground mt-2 text-sm">
					Faça login para continuar
				</p>
			</div>
			<LoginForm />
			<p className="text-muted-foreground mt-6 text-center text-sm">
				Não tem uma conta?{" "}
				<Link to="/" className="text-primary font-medium hover:underline">
					Cadastre-se
				</Link>
			</p>
		</div>
	);
}
