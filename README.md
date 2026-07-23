# Stage CNI

## Prérequis

- Docker et Docker Compose
- Facultatif pour un lancement en local :
  - Java 21
  - Node.js 20+
  - npm

## Lancer avec Docker

Depuis la racine du dépôt, démarrez toute la pile avec :

```bash
docker compose up --build
```

Cela démarre :

- Frontend : http://localhost
- API backend : http://localhost:8081
- LDAP : localhost:10389

## Lancer en local

Si vous souhaitez exécuter les applications sans Docker, démarrez chaque partie séparément.

### Backend

```bash
cd demoLdap
./mvnw spring-boot:run
```

Sous Windows, utilisez :

```bash
cd demoLdap
.\mvnw.cmd spring-boot:run
```

Le backend s’exécute sur le port `8080` dans le conteneur et est exposé sur `8081` avec Docker Compose.

### Frontend

```bash
cd frontend
npm install
npm start
```

L’application Angular utilise le serveur de développement par défaut lorsqu’elle est lancée en local.
