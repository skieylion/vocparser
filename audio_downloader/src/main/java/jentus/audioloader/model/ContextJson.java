package jentus.audioloader.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ContextJson {
    private String text;
    private List<ExampleJson> exampleJsonList;
}
