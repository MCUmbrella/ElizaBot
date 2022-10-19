package vip.floatationdevice.eliza;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Eliza
{
    // misc
    public static final String
            WELCOME_MSG = "Hi! I'm Eliza. I am your personal therapy computer.\nPlease tell me your problem.",
            RESPONSE_DAT_ERROR_MSG = "Sorry, I seem to have mis-placed the response files.",
            LOGIC_ERROR_MSG = "Hmmm, I seem to be having problems myself.";
    // Keywords
    private static final String[] KEYWORDS = new String[]{
            " can you ", " can i ", " you are ", " you're ", " i do not ",
            " i feel ", " why do not you ", " why can not i ", " are you ", " i can not ",
            " i am ", " i'm ", " you ", " i want ", " what ",
            " how ", " who ", " where ", " when ", " why ",
            " name ", " cause ", " sorry ", " dream ", " hello ",
            " hi ", " maybe ", " no ", " your ", " always ",
            " think ", " alike ", " yes ", " friend ", " computer ",
            "no key found", "repeat input"
    };
    private static final int MAX_KEYWORDS = 37;
    private static final ArrayList<String> RESPONSES = new ArrayList<>();
    private static final int MAX_RESP_NUM = 116;
    // this array contains the start index to the response strings
    private static final int[] KEY_INDEX = new int[]{
            1, 4, 6, 6, 10,
            14, 17, 20, 22, 25,
            28, 28, 32, 35, 40,
            40, 40, 40, 40, 40,
            49, 51, 55, 59, 63,
            63, 64, 69, 74, 76,
            80, 83, 90, 93, 99,
            106, 113
    };
    // this array contains the end index to the response strings
    private static final int[] KEY_END = new int[]{
            3, 5, 9, 9, 13,
            16, 19, 21, 24, 27,
            32, 32, 34, 39, 48,
            48, 48, 48, 48, 48,
            50, 54, 58, 62, 68,
            63, 68, 73, 75, 79,
            82, 89, 92, 98, 105,
            112, 116
    };
    // String data for conjugations
    private static final String[] CON1 = new String[]{
            "are", "were", "you", "your",
            "I've", "I'm", "me", "yours"
    };
    private static final String[] CON2 = new String[]{
            "am", "was", "I", "my",
            "you've", "you're", "you", "mine"
    };
    // possible punctuation
    private static final char[] PUNC_SET = new char[]{
            '.', '!', '?', ',', ';'
    };
    private static boolean RESPONSE_DAT_INITIALIZED = false;
    private final Random r = new Random();
    private String prevInput = "";

    public Eliza()
    {
        if(!RESPONSE_DAT_INITIALIZED)
            try
            {
                // Data for finding the right responses
                Scanner responseDat = new Scanner(Eliza.class.getResourceAsStream("/RESPONSE.DAT"));
                for(int i = 0; i != MAX_RESP_NUM; i++)
                    RESPONSES.add(responseDat.nextLine());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                RESPONSE_DAT_INITIALIZED = true;
            }
    }

    /**
     * try to 'normalize' a string to make it fit to the text processing functions
     */
    private static String strNormalize(String s)
    {
        return (' ' + s + ' ').toLowerCase()
                .replace('‘', '\'')
                .replace('’', '\'')
                .replace(",", " , ")
                .replace(".", " . ")
                .replace("?", " ? ")
                .replace("!", " ! ")
                .replace(";", " ; ")
                .replace("n't ", " not ")
                .replace("n t ", " not ")
                .replace("n' t ", " not ")
                .replace("n 't ", " not ")
                .replace(" dont ", " do not ")
                .replace(" cant ", " can not ")
                .replace(" cannot ", " can not ")
                .replace(" i 'm ", " I'm ")
                .replace(" i' m ", " I'm ")
                .replace(" im ", " I'm ")
                .replace(" i m ", " I'm ")
                .replace(" u ", " you ")
                .replace(" r ", " are ")
                .replace(" ru ", " are you ")
                .replace(" yoy ", " you ")
                .replace(" uou ", " you ")
                .replace(" ur ", " your ")
                .replace(" we are ", " we're ")
                .replace(" we re ", " we're ")
                .replace(" we' re ", " we're ")
                .replace(" we 're ", " we're ")
                .replace(" you are ", " you're ")
                .replace(" they are ", " they're ")
                .replace(" they re ", " they're ")
                .replace(" they' re ", " they're ")
                .replace(" they 're ", " they're ")
                .replace("'ll ", " will ")
                .replace("' ll ", " will ")
                .replace(" 'll ", " will ")
                .replace(" ll ", " will ")
                .replace(" whats ", " what is ")
                .replace(" what's ", " what is ")
                .replace(" what 's ", " what is ")
                .replace(" what' s ", " what is ")
                .replace(" what s ", " what is ")
                .replace(" wheres ", " where is ")
                .replace(" where's ", " where is ")
                .replace(" where 's ", " where is ")
                .replace(" where' s ", " where is ")
                .replace(" where s ", " where is ")
                .replace(" hows ", " how is ")
                .replace(" how's ", " how is ")
                .replace(" how 's ", " how is ")
                .replace(" how' s ", " how is ")
                .replace(" how s ", " how is ")
                .replace(" whos ", " who is ")
                .replace(" who's ", " who is ")
                .replace(" who 's ", " who is ")
                .replace(" who' s ", " who is ")
                .replace(" yeah ", " yes ")
                .replace(" yea ", " yes ")
                .replace(" ye ", " yes ")
                .replace(" naw ", " no ")
                .replace(" nope ", " no ")
                .replace(" hey ", " hi ")
                .replace(" because ", " cause ")
                .replace(" cuz ", " cause ");

    }

    /**
     * drop leading and trailing spaces and punctuation
     */
    private static String cTrim(String s)
    {
        StringBuilder sb = new StringBuilder(s.trim());
        for(char c : PUNC_SET)
        {
            if(sb.length() != 0 && sb.charAt(0) == c)
                sb.deleteCharAt(0);
            if(sb.length() != 0 && sb.charAt(sb.length() - 1) == c)
                sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * try to find a keyword.
     * @return Object[0]: the first sentence after the first keyword found in the string;
     * Object[1]: the index of the keyword in the keywords array;
     * return {"", 35} if no keyword was found;
     */
    private static Object[] findKey(String s0)
    {
        String s = strNormalize(cTrim(s0)).toLowerCase();
        for(int i = 0; i != MAX_KEYWORDS; i++)
        {
            if(s.contains(KEYWORDS[i]))
            {
                int start = s.indexOf(KEYWORDS[i]) + KEYWORDS[i].length();
                int end = s.indexOf('.');
                return new Object[]{
                        s.substring(start, end > start ? end : s.length()),
                        i
                };
            }
        }
        return new Object[]{"", 35};
    }

    /**
     * conjugate a string using the list of strings to be swapped
     */
    public static String conjugate(String s)
    {
        if(s.isEmpty() || cTrim(s).isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String prev = "";
        a:
        for(String ss : cTrim(strNormalize(s)).split(" "))
        {
            if(cTrim(s).isEmpty()) continue;
            for(int i = 0; i != CON1.length; i++)
                if(ss.equalsIgnoreCase(CON1[i]))
                {
                    if(CON1[i].equals("you") && !(prev.equals("are") || prev.equals("were")))
                        sb.append("me").append(' ');
                    else sb.append(CON2[i]).append(' ');
                    prev = ss;
                    continue a;
                }
            for(int i = 0; i != CON2.length; i++)
            {
                if(ss.equalsIgnoreCase(CON2[i]))
                {
                    sb.append(CON1[i]).append(' ');
                    prev = ss;
                    continue a;
                }
            }
            sb.append(ss).append(' ');
        }
        if(sb.length() != 0) sb.deleteCharAt(sb.length() - 1);
        return sb.toString()
                .replace("  ", " ")
                .replace(" ,", ",")
                .replace(" .", ".")
                .replace(" ?", "?")
                .replace(" !", "!")
                .replace(" ;", ";");
    }

    /**
     * read a random response template
     */
    private String readResponseTemplete(int keywordIndex)
    {
        return RESPONSES.get(KEY_INDEX[keywordIndex] - 1 + r.nextInt(KEY_END[keywordIndex] - KEY_INDEX[keywordIndex] + 1));
    }

    /**
     * Get a response based on the keyword in the string
     */
    public String getResponse(String s)
    {
        if(RESPONSES.size() != MAX_RESP_NUM)
            return RESPONSE_DAT_ERROR_MSG;
        if(s == null) return readResponseTemplete(35);
        if(prevInput.equals(s))
            return readResponseTemplete(36);
        prevInput = s;
        try
        {
            Object[] result = findKey(s);
            return readResponseTemplete((int) result[1]).replace("*", conjugate((String) result[0]));
        }
        catch(Exception e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.err.println("[ELIZA] Exception occurred while parsing input \"" + s +
                    "\"\n==================== BEGIN STACKTRACE ====================\n" +
                    sw + "===================== END STACKTRACE ====================="
            );
            return LOGIC_ERROR_MSG;
        }
    }
}
