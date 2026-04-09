package com.websites.coffeeshop.service;

import com.websites.coffeeshop.model.Category;
import com.websites.coffeeshop.model.ItemDTO;
import com.websites.coffeeshop.model.Manufacturer;
import com.websites.coffeeshop.model.Supplier;
import com.websites.coffeeshop.repository.CategoryRepository;
import com.websites.coffeeshop.repository.ItemRepository;
import com.websites.coffeeshop.repository.ManufacturerRepository;
import com.websites.coffeeshop.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.sql.init.mode=never")
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Manufacturer manufacturer;
    private Supplier supplier;
    private Category category;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        manufacturerRepository.deleteAll();
        supplierRepository.deleteAll();

        manufacturer = new Manufacturer();
        manufacturer.setName("BrewCo");
        manufacturer.setUrl("https://brewco.example.com");
        manufacturer = manufacturerRepository.save(manufacturer);

        supplier = new Supplier();
        supplier.setName("Supply Co");
        supplier.setContactPerson("Alice");
        supplier.setContactPersonEmail("alice@example.com");
        supplier = supplierRepository.save(supplier);

        category = new Category();
        category.setName("Espresso Machines");
        category = categoryRepository.save(category);
    }

    // ---- validateItem: called via addNewItem ----

    @Test
    void addNewItemThrowsWhenNameIsNull() {
        ItemDTO dto = validDto();
        dto.setName(null);
        assertThatThrownBy(() -> adminService.addNewItem(dto, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Name cannot be empty");
    }

    @Test
    void addNewItemThrowsWhenNameIsEmpty() {
        ItemDTO dto = validDto();
        dto.setName("");
        assertThatThrownBy(() -> adminService.addNewItem(dto, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Name cannot be empty");
    }

    @Test
    void addNewItemThrowsWhenDescriptionIsNull() {
        ItemDTO dto = validDto();
        dto.setDescription(null);
        assertThatThrownBy(() -> adminService.addNewItem(dto, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Description cannot be empty");
    }

    @Test
    void addNewItemThrowsWhenDescriptionIsEmpty() {
        ItemDTO dto = validDto();
        dto.setDescription("");
        assertThatThrownBy(() -> adminService.addNewItem(dto, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Description cannot be empty");
    }

    @Test
    void addNewItemThrowsWhenPriceIsNull() {
        ItemDTO dto = validDto();
        dto.setPrice(null);
        assertThatThrownBy(() -> adminService.addNewItem(dto, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Price must be greater than zero");
    }

    @Test
    void addNewItemThrowsWhenPriceIsZero() {
        ItemDTO dto = validDto();
        dto.setPrice(BigDecimal.ZERO);
        assertThatThrownBy(() -> adminService.addNewItem(dto, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Price must be greater than zero");
    }

    @Test
    void addNewItemThrowsWhenPriceIsNegative() {
        ItemDTO dto = validDto();
        dto.setPrice(BigDecimal.valueOf(-5));
        assertThatThrownBy(() -> adminService.addNewItem(dto, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Price must be greater than zero");
    }

    @Test
    void addNewItemThrowsWhenCategoryIdIsNull() {
        ItemDTO dto = validDto();
        dto.setCategoryId(null);
        assertThatThrownBy(() -> adminService.addNewItem(dto, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category ID cannot be null");
    }

    @Test
    void addNewItemThrowsWhenManufacturerIsNull() {
        ItemDTO dto = validDto();
        dto.setManufacturer(null);
        assertThatThrownBy(() -> adminService.addNewItem(dto, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Manufacturer cannot be null");
    }

    @Test
    void addNewItemThrowsWhenSupplierIsNull() {
        ItemDTO dto = validDto();
        dto.setSupplier(null);
        assertThatThrownBy(() -> adminService.addNewItem(dto, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Supplier cannot be null");
    }

    // ---- createImageFromFile: called via addNewItem with a valid DTO ----

    @Test
    void addNewItemThrowsForUnsupportedImageType() {
        MockMultipartFile gifFile = new MockMultipartFile(
                "file", "image.gif", "image/gif", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> adminService.addNewItem(validDto(), gifFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only PNG and JPEG images are allowed");
    }

    @Test
    void addNewItemThrowsWhenImageExceedsOneMegabyte() {
        byte[] oversized = new byte[1024 * 1024 + 1];
        MockMultipartFile bigFile = new MockMultipartFile(
                "file", "big.png", "image/png", oversized);

        assertThatThrownBy(() -> adminService.addNewItem(validDto(), bigFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image size must be less than 1MB");
    }

    @Test
    void addNewItemAcceptsValidPngImage() {
        byte[] content = new byte[100];
        MockMultipartFile png = new MockMultipartFile(
                "file", "item.png", "image/png", content);

        var saved = adminService.addNewItem(validDto(), png);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getImage()).isNotNull();
        assertThat(saved.getImage().getMediaType()).isEqualTo("image/png");
    }

    // ---- deleteItem ----

    @Test
    void deleteItemThrowsWhenItemNotFound() {
        assertThatThrownBy(() -> adminService.deleteItem(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found");
    }

    // ---- deleteManufacturer ----

    @Test
    void deleteManufacturerThrowsWhenNotFound() {
        assertThatThrownBy(() -> adminService.deleteManufacturer(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Manufacturer not found");
    }

    // ---- deleteSupplier ----

    @Test
    void deleteSupplierThrowsWhenNotFound() {
        assertThatThrownBy(() -> adminService.deleteSupplier(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Supplier not found");
    }

    // ---- addManufacturer validation ----

    @Test
    void addManufacturerThrowsForEmptyName() {
        Manufacturer m = new Manufacturer();
        m.setName("");
        m.setUrl("https://example.com");
        assertThatThrownBy(() -> adminService.addManufacturer(m))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be empty");
    }

    @Test
    void addManufacturerThrowsForEmptyUrl() {
        Manufacturer m = new Manufacturer();
        m.setName("Brand");
        m.setUrl("");
        assertThatThrownBy(() -> adminService.addManufacturer(m))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL cannot be empty");
    }

    // ---- updateManufacturer no-op guard ----

    @Test
    void updateManufacturerReturnsExistingWhenUnchanged() {
        Manufacturer unchanged = new Manufacturer();
        unchanged.setId(manufacturer.getId());
        unchanged.setName(manufacturer.getName());
        unchanged.setUrl(manufacturer.getUrl());

        Manufacturer result = adminService.updateManufacturer(unchanged);

        assertThat(result.getId()).isEqualTo(manufacturer.getId());
        assertThat(result.getName()).isEqualTo(manufacturer.getName());
    }

    // ---- addSupplier validation ----

    @Test
    void addSupplierThrowsForEmptyName() {
        Supplier s = new Supplier();
        s.setName("");
        s.setContactPerson("Bob");
        s.setContactPersonEmail("bob@example.com");
        assertThatThrownBy(() -> adminService.addSupplier(s))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be empty");
    }

    @Test
    void addSupplierThrowsForEmptyContactPerson() {
        Supplier s = new Supplier();
        s.setName("Supplier X");
        s.setContactPerson("");
        s.setContactPersonEmail("bob@example.com");
        assertThatThrownBy(() -> adminService.addSupplier(s))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Contact person cannot be empty");
    }

    @Test
    void addSupplierThrowsForEmptyContactEmail() {
        Supplier s = new Supplier();
        s.setName("Supplier X");
        s.setContactPerson("Bob");
        s.setContactPersonEmail("");
        assertThatThrownBy(() -> adminService.addSupplier(s))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Contact person email cannot be empty");
    }

    // ---- updateSupplier no-op guard ----

    @Test
    void updateSupplierReturnsExistingWhenUnchanged() {
        Supplier unchanged = new Supplier();
        unchanged.setId(supplier.getId());
        unchanged.setName(supplier.getName());
        unchanged.setContactPerson(supplier.getContactPerson());
        unchanged.setContactPersonEmail(supplier.getContactPersonEmail());

        Supplier result = adminService.updateSupplier(unchanged);

        assertThat(result.getId()).isEqualTo(supplier.getId());
        assertThat(result.getName()).isEqualTo(supplier.getName());
    }

    // ---- helpers ----

    private ItemDTO validDto() {
        ItemDTO dto = new ItemDTO();
        dto.setName("Test Item");
        dto.setDescription("A great item");
        dto.setPrice(BigDecimal.valueOf(99.99));
        dto.setCategoryId(category.getId());
        dto.setManufacturer(manufacturer);
        dto.setSupplier(supplier);
        return dto;
    }
}
