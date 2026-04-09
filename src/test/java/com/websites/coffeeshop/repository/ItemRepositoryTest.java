package com.websites.coffeeshop.repository;

import com.websites.coffeeshop.model.Category;
import com.websites.coffeeshop.model.Item;
import com.websites.coffeeshop.model.Manufacturer;
import com.websites.coffeeshop.model.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.sql.init.mode=never")
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    private Category category;
    private Manufacturer manufacturer;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        manufacturerRepository.deleteAll();
        supplierRepository.deleteAll();

        category = new Category();
        category.setName("Espresso Machines");
        category = categoryRepository.save(category);

        manufacturer = new Manufacturer();
        manufacturer.setName("BrewCo");
        manufacturer.setUrl("https://brewco.example.com");
        manufacturer = manufacturerRepository.save(manufacturer);

        supplier = new Supplier();
        supplier.setName("Supply Co");
        supplier.setContactPerson("Alice");
        supplier.setContactPersonEmail("alice@example.com");
        supplier = supplierRepository.save(supplier);
    }

    private Item createItem(String name, BigDecimal price) {
        Item item = new Item();
        item.setName(name);
        item.setDescription("desc");
        item.setPrice(price);
        item.setCategory(category);
        item.setManufacturer(manufacturer);
        item.setSupplier(supplier);
        return itemRepository.save(item);
    }

    // ---- findNextItem ----

    @Test
    void findNextItemReturnsItemWithHigherId() {
        Item first = createItem("Alpha", BigDecimal.valueOf(10));
        Item second = createItem("Beta", BigDecimal.valueOf(20));

        Optional<Item> next = itemRepository.findNextItem(first.getId());

        assertThat(next).isPresent();
        assertThat(next.get().getId()).isEqualTo(second.getId());
    }

    @Test
    void findNextItemReturnsEmptyWhenNoHigherId() {
        Item only = createItem("Solo", BigDecimal.valueOf(10));

        Optional<Item> next = itemRepository.findNextItem(only.getId());

        assertThat(next).isEmpty();
    }

    @Test
    void findNextItemReturnsImmediateSuccessor() {
        Item first = createItem("First", BigDecimal.valueOf(10));
        Item second = createItem("Second", BigDecimal.valueOf(20));
        Item third = createItem("Third", BigDecimal.valueOf(30));

        Optional<Item> next = itemRepository.findNextItem(first.getId());

        assertThat(next).isPresent();
        assertThat(next.get().getId()).isEqualTo(second.getId());
    }

    // ---- findPreviousItem ----

    @Test
    void findPreviousItemReturnsItemWithLowerId() {
        Item first = createItem("Alpha", BigDecimal.valueOf(10));
        Item second = createItem("Beta", BigDecimal.valueOf(20));

        Optional<Item> prev = itemRepository.findPreviousItem(second.getId());

        assertThat(prev).isPresent();
        assertThat(prev.get().getId()).isEqualTo(first.getId());
    }

    @Test
    void findPreviousItemReturnsEmptyWhenNoLowerId() {
        Item only = createItem("Solo", BigDecimal.valueOf(10));

        Optional<Item> prev = itemRepository.findPreviousItem(only.getId());

        assertThat(prev).isEmpty();
    }

    @Test
    void findPreviousItemReturnsImmediatePredecessor() {
        Item first = createItem("First", BigDecimal.valueOf(10));
        Item second = createItem("Second", BigDecimal.valueOf(20));
        Item third = createItem("Third", BigDecimal.valueOf(30));

        Optional<Item> prev = itemRepository.findPreviousItem(third.getId());

        assertThat(prev).isPresent();
        assertThat(prev.get().getId()).isEqualTo(second.getId());
    }

    // ---- findByCategoryId ----

    @Test
    void findByCategoryIdReturnsOnlyItemsInThatCategory() {
        Category other = new Category();
        other.setName("Grinders");
        other = categoryRepository.save(other);

        Item inCategory = createItem("Machine A", BigDecimal.valueOf(100));

        Item outOfCategory = new Item();
        outOfCategory.setName("Grinder X");
        outOfCategory.setDescription("desc");
        outOfCategory.setPrice(BigDecimal.valueOf(50));
        outOfCategory.setCategory(other);
        outOfCategory.setManufacturer(manufacturer);
        outOfCategory.setSupplier(supplier);
        itemRepository.save(outOfCategory);

        Page<Item> result = itemRepository.findByCategoryId(category.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(inCategory.getId());
    }

    // ---- findByCategoryIdAndName ----

    @Test
    void findByCategoryIdAndNameMatchesPartialNameCaseInsensitive() {
        createItem("Professional Espresso Machine", BigDecimal.valueOf(500));
        createItem("Home Espresso Maker", BigDecimal.valueOf(300));
        createItem("Coffee Grinder", BigDecimal.valueOf(150));

        Page<Item> result = itemRepository.findByCategoryIdAndName(
                category.getId(), "espresso", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2)
                .allMatch(i -> i.getName().toLowerCase().contains("espresso"));
    }

    @Test
    void findByCategoryIdAndNameReturnsEmptyWhenNoMatch() {
        createItem("Espresso Machine", BigDecimal.valueOf(400));

        Page<Item> result = itemRepository.findByCategoryIdAndName(
                category.getId(), "cappuccino", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByCategoryIdAndNameIgnoresOtherCategories() {
        Category other = new Category();
        other.setName("Accessories");
        other = categoryRepository.save(other);

        Item inOther = new Item();
        inOther.setName("Espresso Cup");
        inOther.setDescription("desc");
        inOther.setPrice(BigDecimal.valueOf(20));
        inOther.setCategory(other);
        inOther.setManufacturer(manufacturer);
        inOther.setSupplier(supplier);
        itemRepository.save(inOther);

        Page<Item> result = itemRepository.findByCategoryIdAndName(
                category.getId(), "espresso", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    // ---- findByManufacturerId ----

    @Test
    void findByManufacturerIdReturnsMatchingItems() {
        Manufacturer other = new Manufacturer();
        other.setName("OtherCo");
        other.setUrl("https://other.example.com");
        other = manufacturerRepository.save(other);

        Item byBrewCo = createItem("BrewCo Machine", BigDecimal.valueOf(300));

        Item byOther = new Item();
        byOther.setName("OtherCo Machine");
        byOther.setDescription("desc");
        byOther.setPrice(BigDecimal.valueOf(250));
        byOther.setCategory(category);
        byOther.setManufacturer(other);
        byOther.setSupplier(supplier);
        itemRepository.save(byOther);

        List<Item> result = itemRepository.findByManufacturerId(manufacturer.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(byBrewCo.getId());
    }

    // ---- findBySupplierId ----

    @Test
    void findBySupplierIdReturnsMatchingItems() {
        Supplier other = new Supplier();
        other.setName("Other Supplier");
        other.setContactPerson("Bob");
        other.setContactPersonEmail("bob@example.com");
        other = supplierRepository.save(other);

        Item bySupplyCo = createItem("Supply Co Item", BigDecimal.valueOf(200));

        Item byOther = new Item();
        byOther.setName("Other Item");
        byOther.setDescription("desc");
        byOther.setPrice(BigDecimal.valueOf(180));
        byOther.setCategory(category);
        byOther.setManufacturer(manufacturer);
        byOther.setSupplier(other);
        itemRepository.save(byOther);

        List<Item> result = itemRepository.findBySupplierId(supplier.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(bySupplyCo.getId());
    }
}
