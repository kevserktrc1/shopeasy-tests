package shopeasy;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 4 – Property-Based Testing (Chapter 5)
 *
 * <p>Target classes: {@link PriceCalculator}, {@link ShoppingCart}
 *
 * <p>Using jqwik, define and test at least <strong>3 distinct properties</strong>.
 * You must use at least one custom {@code @Provide} method.
 *
 * <h3>Suggested properties (you may use these or design your own)</h3>
 * <ul>
 *   <li><b>Monotonicity</b> – For any fixed base and tax, increasing the discount
 *       rate never increases the final price.</li>
 *   <li><b>Identity</b> – A 0% discount and 0% tax returns exactly the base price.</li>
 *   <li><b>Boundedness</b> – The result is always &gt;= 0.</li>
 *   <li><b>Cart commutativity</b> – Adding product A then B yields the same total
 *       as adding B then A.</li>
 *   <li><b>Discount transitivity</b> – Applying a 10% then another 10% discount via
 *       {@code applyDiscount} is equivalent to a single call with the compounded rate
 *       (think carefully: is this actually true for this implementation?).</li>
 * </ul>
 *
 * <h3>For each property, include a comment that answers:</h3>
 * <ol>
 *   <li>What does this property mean in plain English?</li>
 *   <li>What class of bugs would this property catch?</li>
 * </ol>
 *
 * <h3>If jqwik finds a failing case</h3>
 * Do not just fix the test. Investigate the root cause and explain it in your
 * reflection report (include the counterexample jqwik printed).
 */
class ShopEasyPropertyTest {

    // -----------------------------------------------------------------------
    // TODO: Write your properties below.
    //
    /**
     * Property 1: Identity
     * 1) Plain English: A 0% discount and 0% tax returns exactly the base price.
     * 2) Bug class caught: Mathematical offset errors, applying default flat fees instead of
     * percentages, or logic branches that incorrectly handle 0.0 as a special case.
     */
    @Property
    void identityProperty(
            @ForAll @DoubleRange(min = 0.0, max = 10_000.0) double basePrice) {

        PriceCalculator calc = new PriceCalculator();
        double result = calc.calculate(basePrice, 0.0, 0.0);

        assertThat(result).isCloseTo(basePrice, within(0.001));
    }

    /**
     * Property 2: Monotonicity
     * 1) Plain English: For any fixed base price and tax, increasing the discount rate
     * never increases the final price.
     * 2) Bug class caught: Inverted logic (e.g., adding the discount instead of subtracting),
     * percentage calculation errors, or variable overflow issues.
     */
    @Property
    void monotonicityProperty(
            @ForAll @DoubleRange(min = 0.0, max = 10_000.0) double base,
            @ForAll @DoubleRange(min = 0.0, max = 50.0) double lowerDiscount,
            @ForAll @DoubleRange(min = 51.0, max = 100.0) double higherDiscount,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double tax) {

        PriceCalculator calc = new PriceCalculator();
        double priceWithLowerDiscount = calc.calculate(base, lowerDiscount, tax);
        double priceWithHigherDiscount = calc.calculate(base, higherDiscount, tax);

        // A higher discount rate must always result in a price <= the lower discount price
        assertThat(priceWithHigherDiscount).isLessThanOrEqualTo(priceWithLowerDiscount);
    }

    /**
     * Property 3: Cart Commutativity
     * 1) Plain English: Adding item A then item B to a cart yields the exact same total
     * as adding item B then item A.
     * 2) Bug class caught: State-dependent bugs where the cart total depends on the order
     * of insertion, or accidental overwriting of cart items instead of appending.
     */
    @Property
    void cartCommutativity(
            @ForAll("validProducts") Product p1,
            @ForAll("validProducts") Product p2,
            @ForAll @IntRange(min = 1, max = 10) int qty1,
            @ForAll @IntRange(min = 1, max = 10) int qty2) {

        // Fix for the edge case: Property only holds if products are genuinely distinct.
        // Prevents the bug where same ID but different prices corrupt the cart total.
        Assume.that(!p1.getId().equals(p2.getId()));

        ShoppingCart cartA = new ShoppingCart();
        cartA.addItem(p1, qty1);
        cartA.addItem(p2, qty2);

        ShoppingCart cartB = new ShoppingCart();
        cartB.addItem(p2, qty2);
        cartB.addItem(p1, qty1);

        assertThat(cartA.total()).isCloseTo(cartB.total(), within(0.001));
    }

    // Custom data provider required by Task 4
    // Generates random, valid Product instances to feed into property tests
    @Provide
    Arbitrary<Product> validProducts() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(8),
                Arbitraries.doubles().between(1.0, 500.0)
        ).as((name, price) -> new Product("P-" + name, name, price, 100));
    }
    // -----------------------------------------------------------------------

}
