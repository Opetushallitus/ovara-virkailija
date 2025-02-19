import { Koulutusala, LanguageCode } from '@/app/lib/types/common';
import { isEmpty, isNullish } from 'remeda';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import {
  MultiComboBox,
  SelectOption,
} from '@/app/components/form/multicombobox';
import { Box } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';

const getKoulutusalaOptions = (
  locale: string,
  koulutusalat: Array<Koulutusala>,
) => {
  if (isNullish(koulutusalat)) {
    return [];
  } else {
    return koulutusalat.map((koulutusala) => {
      return {
        value: koulutusala.koodiarvo,
        label: `${koulutusala.nimi[locale]}` || '',
      };
    });
  }
};

export const KoulutusalaValikot = () => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';

  const {
    selectedKoulutusalat1,
    setSelectedKoulutusalat1,
    selectedKoulutusalat2,
    setSelectedKoulutusalat2,
    selectedKoulutusalat3,
    setSelectedKoulutusalat3,
  } = useHakeneetSearchParams();

  const koulutusalat1data = useQuery({
    queryKey: ['fetchKoulutusalat1'],
    queryFn: () => doApiFetch('koulutusalat1'),
  });

  const koulutusalat2data = useQuery({
    queryKey: ['fetchKoulutusalat2', selectedKoulutusalat1],
    queryFn: () =>
      doApiFetch('koulutusalat2', {
        queryParams: selectedKoulutusalat1
          ? `?koulutusalat1=${selectedKoulutusalat1}`
          : null,
      }),
  });

  const koulutusalat3data = useQuery({
    queryKey: ['fetchKoulutusalat3', selectedKoulutusalat2],
    queryFn: () =>
      doApiFetch('koulutusalat3', {
        queryParams: selectedKoulutusalat1
          ? `?koulutusalat2=${selectedKoulutusalat2}`
          : null,
      }),
  });

  const koulutusalat1: Array<Koulutusala> = koulutusalat1data.data || [];
  const koulutusalat2: Array<Koulutusala> = koulutusalat2data.data || [];
  const koulutusalat3: Array<Koulutusala> = koulutusalat3data.data || [];

  const koulutusalat1_id = 'koulutusalat1';
  const koulutusalat2_id = 'koulutusalat2';
  const koulutusalat3_id = 'koulutusalat3';

  const changeKoulutusalat1 = (
    _: React.SyntheticEvent,
    value: Array<SelectOption>,
  ) => {
    return setSelectedKoulutusalat1(
      isEmpty(value) ? null : value?.map((v) => v.value),
    );
  };

  const changeKoulutusalat2 = (
    _: React.SyntheticEvent,
    value: Array<SelectOption>,
  ) => {
    return setSelectedKoulutusalat2(
      isEmpty(value) ? null : value?.map((v) => v.value),
    );
  };

  const changeKoulutusalat3 = (
    _: React.SyntheticEvent,
    value: Array<SelectOption>,
  ) => {
    return setSelectedKoulutusalat3(
      isEmpty(value) ? null : value?.map((v) => v.value),
    );
  };

  return (
    <Box>
      <MultiComboBox
        id={koulutusalat1_id}
        label={t(`raportti.${koulutusalat1_id}`)}
        value={selectedKoulutusalat1 ?? ''}
        options={getKoulutusalaOptions(locale, koulutusalat1)}
        onChange={changeKoulutusalat1}
      />
      <MultiComboBox
        id={koulutusalat2_id}
        label={t(`raportti.${koulutusalat2_id}`)}
        value={selectedKoulutusalat2 ?? []}
        options={getKoulutusalaOptions(locale, koulutusalat2)}
        onChange={changeKoulutusalat2}
      />
      <MultiComboBox
        id={koulutusalat3_id}
        label={t(`raportti.${koulutusalat3_id}`)}
        value={selectedKoulutusalat3 ?? []}
        options={getKoulutusalaOptions(locale, koulutusalat3)}
        onChange={changeKoulutusalat3}
      />
    </Box>
  );
};
