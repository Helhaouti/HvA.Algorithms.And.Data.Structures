package nl.hva.ict.ads;

import java.util.Comparator;
import java.util.List;

/**
 * Provides a generic implementation of various sorting algorithms, specifically: insertion sort,
 * quick sort and heap sort.
 *
 * @author ADS Docenten
 * @author Rida Zeamari
 * @author Hamza el Haouti
 */
public class SorterImpl<E> implements Sorter<E> {

    /**
     * Sorts all items by selection or insertion sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array
     *
     * @param items      List of items
     * @param comparator Comparator
     * @return The items sorted in place
     */
    public List<E> selInsSort(List<E> items, Comparator<E> comparator) {
        // Loop through all items.
        for (int i = 1; i < items.size(); i++) {
            E toBeSortedItem = items.get(i);
            int j = i;

            /* Determine whether the currentItem is smaller than any in
                the sorted section, and place in front of it, if need be. */
            while (j >= 1) {
                E previousItem = items.get(j - 1);
                if (comparator.compare(toBeSortedItem, previousItem) >= 0) break;
                items.set(j, previousItem);
                j--;
            }

            // Place currentItem at behind the currentItem.
            items.set(j, toBeSortedItem);
        }

        return items;
    }

    /**
     * Sorts all items by quick sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array
     *
     * @param items      List of items
     * @param comparator Comparator
     * @return The items sorted in place
     */
    public List<E> quickSort(List<E> items, Comparator<E> comparator) {
        // Sorts the complete list of items from position 0 till size-1, including position size
        this.quickSortPart(items, 0, items.size() - 1, comparator);
        return items;
    }

    /**
     * Sorts all items between index positions 'from' and 'to' inclusive by quick sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array or other positions in items
     *
     * @param items      List of items
     * @param comparator Comparator
     */
    private void quickSortPart(List<E> items, int from, int to, Comparator<E> comparator) {
        // Checks if "from" is bigger than "to" to prevent an index out of bounds.
        if (from >= to) return;

        // Quick sorts the sublist of items between index positions 'from' and 'to' inclusive
        int pivotIndex = partition(items, from, to, comparator);

        // Recursively, calls the quicksort with the different left and right parameters of the sub-array
        quickSortPart(items, from, pivotIndex - 1, comparator);
        quickSortPart(items, pivotIndex + 1, to, comparator);
    }

    /**
     * This method is used to partition the given list and returns the integer which points to the sorted pivot index.
     *
     * @param items      List of items
     * @param from       Index of the list of items
     * @param to         Index of the list of items
     * @param comparator Comparator
     * @return The right cursor
     */
    private int partition(List<E> items, int from, int to, Comparator<E> comparator) {
        E pivot = items.get(from);
        int leftCursor = from + 1;
        int rightCursor = to;

        do {
            // Reduces the scope of the cursors that need to be swiped, because they are larger/smaller than the pivot.

            while (leftCursor <= rightCursor && comparator.compare(pivot, items.get(leftCursor)) > 0) leftCursor++;
            while (leftCursor <= rightCursor && comparator.compare(pivot, items.get(rightCursor)) < 0) rightCursor--;

            
            if (leftCursor > rightCursor) break;

            // Swaps the items at the cursors, because they are smaller/larger than the pivot.
            swapValues(items, leftCursor, rightCursor);
            leftCursor++;
            rightCursor--;
        } while (leftCursor <= rightCursor); //prevents index out of bounds.

        // The rightCursor is the last index of the left column.
        // Places it at the front.
        // Place the pivot item between the two sections.
        items.set(from, items.get(rightCursor));
        items.set(rightCursor, pivot);

        // Returns index of the pivot.
        return rightCursor;
    }

    /**
     * Swaps the first two items in the list with each other.
     *
     * @param items       List of items
     * @param firstIndex  The first index in the list of items
     * @param secondIndex The second index in the list of items
     */
    private void swapValues(List<E> items, int firstIndex, int secondIndex) {
        E temp = items.get(firstIndex);
        items.set(firstIndex, items.get(secondIndex));
        items.set(secondIndex, temp);
    }

