package models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BinaryOperator;

/**
 * Resizable-array implementation of the OrderedList interface. Extends and overrides all necessary methods
 * of Arraylist to optimize this List for binary search.
 * <p>
 * Representation-invariant:
 * all items at index positions 0 <= index < nSorted have been ordered by the given ordening comparator
 * other items at index position nSorted <= index < size() can be in any order amongst themselves
 * and also relative to the sorted section.
 * <p>
 * All values of this list also need to be not null, to make sure all comparators, used for sorting & searching
 * function without NullPointerExceptions.
 *
 * @param <E> The class of to be stored objects.
 * @author    ADS docenten
 * @author    Hamza el Haouti
 */
public class OrderedArrayList<E> extends ArrayList<E> implements OrderedList<E> {
    /**
     * The comparator that has been used with the latest sort.
     */
    protected Comparator<? super E> ordening;

    /**
     * The number of items that have been ordered by barcode in the list
     */
    protected int nSorted;

    /**
     * Initiates a List without ordening.
     */
    public OrderedArrayList() {
        this(null);
    }

    /**
     * Initiates a List with ordening.
     *
     * @param ordening The comparator that needs to be used.
     */
    public OrderedArrayList(Comparator<? super E> ordening) {
        super();
        this.ordening = ordening;
        this.nSorted = 0;
    }

    /**
     * {@inheritDoc}
     *
     * @param newItem {@inheritDoc}
     * @param merger  {@inheritDoc}
     * @return        {@inheritDoc}
     */
    @Override
    public boolean merge(E newItem, BinaryOperator<E> merger) {
        if (newItem == null || merger == null) return false;
        int matchedItemIndex = this.indexOfByRecursiveBinarySearch(newItem);

        // Adds newItem if it was not found.
        if (matchedItemIndex == ITEM_NOT_FOUND) {
            this.add(newItem);
            return true;
        }

        // Replaces the item from this list, with a merged version.
        E mergedItem = merger.apply(this.get(matchedItemIndex), newItem);
        this.set(matchedItemIndex, mergedItem);

        return true;
    }

    /**
     * Returns the index of the first occurrence the binary or linear (,in case the specified element is null or present
     * in the unsorted part of this ArrayList,) search finds of the specified element in this list, or
     * -1 (<code>OrderedList.ITEM_NOT_FOUND</code>) if this list does not contain the element.
     *
     * @param item The item that needs to be found.
     * @return     The index of the requested item or -1 (<code>OrderedList.ITEM_NOT_FOUND</code>)
     */
    @Override
    public int indexOf(Object item) {
        if (item == null) return ITEM_NOT_FOUND;

        return this.indexOfByIterativeBinarySearch((E) item);
    }

    /**
     * {@inheritDoc}
     *
     * @param searchItem {@inheritDoc}
     * @return           {@inheritDoc}
     */
    @Override
    public int indexOfByBinarySearch(E searchItem) {
        if (searchItem == null) return ITEM_NOT_FOUND;

        return this.indexOfByRecursiveBinarySearch(searchItem);
    }

    /**
     * Finds the position of the searchItem by an iterative binary search algorithm in the
     * sorted section of the arrayList, using the this.ordening comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.ordening comparator, and that need not to be in agreement with the .equals test.
     * Here we follow the comparator for ordening items and for deciding on equality.
     *
     * @param searchItem The item to be searched on the basis of comparison by this.ordening
     * @return           The position index of the found item in the arrayList, or -1
     *                   (<code>OrderedList.ITEM_NOT_FOUND</code>) if no item matches the search item.
     */
    public int indexOfByIterativeBinarySearch(E searchItem) {
        if (searchItem == null) return ITEM_NOT_FOUND;
        if (nSorted == 0) return linearSearch(searchItem);

        // Indexes of the start and end of the sorted sections of this list.
        int startIndex = 0;
        int endIndex = nSorted - 1;

        // Splits the list continuously in half, until it finds the item or reaches the last item in this section.
        while (startIndex <= endIndex) {
            // Determine the location and get the item in the middle of this section.
            int midIndex = (startIndex + endIndex) / 2;
            E midItem = get(midIndex);

            int comparison = ordening.compare(searchItem, midItem);

            // Sets the section, that contains items lesser than searchItem, as the search area.
            if (comparison > 0) startIndex = midIndex + 1;

            // Sets the section, that contains items greater than searchItem, as the search area.
            else if (comparison < 0) endIndex = midIndex - 1;

            // Returns the item in the middle if it is equal to searchItem.
            else return midIndex;
        }

        // Performs linear search and returns its result, if binary search yielded no result.
        return linearSearch(searchItem);
    }

