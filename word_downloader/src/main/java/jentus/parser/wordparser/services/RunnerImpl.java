package jentus.parser.wordparser.services;

import jentus.parser.wordparser.domain.ContextA;
import jentus.parser.wordparser.repositories.ContextARepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

@Service
@AllArgsConstructor
public class RunnerImpl implements Runner {

    private final ContextARepository contextARepository;
    private static final int max = 6000;
    private static final String netPath="https://www.oxfordlearnersdictionaries.com/definition/english";
    private static final String localPath="D:\\work\\word_downloader\\files\\";
    private static final String ext=".html";


    private void getHtmlToFile(int stage, ContextA context) throws MalformedURLException {
        System.out.println("");
        String index = String.valueOf(stage);
        String word = context.getWord().trim().toLowerCase(Locale.ROOT);
        String buffer = "{word}_{index}".replace("{word}", word).replace("{index}", index);
        String path = netPath + "/" + buffer;

        System.out.print("stage ");System.out.print(stage);
        System.out.println(path);


        URL url = new URL(path);
        try (InputStream in = url.openStream()) {
            try (OutputStream out = new FileOutputStream(localPath + buffer+ext)) {
                out.write(in.readAllBytes());
                out.flush();
            } catch (Exception e) {
                System.err.println(e.toString());
            }
            getHtmlToFile(++stage,context);
        } catch (Exception e) {
            System.err.println(e.toString());
        }

    }

    @Override
    public void run() {
        List<ContextA> contextA = contextARepository.findAll();
        contextA = contextA.subList(0, Math.min(contextA.size(), max));

        contextA.stream().parallel().forEach(context -> {
            try {
                getHtmlToFile(1, context);
            } catch (Exception exception){
                System.out.println(exception.toString());
            }
        });
    }
}
