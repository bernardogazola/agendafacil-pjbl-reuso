import { Link, useMatches } from "@tanstack/react-router";
import { Fragment } from "react";

import {
	Breadcrumb,
	BreadcrumbItem,
	BreadcrumbLink,
	BreadcrumbList,
	BreadcrumbPage,
	BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";

interface CrumbStaticData {
	crumb?: string;
}

export function DashboardBreadcrumb() {
	const matches = useMatches();
	const crumbs = matches
		.map((m) => {
			const data = m.staticData as CrumbStaticData | undefined;
			return data?.crumb ? { pathname: m.pathname, label: data.crumb } : null;
		})
		.filter((c): c is { pathname: string; label: string } => c !== null);

	if (crumbs.length === 0) return null;

	return (
		<Breadcrumb>
			<BreadcrumbList>
				{crumbs.map((c, i) => {
					const isLast = i === crumbs.length - 1;
					return (
						<Fragment key={c.pathname}>
							<BreadcrumbItem>
								{isLast ? (
									<BreadcrumbPage>{c.label}</BreadcrumbPage>
								) : (
									<BreadcrumbLink render={<Link to={c.pathname} />}>
										{c.label}
									</BreadcrumbLink>
								)}
							</BreadcrumbItem>
							{isLast ? null : <BreadcrumbSeparator />}
						</Fragment>
					);
				})}
			</BreadcrumbList>
		</Breadcrumb>
	);
}
