package com.alexsp.englishhelper;

public class DictionaryType
{
    public static final int Translate = 0;
    public static final int Dictionary = 1;
    public static final int Synonyms = 2;
    public static final int Usage = 3;
    public static final int Slang= 4;
    public static final int Acronym = 5;
    public static final int length = 6;

    public static String getString(int type)
    {
        switch (type)
        {
            case Translate  : return "Translate";
            case Dictionary : return "Dictionary";
            case Synonyms   : return "Synonyms";
            case Usage      : return "Usage";
            case Slang      : return "Slang";
            case Acronym    : return "Acronym";
            default         : return "";
        }
    }

}
