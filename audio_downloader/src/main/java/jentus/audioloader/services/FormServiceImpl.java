package jentus.audioloader.services;

import com.google.common.io.Files;
import jentus.audioloader.model.Accent;
import jentus.audioloader.model.AudioFile;
import jentus.audioloader.model.FormJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FormServiceImpl implements FormService {
    private final String pathToDirectory;

    FormServiceImpl(@Value("${pathToDirectory}") String pathToDirectory) {
        this.pathToDirectory = pathToDirectory;
    }

    private byte[] download(String url) throws IOException {
        System.out.println(url);
        URL net = new URL(url);
        URLConnection connection=net.openConnection();
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(10000);
        try (InputStream in = connection.getInputStream()) {
            return in.readAllBytes();
        }
    }

    private AudioFile getAudioFile(String url, String fileName, Accent accent,String ext) {

        String fullPath=pathToDirectory+"/"+fileName+"_"+accent.name()+"."+ext;
        File file=new File(fullPath);
        if(!file.exists()) {
            AudioFile audioFile = new AudioFile();
            audioFile.setFileName(fileName);
            audioFile.setAccent(accent);
            audioFile.setExt(ext);
            try {
                audioFile.setSound(download(url));
                return audioFile;
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }


    @Override
    public List<AudioFile> convert(FormJson formJson) throws IOException {

        List<AudioFile> audioFileList=new ArrayList<>();

        String urlUK = formJson.getSoundUKUrl();
        AudioFile audioFileUK=getAudioFile(urlUK,formJson.getFileName(),Accent.UK,Files.getFileExtension(urlUK));
        if(audioFileUK!=null) audioFileList.add(audioFileUK);
        String urlUS = formJson.getSoundUSUrl();
        AudioFile audioFileUS=getAudioFile(urlUK,formJson.getFileName(),Accent.US,Files.getFileExtension(urlUS));
        if(audioFileUS!=null) audioFileList.add(audioFileUS);

        return audioFileList.size()>0?audioFileList:null;
    }
}
