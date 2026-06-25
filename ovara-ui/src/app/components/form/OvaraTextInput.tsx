import { OvaraFormControl } from '@/app/components/form/ovara-form-control';
import { OphInput } from '@opetushallitus/oph-design-system';

type OvaraTextInputProps = {
  label: string;
  helperText?: string;
  errorMessages?: string[];
  inline?: boolean;
} & React.ComponentProps<typeof OphInput>;

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
      renderInput={() => <OphInput fullWidth {...props} />}
    />
  );
};
