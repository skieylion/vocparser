package jentus.audioloader.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FormJson {
    private String fileName;
    private String word;
    private String transcriptionUK;
    private String soundUKUrl;
    private String transcriptionUS;
    private String soundUSUrl;
    private List<ContextJson> contextJsonList;
    public FormJson(){}
}
