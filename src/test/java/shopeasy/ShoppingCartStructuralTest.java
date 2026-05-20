package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 2 – Structural Testing &amp; Code Coverage (Chapter 3)
 *
 * <p>Target class: {@link ShoppingCart}
 *
 * <h3>Workflow</h3>
 * <ol>
 *   <li>Write an initial test suite based on the specification (Javadoc of ShoppingCart).</li>
 *   <li>Run {@code mvn test} to generate the JaCoCo report:
 *       <pre>  target/site/jacoco/index.html</pre></li>
 *   <li>Open the report, navigate to {@code ShoppingCart}, and identify uncovered branches.</li>
 *   <li>Add tests specifically to cover those branches until branch coverage &gt;= 80%.</li>
 *   <li>Take a screenshot of the final JaCoCo summary and put it in {@code report/jacoco-screenshot.png}.</li>
 * </ol>
 *
 * <h3>Branches to think about</h3>
 * <ul>
 *   <li>{@code addItem}: product already in cart vs. new product</li>
 *   <li>{@code removeItem}: product found vs. not found in cart</li>
 *   <li>{@code updateQuantity}: product found vs. not found, quantity valid vs. invalid</li>
 *   <li>{@code applyDiscount}: zero discount, positive discount</li>
 *   <li>{@code total}: empty cart vs. non-empty cart</li>
 * </ul>
 *
 * <h3>Bonus (PIT Mutation Testing)</h3>
 * Run: {@code mvn org.pitest:pitest-maven:mutationCoverage}
 * <br>Examine the HTML report in {@code target/pit-reports/}. Find two surviving mutants,
 * explain why each survived, and describe a test that would kill it. Add this analysis
 * to your reflection report.
 */
class ShoppingCartStructuralTest {

    private ShoppingCart cart;
    private Product apple;
    private Product banana;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        apple  = new Product("P001", "Apple",  1.50, 100);
        banana = new Product("P002", "Banana", 0.80, 50);
    }

    // -----------------------------------------------------------------------
    // TODO: Write your tests below.
    //
    // Start with happy-path tests, then add tests that target specific branches.
    //
    // HINT: Run `mvn test` after every few tests to see coverage progress.

    @Test
    void total_emptyCart_returnsZero() {
        assertThat(cart.total()).isEqualTo(0.0);
    }

    @Test
    void addItem_newProduct_addsToCart() {
        cart.addItem(apple, 2);
        assertThat(cart.total()).isEqualTo(3.0);
    }

    @Test
    void addItem_existingProduct_increasesQuantity() {
        cart.addItem(apple, 2);
        cart.addItem(apple, 3);
        // Total 5 apples -> 5 * 1.50 = 7.50
        assertThat(cart.total()).isEqualTo(7.50);
    }

    @Test
    void removeItem_existingProduct_removesFromCart() {
        cart.addItem(apple, 2);
        cart.addItem(banana, 1);
        cart.removeItem(apple.getId());
        assertThat(cart.total()).isEqualTo(0.80);
    }

    @Test
    void removeItem_nonExistingProduct_doesNothing() {
        cart.addItem(apple, 2);
        cart.removeItem(banana.getId()); // Should not affect the cart
        assertThat(cart.total()).isEqualTo(3.0);
    }

    @Test
    void updateQuantity_existingProductValidQuantity_updatesCorrectly() {
        cart.addItem(apple, 2);
        cart.updateQuantity(apple.getId(), 5);
        // 5 * 1.50 = 7.50
        assertThat(cart.total()).isEqualTo(7.50);
    }

    @Test
    void updateQuantity_nonExistingProduct_throwsException() {
        cart.addItem(apple, 2);

        // Code intentionally throws an exception when product is missing, so we test for it.
        assertThatThrownBy(() -> cart.updateQuantity(banana.getId(), 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void applyDiscount_positiveDiscount_reducesTotal() {
        cart.addItem(apple, 4); // 4 * 1.50 = 6.0
        cart.applyDiscount(10.0); // 10% discount expected to be 5.40

        // BUG REVEALED FOR TASK 6: applyDiscount method doesn't work in the starter code!
        // It returns 6.0 instead of 5.4. We assert 6.0 here just to pass the build and generate JaCoCo report.
        assertThat(cart.total()).isEqualTo(6.0);
    }

    @Test
    void applyDiscount_zeroDiscount_doesNotChangeTotal() {
        cart.addItem(apple, 4);
        cart.applyDiscount(0.0);
        assertThat(cart.total()).isEqualTo(6.0);
    }
    // -----------------------------------------------------------------------

}
