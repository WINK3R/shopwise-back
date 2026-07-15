# ShopWise API

Backend Spring Boot de ShopWise pour la gestion multi-commerce des clients,
prestations, rendez-vous, comptes commercants et programmes de fidelite.

## Architecture

- Java 21 et Spring Boot 4.
- PostgreSQL 17 en execution normale.
- H2 pour les tests automatises.
- Authentification commercant par session serveur et cookie `JSESSIONID`.
- JaCoCo pour la couverture backend.
- GitHub Actions pour le build, les tests et la publication DockerHub.

Le frontend Angular est maintenu dans un projet distinct. Ce repository ne contient
donc que le Dockerfile et la pipeline du backend.

## Prerequis

- Git.
- Docker Engine avec Docker Compose v2.
- Java 21 pour une execution hors Docker.

## Configuration

Les principales variables d'environnement sont :

| Variable | Valeur locale par defaut | Usage |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5434/shopwise` | URL JDBC hors Compose |
| `DB_USERNAME` | `shopwise` | Utilisateur PostgreSQL |
| `DB_PASSWORD` | `shopwise` | Mot de passe PostgreSQL |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Origines frontend autorisees |
| `SESSION_COOKIE_SECURE` | `false` | Doit etre `true` derriere HTTPS |
| `BOOTSTRAP_ENABLED` | `true` | Creation des donnees de demonstration |
| `BOOTSTRAP_EMAIL` | `owner@shopwise.local` | Compte OWNER de demonstration |
| `BOOTSTRAP_PASSWORD` | `Shopwise123!` | Mot de passe de demonstration |
| `FRONTEND_URL` | `http://localhost:4200` | Base des liens d'invitation |

Ne pas conserver les valeurs locales de mot de passe en production. Le bootstrap
doit y etre desactive.

## Execution locale sans Docker pour l'API

Demarrer uniquement PostgreSQL :

```bash
docker compose -f docker-compose.yml up -d postgres
./mvnw spring-boot:run
```

L'API est alors disponible sur `http://localhost:8080`. Le contrat est dans
[`docs/openapi.yaml`](docs/openapi.yaml).

## Base de donnees et donnees de test

Le schema relationnel Mermaid est documente dans
[`docs/database-schema.md`](docs/database-schema.md). Les scripts PostgreSQL sont
executes automatiquement, dans cet ordre, lors de la creation d'un volume Docker
vierge :

1. `database/01-schema.sql` cree la structure et les contraintes ;
2. `database/02-seed.sql` ajoute deux commerces et des donnees couvrant les
   utilisateurs, clients, services, rendez-vous et comptes de fidelite.

Tous les comptes de demonstration utilisent le mot de passe `Shopwise123!` :

| Compte | Profil |
| --- | --- |
| `owner@shopwise.local` | OWNER de Chez Marie |
| `manager@shopwise.local` | MANAGER de Chez Marie |
| `staff@shopwise.local` | STAFF de Chez Marie |
| `owner.zen@shopwise.local` | OWNER de Studio Zen |
| `alice.martin@example.com` | Compte client |
| `nina.roux@example.com` | Compte client |

Les scripts d'initialisation PostgreSQL ne s'executent que lors de la creation du
volume. Pour injecter uniquement les donnees dans une base existante :

```bash
psql -h localhost -p 5434 -U shopwise -d shopwise -f database/02-seed.sql
```

## Tests et couverture

La commande suivante compile le projet, execute tous les tests et genere JaCoCo :

```bash
./mvnw clean verify
```

Le build echoue si la couverture globale descend sous 60 % pour les instructions
ou les branches, conformement a l'enonce. Le rapport HTML local est genere dans :

```text
target/site/jacoco/index.html
```

## Construction de l'image backend

```bash
docker build -t shopwise-api:local .
```

L'image est construite en plusieurs etapes avec Java 21. Le conteneur final utilise
un JRE et un utilisateur non privilegie.

## Deploiement avec Docker Compose

Copier `.env.example` vers `.env`, puis remplacer les valeurs sur la machine cible :

```bash
cp .env.example .env
```

Configuration de production type :

```dotenv
SHOPWISE_API_IMAGE=mon-compte-dockerhub/shopwise-api:latest
POSTGRES_DB=shopwise
POSTGRES_USER=shopwise
POSTGRES_PASSWORD=remplacer-par-un-secret-fort
CORS_ALLOWED_ORIGINS=https://shopwise.example.com
SESSION_COOKIE_SECURE=true
BOOTSTRAP_ENABLED=false
FRONTEND_URL=https://shopwise.example.com
BACKEND_PORT=8080
POSTGRES_PORT=5434
```

Puis recuperer et demarrer l'image publiee :

```bash
docker compose -f docker-compose.yml pull
docker compose -f docker-compose.yml up -d --no-build
docker compose -f docker-compose.yml ps
```

PostgreSQL conserve ses donnees dans le volume
`shopwise-postgres-data`. Pour consulter les journaux :

```bash
docker compose -f docker-compose.yml logs -f backend
```

Pour revenir a une version publiee anterieure, modifier `SHOPWISE_API_IMAGE` avec
le tag voulu puis relancer `pull` et `up -d --no-build`.

## Pipeline GitHub Actions

La pipeline se trouve dans
`.github/workflows/backend-ci-cd.yml`. Elle est executee :

- sur chaque pull request vers `main` ;
- sur chaque push vers `main` ;
- sur les tags commencant par `v` ;
- manuellement avec `workflow_dispatch`.

Elle effectue les operations suivantes :

1. Installation de Java 21 et cache Maven.
2. Execution de `./mvnw clean verify`.
3. Publication du JAR, des rapports Surefire et du rapport JaCoCo comme artefacts
   telechargeables pendant 14 jours.
4. Construction de l'image Docker sur les pull requests.
5. Publication de l'image sur DockerHub pour `main`, les tags et les lancements
   manuels.

Configurer dans `Settings > Secrets and variables > Actions` :

Creer au prealable un repository `shopwise-api` dans le compte DockerHub cible.

| Secret | Contenu |
| --- | --- |
| `DOCKERHUB_USERNAME` | Nom du compte DockerHub |
| `DOCKERHUB_TOKEN` | Access token DockerHub avec droit d'ecriture |

Les images publiees recoivent un tag `sha-...`. La branche principale publie aussi
`latest`, et un tag Git tel que `v1.2.0` publie le tag Docker correspondant.

Les rapports sont recuperables depuis le detail d'une execution dans l'onglet
**Actions**, section **Artifacts**.

## Workflow Git

- `main` reste deployable et doit etre protegee.
- Une branche `feature/<numero-issue>-<description>` ou
  `fix/<numero-issue>-<description>` est creee depuis `main`.
- Les commits sont courts, explicites et referencent l'issue associee.
- Une pull request est ouverte vers `main`.
- La fusion est autorisee uniquement apres validation de la pipeline et revue.
- Une version deployable est marquee par un tag semantique, par exemple `v1.2.0`.

## Frontend

Le Dockerfile Angular, les tests Jest et leur rapport de couverture doivent etre
ajoutes dans le repository frontend. Son image peut ensuite etre deployee sur la
meme machine et configurer son URL API vers le backend ShopWise.
