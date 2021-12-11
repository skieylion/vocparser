package jentus.parser.wordparser.component;

import jentus.parser.wordparser.domain.ContextFile;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;

@Component
public class ContextFileReader implements ItemReader<ContextFile> {

    private final Deque<File> files;

    ContextFileReader(@Value("${pathToDirectory}") String pathToDirectory) {
        File folder = new File(pathToDirectory);
        this.files = new ArrayDeque<>(Arrays.asList(Objects.requireNonNull(folder.listFiles())));
    }

    @Override
    public ContextFile read() throws IOException {
        File file = files.pop();
        if (files.isEmpty()) return null;
        try (InputStream targetStream = new FileInputStream(file)) {
            return new ContextFile(file.getName(), targetStream.readAllBytes());
        }
    }
}
