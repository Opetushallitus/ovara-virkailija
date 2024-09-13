import { OphButton, OphTypography } from "@opetushallitus/oph-design-system";
import {useTranslations} from 'next-intl';

export default function Home() {
  const t = useTranslations('Home');
  return (
    <div>
      <main>
        <OphTypography>{t('title')}</OphTypography>
        <OphButton>Testi</OphButton>
      </main>
    </div>
  );
}
