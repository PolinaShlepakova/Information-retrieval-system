package main.retrieval;

import main.dictionary.Dictionary;
import main.dictionary.DictionaryTrie;
import main.indexes.PositionalIndex;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class WildcardRetrieval {

    private DictionaryTrie dic;

    WildcardRetrieval(DictionaryTrie dic) {
        this.dic = dic;
    }

    public File[] query(String query) {
        Iterable<String> words = null;
        String[] subQueries = getSubQueries(query);
        int nSubQueries = subQueries.length;

        if (query.indexOf('*') == 0 && query.lastIndexOf("*") == query.length() - 1) {
            words = filter(dic.getAllPrefixedWords(), subQueries);
            return getFiles(words);
        } else if (query.indexOf("*") == 0 && nSubQueries > 1) {
            words = filter(dic.getPostfixedWords(subQueries[nSubQueries - 1]), subQueries);
            return getFiles(words);
        } else if (query.lastIndexOf("*") == (query.length() - 1) && nSubQueries > 1) {
            words = filter(dic.getPrefixedWords(subQueries[0]), subQueries);
            return getFiles(words);
        }

        switch (nSubQueries) {
            case 0:
                return dic.getFiles();
            case 1:
                if (query.indexOf('*') == 0) {
                    // postfix tree
                    words = dic.getPostfixedWords(subQueries[0]);
                } else if (query.indexOf('*') == query.length() - 1) {
                    // prefix tree
                    words = dic.getPrefixedWords(subQueries[0]);
                } else {
                    // not a wildcard query
                    return getFiles(dic.getPositionalIndex(subQueries[0]));
                }
                break;
            case 2:
                words = intersect(dic.getPrefixedWords(subQueries[0]), dic.getPostfixedWords(subQueries[1]));
                break;
            default:
                words = intersect(dic.getPrefixedWords(subQueries[0]), dic.getPostfixedWords(subQueries[nSubQueries - 1]));
                words = filter(words, subQueries);
                break;
        }
        return getFiles(words);
    }

    private String[] getSubQueries(String query) {
        String[] input = query.split("\\*");
        int len = input.length;
        if (len > 0 && input[0].equals("")) {
            String[] res = new String[len - 1];
            System.arraycopy(input, 1, res, 0, len - 1);
            return res;
        } else {
            return input;
        }
    }

    private Iterable<String> filter(Iterable<String> words, String[] subQueries) {
        ArrayList<String> result = new ArrayList<String>();
        for (String word : words) {
            boolean matches = true;
            String wordCopy = new String(word);
            for (String subQuery : subQueries) {
                int pos = wordCopy.indexOf(subQuery);
                if (pos < 0) {
                    matches = false;
                    break;
                }
                wordCopy = wordCopy.substring(pos + subQuery.length());
            }
            if (matches) {
                result.add(word);
            }
        }
        return result;
    }

    private Iterable<String> intersect(Iterable<String> list1, Iterable<String> list2) {
        ArrayList<String> result = new ArrayList<String>();
        Iterator<String> itr1 = list1.iterator();
        Iterator<String> itr2 = list2.iterator();
        String str1 = null, str2 = null;
        if (itr1.hasNext() && itr2.hasNext()) {
            str1 = itr1.next();
            str2 = itr2.next();
        }
        while (true) {
            int cmp = str1.compareTo(str2);
            if (cmp == 0) {
                result.add(str1);
                // advance both iterators
                if (itr1.hasNext() && itr2.hasNext()) {
                    str1 = itr1.next();
                    str2 = itr2.next();
                } else {
                    break;
                }
            } else if (cmp < 0) {
                // advance first iterator
                if (itr1.hasNext()) {
                    str1 = itr1.next();
                } else {
                    break;
                }
            } else {
                // advance second iterator
                if (itr2.hasNext()) {
                    str2 = itr2.next();
                } else {
                    break;
                }
            }
        }
        return result;
    }

    private File[] getFiles(PositionalIndex index) {
        PositionalIndex.IDIterator idItr = index.idIterator();
        File[] files = new File[index.size()];
        int filesIndex = 0;
        // walk through the IDs of index
        while (idItr.hasNext()) {
            files[filesIndex++] = dic.getFile(idItr.next());
        }
        return files;
    }

    private File[] getFiles(Iterable<String> words) {
        Set<Integer> IDs = new TreeSet<Integer>();
        // walk through all the words
        for (String word : words) {
            PositionalIndex index = dic.getPositionalIndex(word);
            PositionalIndex.IDIterator idItr = index.idIterator();
            // walk through the IDs of current word and add them to set
            while (idItr.hasNext()) {
                IDs.add(idItr.next());
            }
        }
        File[] files = new File[IDs.size()];
        int filesIndex = 0;
        for (Integer ID : IDs) {
            files[filesIndex++] = dic.getFile(ID);
        }
        return files;
    }

    public static void main(String[] args) {
        File[] files = {
                new File("files\\docs\\0.txt"),
                new File("files\\docs\\1.txt"),
                new File("files\\docs\\2.txt"),
                new File("files\\docs\\3.txt"),
                new File("files\\docs\\4.txt"),
                new File("files\\docs\\5.txt"),
                new File("files\\docs\\6.txt"),
                new File("files\\docs\\7.txt")
        };
        DictionaryTrie dic = new DictionaryTrie(files);
        WildcardRetrieval retrieval = new WildcardRetrieval(dic);

        String[] queries = {
                "*",
                "*s*",
                "*s",
                "*h*o*m*",
                "hom*",
                "sa*s",
                "*se",
                "h*o*m*e",
                "h*m*",
                "sal*"
        };

        for (int i = 0; i < queries.length; i++) {
            System.out.println("\n" + queries[i] + ": \n");
            File[] res = retrieval.query(queries[i]);

            if (res.length == 0) {
                System.out.println("no documents found");
            } else {
                for (File f : res) {
                    System.out.println(f.getPath());
                }
            }
        }
    }

}
