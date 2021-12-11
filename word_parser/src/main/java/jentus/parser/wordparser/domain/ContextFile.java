package jentus.parser.wordparser.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
@AllArgsConstructor
public class ContextFile {
    private String fileName;
    private byte[] data;

    public String getDataStr(){
        return new String(data, StandardCharsets.UTF_8);
    }
}