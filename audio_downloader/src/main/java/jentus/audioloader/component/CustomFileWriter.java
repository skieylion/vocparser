package jentus.audioloader.component;

import jentus.audioloader.model.AudioFile;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Component
public class CustomFileWriter implements ItemWriter<List<AudioFile>> {

    private final String pathToDirectory;

    CustomFileWriter(@Value("${pathToDirectory}") String pathToDirectory) {
        this.pathToDirectory=pathToDirectory;
    }

    private void writeOne(AudioFile audioFile){
        String name=audioFile.getFileName();
        String accent=audioFile.getAccent().name();
        String ext=audioFile.getExt();
        byte[] data=audioFile.getSound();

        String fullPath=pathToDirectory+"/"+name+"_"+accent+"."+ext;

        try (OutputStream out = new FileOutputStream(fullPath)) {
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(List<? extends List<AudioFile>> list)  {
        list.stream().parallel().forEach(audioFiles -> {
            audioFiles.stream().parallel().forEach(this::writeOne);
        });
    }

}