## Ovara-virkailija - Opiskelijavalinnan raportoinnin virkailijakäyttöliittymä

Ovara koostuu kahdesta sovelluksesta: Spring bootilla ja Scalalla toteutetusta backendistä,
jonka tarjoamien HTTP-rajapintojen kautta Next.js:llä toteutettu käyttöliittymä noutaa virkailijoiden tarvitsemia raportteja.
Käyttöliittymässä käyttäjä täyttää lomakkeen avulla kyselyn, jonka perusteella backend noutaa tiedot Ovaran omasta tietokannasta ja
muodostaa excel-tiedoston, joka ladataan käyttäjän selaimeen.

# Ovara-backendin

Backend käyttää Java Corretton versiota 21.

Backendiä ajetaan IDEA:ssa. Kehitysympäristön konfiguraatio määritellään `application-dev.properties`-nimisessä tiedostossa
````
spring.datasource.url=jdbc:postgresql://localhost:5432/ovara
spring.datasource.username=app
spring.datasource.password=app

opintopolku.virkailija.domain=https://virkailija.hahtuvaopintopolku.fi
cas.url=${opintopolku.virkailija.domain}/cas
ovara.ui.url=https://localhost:3405
ovara.backend.url=http://localhost:8080/ovara-backend
ovara-backend.cas.password=<CAS-SALASANA>

#logging.level.org.springframework.cache=TRACE
````

Backendiä voi ajaa sekä lokaalia dockeriin käynnistettävää postgresia että testiympäristön tietokantaa vasten.
(Kirjoitushetkellä ovaran tietokanta on olemassa pelkästään QA:lla.) `justfile`:stä
löytyvät komennot tietokannan pystyttämiseen tai tietokantayhteyden avaamiseen. [just](https://github.com/casey/just) on
komentorivityökalu komentojen dokumentoimiseen ja ajamiseen. Esimerkiksi `just ssh-to-remote-db-in-pallero` avaa tietokantayhteyden
QA:n ovara-tietokantaan. Lokaalin tietokannan käyttäminen vaatii tietokantadumpin lataamisen omalle koneelle,
ja sen voi tehdä komennolla `just dump-remote-db`. `just`:in asentaminen ei ole välttämätöntä backendin ajamiseksi,
vaan voit katsoa tarvittavat komennot `justfile`:stä ja ajaa ne sellaisinaan komentoriviltä.

Ovara-backendin rajapinta on dokumentoitu Swaggeriä käyttäen ja se löytyy osoitteesta: `http://localhost:8080/ovara-backend/swagger-ui/index.html`.
Rajapintojen kutsuminen edellyttää kirjautumista. Kehitysympäristössä tämä tapahtuu helpoiten siten, että myös ovara-ui on
lokaalisti käynnissä ja kirjaudut sen kautta sisään ennen swaggerin rajapintojen käyttämistä.




# Ovara-ui

Ovara-ui on toteutettu Next.js:llä.

Käyttöliittymän saa käynnistettyä komennolla `npm run dev`. Käyttöliittymä avautuu osoitteeseen: `https://localhost:3405`.
