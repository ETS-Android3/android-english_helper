package com.alexsp.englishhelper;

import android.provider.Settings;
import android.view.Menu;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles dictionary tabs
 */
public class DictionarySelection
{
    public interface DictionaryChange
    {
        /**
         * Called when selected dictionary is changed for a dictionary type
         */
        void onDictionaryChanged();
    }

    Dictionaries dictionaries;
    int[] selections = { 0, 0, 0, 0, 0, 0};
    ArrayList<ArrayList<DictionaryEntry>> dicsByType = new ArrayList<>();
    TabLayout tabLayout;
    DictionaryChange dictionaryChange;
    boolean ignore_tab_selection = false;

    public DictionarySelection(Fragment fragment, TabLayout tabLayout, DictionaryChange dictionaryChange)
    {
        try
        {
            dictionaries = Dictionaries.Load(fragment.getResources().openRawResource(R.raw.dictionaries));
            this.tabLayout = tabLayout;
            this.dictionaryChange = dictionaryChange;
            for (int i = 0; i< selections.length; ++i)
            {
                dicsByType.add(new ArrayList<>());
                dicsByType.set(i, dictionaries.getEntriesByType(i));
                selections[i] = GlobalSettings.getInt("SelectedDictionary_" + DictionaryType.getString(i), 0);
            }
            ConfigureDictionaryTabs();
        }
        catch (Exception e)
        {
            dictionaries = new Dictionaries(); // TODO: add default dictionaries or crash?
        }
    }

    public DictionaryEntry getSelectedEntry(int type)
    {
        return dicsByType.get(type).get(selections[type]);
    }

    public void setSelectedEntry(int type, int selection)
    {
        if (dicsByType.get(type).size() <= selection)
            throw new RuntimeException("Invalid dictionary selection");
        GlobalSettings.setInt("SelectedDictionary_" + DictionaryType.getString(type), selection);
        selections[type] = selection;
    }

    public List<DictionaryEntry> getDictionaries(int type)
    {
        return dicsByType.get(type);
    }

    /**
     * Adds tabs to tablayout
     */
    public void ConfigureDictionaryTabs()
    {
        int selected = tabLayout.getSelectedTabPosition();
        tabLayout.removeAllTabs();
        for (int i=0; i<DictionaryType.length; ++i)
        {
            int icon_res = ResourceUtils.getResId(getSelectedEntry(i).icon, R.drawable.class);
            if (icon_res == -1)
                icon_res = R.drawable.ic_baseline_menu_book_24;
            tabLayout.addTab(tabLayout.newTab().setText(DictionaryType.getString(i)).setIcon(icon_res));
        }
        if (selected > -1)
            tabLayout.selectTab(tabLayout.getTabAt(selected), false);
    }

    /**
     * Show available dictionaries menu for this dictionary type
     * @param tab
     */
    public void showMenu(TabLayout.Tab tab)
    {
        if (ignore_tab_selection) {
            ignore_tab_selection = false;
            return;
        }
        int position = tab.getPosition();
        List<DictionaryEntry> entries = getDictionaries(position);
        PopupMenu popup = new PopupMenu(tabLayout.getContext(), tab.view);
        int i=0;
        for (DictionaryEntry entry: entries)
        {
            popup.getMenu().add(0, i++, Menu.NONE, entry.name).setIcon(ResourceUtils.getResId(entry.icon, R.drawable.class));
        }
        popup.setOnMenuItemClickListener(item -> {
            ignore_tab_selection = true;
            setSelectedEntry(position, item.getItemId());
            ConfigureDictionaryTabs();
            dictionaryChange.onDictionaryChanged();
            return true;
        });
        ResourceUtils.showPopupMenuIcons(popup);
        popup.show();
    }

    public String composeUrl(int type, String text, String languageFrom, String languageTo) throws UnsupportedEncodingException
    {
        String ret = "";
        DictionaryEntry selection = dicsByType.get(type).get(selections[type]);
        String url = selection.url;

        if (selection.urlcomposer == null)
        {
            url = url.replaceAll("%TEXT%", URLEncoder.encode(text.trim(), "UTF-8"));
            if (!languageFrom.isEmpty())
                url = url.replaceAll("%FROM%", languageFrom);
            if (!languageTo.isEmpty())
                url = url.replaceAll("%TO%", languageTo);
            return url;
        }
        else
        {
            try
            {
                tabLayout.getResources().getStringArray(R.array.lang_entries);
                Class<?> urlComposerClass = Class.forName("com.alexsp.englishhelper." + selection.urlcomposer);
                UrlComposer urlComposer = (UrlComposer) urlComposerClass.newInstance();
                return urlComposer.composeUrl(tabLayout.getContext(), url, URLEncoder.encode(text.trim(), "UTF-8"), languageFrom, languageTo);
            }
            catch (ClassNotFoundException e)
            {
                ErrorDisplay.Show(e);
            } catch (IllegalAccessException e) {
                ErrorDisplay.Show(e);
            } catch (InstantiationException e) {
                ErrorDisplay.Show(e);
            }
            return "";
        }
    }
}
