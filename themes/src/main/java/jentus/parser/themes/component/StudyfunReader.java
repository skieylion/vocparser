package jentus.parser.themes.component;

import jentus.parser.themes.domain.WordList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.codecraft.xsoup.Xsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class StudyfunReader implements ItemReader<WordList> {

    private static final String url="http://studyfun.ru/%D0%92%D0%B8%D0%B7%D1%83%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D0%B5%20%D1%81%D0%BB%D0%BE%D0%B2%D0%B0%D1%80%D0%B8";
    private static short index = 1;
    private final Document document;
    private static final String containerXPath = "//*[@id='contentContainer']/div[4]";
    private static final String baseWordXPath="//*[@id=\"{pre}WordsContainer\"]";

    StudyfunReader() throws IOException {

        this.document = Jsoup.connect(url).get();
    }

    private String getDivXPath() {
        return containerXPath + "/div[" + String.valueOf(index) + "]";
    }

    private String getLinkXPath() {
        return getDivXPath() + "/a";
    }


    private String getLink() {
        return Xsoup.compile(getLinkXPath()).evaluate(document).getElements().attr("href");
    }

    private String getRootName() {
        return Xsoup.compile(getLinkXPath() + "/div/div/h3").evaluate(document).getElements().text();
    }

    private Elements getRoot() {
        //System.out.println(getDivXPath());
        return Xsoup.compile(getDivXPath()).evaluate(document).getElements();
    }



    private String getWord(int step,Document doc,String pre){
        String xpath=baseWordXPath.replace("{pre}",pre)+"/div[{step}]/span[1]/text()".replace("{step}",String.valueOf(step));
        //System.out.println(xpath);
        return Xsoup.compile(xpath).evaluate(doc).getElements().text();
    }

    private List<String> getWords(WordList wordList) throws IOException {
        Document doc=Jsoup.connect(wordList.getLink()).get();
        List<String> words=new ArrayList<>();
        Set.of("left","right").forEach(pre -> {
            String path=baseWordXPath.replace("{pre}",pre)+"/div";
            int size=Xsoup.compile(path).evaluate(doc).getElements().size();
            //System.out.println(path);
            for (int i=1;i<=size;i++){
                words.add(getWord(i,doc,pre));
            }
        });
        return words;
    }

    @Override
    public WordList read() throws IOException {
        Elements root = getRoot();
        if (root.isEmpty()) return null;
        WordList wordList = new WordList();
        wordList.setLink(getLink());
        wordList.setName(getRootName());
        wordList.setWords(getWords(wordList));
        //System.out.println(wordList);
        index++;
        return wordList;
    }
}
