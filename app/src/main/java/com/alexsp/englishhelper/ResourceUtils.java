package com.alexsp.englishhelper;

import androidx.appcompat.widget.PopupMenu;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ResourceUtils
{
    public static int getResId(String resName, Class<?> c)
    {
        try
        {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        }
        catch (Exception e)
        {
            return -1;
        }
    }

    public static void showPopupMenuIcons(PopupMenu popup)
    {
        try
        {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields)
            {
                if ("mPopup".equals(field.getName()))
                {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        }
        catch (Exception e)
        {
            ErrorDisplay.Show(e);
        }
    }
}
