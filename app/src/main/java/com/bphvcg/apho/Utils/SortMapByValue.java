package com.bphvcg.apho.Utils;

import com.bphvcg.apho.Models.Account;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class SortMapByValue
{
    public static boolean ASC = true;
    public static boolean DESC = false;

    public static HashMap<String, Account> sortByComparator(HashMap<String, Account> unsortMap, final boolean order)
    {

        List<Entry<String, Account>> list = new LinkedList<Entry<String, Account>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Entry<String, Account>>()
        {
            public int compare(Entry<String, Account> o1, Entry<String, Account> o2)
            {
                if (order)
                {
                    return o1.getValue().getFullName().compareTo(o2.getValue().getFullName());
                }
                else
                {
                    return o2.getValue().getFullName().compareTo(o1.getValue().getFullName());

                }
            }
        });

        HashMap<String, Account> sortedMap = new LinkedHashMap<String, Account>();
        for (Entry<String, Account> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}