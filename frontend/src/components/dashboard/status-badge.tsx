import { Badge } from "@/components/ui/badge";
import {
	ACCESS_LEVEL_LABELS,
	CANCELLATION_POLICY_LABELS,
	PRICING_POLICY_LABELS,
	STATUS_LABELS,
} from "@/lib/labels";
import type {
	AccessLevel,
	AppointmentStatus,
	CancellationPolicyType,
	PricingPolicyType,
} from "@/lib/types";

type BadgeVariant = React.ComponentProps<typeof Badge>["variant"];

const APPOINTMENT_VARIANT: Record<AppointmentStatus, BadgeVariant> = {
	SCHEDULED: "secondary",
	CONFIRMED: "default",
	COMPLETED: "outline",
	CANCELED: "destructive",
	NO_SHOW: "destructive",
};

export function AppointmentStatusBadge({
	status,
}: Readonly<{
	status: AppointmentStatus;
}>) {
	return (
		<Badge variant={APPOINTMENT_VARIANT[status]}>{STATUS_LABELS[status]}</Badge>
	);
}

export function ActiveBadge({ active }: { active: boolean }) {
	return active ? (
		<Badge variant="default">Ativo</Badge>
	) : (
		<Badge variant="outline">Inativo</Badge>
	);
}

export function PricingPolicyBadge({
	type,
}: Readonly<{ type: PricingPolicyType }>) {
	return <Badge variant="outline">{PRICING_POLICY_LABELS[type]}</Badge>;
}

export function CancellationPolicyBadge({
	type,
}: Readonly<{
	type: CancellationPolicyType;
}>) {
	return <Badge variant="outline">{CANCELLATION_POLICY_LABELS[type]}</Badge>;
}

export function AccessLevelBadge({ level }: Readonly<{ level: AccessLevel }>) {
	return (
		<Badge variant={level === "SUPER_ADMIN" ? "default" : "secondary"}>
			{ACCESS_LEVEL_LABELS[level]}
		</Badge>
	);
}
