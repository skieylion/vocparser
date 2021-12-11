package jentus.parser.wordparser.services;

import jentus.parser.wordparser.domain.ContextFile;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import us.codecraft.xsoup.Xsoup;
import jentus.parser.common.model.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ContextServiceImpl implements ContextService {

    private static final String basePath="https://www.oxfordlearnersdictionaries.com/definition/english";
    private static final String audioPath="D:/work/vocparser/audio_downloader/files";

    private static int buff=0;

    private String entry(String path){
        return "//*[@id=\"entryContent\"]/div/div/div/div[1]"+path;
    }
    private String ol(String path){
        return "//*[@id=\"entryContent\"]/div/ol"+path;
    }

    private String repl(String text){
        return text!=null?text.replaceAll("/",""):null;
    }

    public FormJson convert(ContextFile contextFile) throws IOException {

        FormJson formJson=new FormJson();

        String fullName=contextFile.getFileName().replace(".html","").trim().toLowerCase(Locale.ROOT);

        Document doc = Jsoup.parse(contextFile.getDataStr());
        String wordXPath=entry("/h1");
        String partXPath=entry("/span[1]");
        String word = Xsoup.compile(wordXPath).evaluate(doc).getElements().text();
        String partAttrClass=Xsoup.compile(partXPath).evaluate(doc).getElements().attr("class").toLowerCase(Locale.ROOT);
        String phoneticsXPath=partAttrClass.equals("phonetics")?partXPath:entry("/span[2]");

        String transcriptionUKXPath=phoneticsXPath+"/div[1]/span[1]";
        String transcriptionUK=Xsoup.compile(transcriptionUKXPath).evaluate(doc).getElements().text();
        String soundUKXPath=phoneticsXPath+"/div[1]/div[1]";
        String soundUKUrl=Xsoup.compile(soundUKXPath).evaluate(doc).getElements().attr("data-src-mp3");

        //String transcriptionUSXPath=phoneticsXPath+"/div[2]/span[1]";
        String transcriptionUS=Xsoup.compile(transcriptionUKXPath).evaluate(doc).getElements().text();
        //String soundUSXPath=phoneticsXPath+"/div[2]/div[1]";
        String soundUSUrl=Xsoup.compile(soundUKXPath).evaluate(doc).getElements().attr("data-src-mp3");

        formJson.setWord(word);
        formJson.setTranscriptionUK(repl(transcriptionUK));
        formJson.setSoundUKUrl(soundUKUrl);
        formJson.setLocalPathSoundUK(audioPath+"/"+fullName+"_UK.mp3");
        formJson.setTranscriptionUS(repl(transcriptionUS));
        formJson.setSoundUSUrl(soundUSUrl);
        formJson.setLocalPathSoundUS(audioPath+"/"+fullName+"_US.mp3");
        formJson.setPosition(Xsoup.compile("//*[@id='main-container']//*[@class='pos']").evaluate(doc).getElements().text());

        List<ContextJson> contextJsonList=new ArrayList<>();


        for(int i=1;i<=8;i++){ //8 достаточно
            String li="/li[{i}]".replace("{i}",String.valueOf(i));
            String context=Xsoup.compile(ol(li)).evaluate(doc).getElements().text();
            int sizeLi=1;

            if(context.equals("")){

                li="/span[{sp}]/li[{i}]".replace("{sp}",String.valueOf(i));
                //System.out.println(li);
                sizeLi=5;
            }
            for(int l=1;l<=sizeLi;l++) {
                String buff=li;
                if(sizeLi!=1){
                    buff=li.replace("{i}",String.valueOf(l));
                    context=Xsoup.compile(ol(buff)).evaluate(doc).getElements().text();

                }
                //System.out.println(buff);

                if(!context.equals("")){
                    Elements el=Xsoup.compile(ol(buff+"//*[@class='def']")).evaluate(doc).getElements();//Xsoup.compile(ol(li+"/span[2]")).evaluate(doc).getElements();

                    String contextText=el.text();
                    if(fullName.equals("think_1")){
                        System.out.println(buff);
                        System.out.println(context);
                    }

                    if(!("".equals(contextText))){
                        ContextJson contextJson=new ContextJson();
                        contextJson.setText(contextText);

                        String attrCefr=Xsoup.compile(ol(buff)).evaluate(doc).getElements().attr("cefr");
                        if(!attrCefr.equals("")){
                            contextJson.setCefr(attrCefr);
                        }

                        List<ExampleJson> exampleJsonList=new ArrayList<>();
                        //примеры
                        for(int j=1;j<=10;j++){
                            String example=Xsoup.compile(ol(buff+"/ul/li[{j}]/span".replace("{j}",String.valueOf(j)))).evaluate(doc).getElements().text();
                            if(!"".equals(example)) {
                                ExampleJson exampleJson=new ExampleJson();
                                exampleJson.setText(example);
                                exampleJsonList.add(exampleJson);
                            }
                        }
                        contextJson.setExampleJsonList(exampleJsonList);
                        contextJsonList.add(contextJson);
                    }
                }
            }
        }
        formJson.setContextJsonList(contextJsonList);
        formJson.setFileName(fullName);

        return formJson;
    }
}
