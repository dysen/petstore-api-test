package com.alemdianov.api.tests;

import com.alemdianov.api.services.PetService;
import io.qameta.allure.Description;
import io.swagger.client.model.ModelApiResponse;
import io.swagger.client.model.Pet;
import io.swagger.client.model.Pet.StatusEnum;
import io.swagger.client.model.Tag;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.swagger.client.model.Pet.StatusEnum.PENDING;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Execution(CONCURRENT)
@DisplayName("Test suite for PET endpoint")
public class PesstoreApiTest {
    private PetService petService;
    private String petName;
    private SoftAssertions softly;
    private Pet pet;

    @BeforeEach
    public void setUp() {
        petService = new PetService();
        petName = UUID.randomUUID().toString();
        softly = new SoftAssertions();
        pet = new Pet()
                .name(petName)
                .status(PENDING)
                .photoUrls(List.of("http://test_elixir_unit_test.com"));
    }

    @Test
    @Description("Test to verify that it is possible to get a pet by id")
    @DisplayName("Test to verify that it is possible to get a pet by id")
    public void getByItTest() {
        ResponseEntity<Pet> response = petService.create(pet);
        Long petId = response.getBody().getId();
        ResponseEntity<Pet> responseGetById = petService.getById(petId);
        softly.assertThat(responseGetById.getStatusCode()).as("Response code for get by id").isEqualTo(HttpStatus.OK);
        softly.assertThat(responseGetById.getBody().getId()).as("Pet id ").isEqualTo(petId);
        softly.assertAll();


    }

    @ParameterizedTest(name = "Find by status for -  {0}")
    @EnumSource(StatusEnum.class)
    @Description("Test to verify that it is possible to  find a pet by status")
    @DisplayName("Test to verify that it is possible to  find a pet by status")
    public void findByStatus(StatusEnum status) {

        pet.status(status);

        ResponseEntity<Pet> response = petService.create(pet);
        Long petId = response.getBody().getId();

        ResponseEntity<Pet[]> responseFindByStatus = petService.findByStatus(status);

        HttpStatus statusCode = responseFindByStatus.getStatusCode();
        softly.assertThat(statusCode).as("Response code for find by status").isEqualTo(HttpStatus.OK);

        Pet[] body = responseFindByStatus.getBody();

        boolean isPetFoundById = Arrays
                .stream(body)
                .filter(pet -> pet.getId().equals(petId))
                .findFirst().
                        isPresent();
        softly.assertThat(isPetFoundById).as("Pet found by id").isTrue();

        softly.assertAll();
    }

    @ParameterizedTest
    @EnumSource(StatusEnum.class)
    @Description("Test to verify that it is possible to create a new pet")
    @DisplayName("Test to verify that it is possible to create a new pet")

    public void createNewPetTest(StatusEnum status) {
        pet.status(status);
        ResponseEntity<Pet> response = petService.create(pet);

        softly.assertThat(response.getStatusCode()).as("Request status code").isEqualTo(HttpStatus.OK);
        softly.assertThat(response.getBody()).as("Created pet").isEqualToIgnoringNullFields(pet);
        softly.assertAll();
    }

    @Test
    @Description("Test to verify that it is possible to update pet")
    @DisplayName("Test to verify that it is possible to update pet")
    public void updatePetTest() {
        ResponseEntity<Pet> response = petService.create(pet);
        Pet createdPet = response.getBody();
        createdPet
                .name("New pet name")
                .tags(
                        List.of(
                                new Tag().name("Tag#1").id(1L),
                                new Tag().name("Tag#2").id(2L)
                        )
                );

        ResponseEntity<Pet> updatedPet = petService.update(createdPet);
        softly.assertThat(updatedPet.getStatusCode()).as("Request status code").isEqualTo(HttpStatus.OK);
        softly.assertThat(updatedPet.getBody()).as("Pet after update").isEqualToIgnoringNullFields(createdPet);
        softly.assertAll();
    }

    @Test
    @Description("Test to verify that it is possible to update existing pet with form data")
    @DisplayName("Test to verify that it is possible to update existing pet with form data")
    public void updateWithFormDataTest() {
        String name = "test-1234";
        String status = "sold";

        MultiValueMap<String, String> parametersMap = new LinkedMultiValueMap<String, String>();
        parametersMap.add("name", name);
        parametersMap.add("status", status);

        ResponseEntity<Pet> response = petService.create(pet);

        ResponseEntity<ModelApiResponse> petUpdateEntity = petService.updateWithForm(response.getBody().getId(), parametersMap);

        ModelApiResponse body = petUpdateEntity.getBody();
        softly.assertThat(petUpdateEntity.getStatusCode()).as("Request status code").isEqualTo(HttpStatus.OK);

        ResponseEntity<Pet> updatedPet = petService.getById(response.getBody().getId());
        softly.assertThat(updatedPet.getStatusCode()).as("Request status code for updated").isEqualTo(HttpStatus.OK);
        softly.assertThat(updatedPet.getBody().getName()).as("Updated pet name").isEqualTo(name);
        softly.assertThat(updatedPet.getBody().getStatus().toString()).isEqualTo(status);
        softly.assertAll();
    }

    @Test
    @Description("Test to verify that pet can be deleted")
    @DisplayName("Test to verify that pet can be deleted")
    public void deletePetTest() {
        ResponseEntity<Pet> response = petService.create(pet);

        Long petId = response.getBody().getId();

        HttpStatus statusCodePetExists = petService.getById(petId).getStatusCode();
        softly.assertThat(statusCodePetExists).as("Status for existing pet").isEqualTo(HttpStatus.OK);

        HttpStatus statusCodePetDeleted = petService.delete(petId).getStatusCode();
        softly.assertThat(statusCodePetDeleted).as("Status for deleted pet").isEqualTo(HttpStatus.OK);

        HttpStatus statusCodePetNotExists = petService.getById(petId).getStatusCode();
        softly.assertThat(statusCodePetNotExists).as("Status for non existing pet").isEqualTo(HttpStatus.NOT_FOUND);
        softly.assertAll();

    }

    @Test
    @Description("Test to verify that it is possible to upload file")
    @DisplayName("Test to verify that it is possible to upload file")
    public void uploadFileTest() {
        String fileName = "pets.png";
        ResponseEntity<Pet> response = petService.create(pet);
        Long petId = response.getBody().getId();

        ResponseEntity<ModelApiResponse> fileUploadResponse = petService.uploadFile(petId, fileName);
        HttpStatus statusCode = fileUploadResponse.getStatusCode();
        softly.assertThat(statusCode).as("status coed for file upload").isEqualTo(HttpStatus.OK);
        String message = fileUploadResponse.getBody().getMessage();
        softly.assertThat(message).as("Response message").contains("File uploaded to ./"+fileName);
        softly.assertAll();

    }
}