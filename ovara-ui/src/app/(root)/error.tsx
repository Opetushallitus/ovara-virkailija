'use client';
import { ErrorView } from '@/app/components/error-view';
import { MainContainer } from '@/app/components/main-container';

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <MainContainer>
      <ErrorView error={error} reset={reset} />
    </MainContainer>
  );
}
