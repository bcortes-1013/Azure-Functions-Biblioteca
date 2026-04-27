package com.duoc.biblioteca.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import java.util.Optional;

public class ProductorUsuarios {

    @FunctionName("ProductorUsuarios")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE}, authLevel = AuthorizationLevel.ANONYMOUS, route = "usuarios/eventos")
            HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {

        String body = request.getBody().orElse("{}");
        String eventGridEndpoint = System.getenv("EVENT_GRID_ENDPOINT");
        String eventGridKey = System.getenv("EVENT_GRID_KEY");

        String httpMethod = request.getHttpMethod().name();
        String tipoEvento = "";
        String mensajeRespuesta = "";

        switch (httpMethod) {
            case "POST":
                tipoEvento = "Biblioteca.UsuarioCreado";
                mensajeRespuesta = "Creación de usuario encolada";
                break;
            case "PUT":
                tipoEvento = "Biblioteca.UsuarioActualizado";
                mensajeRespuesta = "Actualización de usuario encolada";
                break;
            case "DELETE":
                tipoEvento = "Biblioteca.UsuarioEliminado";
                mensajeRespuesta = "Eliminación de usuario encolada";
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
                "Biblioteca/Usuarios",
                tipoEvento,
                BinaryData.fromObject(body),
                "1.0"
            );

            client.sendEvent(event);
            context.getLogger().info("Evento de usuario publicado: " + tipoEvento);

            return request.createResponseBuilder(HttpStatus.ACCEPTED)
                .body("{\"status\": \"" + mensajeRespuesta + " exitosamente\"}")
                .build();

        } catch (Exception e) {
            context.getLogger().severe("Error al publicar evento de usuario: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()).build();
        }
    }
}