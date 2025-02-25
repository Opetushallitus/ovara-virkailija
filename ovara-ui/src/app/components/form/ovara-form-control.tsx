import {
  FormControl,
  FormControlProps,
  FormHelperText,
  FormLabel,
} from '@mui/material';
import { useId } from 'react';
import { styled } from '@mui/material/styles';

const StyledFormHelperText = styled(FormHelperText)(({ theme }) => ({
  margin: theme.spacing(0.5, 0),
}));

export const OvaraFormControl = ({
  label,
  renderInput,
  helperText,
  errorMessages = [],
  ...props
}: Omit<FormControlProps, 'children'> & {
  label?: string;
  helperText?: string;
  errorMessages?: Array<string>;
  renderInput: (props: { labelId: string }) => React.ReactNode;
}) => {
  const id = useId();
  const labelId = `OvaraFormControl-${id}-label`;
  const { sx } = props;
  return (
    <FormControl
      {...props}
      sx={{
        flexDirection: 'row',
        paddingTop: '20px',
        width: '100%',
        ...sx,
      }}
    >
      {label && (
        <FormLabel
          id={labelId}
          sx={{
            paddingRight: '1rem',
            alignContent: 'center',
            textAlign: 'end',
            width: '20%',
          }}
        >
          {label}
        </FormLabel>
      )}
      {helperText && (
        <StyledFormHelperText error={false}>{helperText}</StyledFormHelperText>
      )}
      {renderInput({ labelId })}
      {errorMessages.map((message, index) => (
        <StyledFormHelperText error={true} key={`${index}_${message}`}>
          {message}
        </StyledFormHelperText>
      ))}
    </FormControl>
  );
};
