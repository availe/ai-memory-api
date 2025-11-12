package io.availe.orchestration.postgres

import io.availe.orchestration.containers.domain.ContainerRunSpecification

internal sealed interface PostgresPorts : ContainerRunSpecification.PortMapping {
    data object Postgres : PostgresPorts {
        override val hostPortNumber = 5433
        override val containerPortNumber = 5432
    }
}

internal object PostgresPortsList : ContainerRunSpecification.PortList {
    val Postgres = PostgresPorts.Postgres
    override val all = listOf(Postgres)
}