package net.rawo.repository;

import io.agroal.api.AgroalDataSource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import net.rawo.resource.Blogpost;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class BlogpostsRepository {
    final PgPool client; //This is nonblocking postgres driver
    final AgroalDataSource agroalDataSource; //This is standard blocking postgres driver
    private final String SELECT_ALL = "SELECT * FROM blogposts";


    @Inject
    public BlogpostsRepository(final PgPool client, final AgroalDataSource agroalDataSource) {
        this.client = client;
        this.agroalDataSource = agroalDataSource;
    }

    public List<Blogpost> findAllJdbc() {
        var quotesList = new ArrayList<Blogpost>();
        try (Connection connection = agroalDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                quotesList.add(BlogpostMapper.toBlogpost(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return quotesList;
    }

    public Uni<List<Blogpost>> findAllReactiveUni() {
        return client.query(SELECT_ALL)
                .execute()
                .onItem().transform(BlogpostMapper::toBlogpost);
    }

    public Multi<Blogpost> findAllReactiveMulti() {
        return client.query(SELECT_ALL)
                .execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(BlogpostMapper::toBlogpost);
    }
}