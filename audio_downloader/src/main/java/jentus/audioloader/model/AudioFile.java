package jentus.audioloader.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AudioFile {
    private String fileName;
    private  String ext;
    private byte[] sound;
    private Accent accent;
}
