package com.alexsp.englishhelper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "DictionaryEntry")
class DictionaryEntry
{
    @Element
    public String name;
    @Element
    public String url;
    @Element
    public int type;
    @Element(required = false)
    public String icon;
    @Element(required = false)
    public String urlcomposer;
    @Element(required = false)
    public boolean javascript;
    @Element(required = false)
    public boolean cookies;
}
