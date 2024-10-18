import { Button, Typography } from '@mui/material';
import Grid from '@mui/material/Grid2';
import { useTranslations } from 'next-intl';

export default function Custom404() {
  const tranlateError = useTranslations('Error');
  const tranlateYleinen = useTranslations('yleinen');
  return (
    <main>
      <Grid
        container
        direction="column"
        spacing={5}
        sx={{
          justifyContent: 'center',
          alignItems: 'center',
          textAlign: 'center',
          paddingTop: '132px',
          paddingBottom: '132px',
        }}
      >
        <Grid>
          <Typography variant="h1" color="primary">
            404
          </Typography>
        </Grid>
        <Grid>
          <Typography variant="h2">{tranlateError('404.otsikko')}</Typography>
          <Typography variant="body1" component="p">
            {tranlateError('404.teksti')}
          </Typography>
        </Grid>
        <Grid>
          <Grid
            container
            direction="row"
            spacing={2}
            sx={{
              justifyContent: 'center',
              alignItems: 'center',
            }}
          >
            <Grid>
              <Button
                variant="contained"
                aria-label={tranlateYleinen('palaa-etusivulle')}
                href="/"
              >
                {tranlateYleinen('palaa-etusivulle')}
              </Button>
            </Grid>
          </Grid>
        </Grid>
      </Grid>
    </main>
  );
}
