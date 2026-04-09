package com.websites.coffeeshop.security;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.websites.coffeeshop.model.Discount;
import com.websites.coffeeshop.repository.DiscountRepository;

@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
class AdminSettingsSecurityTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private DiscountRepository discountRepository;

  @BeforeEach
  void setUp() {
    discountRepository.deleteAll();
    discountRepository.save(new Discount(10.0));
  }

  @Test
  void anonymousUserIsRedirectedToLoginForAdminSettingsPage() throws Exception {
    mockMvc.perform(get("/admin/settings"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", containsString("/login")));
  }

  @Test
  void regularUserCannotAccessAdminSettingsPage() throws Exception {
    mockMvc.perform(get("/admin/settings").with(user("regular").roles("USER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminUserCanAccessAdminSettingsPage() throws Exception {
    mockMvc.perform(get("/admin/settings").with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(view().name("adminSettings"))
        .andExpect(model().attributeExists("discount"));
  }

  @Test
  void regularUserCannotUpdateDiscountInAdminSettings() throws Exception {
    mockMvc.perform(post("/admin/settings")
            .with(user("regular").roles("USER"))
            .with(csrf())
            .param("discount", "15.0"))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminUserCanUpdateDiscountInAdminSettings() throws Exception {
    mockMvc.perform(post("/admin/settings")
            .with(user("admin").roles("ADMIN"))
            .with(csrf())
            .param("discount", "15.0"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/settings"));

    Optional<Discount> updatedDiscount = discountRepository.findAll().stream().findFirst();
    if (updatedDiscount.isEmpty()) {
      throw new AssertionError("Expected a discount row to exist after update");
    }

    double value = updatedDiscount.get().getDiscount();
    if (Math.abs(value - 15.0) > 0.001) {
      throw new AssertionError("Expected discount to be 15.0 but was " + value);
    }
  }
}
