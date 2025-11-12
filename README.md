Steps to Run:

1) Install and setup Podman Desktop
2) Install Ollama and run `ollama pull nomic-embed-text`
3) Open project.

The project is meant to be consumed with a user-level `gradle.properties` files to set up environment variables.
However, since you're unlikely to have this pre-prepared, instead, we can use CLI arguments (which is more verbose, but
we can simply copy paste them).

Note that all the commands below are meant to be executed from the project root.

4)To set up project Postgres database, run:

```
./gradlew :backend:infrastructure:containersSetup \
  -Pdb.url=jdbc:postgresql://localhost:5433/ai_memory_api \
  -Pdb.user=postgres \
  -Pdb.password=postgres \
  -Pdevelopment
```

5) To start project backend, run:

```
./gradlew :backend:server:run \
  -Pdb.url=jdbc:postgresql://localhost:5433/ai_memory_api \
  -Pdb.user=postgres \
  -Pdb.password=postgres \
  -Pdevelopment
```

Note this command will not self-terminate, as the server runs until manually cancelled. It will get stuck at 94%, that
just means the server is working.

6) To start project frontend, you can run `pnpm --filter ./client dev`

**Project Teardown Steps:**

The following command will delete the Postgres container and its volumes. However, the command does not delete the
Postgres image from your computer.

```
./gradlew :backend:infrastructure:destroyAll \
  -Pdb.url=jdbc:postgresql://localhost:5433/ai_memory_api \
  -Pdb.user=postgres \
  -Pdb.password=postgres \
  -Pdevelopment
```