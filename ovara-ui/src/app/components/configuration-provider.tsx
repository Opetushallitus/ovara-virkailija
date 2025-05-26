'use client';

import { createContext, useContext } from 'react';

type Configuration = {
  raamitUrl: string;
  ovaraBackendApiUrl: string;
  virkailijaUrl: string;
  lokalisointiPrefix: string;
};

const ConfigurationContext = createContext<Configuration | null>(null);

export function ConfigurationProvider({
  children,
  config,
}: {
  children: React.ReactNode;
  config: Configuration;
}) {
  return (
    <ConfigurationContext.Provider value={config}>
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
  return context;
}
