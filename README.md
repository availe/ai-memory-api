Steps to Run:

1) Install and setup Podman Desktop
2) Install Ollama and run `ollama pull nomic-embed-text`
3) Open project.

The project is meant to be consumed with a user-level `gradle.properties` files to set up environment variables. However, since you're unlikely to have this pre-prepared, instead, we can use CLI arguments (which is more verbose, but we can simply copy paste them).

4)To set up project Postgres database, run:
```
./gradlew :backend:infrastructure:containersSetup \
  -Paimemory.db.url=jdbc:postgresql://localhost:5432/ai_memory_api \
  -Paimemory.db.user=postgres \
  -Paimemory.db.password=postgres \
  -Pdevelopment
```
5) To start project backend, run:
```
./gradlew :backend:server:run \
  -Paimemory.db.url=jdbc:postgresql://localhost:5432/ai_memory_api \
  -Paimemory.db.user=postgres \
  -Paimemory.db.password=postgres \
  -Pdevelopment
```
6) To start project frontend, you can run `pnpm --filter ./client dev`

To wipe Postgres database to clean slate, run:
```
./gradlew :backend:infrastructure:destroyAll \
  -Paimemory.db.url=jdbc:postgresql://localhost:5432/ai_memory_api \
  -Paimemory.db.user=postgres \
  -Paimemory.db.password=postgres \
  -Pdevelopment
```
. You will need to set up the database again (step 4) to use the project again.
