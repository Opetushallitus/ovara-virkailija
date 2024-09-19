'use client';

import { PageLayout } from '@/app/components/page-layout';

export default function OpoRaporttiLayout({
  children,
  header,
}: {
  children: React.ReactNode;
  header: React.ReactNode;
}) {
  return <PageLayout header={header}>{children}</PageLayout>;
}
