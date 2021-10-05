package com.alexsp.englishhelper;

public class IgnoreFlag
{
    boolean ignore = false;
    public void setIgnore()
    {
        ignore = true;
    }
    public void clearIgnore()
    {
        ignore = false;
    }
    public boolean get()
    {
        boolean ret = ignore;
        ignore = false;
        return ret;
    }
}
