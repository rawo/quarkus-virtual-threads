package net.rawo.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import net.rawo.resource.Blogpost;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
class InitDb {
    boolean schemaCreate;
    final PgPool client;

    @Inject
    public InitDb(@ConfigProperty(name = "blogposts.schema.create", defaultValue = "true") boolean schemaCreate,
                  PgPool client) {
        this.schemaCreate = schemaCreate;
        this.client = client;
    }

    void config(@Observes StartupEvent ev) {
        if (schemaCreate) {
            run();
        }
    }

    private void run() {
        List<Tuple> batch = prepareData();
        client.query("DROP TABLE IF EXISTS blogposts").execute()
                .flatMap(r -> client.query("CREATE TABLE blogposts (id SERIAL PRIMARY KEY, author VARCHAR(256) NOT NULL, content TEXT NOT NULL , tags VARCHAR(256))").execute())
                .await().indefinitely();
        client.preparedQuery("INSERT INTO blogposts (author, content, tags) VALUES ($1, $2, $3)")
                .executeBatch(batch)
                .await().indefinitely();

    }

    private List<Tuple> prepareData() {
        try {
            InputStream in = getClass().getResourceAsStream("/sample-data.json");
            String result = new BufferedReader(new InputStreamReader(in))
                    .lines().collect(Collectors.joining("\n"));

            ObjectMapper mapper = new ObjectMapper();
            TypeReference<List<Blogpost>> documentMapType =
                    new TypeReference<>() {
                    };

            var document = mapper.readValue(result, documentMapType);

            return document.stream().map(i -> Tuple.of(i.author(), i.content(), i.tags())).toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
