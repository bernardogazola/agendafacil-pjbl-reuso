import {
	Field,
	FieldDescription,
	FieldError,
	FieldGroup,
	FieldLabel,
	FieldTitle,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectItemDescription,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import type { SignupFormApi } from "@/lib/config/signup-form.config";
import {
	BUSINESS_CATEGORY_HINTS,
	BUSINESS_CATEGORY_LABELS,
} from "@/lib/labels";
import type { BusinessCategory } from "@/lib/types";
import { emailField } from "@/lib/validator/auth.schema";
import {
	businessTradeNameField,
	CATEGORY_OPTIONS,
	categoryField,
} from "@/lib/validator/signup.schema";

interface BusinessStepProps {
	form: SignupFormApi;
}

export function BusinessStep({ form }: Readonly<BusinessStepProps>) {
	return (
		<div className="space-y-4">
			<div className="space-y-1">
				<FieldTitle className="text-base">Sobre seu negócio</FieldTitle>
				<FieldDescription>
					Como seus clientes vão te encontrar no AgendaFácil.
				</FieldDescription>
			</div>

			<FieldGroup>
				<form.Field
					name="businessTradeName"
					validators={{ onChange: businessTradeNameField }}
				>
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>Nome do negócio</FieldLabel>
								<Input
									id={field.name}
									name={field.name}
									value={field.state.value}
									onBlur={field.handleBlur}
									onChange={(e) => field.handleChange(e.target.value)}
									aria-invalid={isInvalid}
									placeholder="Ex.: Barbearia Central"
									autoComplete="organization"
								/>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>

				<form.Field name="businessEmail" validators={{ onChange: emailField }}>
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>E-mail do negócio</FieldLabel>
								<Input
									id={field.name}
									name={field.name}
									value={field.state.value}
									onBlur={field.handleBlur}
									onChange={(e) => field.handleChange(e.target.value)}
									aria-invalid={isInvalid}
									placeholder="contato@seunegocio.com"
									type="email"
									autoComplete="email"
								/>
								<FieldDescription>
									Usado para comunicações com seus clientes.
								</FieldDescription>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>

				<form.Field name="category" validators={{ onChange: categoryField }}>
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						const selected = field.state.value;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>Categoria</FieldLabel>
								<Select
									items={BUSINESS_CATEGORY_LABELS}
									value={selected}
									onValueChange={(value) =>
										field.handleChange(value as BusinessCategory)
									}
								>
									<SelectTrigger
										id={field.name}
										aria-invalid={isInvalid}
										className="w-full cursor-pointer"
									>
										<SelectValue placeholder="Selecione uma categoria" />
									</SelectTrigger>
									<SelectContent>
										{CATEGORY_OPTIONS.map((category) => (
											<SelectItem
												key={category}
												value={category}
												className="cursor-pointer"
											>
												<span>{BUSINESS_CATEGORY_LABELS[category]}</span>
												<SelectItemDescription>
													{BUSINESS_CATEGORY_HINTS[category]}
												</SelectItemDescription>
											</SelectItem>
										))}
									</SelectContent>
								</Select>
								{selected && (
									<FieldDescription>
										{BUSINESS_CATEGORY_HINTS[selected]}
									</FieldDescription>
								)}
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>
			</FieldGroup>
		</div>
	);
}
