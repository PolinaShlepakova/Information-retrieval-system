package main.retrieval;

import main.dictionary.DictionaryList;
import main.indexes.PositionalIndex;
import utils.PeekingIntegerIterator;
import main.Tokenizer;
import main.Utils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class PhraseRetrieval {

    private DictionaryList dic;

    public PhraseRetrieval(DictionaryList dic) {
        this.dic = dic;
    }

    public File[] query(String query) throws IOException, ClassNotFoundException {
        BooleanRetrievalList boolRet = new BooleanRetrievalList(dic);
        String[] input = Tokenizer.tokenize(query);
        int inputLen = input.length;
        PositionalIndex IDs;
        switch (inputLen) {
            case 0:
                return new File[0];
            case 1:
                IDs = dic.getPositionalIndex(input[0].toLowerCase());
                break;
            case 2:
                IDs = dic.getBiwordPositionalIndex(input[0].toLowerCase() + " " + input[1].toLowerCase());
                break;
            default:
                int biwordsLen = inputLen - 1;
                String[] biwords = new String[biwordsLen];
                for (int i = 0; i < biwordsLen; i++) {
                    biwords[i] = input[i].toLowerCase() + " " + input[i + 1].toLowerCase();
                }
                // AND first two biwords together
                IDs = boolRet.andList(dic.getBiwordPositionalIndex(biwords[0]), dic.getBiwordPositionalIndex(biwords[1]));
                // AND the following biwords with the previous result one by one
                for (int i = 2; i < biwordsLen; i++) {
                    IDs = boolRet.andList(IDs, dic.getBiwordPositionalIndex(biwords[i]));
                }
        }

        File[] files = new File[IDs.size()];
        int filesIndex = 0;
        PositionalIndex.IDIterator itr = IDs.idIterator();
        while (itr.hasNext()) {
            files[filesIndex++] = dic.getFile(itr.next());
        }
        assert (filesIndex == IDs.size());
        return files;
    }

    public File[] queryProximity(String query) throws IOException, ClassNotFoundException {
        String[] input = Tokenizer.tokenize(query);
        int inputLen = input.length;

        if (inputLen == 0) {
            return new File[0];
        }
        if (input[0].charAt(0) == '/') {
            throw new IllegalArgumentException("Incorrectly formulated query, " +
                    "/k operator is at the beginning of the query");
        }

        // tokenize query into words and /k operators
        String[] words = new String[inputLen];
        int[] proximities = new int[inputLen];
        boolean[] ignoreOrder = new boolean[inputLen];
        int wordsIndex = 0;
        for (int i = 0; i < inputLen; i++, wordsIndex++) {
            words[wordsIndex] = input[i].toLowerCase();
            if (i + 1 < inputLen && input[i + 1].charAt(0) == '/') {
                try {
                    int proximity = Integer.parseInt(input[i + 1].substring(1));
                    proximities[wordsIndex] = proximity;
                    ignoreOrder[wordsIndex] = true;
                    // skip next input arr element, because we already processed it (as /k operator)
                    i++;
                } catch (NumberFormatException e) {
                    // not a proximity operator, but a word
                    // if two words ara separated by space, it's a phrase query, so proximity is 1
                    proximities[wordsIndex] = 1;
                    ignoreOrder[wordsIndex] = false;
                }
            }
        }
        words = Utils.resize(words, wordsIndex);
        proximities = Utils.resize(proximities, wordsIndex);
        ignoreOrder = Utils.resize(ignoreOrder, wordsIndex);

        // AND all words with regard to /k operators or whitespaces (which mean phrase query)
        PositionalIndex IDs;
        if (words.length == 1) {
            IDs = dic.getPositionalIndex(words[0]);
        } else {
            // AND first two words together
            IDs = andProximity(dic.getPositionalIndex(words[0]), dic.getPositionalIndex(words[1]),
                    proximities[0], ignoreOrder[0]);
            for (int i = 2; i < wordsIndex; i++) {
                IDs = andProximity(IDs, dic.getPositionalIndex(words[i]), proximities[i - 1], ignoreOrder[i - 1]);
            }
        }

        // transform main.indexes.PositionalIndex to an array of Files
        File[] files = new File[IDs.size()];
        int filesIndex = 0;
        PositionalIndex.IDIterator itr = IDs.idIterator();
        while (itr.hasNext()) {
            files[filesIndex++] = dic.getFile(itr.next());
        }
        assert (filesIndex == IDs.size());
        return files;
    }

    PositionalIndex andProximity(PositionalIndex arg1, PositionalIndex arg2, int proximity, boolean ignoreOrder) {
        if (arg1 == null || arg2 == null) {
            return new PositionalIndex();
        }

        PositionalIndex res = new PositionalIndex();
        PositionalIndex.NodeIterator itr1 = arg1.nodeIterator();
        PositionalIndex.NodeIterator itr2 = arg1.nodeIterator();
        while (itr1.hasNext() && itr2.hasNext()) {
            PositionalIndex.Node node1 = itr1.peek();
            PositionalIndex.Node node2 = itr2.peek();
            if (node1.getID() == node2.getID()) {
                int ID = node1.getID();
                LinkedList<Integer> positions = andPositions(itr1.next().getPositions(), itr2.next().getPositions(),
                        proximity, ignoreOrder);
                res.addLastNode(ID, positions);
            } else if (node1.getID() < node2.getID()) {
                if (itr1.hasSkip()) {
                    if (itr1.skipPeek().getID() <= itr2.peek().getID()) {
                        itr1.skip();
                    }
                } else {
                    itr1.next();
                }
            } else {
                if (itr2.hasSkip()) {
                    if (itr2.skipPeek().getID() <= itr1.peek().getID()) {
                        itr2.skip();
                    }
                } else {
                    itr2.next();
                }
            }
        }
        res.addSkips();
        return res;
    }

    LinkedList<Integer> andPositions(LinkedList<Integer> arg1, LinkedList<Integer> arg2, int proximity, boolean ignoreOrder) {
        LinkedList<Integer> res = new LinkedList<Integer>();
        PeekingIntegerIterator itr1 = new PeekingIntegerIterator(arg1.iterator());
        PeekingIntegerIterator itr2 = new PeekingIntegerIterator(arg2.iterator());
        while (itr1.hasNext() && itr2.hasNext()) {
            int pos1 = itr1.peek();
            int pos2 = itr2.peek();
            if (ignoreOrder ? Math.abs(pos2 - pos1) <= proximity : (pos2 - pos1) <= proximity) {
                res.add(itr1.next());
                itr2.next();
            } else if (pos1 < pos2) {
                itr1.next();
            } else {
                itr2.next();
            }
        }
        return res;
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
        DictionaryList dic = new DictionaryList(files);
        PhraseRetrieval retrieval = new PhraseRetrieval(dic);

        String[] queries = {
                "home /3 sales",
                "sales rise in july",
                "for /5 schizophrenia",
                "sales rise",
                "in july",
                "home",
                "new home",
                "new /4 home /3 sales"
        };

        for (int i = 0; i < queries.length; i++) {
            System.out.println("\n" + queries[i] + ": ");
            File[] res;
            try {
//                res = main.retrieval.query(queries[i]);
                res = retrieval.queryProximity(queries[i]);
            } catch (IOException e) {
                System.err.println("Could not get ID list from disk.");
                // move to next query
                continue;
            } catch (ClassNotFoundException e) {
                System.err.println("Could not find main.indexes.PositionalIndex's class.");
                // move to next query
                continue;
            }

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
