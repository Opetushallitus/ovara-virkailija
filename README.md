## Ovara-virkailija - Opiskelijavalinnan raportoinnin virkailijakäyttöliittymä

Ovara koostuu kahdesta sovelluksesta: Spring bootilla ja Scalalla toteutetusta backendistä,
jonka tarjoamien HTTP-rajapintojen kautta Next.js:llä toteutettu käyttöliittymä noutaa virkailijoiden tarvitsemia raportteja.
Käyttöliittymässä käyttäjä täyttää lomakkeen avulla kyselyn, jonka perusteella backend noutaa tiedot Ovaran omasta tietokannasta ja
muodostaa excel-tiedoston, joka ladataan käyttäjän selaimeen.

# Ovara-backend

Backend käyttää Java Corretton versiota 21.

## Varmenteen generointi

Asenna `mkcert`-ohjelma jos sitä ei löydy:
```
brew install mkcert
```

Generoi varmenne ajamalla projektin juuressa:
```
./generate-certs.sh
```

Backendiä ajetaan IDEA:ssa (`/src/main/scala/fi/oph/ovara/backend/OvaraBackendApplication.scala`). Kehitysympäristön konfiguraatio määritellään `/src/main/resources/application-dev.properties`-nimisessä tiedostossa
````
spring.datasource.url=jdbc:postgresql://localhost:5432/ovara
#readonly datasource url, voit käyttää samaa kantayhteyttä molempiin datasourceihin lokaalisti
app.readonly.datasource.url=jdbc:postgresql://localhost:5432/ovara
spring.datasource.username=app
spring.datasource.password=app
session.schema.name=ovara_virkailija

opintopolku.virkailija.url=https://virkailija.hahtuvaopintopolku.fi
cas.url=${opintopolku.virkailija.url}/cas
ovara.ui.url=https://localhost:3405
ovara.backend.url=https://localhost:8443/ovara-backend
ovara-backend.cas.username=<CAS-KÄYTTÄJÄTUNNUS>
ovara-backend.cas.password=<CAS-SALASANA>
#logging.level.org.springframework.cache=TRACE

server.port=8443
#self-signed SSL-sertifikaatti lokaalia käyttöä varten
server.ssl.key-store=classpath:localhost-keystore.p12
server.ssl.key-store-password=ovarabackendkey
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=ovara-backend
````

Backendiä voi ajaa sekä lokaalia dockeriin käynnistettävää postgresia että testiympäristön tietokantaa vasten.
(Kirjoitushetkellä ovaran tietokanta on olemassa pelkästään QA:lla.) `justfile`:stä
löytyvät komennot tietokannan pystyttämiseen tai tietokantayhteyden avaamiseen. [just](https://github.com/casey/just) on
komentorivityökalu komentojen dokumentoimiseen ja ajamiseen. Esimerkiksi `just ssh-to-remote-db-in-pallero` avaa tietokantayhteyden
QA:n ovara-tietokantaan. Lokaalin tietokannan käyttäminen vaatii tietokantadumpin lataamisen omalle koneelle,
ja sen voi tehdä komennolla `just dump-remote-db`. `just`:in asentaminen ei ole välttämätöntä backendin ajamiseksi,
vaan voit katsoa tarvittavat komennot `justfile`:stä ja ajaa ne sellaisinaan komentoriviltä.

`justfile`:stä löytyvät komennot QA-tietokantayhteydelle olettavat että QA-ympäristön bastion-putkitus löytyy ssh configista aliaksella `pallero-bastion`

Ovara-backendin rajapinta on dokumentoitu Swaggeriä käyttäen ja se löytyy osoitteesta: `http://localhost:8080/ovara-backend/swagger`.
Rajapintojen kutsuminen edellyttää kirjautumista. Kehitysympäristössä tämä tapahtuu helpoiten siten, että myös ovara-ui on
lokaalisti käynnissä ja kirjaudut sen kautta sisään ennen swaggerin rajapintojen käyttämistä.

Lokaalisti backendia ajaessa lisää `spring.profiles.active=dev`-rivi `application.properties`-tiedostoon
tai anna käynnistysparametri `-Dspring.profiles.active=dev`.

# Ovara-ui

Ovara-ui on toteutettu Next.js:llä.

Luo lokaaliajoon `.env.development.local`-tiedosto, johon lisäät seuraavat ympäristömuuttujat niin että ne vastaavat lokaalin backendin konfiguraatiota:
````
VIRKAILIJA_URL=https://virkailija.testiopintopolku.fi
OVARA_BACKEND=https://localhost:8443
APP_URL=https://localhost:3405
````

Käyttöliittymän saa käynnistettyä komennolla `npm run dev`. Käyttöliittymä avautuu osoitteeseen: `https://localhost:3405`.

Lokaaliympäristössä backendin ja käyttöliittymän käyttö https yli cas-autentikoinnilla ja sessiohallinnalla edellyttää sertifikaattien ja keystoren generointia.
Nämä saa luotua ajamalla projektin juuressa skriptin `generate-certs.sh`.

## Deploy

Asenna ensin sovelluksen riippuvuudet ja buildaa next.js sovellus:
````
    npm ci
    SKIP_TYPECHECK=true npm run build
````
Deploy onnistuu komennolla:
````
    SKIP_TYPECHECK=true ./deploy.sh untuva deploy -d
````

# Tietokanta

Ovara-backendilla on ovara-tietokannassa oma skeema ovara-virkailija jossa on mm. sessiohallintaan liittyvät taulut.

Backendin tietokantamigraatiot on toteutettu [flywaylla](https://flywaydb.org/) ja ajetaan automaattisesti asennuksen
yhteydessä. Migraatiotiedostot löytyvät kansiosta `ovara-backend/src/main/resources/db/migration`
