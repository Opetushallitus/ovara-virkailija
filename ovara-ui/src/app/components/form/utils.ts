import { SelectOption } from '@/app/components/form/multicombobox';
import { SelectChangeEvent } from '@mui/material';
import { match } from 'ts-pattern';
import { isEmpty, isNullish } from 'remeda';
import { apiFetch } from '@/app/lib/ovara-backend/api';
import { Koodi, LanguageCode } from '@/app/lib/types/common';

export const changeRadioGroupSelection = (
  e: SelectChangeEvent,
  queryParamSetter: (v: boolean | null) => void,
) => {
  const value = e.target.value;
  if (value === 'no') {
    return queryParamSetter(false);
  } else if (value === 'yes') {
    return queryParamSetter(true);
  } else {
    return queryParamSetter(null);
  }
};

export const getSelectedRadioGroupValue = (selected: boolean | null) =>
  match(selected)
    .with(true, () => 'yes')
    .with(false, () => 'no')
    .otherwise(() => 'neutral');

export const includesValue = (
  id: string,
  selectedValues: Array<string> | null,
) => {
  return isNullish(selectedValues) ? false : selectedValues?.includes(id);
};

export const isChecked = (
  value: string,
  selectedValues: Array<string> | null,
) => {
  return includesValue(value, selectedValues);
};

export const changeChecked = (
  _: SelectChangeEvent,
  id: string,
  selected: Array<string> | null,
  setSelected: (v: Array<string> | null) => void,
) => {
  let newValue = null;

  if (includesValue(id, selected)) {
    newValue = selected?.filter((value) => id !== value) || null;
  } else {
    if (isNullish(selected)) {
      newValue = [id];
    } else {
      newValue = selected?.concat([id]);
    }
  }

  setSelected(isNullish(newValue) || isEmpty(newValue) ? null : newValue);
};

export const changeMultiComboBoxSelection = (
  _: React.SyntheticEvent,
  value: Array<SelectOption>,
  setSelected: (v: Array<string> | null) => void,
) => {
  return setSelected(isEmpty(value) ? null : value?.map((v) => v.value));
};

export const getKoodiOptions = (locale: string, koodit: Array<Koodi>) => {
  if (isNullish(koodit)) {
    return [];
  } else {
    return koodit.map((koodi) => {
      return {
        value: koodi.koodiarvo,
        label: koodi.koodinimi[locale as LanguageCode] || '',
      };
    });
  }
};

// https://stackoverflow.com/a/59940621
// https://www.stefanjudis.com/snippets/how-trigger-file-downloads-with-javascript/
export const downloadExcel = async (
  raporttiEndpoint: string,
  queryParamsStr: string,
  setIsLoading: (v: boolean) => void,
) => {
  setIsLoading(true);
  const response = await apiFetch(
    raporttiEndpoint,
    {
      queryParams: `?${queryParamsStr}`,
    },
    'no-store',
  );

  const contentDisposition = response.headers.get('content-disposition')!;
  const filename = contentDisposition.match(/.*filename=(.*)/)![1];

  const link = document.createElement('a');
  link.style.display = 'none';
  link.href = URL.createObjectURL(await response.blob());
  link.download = filename;

  document.body.appendChild(link);
  link.click();
  setIsLoading(false);

  setTimeout(() => {
    URL.revokeObjectURL(link.href);
    link.remove();
  });
};
