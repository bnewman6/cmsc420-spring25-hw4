import java.lang.String;
import java.util.ArrayList;

class TrieNode {
    public String label;
    public ArrayList<TrieNode> children;
    public boolean isWordEnd;
    public String definition;

    public TrieNode(String label) {
        this.label = label;
        this.children = new ArrayList<>();
        this.isWordEnd = false;
        this.definition = null;
    }
}

/**
 * Dictionary class that stores words and associates them with their definitions
 */
public class Dictionary {
    private TrieNode root;
    private boolean isCompressed;

    /**
     * Constructor to initialize the Dictionary
     */
    public Dictionary() {
        /*
         * create a base Trie
         */
        root = new TrieNode("");
        isCompressed = false;
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
        if (isCompressed)
            return;

        TrieNode curr = root;
        int idx = 0;

        while (idx < word.length()) {
            boolean found = false;
            for (TrieNode child : curr.children) {
                String label = child.label;
                int len = Math.min(label.length(), word.length() - idx);

                int match = 0;
                while (match < len && label.charAt(match) == word.charAt(idx + match)) {
                    match++;
                }

                if (match > 0) {
                    if (match < label.length()) {
                        // Split the child
                        TrieNode split = new TrieNode(label.substring(match));
                        split.children = child.children;
                        split.isWordEnd = child.isWordEnd;
                        split.definition = child.definition;

                        child.label = label.substring(0, match);
                        child.children = new ArrayList<>();
                        child.children.add(split);
                        child.isWordEnd = false;
                        child.definition = null;
                    }

                    curr = child;
                    idx += match;
                    found = true;
                    break;
                }
            }

            if (!found) {
                TrieNode newNode = new TrieNode(word.substring(idx));
                newNode.isWordEnd = true;
                newNode.definition = definition;
                curr.children.add(newNode);
                return;
            }
        }

        curr.isWordEnd = true;
        curr.definition = definition;
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
        if (isCompressed)
            return;
        removeHelper(root, word, 0);
    }

    private boolean removeHelper(TrieNode node, String word, int idx) {
        if (idx == word.length()) {
            if (!node.isWordEnd)
                return false;
            node.isWordEnd = false;
            node.definition = null;
            return node.children.isEmpty();
        }

        for (int i = 0; i < node.children.size(); i++) {
            TrieNode child = node.children.get(i);
            String label = child.label;
            if (word.startsWith(label, idx)) {
                boolean shouldRemove = removeHelper(child, word, idx + label.length());
                if (shouldRemove) {
                    node.children.remove(i);
                    return !node.isWordEnd && node.children.isEmpty();
                }
                return false;
            }
        }

        return false;
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
        TrieNode curr = root;
        int idx = 0;

        while (idx < word.length()) {
            boolean found = false;
            for (TrieNode child : curr.children) {
                String label = child.label;
                if (word.startsWith(label, idx)) {
                    idx += label.length();
                    curr = child;
                    found = true;
                    break;
                }
            }
            if (!found)
                return null;
        }

        return curr.isWordEnd ? curr.definition : null;
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
        if (!isCompressed)
            return null;
        TrieNode curr = root;
        int idx = 0;
        StringBuilder sb = new StringBuilder();

        while (idx < word.length()) {
            boolean found = false;
            for (TrieNode child : curr.children) {
                String label = child.label;
                if (word.startsWith(label, idx)) {
                    if (sb.length() > 0)
                        sb.append("-");
                    sb.append(label);
                    idx += label.length();
                    curr = child;
                    found = true;
                    break;
                }
            }
            if (!found)
                return null;
        }

        return curr.isWordEnd ? sb.toString() : null;
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
        TrieNode curr = root;
        int idx = 0;

        while (idx < prefix.length()) {
            boolean found = false;
            for (TrieNode child : curr.children) {
                String label = child.label;
                int len = Math.min(label.length(), prefix.length() - idx);
                if (prefix.substring(idx, idx + len).equals(label.substring(0, len))) {
                    idx += len;
                    curr = child;
                    found = true;
                    break;
                }
            }
            if (!found)
                return 0;
        }

        return countWords(curr);
    }

    private int countWords(TrieNode node) {
        int count = node.isWordEnd ? 1 : 0;
        for (TrieNode child : node.children) {
            count += countWords(child);
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
        isCompressed = true;
        for (int i = 0; i < root.children.size(); i++) {
            root.children.set(i, compressNode(root.children.get(i)));
        }
    }

    private TrieNode compressNode(TrieNode node) {
        while (node.children.size() == 1 && !node.isWordEnd) {
            TrieNode child = node.children.get(0);
            node.label += child.label;
            node.isWordEnd = child.isWordEnd;
            node.definition = child.definition;
            node.children = child.children;
        }
        for (int i = 0; i < node.children.size(); i++) {
            node.children.set(i, compressNode(node.children.get(i)));
        }
        return node;
    }
}