package integration;

import io.github.jtestkit.joot.JootContext;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Orders;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.github.jtestkit.joot.test.fixtures.Tables.ORDERS;
import static io.github.jtestkit.joot.test.fixtures.Tables.PRODUCT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD tests for nullable FK handling with generateNullables flag.
 * 
 * According to JOOT_PLAN.md:
 * - generateNullables=true (default): nullable FK should be auto-created
 * - generateNullables=false: nullable FK should be NULL
 */
class NullableForeignKeyTest extends BaseIntegrationTest {
    
    private JootContext ctx;
    
    @BeforeEach
    void setup() {
        ctx = JootContext.create(dsl);
    }
    
    @Test
    void shouldAutoCreateNullableForeignKeyByDefault() {
        // ACT: Create Order with nullable FK (default generateNullables=true)
        Orders order = ctx.create(ORDERS, Orders.class).build();
        
        // ASSERT: Nullable FK should be auto-created by default
        assertThat(order.getProductId()).isNotNull();  // Product auto-created!
        
        // ASSERT: Product exists in DB
        Product product = dsl.selectFrom(PRODUCT)
            .where(PRODUCT.ID.eq(order.getProductId()))
            .fetchOneInto(Product.class);
        
        assertThat(product).isNotNull();
        assertThat(product.getName()).isNotNull();
    }
    
    @Test
    void shouldSkipNullableForeignKeyInMinimalistMode() {
        // ACT: Create Order with generateNullables=false
        Orders order = ctx.create(ORDERS, Orders.class)
            .generateNullables(false)
            .build();
        
        // ASSERT: Nullable FK should be NULL in minimalist mode
        assertThat(order.getProductId()).isNull();
        
        // ASSERT: No Product was created
        assertThat(dsl.fetchCount(PRODUCT)).isZero();
    }
    
    @Test
    void shouldRespectGlobalMinimalistSetting() {
        // ACT: Set global generateNullables=false
        ctx.generateNullables(false);
        
        Orders order = ctx.create(ORDERS, Orders.class).build();
        
        // ASSERT: Global setting applies to nullable FK
        assertThat(order.getProductId()).isNull();
        assertThat(dsl.fetchCount(PRODUCT)).isZero();
    }
    
    @Test
    void shouldAllowBuilderOverrideForNullableFK() {
        // ACT: Global is false, but builder overrides to true
        ctx.generateNullables(false);
        
        Orders order = ctx.create(ORDERS, Orders.class)
            .generateNullables(true)  // Override: create nullable FK
            .build();
        
        // ASSERT: Builder override wins - Product is created
        assertThat(order.getProductId()).isNotNull();
        assertThat(dsl.fetchCount(PRODUCT)).isEqualTo(1);
    }
    
    @Test
    void shouldRespectExplicitFKValueEvenInDefaultMode() {
        // ARRANGE: Create a Product explicitly
        Product product = ctx.create(PRODUCT, Product.class).build();
        
        // ACT: Create Order with explicit FK (even though default would auto-create)
        Orders order = ctx.create(ORDERS, Orders.class)
            .set(ORDERS.PRODUCT_ID, product.getId())
            .build();
        
        // ASSERT: Explicit FK is used, no new Product created
        assertThat(order.getProductId()).isEqualTo(product.getId());
        assertThat(dsl.fetchCount(PRODUCT)).isEqualTo(1);  // Only one Product
    }
    
    @Test
    void shouldRespectExplicitNullForNullableFK() {
        // ACT: Explicitly set nullable FK to null (despite default generateNullables=true)
        Orders order = ctx.create(ORDERS, Orders.class)
            .set(ORDERS.PRODUCT_ID, null)
            .build();
        
        // ASSERT: Explicit null wins over auto-creation
        assertThat(order.getProductId()).isNull();
        assertThat(dsl.fetchCount(PRODUCT)).isZero();
    }
}

