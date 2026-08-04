import { OphInput } from '@opetushallitus/oph-design-system';
import { useEffect, useState } from 'react';
import { useDebounceCallback } from 'usehooks-ts';

type DebouncedOphInputProps = Omit<
  React.ComponentProps<typeof OphInput>,
  'value' | 'onChange'
> & {
  value: string | null;
  onValueChange: (value: string) => void;
  debounceMs?: number;
};

/**
 * OphInput, joka pitää oman paikallisen tilan ja synkkaa arvon
 * (esim. URL:iin/query-stateen) vasta viiveellä. Näin nopeasti kirjoitettaessa
 * kirjaimet eivät katoa, kun ulkoisen tilan päivitys on throttlattu.
 */
export const DebouncedOphInput = ({
  value,
  onValueChange,
  debounceMs = 300,
  onBlur,
  ...props
}: DebouncedOphInputProps) => {
  const [localValue, setLocalValue] = useState(value ?? '');

  // Synkataan paikallinen tila, kun ulkoinen arvo muuttuu (esim. tyhjennys
  // tai palautus localStoragesta).
  useEffect(() => {
    setLocalValue(value ?? '');
  }, [value]);

  const debouncedOnValueChange = useDebounceCallback(onValueChange, debounceMs);

  return (
    <OphInput
      {...props}
      value={localValue}
      onChange={(e) => {
        setLocalValue(e.target.value);
        debouncedOnValueChange(e.target.value);
      }}
      onBlur={(e) => {
        // Varmistetaan, että viimeisin arvo ehtii tilaan ennen esim.
        // lataus-napin painallusta.
        debouncedOnValueChange.flush();
        onBlur?.(e);
      }}
    />
  );
};