    /**
     * Finds the position of the searchItem by a recursive binary search algorithm in the
     * sorted section of the arrayList, using the this.ordening comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.ordening comparator, and that need not to be in agreement with the .equals test.
     * Here we follow the comparator for ordening items and for deciding on equality.
     *
     * @param searchItem The item to be searched on the basis of comparison by this.ordening
     * @return           The position index of the found item in the arrayList, or -1
     *                   (<code>OrderedList.ITEM_NOT_FOUND</code>) if no item matches the search item.
     */
    public int indexOfByRecursiveBinarySearch(E searchItem) {
        if (searchItem == null) return ITEM_NOT_FOUND;

        // Indexes of the start and end of the sorted sections of this list.
        int startIndex = 0;
        int endIndex = nSorted - 1;

        // Splits the list continuously in half, until it finds the item or reaches the last item in this section.
        int index = indexOfByRecursiveBinarySearch(searchItem, startIndex, endIndex);

        // Performs linear search and returns its result, if binary search yielded no result.
        return index == ITEM_NOT_FOUND ? linearSearch(searchItem) : index;
    }

    private int indexOfByRecursiveBinarySearch(E searchItem, int startIndex, int endIndex) {
        if (searchItem == null) return ITEM_NOT_FOUND;

        // Determines whether the item is not in the list,
        // by checking whether specified or sorted section of this list is searchable,
        if (nSorted == 0 || endIndex < startIndex) return ITEM_NOT_FOUND;

        // Determine the location and get the item in the middle of this section.
        int midIndex = (startIndex + endIndex) / 2;
        E midItem = get(midIndex);

        int comparison = ordening.compare(searchItem, midItem);

        // Sets the section, that contains items lesser than searchItem, as the search area.
        if (comparison > 0)
            return indexOfByRecursiveBinarySearch(searchItem, midIndex + 1, endIndex);

        // Sets the section, that contains items greater than searchItem, as the search area.
        else if (comparison < 0)
            return indexOfByRecursiveBinarySearch(searchItem, startIndex, midIndex - 1);

        // Returns the item in the middle if it is equal to searchItem.
        return midIndex;
    }

    /**
     * Attempts linear search of <code>searchItem</code>, if <code>searchItem</code> is an <code>Object</code>, in
     * the unsorted section (nSorted <= index < size()).
     *
     * @param searchItem The item to be searched on the basis of comparison by this.ordening.
     * @return           The index of the found item in the unsorted section of this List,
     *                   or -1 (<code>OrderedList.ITEM_NOT_FOUND</code>) if no item matches the search item.
     */
    private int linearSearch(E searchItem) {
        if (searchItem == null) return ITEM_NOT_FOUND;

        for (int item = nSorted; item < super.size(); item++)
            if (this.ordening.compare(this.get(item), searchItem) == 0) return item;

        return ITEM_NOT_FOUND;
    }

    /**
     * {@inheritDoc}
     *
     * @param element               {@inheritDoc}
     * @return                      {@inheritDoc}
     */
    @Override
    public boolean add(E element) {
        if (element == null) return false;

        return super.add(element);
    }

    /**
     * {@inheritDoc}
     *
     * @param index                      {@inheritDoc}
     * @param element                    {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException      If the provided element is null.
     */
    @Override
    public void add(int index, E element) {
        Objects.requireNonNull(element,
                "Cannot add null to OrderedArrayList, because it does not hold null values.");
        if (this.isWithinSortedPartition(index)) nSorted = index;

        super.add(index, element);
    }

    /**
     * {@inheritDoc}
     * <p>
     * And reduces the sorted section by 1, to compensate for the deletion,
     * if this index is part of the sorted section.
     *
     * @param index {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public E remove(int index) {
        if (isWithinSortedPartition(index)) nSorted--;

        return super.remove(index);
    }

    /**
     * {@inheritDoc}
     * <p>
     * And reduces the sorted section by 1, to compensate for the deletion,
     * if this index is part of the sorted section.
     *
     * @param element {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean remove(Object element) {
        if (element == null) return false;
        if (isWithinSortedPartition(this.indexOf(element))) nSorted--;

        return super.remove(element);
    }

    /**
     * Checks whether the given <code>index</code> is part of the sorted section of this list.
     *
     * @param index The index to check.
     * @return Whether the given <code>index</code> is part of the sorted section of this list.
     */
    private boolean isWithinSortedPartition(int index) {
        return index < nSorted;
    }

    /**
     * {@inheritDoc}
     * <p>
     * And sets the sorted section to 0.
     */
    @Override
    public void clear() {
        super.clear();
        this.nSorted = 0;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * And sets the given comparator as ordening for this list, and sets the sorted section to super.size().
     *
     * @throws NullPointerException If the provided ordening of this list is null.
     */
    @Override
    public void sort(Comparator<? super E> c) {
        Objects.requireNonNull(c, "OrderedArrayList needs a Comparator to sort itself.");
        super.sort(c);

        this.ordening = c;
        this.nSorted = super.size();
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException If the last provided ordening of this list is null.
     */
    @Override
    public void sort() {
        Objects.requireNonNull(this.ordening, "OrderedArrayList needs a Comparator to sort itself.");
        this.sort(this.ordening);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Comparator<? super E> getOrdening() {
        return this.ordening;
    }
}