package io.availe.http4k

import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.MultipartFormField
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.Validator
import org.http4k.lens.multipartForm

internal class FileController {
    val routes = listOf(uploadFile())

    private fun uploadFile(): ContractRoute {
        val documentPart = MultipartFormFile.required("document")
        val ownerPart = MultipartFormField.required("owner")

        val formLens = Body.multipartForm(Validator.Strict, documentPart, ownerPart).toLens()

        return ApiRoutes.FILE meta {
            summary = "Upload a file to the server."
            receiving(formLens)
            returning(Status.CREATED to "The file was uploaded successfully.")
            returning(Status.BAD_REQUEST to "Invalid multipart form data was provided.")
        } bindContract Method.POST to { request ->
            val form = formLens(request)

            val document = documentPart(form)
            val owner = ownerPart(form)


            Response(Status.CREATED)
        }
    }
}