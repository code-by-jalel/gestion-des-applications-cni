# Stage CNI

Application composée de :

- Frontend Angular
- Backend Spring Boot
- Serveur LDAP ApacheDS

## Prérequis

Pour lancer le projet avec Docker, il faut uniquement :

- Git
- Docker Desktop (Windows/macOS) ou Docker Engine + Docker Compose (Linux)

Les outils suivants ne sont pas nécessaires :
- Java
- Maven
- Node.js
- npm
- Angular CLI
- LDAP

## Structure du projet

```
stage/
├── docker-compose.yml
├── ldap/
│   └── fichiers LDIF
├── frontend/
├── demoLdap/
└── README.md
```

## Lancer le projet

Depuis la racine du projet :

```bash
docker compose up -d --build
```

Les services disponibles :

- Frontend : http://localhost
- Backend API : http://localhost:8081
- LDAP : localhost:10389

Pour s'authentifier en tant que admin:

email: jaleleddine.benromdhane@gmail.com

mdp:1234

## Arrêter le projet

```bash
docker compose down
```

Pour supprimer les données LDAP :

```bash
docker compose down -v
```

⚠️ Cette commande supprime le volume LDAP.
