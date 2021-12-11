package jentus.parser.common.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ContextJson {
    private String cefr;
    private String text;
    private List<ExampleJson> exampleJsonList;
}
