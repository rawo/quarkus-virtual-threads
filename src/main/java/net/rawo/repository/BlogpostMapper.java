package net.rawo.repository;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import net.rawo.resource.Blogpost;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

final class BlogpostMapper {
    static Blogpost toBlogpost(ResultSet i) throws SQLException {
        return new Blogpost(i.getLong("id"),
                i.getString("author"),
                i.getString("content"),
                i.getString("tags"));
    }

    static Blogpost toBlogpost(Row i) {
        return new Blogpost(i.getLong("id"),
                i.getString("author"),
                i.getString("content"),
                i.getString("tags"));
    }

    static List<Blogpost> toBlogpost(RowSet<Row> r) {
        RowIterator<Row> iterator = r.iterator();
        var result = new ArrayList<Blogpost>();
        while (iterator.hasNext()) {
            Row next = iterator.next();
            result.add(toBlogpost(next));
        }
        return result;
    }
}
