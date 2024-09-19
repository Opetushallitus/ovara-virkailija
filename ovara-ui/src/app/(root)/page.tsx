import { OphButton, OphTypography } from '@opetushallitus/oph-design-system';

export default async function Home() {
  const backendUrl = process.env.OVARA_BACKEND;
  const element = await fetch(`${backendUrl}/api/ping`)
    .then((element) => {
      if (element.ok) {
        return <OphButton>Testi</OphButton>;
      }
    })
    .catch((error) => {
      return <OphTypography>{`ERROR: ${error.message}`}</OphTypography>;
    });

  return (
    <div>
      <main>{element}</main>
    </div>
  );
}
