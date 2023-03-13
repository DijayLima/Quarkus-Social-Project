package io.github.DijayLima;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.core.Application;

@OpenAPIDefinition(
        tags = {
                @Tag(name="widget", description="Widget operations."),
                @Tag(name="gasket", description="Operations related to gaskets")
        },
        info = @Info(
                title="API Quarkus Social",
                version = "1.0",
                contact = @Contact(
                        name = "Dijay Lima",
                        url = "",
                        email = "dijay.pereira@gmail.com"),
                license = @License(
                        name = "",
                        url = ""))
)
public class QuarkusSocialApplication extends Application {
}