package jentus.parser.themes.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.nio.charset.StandardCharsets;
import java.util.List;

@ToString
@Getter
@Setter
public class WordList {
    private String name;
    private String link;
    private List<String> words;
}