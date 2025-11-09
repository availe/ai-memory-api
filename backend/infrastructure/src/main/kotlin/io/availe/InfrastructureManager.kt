package io.availe

import io.availe.orchestration.containers.ContainerOrchestrator
import io.availe.orchestration.containers.domain.ContainerRunSpecification
import io.availe.orchestration.postgres.PostgresConfig
import io.availe.provisioning.PostgresProvisioner
import io.availe.provisioning.keycloak.KeycloakProvisioner
import io.availe.utils.ServiceWaiter
import org.slf4j.Logger

internal class InfrastructureManager(
    private val logger: Logger,
    private val containerOrchestrator: ContainerOrchestrator,
    private val postgresProvisioner: PostgresProvisioner,
    private val keycloakProvisioner: KeycloakProvisioner,
    private val pgConfig: PostgresConfig,
    private val postgresSpec: ContainerRunSpecification<*, *>,
    private val keycloakSpec: ContainerRunSpecification<*, *>
) {
    private val allSpecsForTeardown = listOf(postgresSpec, keycloakSpec)

    fun setup() {
        logger.info("Starting Postgres container...")
        containerOrchestrator.orchestrationSetup(listOf(postgresSpec))

        ServiceWaiter.waitForPostgres(pgConfig)
        logger.info("Postgres is ready.")

        logger.info("Provisioning Postgres (creating schema)...")
        postgresProvisioner.provision()

        logger.info("Starting Keycloak container...")
        containerOrchestrator.orchestrationSetup(listOf(keycloakSpec))

        ServiceWaiter.waitForKeycloak()
        logger.info("Keycloak is ready.")

        logger.info("All services are healthy. Starting Keycloak provisioning...")
        keycloakProvisioner.provision()
    }

    fun teardown() {
        logger.info("Starting infrastructure teardown...")
        containerOrchestrator.orchestrationTeardown(allSpecsForTeardown)
        logger.info("Infrastructure teardown complete.")
    }
}