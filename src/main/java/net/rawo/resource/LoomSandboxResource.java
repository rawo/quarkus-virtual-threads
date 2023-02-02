package net.rawo.resource;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.RunOnVirtualThread;
import net.rawo.repository.BlogpostsRepository;
import org.jboss.resteasy.reactive.NoCache;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/sandbox")
@Produces(MediaType.APPLICATION_JSON)
@NoCache()
@Blocking
public class LoomSandboxResource {

    BlogpostsRepository repository;

    @Inject
    public LoomSandboxResource(BlogpostsRepository repository) {
        this.repository = repository;
    }


    @GET
    @Path("/blocking/jdbc")
    public List<Blogpost> blogPostsBlockingJDBC() {
        return repository.findAllJdbc();
    }

    @GET
    @RunOnVirtualThread
    @Path("/loom/jdbc")
    public List<Blogpost> blogPostsLoomJdbc() {
        return repository.findAllJdbc();
    }
}