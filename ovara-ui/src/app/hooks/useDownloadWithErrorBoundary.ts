import { useState } from 'react';

type AsyncAction = () => Promise<void>;

export const useDownloadWithErrorBoundary = () => {
  const [error, setError] = useState<Error | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const run = async (action: AsyncAction) => {
    setIsLoading(true);
    try {
      await action();
    } catch (err) {
      setError(err as Error);
    } finally {
      setIsLoading(false);
    }
  };

  if (error) throw error;

  return { run, isLoading };
};
