import { SelectChangeEvent } from '@mui/material';
import { match } from 'ts-pattern';
import { isEmpty, isNullish } from 'remeda';

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
