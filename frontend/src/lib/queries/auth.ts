import { useMutation } from "@tanstack/react-query";
import { useRouter } from "@tanstack/react-router";
import { notifySessionChange, type Session } from "../auth";
import {
	type AuthResult,
	loginFn,
	signupCustomerFn,
	signupOwnerFn,
} from "../server/auth-fns";
import type {
	LoginRequest,
	SignupCustomerRequest,
	SignupOwnerRequest,
} from "../types";

/**
 * Cria uma mutation de autenticação baseada em uma server function.
 *
 * <p>Quando a autenticação falha, a mensagem retornada pela server function é
 * convertida em `Error`, para que a tela consiga exibir o erro no fluxo normal
 * do React Query.</p>
 *
 * @param serverFn server function responsável pela operação de autenticação
 * @returns mutation React Query para login ou cadastro
 */
function useAuthMutation<TVars>(
	serverFn: (opts: { data: TVars }) => Promise<AuthResult>,
) {
	const router = useRouter();
	return useMutation<Session, Error, TVars>({
		mutationFn: async (vars) => {
			const result = await serverFn({ data: vars });
			if (!result.ok) throw new Error(result.error);
			return result.session;
		},
		onSuccess: async () => {
			notifySessionChange();

			// Re-executa o beforeLoad da rota raiz para atualizar o contexto de sessão
			// antes da navegação feita pela tela após login ou cadastro.
			await router.invalidate();
		},
	});
}

/**
 * Mutation para autenticar um usuário existente.
 */
export function useLogin() {
	return useAuthMutation<LoginRequest>(loginFn);
}

/**
 * Mutation para cadastrar um novo cliente.
 */
export function useSignupCustomer() {
	return useAuthMutation<SignupCustomerRequest>(signupCustomerFn);
}

/**
 * Mutation para cadastrar um dono com seu primeiro estabelecimento.
 */
export function useSignupOwner() {
	return useAuthMutation<SignupOwnerRequest>(signupOwnerFn);
}
