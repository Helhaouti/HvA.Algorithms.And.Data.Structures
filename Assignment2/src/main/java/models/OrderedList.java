package models;

import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.ToDoubleFunction;

/**
 * An interface for the creation of <code>List</code>, which is optimized for the use of a binary search algorithm.
 *
 * @param <E> The class of to be stored objects.
 * @author    ADS Docenten
 * @author    Hamza el Haouti
 */
public interface OrderedList<E> extends List<E> {
    int ITEM_NOT_FOUND = -1;

    /**
     * Aggregates data from this <code>OrderedList</code>. It does this by looping through all items, with the help of
     * the * <code>Iterable</code> interface of <code>List</code>. On every item then the provided <code>mapper</code>
     * is called to determine the value of each item, which is summed and returned.
     *
     * @param mapper A <code>ToDoubleFunction</code> to determine the value of the items of this
     * @return       The sum of the value of each item, determined by the <code>mapper</code>
     */
    default double aggregate(ToDoubleFunction<E> mapper) {
        double sum = 0;

        for (E item : this)
            sum += mapper.applyAsDouble(item);

        return sum;
    }

    /**
     * Finds a match of newItem in the list and applies the merger operator with the newItem to that match
     * i.e. the found match is replaced by the outcome of the merge between the match and the newItem
     * If no match is found in the list, the newItem is added to the list.
     *
     * @param item   The item that needs to be merged, with its counterpart in the list.
     * @param merger A function that takes two items and returns an item that contains the merged content of
     *               the two items according to some merging rule.
     *               e.g. a merger could add the value of attribute X of the second item
     *               to attribute X of the first item and then return the first item
     * @return whether a new item was added to the list or not
     */
    boolean merge(E item, BinaryOperator<E> merger);

    /**
     * Returns the index of the first occurrence the binary or linear (,in case the specified element is null or present
     * in the unsorted part of this ArrayList,) search finds of the specified element in this list, or
     * -1 (<code>OrderedArrayList.ITEM_NOT_FOUND</code>) if this list does not contain the element.
     *
     * @param searchItem The item that needs to be found.
     * @return           The index of the requested item or -1 (<code>OrderedArrayList.ITEM_NOT_FOUND</code>)
     */
    int indexOfByBinarySearch(E searchItem);

    /**
     * Sorts the OrderedList, with the last used/provided ordening (<code>Comparator</code>).
     */
    void sort();

    /**
     * Returns the comparator that has been used with the latest sort, or null if no comparator has been defined.
     *
     * @return The comparator used for sorting, or null.
     */
    Comparator<? super E> getOrdening();

}