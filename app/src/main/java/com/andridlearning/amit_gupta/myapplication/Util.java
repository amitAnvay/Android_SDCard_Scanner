package com.andridlearning.amit_gupta.myapplication;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Amit_Gupta on 2/14/16.
 */
public class Util {

    public static Map sortByValue(Map unsortedMap) {
        Map sortedMap = new TreeMap(new ValueComparator(unsortedMap));
        sortedMap.putAll(unsortedMap);


        		      /* Display content using Iterator*/
//        Set set = sortedMap.entrySet();
//        Iterator iterator = set.iterator();
//        while(iterator.hasNext()) {
//            Map.Entry mentry = (Map.Entry)iterator.next();
//            System.out.print("key is: " + mentry.getKey() + " & Value is: " + mentry.getValue());
//        }


        return sortedMap;
    }

    public static String getMostFrequentFileExtenstions(TreeMap treeMap){

        StringBuilder mostFrequentfileExtensions = new StringBuilder();
        Set set = treeMap.entrySet();
        Iterator iterator = set.iterator();
        int i = 0;
        while(iterator.hasNext() && i < 5) {
            Map.Entry mentry = (Map.Entry)iterator.next();
            //System.out.print("key is: " + mentry.getKey() + " & Value is: " + mentry.getValue());
            mostFrequentfileExtensions.append(mentry.getKey()+" (Total Count:"+mentry.getValue()+") \n");
            i++;
        }

        return mostFrequentfileExtensions.toString();
    }


    static class ValueComparator implements Comparator {

        Map map;

        public ValueComparator(Map map) {
            this.map = map;
        }

        public int compare(Object keyA, Object keyB) {
            Comparable valueA = (Comparable) map.get(keyA);
            Comparable valueB = (Comparable) map.get(keyB);
            return valueB.compareTo(valueA);
        }
    }
}


