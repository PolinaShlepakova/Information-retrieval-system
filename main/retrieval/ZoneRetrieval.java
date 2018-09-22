package main.retrieval;

import main.PostingZones;
import main.Tokenizer;
import main.dictionary.Dictionary;
import main.dictionary.DictionaryZones;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Polina Shlepakova
 */
public class ZoneRetrieval {

    DictionaryZones dic;

    public ZoneRetrieval(DictionaryZones dic) {
        this.dic = dic;
    }

    public PostingZones[] query(String query) throws IOException {
        String[] queryTerms = Tokenizer.tokenize(query);
        PostingZones[] IDList = and(queryTerms);
        if (IDList != null) {
            Arrays.sort(IDList);
        }
        return IDList;
    }

    public File[] getFiles(PostingZones[] IDList) {
        File[] files = new File[IDList.length];
        for (int i = 0; i < IDList.length; i++) {
            files[i] = dic.getFile(IDList[i].getID());
        }
        return files;
    }

    private PostingZones[] and(String[] queryTerms) throws IOException {
        if (queryTerms.length == 0) {
            // no documents found
            return null;
        }
        PostingZones[] p1 = dic.getZonesIDList(queryTerms[0].toLowerCase()).toArray();
        for (int i = 1; i < queryTerms.length; i++) {
            p1 = and(p1, dic.getZonesIDList(queryTerms[i].toLowerCase()).toArray());
        }
        return p1;
    }

    private PostingZones[] and(PostingZones[] p1, PostingZones[] p2) {
        PostingZones[] intersection = new PostingZones[(p1.length < p2.length) ? p1.length : p2.length];
        int index = 0;
        for (int i = 0, j = 0; i < p1.length && j < p2.length; ) {
            if (p1[i].getID() == p2[j].getID()) {
                PostingZones temp = PostingZones.and(p1[i], p2[j]);
                if (temp != null) {
                    intersection[index++] = temp;
                }
                i++;
                j++;
            } else if (p1[i].getID() < p2[j].getID()) {
                i++;
            } else {
                j++;
            }
        }
        PostingZones[] res = new PostingZones[index];
        System.arraycopy(intersection, 0, res, 0, index);
        return res;
    }

    public static void main(String[] args) {
        File[] files = {
                new File("D:\\gutenberg\\1\\0\\0\\0\\10001\\10001.txt"),
                new File("D:\\gutenberg\\1\\0\\0\\0\\10002\\10002.txt"),
                new File("D:\\gutenberg\\1\\0\\0\\0\\10003\\10003.txt")
        };

        DictionaryZones dic = new DictionaryZones(files);
        ZoneRetrieval ret = new ZoneRetrieval(dic);

        String[] queries = {
                "mary",
                "king",
                "Mary King",
                "My First Years As A Frenchwoman",
                "english",
                "ascii",
                "Apocolocyntosis",
                "Lucius Seneca",
                "listen",
                "Borderland"
        };

        for (String query : queries) {
            System.out.println("\n" + query);
            try {
                PostingZones[] list = ret.query(query);
                File[] filesRet = ret.getFiles(list);

                for (int i = 0; i < list.length; i++) {
                    System.out.println(filesRet[i]);
                    System.out.println("zones: " + list[i].zonesToString() + "\n");
                }
            } catch (IOException e) {
                System.out.println("Could not open file");
            }
        }
    }
}
