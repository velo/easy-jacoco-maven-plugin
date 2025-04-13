// File: src/test/java/com/marvinformatics/easyjacoco/SimpleMathTest.java
package com.marvinformatics.easyjacoco;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SimpleMathTest {

    // The tested instance is named underTest.
    private final SimpleMath underTest = new SimpleMath();

    /**
     * given two positive integers,
     * when adding them,
     * then the result is their sum.
     */
    @Test
    public void test_givenTwoIntegers_whenAdd_thenSum() {
        int result = underTest.add(3, 5);
        assertThat(result).isEqualTo(8);
    }

    /**
     * given two integers with a non-zero divisor,
     * when dividing them,
     * then the result is the quotient.
     */
    @Test
    public void test_givenNonZeroDivisor_whenDivide_thenQuotient() {
        int result = underTest.divide(20, 5);
        assertThat(result).isEqualTo(4);
    }

    /**
     * given a zero divisor,
     * when dividing,
     * then an IllegalArgumentException is thrown.
     */
    @Test
    public void test_givenZeroDivisor_whenDivide_thenIllegalArgumentException() {
        assertThatThrownBy(() -> underTest.divide(10, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Divisor cannot be zero.");
    }
}
