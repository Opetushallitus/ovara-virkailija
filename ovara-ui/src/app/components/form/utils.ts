import { SelectChangeEvent } from '@mui/material';
import { match } from 'ts-pattern';

export const changeRadioGroupSelection = (
  e: SelectChangeEvent,
  queryParamSetter,
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

export const getSelectedRadioGroupValue = (selected) =>
  match(selected)
    .with(true, () => 'yes')
    .with(false, () => 'no')
    .otherwise(() => 'neutral');
