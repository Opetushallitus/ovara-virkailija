'use client';

import Header from '../components/header';
import { PageLayout } from '../components/page-layout';
import { useTranslations } from 'next-intl';
import { usePathname } from 'next/navigation';

export default function HomeLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const t = useTranslations('Header');
  const currentRoute = usePathname();
  const isHome = currentRoute == '/';
  const headerTranslationKey = isHome
    ? 'home'
    : currentRoute.match(/[a-z]+-?[a-z]*?/gm)?.join('.');
  return (
    <PageLayout
      header={<Header isHome={isHome} title={t(headerTranslationKey)} />}
    >
      {children}
    </PageLayout>
  );
}
