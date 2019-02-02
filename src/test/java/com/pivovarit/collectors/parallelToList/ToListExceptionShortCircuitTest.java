package com.pivovarit.collectors.parallelToList;

import com.pivovarit.collectors.infrastructure.ExecutorAwareTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.IntStream;

import static com.pivovarit.collectors.ParallelCollectors.parallelToList;
import static com.pivovarit.collectors.ParallelCollectors.supplier;
import static com.pivovarit.collectors.infrastructure.TestUtils.incrementAndThrow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToListExceptionShortCircuitTest extends ExecutorAwareTest {

    private static final int T_POOL_SIZE = 4;

    @BeforeEach
    void setup() {
        executor = threadPoolExecutor(T_POOL_SIZE);
    }

    @Test
    void shouldCollectToCollectionAndShortCircuitOnException() {

        // given
        LongAdder counter = new LongAdder();

        assertThatThrownBy(() ->
          IntStream.generate(() -> 42).boxed().limit(100)
            .map(i -> supplier(() -> incrementAndThrow(counter)))
            .collect(parallelToList(executor, 10))
            .join()).isInstanceOf(CompletionException.class)
          .hasCauseExactlyInstanceOf(IllegalArgumentException.class);

        assertThat(counter.longValue()).isLessThanOrEqualTo(T_POOL_SIZE);
    }

    @Test
    void shouldCollectToCollectionAndShortCircuitOnExceptionUnbounded() {

        // given
        LongAdder counter = new LongAdder();

        assertThatThrownBy(() ->
          IntStream.generate(() -> 42).boxed().limit(100)
            .map(i -> supplier(() -> incrementAndThrow(counter)))
            .collect(parallelToList(executor))
            .join()).isInstanceOf(CompletionException.class)
          .hasCauseExactlyInstanceOf(IllegalArgumentException.class);

        assertThat(counter.longValue()).isLessThanOrEqualTo(T_POOL_SIZE);
    }
}