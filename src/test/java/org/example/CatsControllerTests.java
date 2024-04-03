package org.example;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import org.example.cat.CatInfoDto;
import org.example.entities.cat.Cat;
import org.example.entities.cat.CatsDao;
import org.example.entities.owner.Owner;
import org.example.entities.owner.OwnersDao;
import org.example.owner.OwnerInfoDto;
import org.example.valueObjects.Color;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.SerializationFeature;

import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.web.client.ClientHttpRequestFactories.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Predicates.equalTo;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest
public class CatsControllerTests {
    /*@LocalServerPort
    private Integer port;*/
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private OwnersDao ownersDao;
    @Autowired
    private CatsDao catsDao;
    private MockMvc mockMvc;
    private Owner testOwner;
    private Cat testCat;
    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }
    @BeforeEach
    void setUp() {
        testOwner = new Owner(LocalDate.parse("2004-12-12"), new ArrayList<>());
        testCat = new Cat("Tina", "British shorthair", Color.GREY,
                testOwner, LocalDate.parse("2017-08-04"), new ArrayList<>());
        testOwner.getCats().add(testCat);
    }
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    @Test
    public void savingCatTest_ShouldReturn200Status() throws Exception {
        ownersDao.save(testOwner);
        var mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        mapper.registerModule(new JavaTimeModule());
        var request = mapper.writeValueAsString(new CatInfoDto(testCat.getId(), testCat.getName(), testCat.getBreed(),
                testCat.getColor(), testCat.getOwner().getId(), testCat.getBirthday(), testCat.getFriends().stream().map(Cat::getId).toList()));
        this.mockMvc.perform(post("/cat").accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isOk());
    }
    @Test
    public void savingCatTest_ShouldReturn500Status() throws Exception {
        ownersDao.save(testOwner);
        var mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        mapper.registerModule(new JavaTimeModule());
        var request = mapper.writeValueAsString(new CatInfoDto(testCat.getId(), null, testCat.getBreed(),
                testCat.getColor(), testCat.getOwner().getId(), testCat.getBirthday(), testCat.getFriends().stream().map(Cat::getId).toList()));
        this.mockMvc.perform(post("/cat").accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().is5xxServerError());
    }
    @Test
    public void getCatTest_ShouldReturn200Status() throws Exception {
        ownersDao.save(testOwner);
        catsDao.save(testCat);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/cat/{catId}", testCat.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
    }
    @Test
    public void getOwnerTest_ShouldReturn404Status() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/cat/5000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError()).andReturn();
    }
}