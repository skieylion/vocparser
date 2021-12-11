package jentus.parser.themes.services;

import jentus.parser.themes.domain.WordList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessorServiceImpl implements ProcessorService {

    public WordList convert(WordList wordList) {

        return wordList;
    }
}
