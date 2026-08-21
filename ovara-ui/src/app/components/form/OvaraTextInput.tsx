import { OvaraFormControl } from '@/app/components/form/ovara-form-control';
import { DebouncedOphInput } from '@/app/components/form/DebouncedOphInput';

type OvaraTextInputProps = {
  label: string;
  helperText?: string;
  errorMessages?: string[];
  inline?: boolean;
} & React.ComponentProps<typeof DebouncedOphInput>;

export const OvaraTextInput = ({
  label,
  helperText,
  errorMessages,
  ...props
}: OvaraTextInputProps) => {
  return (
    <OvaraFormControl
      sx={{ pb: 2 }}
      label={label}
      helperText={helperText}
      errorMessages={errorMessages}
      renderInput={() => <DebouncedOphInput fullWidth {...props} />}
    />
  );
};
