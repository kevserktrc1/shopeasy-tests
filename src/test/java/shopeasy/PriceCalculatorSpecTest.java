package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 1 – Specification-Based Testing (Chapter 2)
 *
 * <p>Target class: {@link PriceCalculator}
 *
 * <p>Your goal is to test {@code PriceCalculator.calculate(basePrice, discountRate, taxRate)}
 * using the domain testing technique from Chapter 2:
 * <ol>
 *   <li>Identify equivalence partitions for each input dimension.</li>
 *   <li>Identify boundary values between partitions (on-point / off-point).</li>
 *   <li>Write at least 10 meaningful test cases that cover both partitions and boundaries.</li>
 *   <li>Use {@code @ParameterizedTest} with {@code @CsvSource} for tests that share structure.</li>
 *   <li>Add a comment above each test method explaining which partition or boundary it covers.</li>
 * </ol>
 *
 * <h3>Input dimensions to consider</h3>
 * <ul>
 *   <li><b>basePrice</b>  – zero, positive, very large</li>
 *   <li><b>discountRate</b> – 0 (no discount), (0,100) typical, 100 (full discount)</li>
 *   <li><b>taxRate</b>    – 0 (no tax), (0,100) typical, 100 (100% tax)</li>
 * </ul>
 */
class PriceCalculatorSpecTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    // -----------------------------------------------------------------------
    // TODO: Write your tests below.

    /** Partition: Zero base price (Boundary). Result must always be 0.0 regardless of discount and tax rates. */
    @Test
    void zeroPriceAlwaysReturnsZero() {
        assertThat(calculator.calculate(0.0, 20.0, 10.0)).isEqualTo(0.0);
    }

    /** Boundary: Discount rate at lower bound (0%) and tax at lower bound (0%). Should return exact base price. */
    @Test
    void zeroDiscountAndZeroTaxReturnsBasePrice() {
        assertThat(calculator.calculate(100.0, 0.0, 0.0)).isEqualTo(100.0);
    }

    /** Boundary: Discount rate at upper bound (100%). Full discount wipes price to 0.0 before tax. */
    @Test
    void discountRateHundredMeansFullDiscount() {
        assertThat(calculator.calculate(100.0, 100.0, 20.0)).isEqualTo(0.0);
    }

    /** Boundary: Tax rate at upper bound (100%). Price should double if there is no discount applied. */
    @Test
    void taxRateHundredDoublesPrice() {
        assertThat(calculator.calculate(100.0, 0.0, 100.0)).isEqualTo(200.0);
    }

    /** Partition: Typical values. Checking formula correctness for valid (0,100) range combinations. */
    @ParameterizedTest(name = "base={0}, disc={1}%, tax={2}% => expected={3}")
    @CsvSource({
            "100.0, 10.0, 20.0, 108.0",
            "200.0, 25.0, 10.0, 165.0",
            "50.0, 50.0, 8.0, 27.0"
    })
    void typicalValidValues(double base, double disc, double tax, double expected) {
        assertThat(calculator.calculate(base, disc, tax)).isCloseTo(expected, within(0.001));
    }

    /** Boundary: Off-point values for discount and tax. Smallest valid > 0 and largest valid < 100. */
    @ParameterizedTest(name = "base={0}, disc={1}%, tax={2}% => expected={3}")
    @CsvSource({
            "100.0, 0.01, 0.01, 99.9999", // Just above 0
            "100.0, 99.99, 99.99, 0.0199" // Just below 100
    })
    void offPointBoundaryValues(double base, double disc, double tax, double expected) {
        assertThat(calculator.calculate(base, disc, tax)).isCloseTo(expected, within(0.001));
    }

    /** Partition: Exceptionally large base price (Stress testing large doubles). */
    @Test
    void extremelyLargeBasePrice() {
        double base = 1_000_000_000.0;
        double expected = 1_000_000_000.0 * 0.90 * 1.10; // 10% disc, 10% tax
        assertThat(calculator.calculate(base, 10.0, 10.0)).isCloseTo(expected, within(0.001));
    }

    /**
     * Exceptional/Invalid Inputs: Negative base price, negative discount, negative tax.
     * Since validation (Pre-conditions) is added in Task 3, currently it just processes the raw math.
     */
    @ParameterizedTest(name = "base={0}, disc={1}%, tax={2}% => expected={3}")
    @CsvSource({
            "-100.0, 10.0, 10.0, -99.0",  // Negative base price
            "100.0, -10.0, 10.0, 121.0",  // Negative discount (technically adds to price)
            "100.0, 10.0, -10.0, 81.0"    // Negative tax (technically reduces price)
    })
    void invalidInputsCalculateMathematically(double base, double disc, double tax, double expected) {
        assertThat(calculator.calculate(base, disc, tax)).isCloseTo(expected, within(0.001));
    }

    /** Helper Method Test: applyDiscountOnly calculates with 0% tax implicitly. */
    @Test
    void applyDiscountOnlyIgnoresTax() {
        assertThat(calculator.applyDiscountOnly(100.0, 20.0)).isEqualTo(80.0);
    }

    /** Helper Method Test: applyTaxOnly calculates with 0% discount implicitly. */
    @Test
    void applyTaxOnlyIgnoresDiscount() {
        assertThat(calculator.applyTaxOnly(100.0, 18.0)).isEqualTo(118.0);
    }

    // -----------------------------------------------------------------------

}
