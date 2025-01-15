## Ovara-virkailija - Opiskelijavalinnan raportoinnin virkailijakäyttöliittymä

Ovara koostuu kahdesta sovelluksesta: Spring bootilla ja Scalalla toteutetusta backendistä,
jonka tarjoamien HTTP-rajapintojen kautta Next.js:llä toteutettu käyttöliittymä noutaa virkailijoiden tarvitsemia raportteja.
Käyttöliittymässä käyttäjä täyttää lomakkeen avulla kyselyn, jonka perusteella backend noutaa tiedot Ovaran omasta tietokannasta ja
muodostaa excel-tiedoston, joka ladataan käyttäjän selaimeen.

# Ovara-backend

Backend käyttää Java Corretton versiota 21.

Backendiä ajetaan IDEA:ssa. Kehitysympäristön konfiguraatio määritellään `application-dev.properties`-nimisessä tiedostossa
````
spring.datasource.url=jdbc:postgresql://localhost:5432/ovara
spring.datasource.username=app
spring.datasource.password=app
session.schema.name=OVARA_VIRKAILIJA_SESSION

opintopolku.virkailija.url=https://virkailija.hahtuvaopintopolku.fi
cas.url=${opintopolku.virkailija.url}/cas
ovara.ui.url=https://localhost:3405
ovara.backend.url=https://localhost:8443/ovara-backend
ovara-backend.cas.username=<CAS-KÄYTTÄJÄTUNNUS>
ovara-backend.cas.password=<CAS-SALASANA>
#logging.level.org.springframework.cache=TRACE

server.port=8443
#self-signed SSL-sertifikaatti lokaalia käyttöä varten
server.ssl.key-store=classpath:keystore.p12
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

Ovara-backendin rajapinta on dokumentoitu Swaggeriä käyttäen ja se löytyy osoitteesta: `http://localhost:8080/ovara-backend/swagger-ui/index.html`.
Rajapintojen kutsuminen edellyttää kirjautumista. Kehitysympäristössä tämä tapahtuu helpoiten siten, että myös ovara-ui on
lokaalisti käynnissä ja kirjaudut sen kautta sisään ennen swaggerin rajapintojen käyttämistä.

Lokaalisti backendia ajaessa lisää `spring.profiles.active=dev`-rivi `application.properties`-tiedostoon
tai anna käynnistysparametri `--spring.profiles.active=dev`.
Jotta properties-tiedostot luetaan hakemiston oph-configuration alta, tulee antaa käynnistysparametri `spring.config.additional-location=classpath:/oph-configuration/application.properties`

# Ovara-ui

Ovara-ui on toteutettu Next.js:llä.

Luo lokaaliajoon `.env.development.local`-tiedosto, johon lisäät seuraavat ympäristömuuttujat niin että ne vastaavat lokaalin backendin konfiguraatiota:
````
VIRKAILIJA_URL=https://virkailija.hahtuvaopintopolku.fi
APP_URL=https://localhost:3405
OVARA_BACKEND=https://localhost:8443/ovara-backend
````

Käyttöliittymän saa käynnistettyä komennolla `npm run dev`. Käyttöliittymä avautuu osoitteeseen: `https://localhost:3405`.
