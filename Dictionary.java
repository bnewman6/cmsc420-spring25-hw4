import java.lang.String;
import java.util.ArrayList;

class TrieNode {
    public String character;
    public ArrayList<TrieNode> children;

    public TrieNode(String character) {
        this.character = character;
        this.children = null;
    }
}

/**
 * Dictionary class that stores words and associates them with their definitions
 */
public class Dictionary {
    private TrieNode root;

    /**
     * Constructor to initialize the Dictionary
     */
    public Dictionary() {
        /*
         * create a base Trie
         */
        root = new TrieNode(null);
    }

    /**
     * A method to add a new word to the dictionary
     * If the word is already in the dictionary, this method will change its
     * definition
     *
     * @param word       The word we want to add to our dictionary
     * @param definition The definition we want to associate with the word
     */
    public void add(String word, String definition) {
        /*
         * Traverse the Trie until the characters aren't present, then add a new chain
         * of the remaining characters
         */
        int idx = 0;
        TrieNode curr = root;
        boolean endFound = false;
        boolean currFound = false;

        while (idx < word.length()) {
            for (TrieNode child : curr.children) {
                if (child.character.equals(String.valueOf(word.charAt(idx)))) {
                    idx++;
                    curr = child;
                    currFound = true;
                    break;
                }
            }
            if (currFound) {
                currFound = false;
            } else {
                break;
            }
        }

        if (idx == word.length()) {
            for (TrieNode child : curr.children) {
                if (child.character.equals("$")) {
                    child.children.get(0).character = definition;
                    endFound = true;
                }
            }
        }
        if (!endFound) {
            curr.children.add(addNewNode(word.concat(" ").substring(idx), definition));
        }
    }

    private TrieNode addNewNode(String word, String definition) {
        if (word.equals(" ")) {
            TrieNode defNode = new TrieNode(definition);
            TrieNode emptyNode = new TrieNode("$");
            emptyNode.children.add(defNode);
            return emptyNode;
        }

        TrieNode curr = new TrieNode(String.valueOf(word.charAt(0)));
        curr.children.add(addNewNode(word.substring(1), definition));

        return curr;
    }

    /**
     * A method to remove a word from the dictionary
     *
     * @param word The word we want to remove from our dictionary
     */
    public void remove(String word) {
        /*
         * traverse the trie down the word until there are no children
         */
        removeHelper(root, word);
    }

    private boolean removeHelper(TrieNode base, String word) {
        TrieNode curr = base;
        TrieNode goodChild = null;
        TrieNode badChild = null;

        for (TrieNode child : curr.children) {
            if (child.character.equals(String.valueOf(word.charAt(0)))) {
                goodChild = child;
                break;
            }
        }

        if (goodChild == null) {
            return false;
        }

        if (word.length() == 1) {
            for (TrieNode child : goodChild.children) {
                if (child.character.equals("$")) {
                    badChild = child;
                    break;
                }
            }
            if (badChild == null) {
                return false;
            }
            goodChild.children.remove(badChild);
            if (goodChild.children.size() > 0) {
                return false;
            } else {
                curr.children.remove(goodChild);
                return true;
            }
        } else if (removeHelper(goodChild, word.substring(1))) {
            if (goodChild.children.size() > 0) {
                return false;
            } else {
                curr.children.remove(goodChild);
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * A method to get the definition associated with a word from the dictionary
     * Returns null if the word is not in the dictionary
     *
     * @param word The word we want to get the definition for
     * @return The definition of the word, or null if not found
     */
    public String getDefinition(String word) {
        /*
         * traverse the trie down the word. Once done, there must be a child '$' that
         * points to the definition
         */
        int idx = 0;
        TrieNode curr = root;
        boolean currFound = false;

        while (idx < word.length()) {
            for (TrieNode child : curr.children) {
                if (child.character.length() <= word.length() - idx && child.character
                        .equals(String.valueOf(word.substring(idx, idx + child.character.length())))) {
                    idx += child.character.length();
                    curr = child;
                    currFound = true;
                    break;
                }
            }
            if (currFound) {
                currFound = false;
            } else {
                return null;
            }
        }

        for (TrieNode child : curr.children) {
            if (child.character.equals("$")) {
                return child.children.get(0).character;
            }
        }

        return null;
    }

    /**
     * A method to get a string representation of the sequence of nodes which would
     * store the word
     * in a compressed trie consisting of all words in the dictionary
     * Returns null if the word is not in the dictionary
     *
     * @param word The word we want the sequence for
     * @return The sequence representation, or null if word not found
     */
    public String getSequence(String word) {
        /*
         * traverse the trie, adding the characters to a string. When there is more than
         * one child, add "-"
         */
        String sequence = "";
        int idx = 0;
        TrieNode curr = root;
        boolean currFound = false;

        while (idx < word.length()) {
            if (curr.children.size() > 1) {
                sequence = sequence + '-';
            }
            for (TrieNode child : curr.children) {
                if (child.character.length() <= word.length() - idx && child.character
                        .equals(String.valueOf(word.substring(idx, idx + child.character.length())))) {
                    sequence = sequence + child.character;
                    idx += child.character.length();
                    curr = child;
                    currFound = true;
                    break;
                }
            }
            if (currFound) {
                currFound = false;
            } else {
                return null;
            }
        }

        return sequence;
    }

    /**
     * Gives the number of words in the dictionary with the given prefix
     *
     * @param prefix The prefix we want to count words for
     * @return The number of words that start with the prefix
     */
    public int countPrefix(String prefix) {
        /*
         * traverses down the prefix then counts all children from that point on
         */
        int idx = 0;
        TrieNode curr = root;
        boolean currFound = false;

        while (idx < prefix.length()) {
            for (TrieNode child : curr.children) {
                if (child.character.length() <= prefix.length() - idx && child.character
                        .equals(String.valueOf(prefix.substring(idx, idx + child.character.length())))) {
                    idx += child.character.length();
                    curr = child;
                    currFound = true;
                    break;
                }
            }
            if (currFound) {
                currFound = false;
            } else {
                return 0;
            }
        }

        return countHelper(curr);
    }

    private int countHelper(TrieNode node) {
        int count = 0;

        while (node.children.size() == 1) {
            node = node.children.get(0);
        }

        if (node.children != null) {
            for (TrieNode child : node.children) {
                count += 1 + countHelper(child);
            }
        }

        return count;
    }

    /**
     * Compresses the trie by combining nodes with single children
     * This operation should not change the behavior of any other methods
     */
    public void compress() {
        /*
         * Traverse the Trie, combining nodes/branches when there is just one child
         */
        root = compressor(root);
    }

    private TrieNode compressor(TrieNode node) {
        String characters = node.character;
        TrieNode curr = node;

        // loop while single child and add to characters
        while (curr.children.size() <= 1 && !curr.character.equals("$")) {
            curr = curr.children.get(0);
            characters = characters + curr.character;
        }

        // create new TrieNode with character = characters
        TrieNode compressedNode = new TrieNode(characters);

        if (curr.character.equals("$")) {
            compressedNode.children = curr.children;
        } else {
            // compressedNode.children = compressor(child)
            for (TrieNode child : curr.children) {
                compressedNode.children.add(compressor(child));
            }
        }

        // return the compressed node
        return compressedNode;
    }
}