    /**
     * Identifies the lead collection of numTops items according to the ordening criteria of comparator
     * and organizes and sorts this lead collection into the first numTops positions of the list
     * with use of (zero-based) heapSwim and heapSink operations.
     * The remaining items are kept in the tail of the list, in arbitrary order.
     * Items are sorted 'in place' without use of an auxiliary list or array or other positions in items
     *
     * @param numTops    The size of the lead collection of items to be found and sorted
     * @param items      List of items
     * @param comparator Comparator
     * @return The items list with its first numTops items sorted according to comparator
     * all other items >= any item in the lead collection
     */
    public List<E> topsHeapSort(int numTops, List<E> items, Comparator<E> comparator) {
        // check 0 < numTops <= items.size()
        if (numTops <= 0) return items;
        else if (numTops > items.size()) return quickSort(items, comparator);

        // the lead collection of numTops items will be organised into a (zero-based) heap structure
        // in the first numTops list positions using the reverseComparator for the heap condition.
        // that way the root of the heap will contain the worst item of the lead collection
        // which can be compared easily against other candidates from the remainder of the list
        Comparator<E> reverseComparator = comparator.reversed();

        // initialise the lead collection with the first numTops items in the list
        for (int heapSize = 2; heapSize <= numTops; heapSize++) {
            // repair the heap condition of items[0..heapSize-2] to include new item items[heapSize-1]
            heapSwim(items, heapSize, reverseComparator);
        }

        // insert remaining items into the lead collection as appropriate
        for (int i = numTops; i < items.size(); i++) {
            // loop-invariant: items[0..numTops-1] represents the current lead collection in a heap data structure
            //  the root of the heap is the currently trailing item in the lead collection,
            //  which will lose its membership if a better item is found from position i onwards
            E item = items.get(i);
            E worstLeadItem = items.get(0);
            if (comparator.compare(item, worstLeadItem) < 0) {
                // item < worstLeadItem, so shall be included in the lead collection
                items.set(0, item);
                // demote worstLeadItem back to the tail collection, at the orginal position of item
                items.set(i, worstLeadItem);
                // repair the heap condition of the lead collection
                heapSink(items, numTops, reverseComparator);
            }
        }

        // the first numTops positions of the list now contain the lead collection
        // the reverseComparator heap condition applies to this lead collection
        // now use heapSort to realise full ordening of this collection
        for (int i = numTops - 1; i > 0; i--) {
            // loop-invariant: items[i+1..numTops-1] contains the tail part of the sorted lead collection
            // position 0 holds the root item of a heap of size i+1 organised by reverseComparator
            // this root item is the worst item of the remaining front part of the lead collection

            // swaps item[0] and item[i];
            // this moves item[0] to its designated position
            swapValues(items, 0, i);

            // The new root may have violated the heap condition
            //  repair the heap condition on the remaining heap of size i
            heapSink(items, i, reverseComparator);
        }

        // alternatively we can realise full ordening with a partial quicksort:
        // quickSortPart(items, 0, numTops-1, comparator);

        return items;
    }

    /**
     * Repairs the zero-based heap condition for items[heapSize-1] on the basis of the comparator
     * all items[0.heapSize-2] are assumed to satisfy the heap condition
     * The zero-bases heap condition says:
     * all items[i] <= items[2*i+1] and items[i] <= items[2*i+2], if any
     * or equivalently:     all items[i] >= items[(i-1)/2]
     *
     * @param items      List of items
     * @param heapSize   The heapSize
     * @param comparator Comparator
     */
    private void heapSwim(List<E> items, int heapSize, Comparator<E> comparator) {
        // swims items[heapSize-1] up the heap until
        // i==0 || items[(i-1]/2] <= items[i]
        int childIndex = heapSize - 1;

        while (childIndex > 0
                && isLargerThan(items.get(getParentIndexOf(childIndex)), items.get(childIndex), comparator)) {
            int parentIndex = getParentIndexOf(childIndex);

            swapValues(items, parentIndex, childIndex);
            childIndex = parentIndex;
        }
    }

    /**
     * Repairs the zero-based heap condition for its root items[0] on the basis of the comparator
     * all items[1.heapSize-1] are assumed to satisfy the heap condition
     * The zero-bases heap condition says:
     * all items[i] <= items[2*i+1] and items[i] <= items[2*i+2], if any
     * or equivalently:     all items[i] >= items[(i-1)/2]
     *
     * @param items      List of items
     * @param heapSize   The heapSize
     * @param comparator Comparator
     */
    private void heapSink(List<E> items, int heapSize, Comparator<E> comparator) {
        // sinks items[0] down the heap until
        // 2*i+1>=heapSize || (items[i] <= items[2*i+1] && items[i] <= items[2*i+2])

        int parentIndex = 0;

        // while parent is smaller than either or both of the existing children,
        // exchange with the greater child so heap order is preserved
        while (getLeftChildIndexOf(parentIndex) < heapSize) {
            int childToSwapIndex = getLeftChildIndexOf(parentIndex);
            int rightChildIndex = getRightChildIndexOf(parentIndex);

            if (rightChildIndex < heapSize
                    && isLargerThan(items.get(childToSwapIndex), items.get(rightChildIndex), comparator)
            ) childToSwapIndex = rightChildIndex;

            if (!isLargerThan(items.get(parentIndex), items.get(childToSwapIndex), comparator)) break;

            swapValues(items, parentIndex, childToSwapIndex);
            parentIndex = childToSwapIndex;
        }
    }

    /**
     * Calculates the parent's index of zero-based heap's node,
     *
     * @param index The index of the node a parent's index is desired.
     * @return The parent's index of provided index.
     */
    private int getParentIndexOf(int index) {
        return (index - 1) / 2;
    }

    /**
     * Calculates the left child's index of zero-based heap's node,
     *
     * @param index The index of the node a left child index is desired.
     * @return The left child index's index of provided index.
     */
    private int getLeftChildIndexOf(int index) {
        return index * 2 + 1;
    }

    /**
     * Calculates the right child's index of zero-based heap's node,
     *
     * @param index The index of the node a right child index is desired.
     * @return The right child index's index of provided index.
     */
    private int getRightChildIndexOf(int index) {
        return this.getLeftChildIndexOf(index) + 1;
    }

    /**
     * Determines whether the first is larger or equal to the second parameter.
     *
     * @param first      An item that needs to be compared to the second item.
     * @param second     An item that needs to be compared to the second item.
     * @param comparator Comparator
     * @return Whether the <code>first</code> is larger than or equal to the <code>second</code> parameter.
     */
    private boolean isLargerThan(E first, E second, Comparator<E> comparator) {
        return comparator.compare(first, second) >= 0;
    }
}