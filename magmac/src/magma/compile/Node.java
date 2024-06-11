package magma.compile;

import magma.api.Tuple;
import magma.compile.attribute.Attribute;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * The Node interface provides methods to manipulate key-value pairs
 * where keys are strings and values are attributes. It supports
 * adding attributes, applying attributes, merging nodes, and
 * streaming entries.
 */
public interface Node {

    /**
     * Adds an attribute to the node.
     *
     * @param key the key to associate with the attribute
     * @param value the attribute to add
     * @return a new Node instance with the added attribute
     */
    Node with(String key, Attribute value);

    /**
     * Retrieves an attribute associated with the given key.
     *
     * @param key the key to lookup
     * @return an Optional containing the attribute if present, otherwise empty
     */
    Optional<Attribute> apply(String key);

    /**
     * Merges the current node with another node.
     *
     * @param other the node to merge with
     * @return a new Node instance representing the merged result
     */
    Node merge(Node other);

    /**
     * Streams the entries of the node as tuples of key and attribute.
     *
     * @return a Stream of tuples containing the keys and attributes
     */
    Stream<Tuple<String, Attribute>> streamEntries();
}