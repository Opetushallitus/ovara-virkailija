import { useEffect, useState } from 'react';
import { apiFetch } from '@/app/lib/ovara-backend/api';

type User = {
  userOid: string;
  authorities: Array<string>;
};

type UserResponse = {
  user: User;
};

export function useFetchUser() {
  const [user, setUser] = useState<User | null>(null);
  useEffect(() => {
    async function fetchUser() {
      const response = await apiFetch('user');
      const { user }: UserResponse = await response.json();
      setUser(user);
    }
    fetchUser();
  }, []);
  return user;
}
