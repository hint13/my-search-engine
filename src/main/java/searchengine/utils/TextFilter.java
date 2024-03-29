package searchengine.utils;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class TextFilter {
    private static final String[] STOP_MORPH_PARTS_RUS = {
            "СОЮЗ", "ПРЕДЛ", "ЧАСТ", "МЕЖД", "МС", "МС-П", "ВВОДН"
    };
    private static final String[] STOP_MORPH_PARTS_ENG = {
            "CONJ", "PREP", "ARTICLE", "PN", "PN_ADJ" //, "ADJECTIVE"
    };
    private static final String[] STOP_MORPH_PARTS =
            Utils.concatTwoStringArrays(STOP_MORPH_PARTS_RUS, STOP_MORPH_PARTS_ENG);

    private String text;
    private final LuceneMorphology rusLM;
    private final LuceneMorphology engLM;

    public TextFilter(String text, LuceneMorphology engLuceneMorph, LuceneMorphology rusLuceneMorph) {
        this.text = text;
        this.engLM = engLuceneMorph;
        this.rusLM = rusLuceneMorph;
    }

    public TextFilter(String text) {
        this.text = text;
        try {
            engLM = new EnglishLuceneMorphology();
            rusLM = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TextFilter(Document jsoupHtmlDocument) {
        this(jsoupHtmlDocument.body().wholeText());
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private LuceneMorphology getLuceneMorphForWord(String word) {
        if (word.matches("\\w+")) {
            return engLM;
        }
        return rusLM;
    }

    public Map<String, Integer> calcLemmas(boolean useOnlyOneMorphForm) {
        Map<String, Integer> lemmasCount = new HashMap<>();

        List<String> words = getWords(text);
        for (String word : words) {
            List<String> lemmas = getLemmas(word, true);
            for (String lemma: lemmas) {
                if (!lemmasCount.containsKey(lemma)) {
                    lemmasCount.put(lemma, 0);
                }
                lemmasCount.put(lemma, lemmasCount.get(lemma) + 1);
                if (useOnlyOneMorphForm)
                    break;
            }
        }
        return lemmasCount;
    }

    public Map<String, Integer> calcLemmas() {
        return calcLemmas(true);
    }

    public List<String> getWords(String text) {
        List<String> res = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\s|\\d+-?\\w*|\\W", Pattern.UNICODE_CHARACTER_CLASS);

        String[] words = pattern.split(text.toLowerCase());

        String regex = "['a-zа-яё-]+";
        for (String word : words) {
            if (word.trim().matches(regex)) {
                res.add(word.trim());
            }
        }
        return res;
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(text, " \t\n\r\f.,:;!?()[]{}<>'\"«»");
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }
        return tokens;
    }

    private List<String> tokenize() {
        return tokenize(text);
    }

    public List<String> getLemmas(String word, boolean checkValid) {
        List<String> lemmas = new LinkedList<>();
        if (word.length() < 2) {
            return lemmas;
        }
        LuceneMorphology luceneMorph = getLuceneMorphForWord(word);
        List<String> wordBaseForms = luceneMorph.getMorphInfo(word);
        for (String wordBaseForm : wordBaseForms) {
            String[] wordFormParts = wordBaseForm.split("\\|");
            if (checkValid && isNotValidForm(wordFormParts[1])) {
                continue;
            }
            lemmas.add(wordFormParts[0]);
        }
        return lemmas;
    }

    private boolean isNotValidForm(String wordFormPart) {
        boolean isNotValid;
        for (String part : STOP_MORPH_PARTS) {
            isNotValid = wordFormPart.contains(part);
            if (isNotValid) {
                return true;
            }
        }
        return false;
    }

    private void printMorphInfo(String... words) {
        for (String word : words) {
            LuceneMorphology morph = getLuceneMorphForWord(word);
            System.out.println(morph.getMorphInfo(word));
        }
    }

    private void printMorphInfo() {
        printMorphInfo(getWords(text).toArray(new String[0]));
    }

    public static void main(String[] args) throws IOException {
        String html = """
                Далеко-далеко за словесными горами в стране гласных и согласных живут рыбные тексты. Вдали от всех живут они в буквенных домах на берегу Семантика большого языкового океана. Маленький ручеек Даль журчит по всей стране и обеспечивает ее всеми необходимыми правилами. Эта парадигматическая страна, в которой жаренные члены предложения залетают прямо в рот.
                Даже всемогущая пунктуация не имеет власти над рыбными текстами, ведущими безорфографичный образ жизни. Однажды одна маленькая строчка рыбного текста по имени Lorem ipsum решила выйти в большой мир грамматики. Великий Оксмокс предупреждал ее о злых запятых, диких знаках вопроса и коварных точках с запятой, но текст не дал сбить себя с толку.
                Он собрал семь своих заглавных букв, подпоясал инициал за пояс и пустился в дорогу. Взобравшись на первую вершину курсивных гор, бросил он последний взгляд назад, на силуэт своего родного города Буквоград, на заголовок деревни Алфавит и на подзаголовок своего переулка Строчка. Грустный риторический вопрос скатился по его щеке и он продолжил свой путь. По дороге встретил текст рукопись. Она предупредила его: «В моей стране все переписывается по несколько раз. Единственное, что от меня осталось, это приставка «и». Возвращайся ты лучше в свою безопасную страну». Не послушавшись рукописи, наш текст продолжил свой путь. Вскоре ему повстречался коварный составитель
                """;

        String text = """
                West End musicals
                The "West End" is London's theatreland - home to over forty theatres. London's plays, shows, and operas attract around 11 million visitors per year and, with tickets costing around £30 each, they bring a lot of income into the capital. The biggest West End attractions are always musicals. Cats ran for 21 years, and Les Miserables is currently celebrating its 18th year in the West End. Here are some of the hottest tickets in town today.\s
                My Fair Lady is based on G. B. Shaw's 1916 play Pygmalion and tells the story of Eliza Doolittle, the Cockney flower-seller chosen from the streets by a professor of linguistics and transformed into a lady. This current production opened in 2001 starring Martine McCutcheon from the TV soap EastEnders. It features well-known songs such as "I'm getting married in the morning" and "On the street where you live".\s
                Bombay Dreams is based in the Indian film industry and features an all-Asian cast. The story centers around Akaash, a poor boy played by Raza Jaffrey, who becomes a film star and falls in love with the daughter of one of Bombay's greatest film directors. The show features modern Indian pop music, such as "Shakalaka Baby", and dazzling costumes and choreography. It opened in 2002 and quickly became one of the most popular shows in London.
                We Will Rock You opened in 2002. This musical, with a script by comedian Ben Elton, takes place in the future when rock music is illegal. The story is based on famous songs by Queen such as "Bohemian Rhapsody" and "I Want to Break Free", the set resembles a rock concert and there are plenty of special effects. And Tony Vincent, who plays the hero Galileo Figaro, sounds very like Queen's lead singer, Freddie Mercury.""";

        String testText = """
                Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа.
                """;

//        TextFilter textFilter = new TextFilter(text + " " + html);
//        TextFilter textFilter = new TextFilter(text);
        TextFilter textFilter = new TextFilter(testText);
//        TextFilter textFilter = new TextFilter("in on at by the one two three two three three four four four four");
        textFilter.calcLemmas().forEach((key, value) -> System.out.println(key + ": " + value));
//        textFilter.printMorphInfo("in", "one", "at");
//        textFilter.printMorphInfo();
    }
}
