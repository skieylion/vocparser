package jentus.parser.importer.component;

import jentus.parser.common.model.ContextJson;
import jentus.parser.common.model.ExampleJson;
import jentus.parser.common.model.FormJson;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

@Component
@AllArgsConstructor
public class CustomItemWriter implements ItemWriter<FormJson> {

    private static boolean flag=true;
    private final NamedParameterJdbcOperations namedParameterJdbcOperations;
    //private final JdbcTemplate jdbcTemplate;

    static class TypeForm {
        public long id;
        public String name;

        public TypeForm(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class FileTable {
        public String uid;
        public FileTable(String uid) {
            this.uid = uid;
        }
    }

    static class Form {
        long id;
        String value;
        long typeId;
        String transcriptionUK;
        String transcriptionUS;
        String audioFileUK;
        String audioFileUS;
    }

    static class Context {
        long id;
        String def;
        long formId;

        public Context(long id){
            this.id=id;
        }
    }

    static class Example {
        long id;
        String text;

        public Example(long id){
            this.id=id;
        }
    }


    private long saveTypeForm(String name) {
        Map<String, Object> params = Collections.singletonMap("name", name);
        TypeForm typeForm = null;
        try {
            typeForm = namedParameterJdbcOperations.queryForObject("select id,name from TypeForm where name = :name", params, new RowMapper<TypeForm>() {
                @Override
                public TypeForm mapRow(ResultSet resultSet, int i) throws SQLException {
                    long id = resultSet.getLong("id");
                    String name = resultSet.getString("name");
                    return new TypeForm(id, name);
                }
            });

            assert typeForm != null;

            return typeForm.id;

        } catch (EmptyResultDataAccessException e) {
            MapSqlParameterSource paramsUp = new MapSqlParameterSource();
            paramsUp.addValue("name", name);
            KeyHolder kh = new GeneratedKeyHolder();
            namedParameterJdbcOperations.update("insert into TypeForm(name) values(:name)", paramsUp, kh);
            return Objects.requireNonNull(kh.getKey()).longValue();
        }
    }



    private String saveFile(String filePath,String fileName){

        File file=new File(filePath);

        if(file.exists()) {

            try {
                Map<String, Object> params = Collections.singletonMap("fileName", fileName);
                FileTable fileTable = namedParameterJdbcOperations.queryForObject("select uid from FileTable where fileNameBuffer = :fileName", params, new RowMapper<FileTable>() {
                    @Override
                    public FileTable mapRow(ResultSet resultSet, int i) throws SQLException {
                        String uid = resultSet.getString("uid");
                        return new FileTable(uid);
                    }
                });
                assert fileTable != null;

                return fileTable.uid;

            } catch (EmptyResultDataAccessException e) {

                try(InputStream inputStream=new FileInputStream(file)){
                    UUID uuid = UUID.randomUUID();
                    String uid=uuid.toString().toUpperCase();

                    String base64="data:audio/mp3;base64,"+Base64.getEncoder().encodeToString(inputStream.readAllBytes());


                    MapSqlParameterSource paramsUp = new MapSqlParameterSource();
                    paramsUp.addValue("fileNameBuffer", fileName, Types.VARCHAR);
                    paramsUp.addValue("name", fileName + ".mp3", Types.VARCHAR);
                    paramsUp.addValue("format", "mp3", Types.VARCHAR);
                    paramsUp.addValue("data",base64.getBytes(StandardCharsets.UTF_8), Types.BLOB);
                    paramsUp.addValue("uid", uid, Types.VARCHAR);

                    namedParameterJdbcOperations.update("insert into FileTable(uid,name,format,data,fileNameBuffer) values(:uid,:name,:format,:data,:fileNameBuffer)", paramsUp);
                    return uid;
                } catch (IOException fileNotFoundException) {
                    return null;
                }


            }
        } else {
            return null;
        }
    }

    private long saveForm(FormJson formJson){
        try {
            Map<String, Object> params = Collections.singletonMap("fileName", formJson.getFileName());
            Form form = namedParameterJdbcOperations.queryForObject("select id from Form where fileName = :fileName", params, new RowMapper<Form>() {
                @Override
                public Form mapRow(ResultSet resultSet, int i) throws SQLException {
                    long id = resultSet.getLong("id");
                    Form form1=new Form();
                    form1.id=id;
                    return form1;
                }
            });
            assert form != null;
            return form.id;

        } catch (EmptyResultDataAccessException e) {
            UUID uuid = UUID.randomUUID();
            String uid=uuid.toString().toUpperCase();

            long posId = saveTypeForm(formJson.getPosition());
            String uidUK=saveFile(formJson.getLocalPathSoundUK(),formJson.getFileName()+"_UK");
            String uidUS=saveFile(formJson.getLocalPathSoundUS(),formJson.getFileName()+"_US");

            String transcriptionUK=formJson.getTranscriptionUK();
            if(transcriptionUK!=null&&transcriptionUK.length()>49) transcriptionUK=transcriptionUK.substring(0,49);
            String transcriptionUS=formJson.getTranscriptionUS();
            if(transcriptionUS!=null&&transcriptionUS.length()>49) transcriptionUS=transcriptionUS.substring(0,49);

            MapSqlParameterSource paramsUp = new MapSqlParameterSource();
            paramsUp.addValue("value", formJson.getWord(), Types.VARCHAR);
            paramsUp.addValue("typeId", posId, Types.INTEGER);
            paramsUp.addValue("transcription", transcriptionUK, Types.VARCHAR);
            paramsUp.addValue("transcriptionUS", transcriptionUS, Types.VARCHAR);
            paramsUp.addValue("audioFile", uidUK, Types.VARCHAR);
            paramsUp.addValue("audioFileUS", uidUS, Types.VARCHAR);
            paramsUp.addValue("fileName", formJson.getFileName(), Types.VARCHAR);

            KeyHolder kh = new GeneratedKeyHolder();
            namedParameterJdbcOperations.update("insert into Form(value,typeId,transcription,transcriptionUS,audioFile,audioFileUS,fileName) values(:value,:typeId,:transcription,:transcriptionUS,:audioFile,:audioFileUS,:fileName)", paramsUp,kh);

            return Objects.requireNonNull(kh.getKey()).longValue();

        }
    }

    private long saveContext(long formId,ContextJson contextJson){
        try {
            Map<String, Object> params = Collections.singletonMap("def", contextJson.getText());
            Context context = namedParameterJdbcOperations.queryForObject("select id from Context where definition = :def", params, new RowMapper<Context>() {
                @Override
                public Context mapRow(ResultSet resultSet, int i) throws SQLException {
                    long id = resultSet.getLong("id");
                    return new Context(id);
                }
            });
            assert context != null;
            return context.id;
        } catch (EmptyResultDataAccessException e) {
            MapSqlParameterSource paramsUp = new MapSqlParameterSource();

            String text=contextJson.getText();
            if(text!=null&&text.length()>499) text=text.substring(0,499);

            paramsUp.addValue("definition", text, Types.VARCHAR);
            paramsUp.addValue("refForm", formId, Types.INTEGER);

            KeyHolder kh = new GeneratedKeyHolder();
            namedParameterJdbcOperations.update("insert into Context(definition,refForm) values(:definition,:refForm)", paramsUp,kh);

            return Objects.requireNonNull(kh.getKey()).longValue();

        }
    }

    private void attachToSet(long contextId,String cefr){
        try {
            long id=0;
            switch (cefr){
                case "a1": id=54; break;
                case "a2": id=55; break;
                case "b1": id=56; break;
                case "b2": id=57; break;
                case "c1": id=58; break;
                case "c2": id=59; break;
            }
            if(id>0) {

                MapSqlParameterSource paramsUp = new MapSqlParameterSource();
                paramsUp.addValue("contextId", contextId, Types.INTEGER);
                paramsUp.addValue("setId", id, Types.INTEGER);
                namedParameterJdbcOperations.update("insert into SetAndContext(setId,contextId) values(:setId,:contextId)", paramsUp);
            }
        } catch (DuplicateKeyException | NullPointerException ignore){

        }
    }

    private void saveExample(long contextId, ExampleJson exampleJson){
        try {
            Map<String, Object> params = Collections.singletonMap("text", exampleJson.getText());
            Example example = namedParameterJdbcOperations.queryForObject("select id from Example where text = :text", params, new RowMapper<Example>() {
                @Override
                public Example mapRow(ResultSet resultSet, int i) throws SQLException {
                    long id = resultSet.getLong("id");
                    return new Example(id);
                }
            });
        } catch (EmptyResultDataAccessException e) {
            MapSqlParameterSource paramsUp = new MapSqlParameterSource();
            paramsUp.addValue("text", exampleJson.getText(), Types.VARCHAR);
            paramsUp.addValue("refMeaning", contextId, Types.INTEGER);

            KeyHolder kh = new GeneratedKeyHolder();
            namedParameterJdbcOperations.update("insert into Example(text,refMeaning) values(:text,:refMeaning)", paramsUp,kh);
        }
    }

    private void writeOne(FormJson formJson) {
        long formId=saveForm(formJson);

        formJson.getContextJsonList().forEach(contextJson -> {
            long contextId=saveContext(formId,contextJson);
            attachToSet(contextId,contextJson.getCefr());
            contextJson.getExampleJsonList().forEach(exampleJson -> {
                saveExample(contextId,exampleJson);
            });
        });
    }


    @Override
    public void write(List<? extends FormJson> list) throws Exception {
        list.forEach(this::writeOne);
    }
}
