import { ComboBox, SelectOption } from '@/app/components/form/multicombobox';
import { useFetchOrganisaatiohierarkiat } from '@/app/hooks/useFetchOrganisaatiohierarkiat';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { LanguageCode } from '@/app/lib/types/common';
import { getOppilaitosOptions } from '@/app/lib/utils';
import { useTranslate } from '@tolgee/react';
import { isNullish } from 'remeda';

type Props = {
  value: string | null;
  onChange: (value: string | null) => void;
  disabled?: boolean;
  required?: boolean;
};

/**
 * Yhden organisaation valitsin. Tarjoaa oppilaitostason organisaatiot joihin
 * käyttäjällä on oikeus; oppilaitosta alemmalla tasolla (toimipiste) oikeutensa
 * saanut käyttäjä ei siis näe valittavia organisaatioita. Käyttää jaettua
 * organisaatiohierarkiahakua mutta pysyy irrallaan `useCommonSearchParams`-tilasta.
 */
export const OrganisaatioSingle = ({
  value,
  onChange,
  disabled,
  required,
}: Props) => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';

  const { data } = useFetchOrganisaatiohierarkiat();
  const options: SelectOption[] = getOppilaitosOptions(data ?? null, locale);

  return (
    <ComboBox
      id="organisaatio-single"
      label={t('raportti.oppilaitos')}
      value={disabled ? undefined : (value ?? undefined)}
      options={disabled ? [] : options}
      required={required}
      onChange={(_, selected) =>
        onChange(isNullish(selected) ? null : selected.value)
      }
    />
  );
};
