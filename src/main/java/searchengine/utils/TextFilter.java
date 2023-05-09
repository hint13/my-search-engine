package searchengine.utils;

import jakarta.persistence.TemporalType;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.apache.lucene.search.RegexpQuery;
import org.intellij.lang.annotations.RegExp;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class TextFilter {
    private static final String[] MORPH_PARTS = {
            "СОЮЗ", "ПРЕДЛ", "ЧАСТ", "CONJ", "PREP", "ARTICLE"
            //"МЕЖД", "МС", "МС-П", "ВВОДН", "PN", "ADJECTIVE"
    };
    private String text;
    private LuceneMorphology rusLM;
    private LuceneMorphology engLM;

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
        if (word.matches("\\w")) {
            return engLM;
        }
        return rusLM;
    }

    public Map<String, Integer> calcLemmas() {
        Map<String, Integer> lemmasCount = new HashMap<>();

        List<String> words = getWords(text);
        words.forEach(System.out::println);

        return lemmasCount;
    }

    public Set<String> getLemmasForText(String text) {
        return getWordsLemmas(getWords(text));
    }

    public List<String> getWords(String text) {
        List<String> res = new ArrayList<>();
        Pattern pattern = Pattern.compile("[^\\S\\w-']", Pattern.UNICODE_CHARACTER_CLASS);

        String[] words = pattern.split(text.toLowerCase());

        String regex = "['a-zа-яё-]+";
        for (String word : words) {
            if (word.trim().matches(regex)) {
                res.add(word.trim());
            }
        }
        return res;
    }

    public Set<String> getWordsLemmas(List<String> words) {
        Set<String> lemmas = new HashSet<>();
        for (String word : words) {
            lemmas.addAll(getLemmas(getLuceneMorphForWord(word), word, true));
        }
        return lemmas;
    }

    public static List<String> getLemmas(LuceneMorphology luceneMorph, String word, boolean checkValid) {
        List<String> lemmas = new LinkedList<>();
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

    private static boolean isNotValidForm(String wordFormPart) {
        boolean isNotValid = false;
        for (String part : MORPH_PARTS) {
            isNotValid = isNotValid || wordFormPart.contains(part);
        }
        return  isNotValid;
    }

    public static void main(String[] args) throws IOException {
        String html =
                "Далеко-далеко за словесными горами в стране гласных и согласных живут рыбные тексты. Вдали от всех живут они в буквенных домах на берегу Семантика большого языкового океана. Маленький ручеек Даль журчит по всей стране и обеспечивает ее всеми необходимыми правилами. Эта парадигматическая страна, в которой жаренные члены предложения залетают прямо в рот.\n" +
                "Даже всемогущая пунктуация не имеет власти над рыбными текстами, ведущими безорфографичный образ жизни. Однажды одна маленькая строчка рыбного текста по имени Lorem ipsum решила выйти в большой мир грамматики. Великий Оксмокс предупреждал ее о злых запятых, диких знаках вопроса и коварных точках с запятой, но текст не дал сбить себя с толку.\n" +
                "Он собрал семь своих заглавных букв, подпоясал инициал за пояс и пустился в дорогу. Взобравшись на первую вершину курсивных гор, бросил он последний взгляд назад, на силуэт своего родного города Буквоград, на заголовок деревни Алфавит и на подзаголовок своего переулка Строчка. Грустный риторический вопрос скатился по его щеке и он продолжил свой путь. По дороге встретил текст рукопись. Она предупредила его: «В моей стране все переписывается по несколько раз. Единственное, что от меня осталось, это приставка «и». Возвращайся ты лучше в свою безопасную страну». Не послушавшись рукописи, наш текст продолжил свой путь. Вскоре ему повстречался коварный составитель\n";

        String text = "\n" +
                "West End musicals\n" +
                "The 'West End is London's theatreland - home to over forty theatres. London's plays, shows, and operas attract around 11 million visitors per year and, with tickets costing around £30 each, they bring a lot of income into the capital. The biggest West End attractions are always musicals. Cats ran for 21 years, and Les Miserables is currently celebrating its 18th year in the West End. Here are some of the hottest tickets in town today. \n" +
                "My Fair Lady is based on G. B. Shaw's 1916 play Pygmalion and tells the story of Eliza Doolittle, the Cockney flower-seller chosen from the streets by a professor of linguistics and transformed into a lady. This current production opened in 2001 starring Martine McCutcheon from the TV soap EastEnders. It features well-known songs such as \"I'm getting married in the morning\" and \"On the street where you live\". \n" +
                "Bombay Dreams is based in the Indian film industry and features an all-Asian cast. The story centers around Akaash, a poor boy played by Raza Jaffrey, who becomes a film star and falls in love with the daughter of one of Bombay's greatest film directors. The show features modern Indian pop music, such as \"Shakalaka Baby\", and dazzling costumes and choreography. It opened in 2002 and quickly became one of the most popular shows in London.\n" +
                "We Will Rock You opened in 2002. This musical, with a script by comedian Ben Elton, takes place in the future when rock music is illegal. The story is based on famous songs by Queen such as \"Bohemian Rhapsody\" and \"I Want to Break Free\", the set resembles a rock concert and there are plenty of special effects. And Tony Vincent, who plays the hero Galileo Figaro, sounds very like Queen's lead singer, Freddie Mercury.";

//        LuceneMorphology luceneMorphology = new EnglishLuceneMorphology();
//        String[] words = {"bombay", "dreams", "is", "based", "in", "the", "indian", "film", "industry", "and", "features", "an", "all-asian", "cast"};
//        Arrays.stream(words).forEach(w -> {
//            List<String> forms = luceneMorphology.getMorphInfo(w);
//            forms.forEach(System.out::println);
//        });
        TextFilter textFilter = new TextFilter(text + " " + html);
        textFilter.calcLemmas().forEach((key, value) -> System.out.println(key + ": " + value));
    }
}
