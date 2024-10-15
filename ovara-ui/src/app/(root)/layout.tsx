import Header from '../components/header';
import { PageLayout } from '../components/page-layout';
import { PageContent } from '../components/page-content';

export default function HomeLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <PageLayout>
      <Header />
      <PageContent>{children}</PageContent>
    </PageLayout>
  );
}
