package jentus.parser.themes.services;

import jentus.parser.themes.domain.WordList;

public interface ProcessorService {
    WordList convert(WordList wordList);
}
