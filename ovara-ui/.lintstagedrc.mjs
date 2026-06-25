import path from 'path';
import process from 'node:process';

const shellQuote = (value) => JSON.stringify(value);

const eslintCommand = (filenames) =>
  `eslint --fix --no-ignore --max-warnings=0 ${filenames
    .map((f) => shellQuote(path.relative(process.cwd(), f)))
    .join(' ')}`;

const prettierCommand = 'prettier --write -u';

const config = {
  '**/*.{js,mjs,cjs,jsx,ts,tsx}': [eslintCommand, prettierCommand],
  '!**/*.{js,mjs,cjs,jsx,ts,tsx}': [prettierCommand],
};

export default config;
