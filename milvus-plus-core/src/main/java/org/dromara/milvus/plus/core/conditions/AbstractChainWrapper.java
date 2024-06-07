package org.dromara.milvus.plus.core.conditions;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Iterator;
import java.util.NoSuchElementException;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbstractChainWrapper<T> extends ConditionBuilder<T>{
    protected static class ArrayIterator<T> implements Iterator<T> {
        private final T[] array;
        private int index = 0;

        public ArrayIterator(T[] array) {
            this.array = array;
        }
        @Override
        public boolean hasNext() {
            return index < array.length;
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            return array[index++];
        }
    }
}
