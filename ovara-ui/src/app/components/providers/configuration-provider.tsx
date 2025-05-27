'use client';

import { createContext, useContext } from 'react';
import { Configuration } from '@/app/lib/configuration/configuration';
import { setConfiguration } from '@/app/lib/configuration/client-configuration';

type ConfigurationContextType = {
  configuration: Configuration;
};

const ConfigurationContext = createContext<ConfigurationContextType | null>(
  null,
);

export function ConfigurationProvider({
  configuration,
  children,
}: {
  configuration: Configuration;
  children: React.ReactNode;
}) {
  // Set immediately during render — not delayed in useEffect
  if (typeof window !== 'undefined') {
    setConfiguration(configuration);
  }

  return (
    <ConfigurationContext.Provider value={{ configuration }}>
      {children}
    </ConfigurationContext.Provider>
  );
}

export function useConfiguration() {
  const context = useContext(ConfigurationContext);
  if (!context) {
    throw new Error(
      'useConfiguration must be used within a ConfigurationProvider',
    );
  }
  return context.configuration;
}
