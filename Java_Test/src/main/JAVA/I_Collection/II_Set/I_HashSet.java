package I_Collection.II_Set;

public class I_HashSet {
    /**
     * HashSet in Java implements the Set interface of the Collections Framework. It is used to store the unique elements, and it doesn't maintain any specific order of elements.
     *
     * HashSet does not allow duplicate elements.
     * Uses HashMap internally which is an implementation of hash table data structure.
     * Also implements Serializable and Cloneable interfaces.
     * HashSet is not thread-safe. To make it thread-safe, synchronization is needed externally.
     *
     * #Capacity of HashSet
     * Capacity refers to the number of buckets in the hash table. The default capacity of a HashSet is 16 and the load factor is 0.75.
     * When the number of elements exceeds the capacity automatically increases (resizes) to maintain performance.
     * new capacity = old capacity × 2
     *
     * #Load Factor
     * Load Factor is a measure that controls how full the HashSet can get before resizing. Default Load Factor = 0.75. If the number of elements exceeds the threshold, the capacity is doubled.
     * Threshold = capacity × load factor
     *
    * */
}
