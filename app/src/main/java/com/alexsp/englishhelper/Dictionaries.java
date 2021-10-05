package com.alexsp.englishhelper;

import androidx.annotation.RawRes;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Root
public class Dictionaries
{
    @ElementList(inline = true)
    List<DictionaryEntry> entries;

    public List<DictionaryEntry> getEntries()
    {
        return entries;
    }

    public static Dictionaries Load(InputStream is) throws Exception
    {
        Serializer serializer = new Persister();
        return serializer.read(Dictionaries.class, is);
    }

    public ArrayList<DictionaryEntry> getEntriesByType(int type)
    {
        ArrayList<DictionaryEntry> ret = new ArrayList<>();
        for (DictionaryEntry e: getEntries())
        {
            if (e.type == type)
                ret.add(e);
        }
        return ret;
    }

}
