import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { LanguageCode } from '@/app/lib/types/common';

export const useAsiointiKieli = () => {
  const user = useAuthorizedUser();
  return (user?.asiointikieli as LanguageCode) ?? 'fi';
};
