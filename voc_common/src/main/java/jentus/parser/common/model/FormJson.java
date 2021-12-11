package jentus.parser.common.model;

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
    private String localPathSoundUK;
    private String transcriptionUS;
    private String soundUSUrl;
    private String localPathSoundUS;
    private String position;

    private List<ContextJson> contextJsonList;
    public FormJson(){}
}
