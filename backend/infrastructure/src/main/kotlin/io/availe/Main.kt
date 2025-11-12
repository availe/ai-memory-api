package io.availe

import com.github.dockerjava.api.DockerClient
import io.availe.orchestration.containers.ContainerOrchestrator
import io.availe.orchestration.containers.core.*
import io.availe.orchestration.postgres.loadPostgresConfig
import io.availe.orchestration.postgres.postgresRunSpec
import io.availe.provisioning.PostgresProvisioner
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

internal fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("io.availe.Main")
    val dockerNetworkName = "ai-memory-api-network"
    val dockerClient: DockerClient = DockerClientProvider().createDockerClient()
    var exitCode = 0

    try {
        val dockerNetworkManager = DockerNetworkManager(dockerClient, dockerNetworkName)
        val dockerVolumeManager = DockerVolumeManager(dockerClient)
        val dockerImageManager = DockerImageManager(dockerClient)
        val dockerContainerManager = DockerContainerManager(dockerClient, dockerNetworkName)

        val containerOrchestrator = ContainerOrchestrator(
            dockerNetworkManager = dockerNetworkManager,
            dockerVolumeManager = dockerVolumeManager,
            dockerImageManager = dockerImageManager,
            dockerContainerManager = dockerContainerManager
        )

        val pgConfig = loadPostgresConfig()
        val postgresSpec = postgresRunSpec()
        val postgresProvisioner = PostgresProvisioner(pgConfig)

        val manager = InfrastructureManager(
            logger,
            containerOrchestrator,
            postgresProvisioner,
            pgConfig,
            postgresSpec,
        )

        if ("--destroyAll" in args) {
            manager.teardown()
        } else {
            manager.setup()
            logger.info("System is fully provisioned and ready.")
        }

    } catch (e: Exception) {
        logger.error("An error occurred: ${e.message}", e)
        exitCode = 1
    } finally {
        logger.info("Closing Docker client.")
        dockerClient.close()
        exitProcess(exitCode)
    }
}