package de.otto.prototype.controller;

import de.otto.prototype.model.Hashable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.ETAG;

/**
 * Test class to verify order-independent ETag generation for lists of entities.
 */
class BaseControllerETagTest {

    private TestBaseController controller;

    @BeforeEach
    void setUp() {
        controller = new TestBaseController();
    }

    @Test
    void testOrderIndependentETagGeneration() {
        // Create test entities with different ETags
        TestHashableEntity entity1 = new TestHashableEntity("etag-001");
        TestHashableEntity entity2 = new TestHashableEntity("etag-002");
        TestHashableEntity entity3 = new TestHashableEntity("etag-003");

        // Test different orderings of the same entities
        List<TestHashableEntity> order1 = Arrays.asList(entity1, entity2, entity3);
        List<TestHashableEntity> order2 = Arrays.asList(entity3, entity1, entity2);
        List<TestHashableEntity> order3 = Arrays.asList(entity2, entity3, entity1);

        // Generate ETags for different orderings
        String etag1 = controller.getETagHeader(order1).getFirst(ETAG);
        String etag2 = controller.getETagHeader(order2).getFirst(ETAG);
        String etag3 = controller.getETagHeader(order3).getFirst(ETAG);

        // All ETags should be identical regardless of order
        assertEquals(etag1, etag2, "ETags should be identical for different orderings");
        assertEquals(etag1, etag3, "ETags should be identical for different orderings");
        assertEquals(etag2, etag3, "ETags should be identical for different orderings");
    }

    @Test
    void testEmptyListETag() {
        List<TestHashableEntity> emptyList = Collections.emptyList();
        
        MultiValueMap<String, String> headers = controller.getETagHeader(emptyList);
        String etag = headers.getFirst(ETAG);
        
        assertNotNull(etag, "ETag should not be null for empty list");
        assertFalse(etag.isEmpty(), "ETag should not be empty for empty list");
        
        // Test consistency - empty lists should always generate the same ETag
        String etag2 = controller.getETagHeader(Collections.emptyList()).getFirst(ETAG);
        assertEquals(etag, etag2, "Empty lists should generate consistent ETags");
    }

    @Test
    void testSingleItemETag() {
        TestHashableEntity entity = new TestHashableEntity("single-etag");
        List<TestHashableEntity> singleItemList = Collections.singletonList(entity);
        
        String etag = controller.getETagHeader(singleItemList).getFirst(ETAG);
        
        assertNotNull(etag, "ETag should not be null for single item list");
        assertFalse(etag.isEmpty(), "ETag should not be empty for single item list");
    }

    @Test
    void testDifferentContentGeneratesDifferentETags() {
        TestHashableEntity entity1 = new TestHashableEntity("etag-001");
        TestHashableEntity entity2 = new TestHashableEntity("etag-002");
        TestHashableEntity entity3 = new TestHashableEntity("etag-003");

        List<TestHashableEntity> list1 = Arrays.asList(entity1, entity2);
        List<TestHashableEntity> list2 = Arrays.asList(entity1, entity3);

        String etag1 = controller.getETagHeader(list1).getFirst(ETAG);
        String etag2 = controller.getETagHeader(list2).getFirst(ETAG);

        assertNotEquals(etag1, etag2, "Different content should generate different ETags");
    }

    @Test
    void testDuplicateItemsHandling() {
        TestHashableEntity entity1 = new TestHashableEntity("etag-001");
        TestHashableEntity entity2 = new TestHashableEntity("etag-002");

        // Lists with different duplicate patterns but same unique items
        List<TestHashableEntity> list1 = Arrays.asList(entity1, entity2, entity1);
        List<TestHashableEntity> list2 = Arrays.asList(entity2, entity1, entity2);

        String etag1 = controller.getETagHeader(list1).getFirst(ETAG);
        String etag2 = controller.getETagHeader(list2).getFirst(ETAG);

        // Note: This test documents current behavior - duplicates are preserved
        // The ETags will be different because the sorted strings will be different:
        // list1: "etag-001,etag-001,etag-002"
        // list2: "etag-001,etag-002,etag-002"
        assertNotEquals(etag1, etag2, "Lists with different duplicate patterns generate different ETags");
    }

    /**
     * Test implementation of BaseController to access protected methods
     */
    private static class TestBaseController extends BaseController {
        // Exposes protected method for testing
    }

    /**
     * Test implementation of Hashable interface
     */
    private static class TestHashableEntity implements Hashable {
        private final String etag;

        TestHashableEntity(String etag) {
            this.etag = etag;
        }

        @Override
        public String getETag() {
            return etag;
        }
    }
}