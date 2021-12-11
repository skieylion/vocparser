package jentus.parser.themes.component;

import jentus.parser.themes.domain.WordList;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@AllArgsConstructor
public class CustomItemWriter implements ItemWriter<WordList> {

    private final NamedParameterJdbcOperations namedParameterJdbcOperations;

    static class Context {
        long id;

        public Context(long id) {
            this.id = id;
        }
    }

    static class WList {
        long id;
        String name;
        WList(long id,String name){
            this.id=id;
            this.name=name;
        }
    }


    @Override
    public void write(List<? extends WordList> wordLists) {
        System.out.println("write");
        System.out.println(wordLists);


        wordLists.forEach(wordList -> {
            Map<String, Object> params = Collections.singletonMap("name", wordList.getName());
            List<WList> wList = namedParameterJdbcOperations.query("select id,name from Sets where name=:name",params, new RowMapper<WList>() {
                @Override
                public WList mapRow(ResultSet resultSet, int i) throws SQLException {
                    long id = resultSet.getLong("id");
                    String name = resultSet.getString("name");
                    return new WList(id,name);
                }
            });

            long setId=-1;

            if(!(wList.size()>0)){
                MapSqlParameterSource paramsUp = new MapSqlParameterSource();
                paramsUp.addValue("name", wordList.getName(),Types.VARCHAR);

                KeyHolder kh = new GeneratedKeyHolder();
                namedParameterJdbcOperations.update("insert into Sets(name) values(:name)", paramsUp,kh);
                setId= Objects.requireNonNull(kh.getKey()).longValue();
            } else {
                setId=wList.get(0).id;
            }

            if(setId>-1){
                long finalSetId = setId;
                wordList.getWords().forEach(word -> {
                    Map<String, Object> params2 = Collections.singletonMap("value", word);
                    List<Context> contextList = namedParameterJdbcOperations.query("select ct.id from Context ct left join Form f on ct.refForm=f.id where f.value = :value", params2, new RowMapper<Context>() {
                        @Override
                        public Context mapRow(ResultSet resultSet, int i) throws SQLException {
                            long id = resultSet.getLong("id");
                            return new Context(id);
                        }
                    });
                    if(contextList.size()>0){
                        contextList.forEach(context -> {
                            long contextId=context.id;
                            MapSqlParameterSource paramsUp = new MapSqlParameterSource();
                            paramsUp.addValue("setId", finalSetId,Types.INTEGER);
                            paramsUp.addValue("contextId", contextId,Types.INTEGER);
                            namedParameterJdbcOperations.update("insert into SetAndContext(setId,contextId) values(:setId,:contextId)", paramsUp);
                        });
                    }

                });
            }
        });
    }
}
