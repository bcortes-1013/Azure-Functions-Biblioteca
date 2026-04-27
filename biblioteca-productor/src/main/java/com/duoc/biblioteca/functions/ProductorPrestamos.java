package com.duoc.biblioteca.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import java.util.Optional;

public class ProductorPrestamos {

    @FunctionName("ProductorPrestamos")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE}, authLevel = AuthorizationLevel.ANONYMOUS, route = "prestamos/eventos")
            HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {

        String body = request.getBody().orElse("{}");
        // Usando tu URL real creada en el grupo de recursos de biblioteca
        String eventGridEndpoint = System.getenv("EVENT_GRID_ENDPOINT");
        String eventGridKey = System.getenv("EVENT_GRID_KEY");

        String httpMethod = request.getHttpMethod().name();
        String tipoEvento = "";
        String mensajeRespuesta = "";

        // Identificamos la acción para la etiqueta del evento
        switch (httpMethod) {
            case "POST":
                tipoEvento = "Biblioteca.PrestamoCreado";
                mensajeRespuesta = "Creación de préstamo encolada";
                break;
            case "PUT":
                tipoEvento = "Biblioteca.PrestamoActualizado";
                mensajeRespuesta = "Actualización de préstamo encolada";
                break;
            case "DELETE":
                tipoEvento = "Biblioteca.PrestamoEliminado";
                mensajeRespuesta = "Eliminación de préstamo encolada";
                break;
            default:
                tipoEvento = "Biblioteca.OperacionDesconocida";
        }

        try {
            EventGridPublisherClient<EventGridEvent> client = new EventGridPublisherClientBuilder()
                .endpoint(eventGridEndpoint)
                .credential(new AzureKeyCredential(eventGridKey))
                .buildEventGridEventPublisherClient();

            EventGridEvent event = new EventGridEvent(
                "Biblioteca/Prestamos",
                tipoEvento,
                BinaryData.fromObject(body),
                "1.0"
            );

            client.sendEvent(event);
            context.getLogger().info("Evento de préstamo publicado: " + tipoEvento);

            return request.createResponseBuilder(HttpStatus.ACCEPTED)
                .body("{\"status\": \"" + mensajeRespuesta + " exitosamente\"}")
                .build();

        } catch (Exception e) {
            context.getLogger().severe("Error al publicar evento de préstamo: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()).build();
        }
    }
}