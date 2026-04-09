package com.websites.coffeeshop.security;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
class AdminPrivilegesSecurityTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void anonymousUserIsRedirectedToLoginForAdminPage() throws Exception {
    mockMvc.perform(get("/admin"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", containsString("/login")));
  }

  @Test
  void regularUserCannotAccessAdminPage() throws Exception {
    mockMvc.perform(get("/admin").with(user("regular").roles("USER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminUserCanAccessAdminPage() throws Exception {
    mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk());
  }

  @Test
  void regularUserCannotUpdateUserRolesInAdminArea() throws Exception {
    mockMvc.perform(post("/admin/users/1")
            .with(user("regular").roles("USER"))
            .with(csrf())
            .param("role", "ROLE_VIP"))
        .andExpect(status().isForbidden());
  }

  @Test
  void regularUserCannotDeleteAdminProduct() throws Exception {
    mockMvc.perform(delete("/admin/products/1/delete")
            .with(user("regular").roles("USER"))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

}
