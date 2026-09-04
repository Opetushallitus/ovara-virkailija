import { ComboBox, SelectOption } from '@/app/components/form/multicombobox';
import { useFetchOrganisaatiohierarkiat } from '@/app/hooks/useFetchOrganisaatiohierarkiat';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { LanguageCode } from '@/app/lib/types/common';
import { getOppilaitosOptions, getToimipisteOptions } from '@/app/lib/utils';
import { useTranslate } from '@tolgee/react';
import { isNullish } from 'remeda';

type Props = {
  value: string | null;
  onChange: (value: string | null) => void;
  taso?: 'oppilaitos' | 'toimipiste';
  /** Rajaa toimipistelistan tämän oppilaitoksen toimipisteisiin. Sivuutetaan kun taso on oppilaitos. */
  rajaavaOppilaitos?: string | null;
  disabled?: boolean;
  required?: boolean;
};

/**
 * Yhden organisaation valitsin yhdellä organisaatiotasolla: `taso` valitsee
 * tarjotaanko oppilaitoksia vai toimipisteitä. Käyttäjä joka on saanut oikeutensa
 * tarjottua tasoa alempaa ei näe valittavia organisaatioita. Käyttää jaettua
 * organisaatiohierarkiahakua mutta pysyy irrallaan `useCommonSearchParams`-tilasta.
 */
export const OrganisaatioSingle = ({
  value,
  onChange,
  taso = 'oppilaitos',
  rajaavaOppilaitos = null,
  disabled,
  required,
}: Props) => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';

  const { data } = useFetchOrganisaatiohierarkiat();
  const options: SelectOption[] =
    taso === 'toimipiste'
      ? getToimipisteOptions(data ?? null, locale, rajaavaOppilaitos)
      : getOppilaitosOptions(data ?? null, locale);

  return (
    <ComboBox
      // Sama komponentti voi esiintyä sivulla kahdesti, joten id on tasokohtainen.
      id={`${taso}-single`}
      label={t(`raportti.${taso}`)}
      value={disabled ? undefined : (value ?? undefined)}
      options={disabled ? [] : options}
      required={required}
      onChange={(_, selected) =>
        onChange(isNullish(selected) ? null : selected.value)
      }
    />
  );
};
